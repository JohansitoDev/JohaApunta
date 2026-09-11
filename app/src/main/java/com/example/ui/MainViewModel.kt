package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.TaskItem
import com.example.data.model.User
import com.example.data.repository.TaskRepository
import com.example.notifications.NotificationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class TaskFilterTab(val label: String) {
    TODAS("Todas"),
    PENDIENTES("Pendientes"),
    COMPLETADAS("Hechas")
}

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val repository = TaskRepository(database.userDao(), database.taskDao())

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _filterTab = MutableStateFlow(TaskFilterTab.TODAS)
    val filterTab: StateFlow<TaskFilterTab> = _filterTab.asStateFlow()

    private val _selectedCategory = MutableStateFlow("Todas")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _userMessage = MutableSharedFlow<String>()
    val userMessage: SharedFlow<String> = _userMessage.asSharedFlow()

    // Tasks flow reacting to current user
    private val userTasksFlow = _currentUser.flatMapLatest { user ->
        if (user != null) {
            repository.getTasksForUser(user.id)
        } else {
            flowOf(emptyList())
        }
    }

    val tasks: StateFlow<List<TaskItem>> = combine(
        userTasksFlow,
        _filterTab,
        _selectedCategory,
        _searchQuery
    ) { allTasks, tab, cat, query ->
        allTasks.filter { task ->
            val matchesTab = when (tab) {
                TaskFilterTab.TODAS -> true
                TaskFilterTab.PENDIENTES -> !task.isCompleted
                TaskFilterTab.COMPLETADAS -> task.isCompleted
            }
            val matchesCat = (cat == "Todas" || task.category.equals(cat, ignoreCase = true))
            val matchesQuery = if (query.isBlank()) true else {
                task.title.contains(query, ignoreCase = true) ||
                        task.content.contains(query, ignoreCase = true)
            }
            matchesTab && matchesCat && matchesQuery
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allUserTasks: StateFlow<List<TaskItem>> = userTasksFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        // Attempt initial auto-login with default user if present
        viewModelScope.launch(Dispatchers.IO) {
            val defaultUser = repository.getAnyUser()
            if (defaultUser != null) {
                _currentUser.value = defaultUser
            }
        }
    }

    fun setFilterTab(tab: TaskFilterTab) {
        _filterTab.value = tab
    }

    fun setSelectedCategory(category: String) {
        _selectedCategory.value = category
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun login(username: String, pass: String, onResult: (Boolean, String?) -> Unit) {
        if (username.isBlank() || pass.isBlank()) {
            onResult(false, "Por favor llena todos los campos")
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            val user = repository.authenticate(username, pass)
            if (user != null) {
                _currentUser.value = user
                onResult(true, null)
                _userMessage.emit("¡Bienvenido, ${user.name}!")
            } else {
                onResult(false, "Usuario o contraseña incorrectos")
            }
        }
    }

    fun register(username: String, pass: String, name: String, onResult: (Boolean, String?) -> Unit) {
        if (username.isBlank() || pass.isBlank() || name.isBlank()) {
            onResult(false, "Completa todos los campos")
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            val result = repository.registerUser(username, pass, name)
            result.onSuccess { user ->
                _currentUser.value = user
                onResult(true, null)
                _userMessage.emit("¡Cuenta creada exitosamente!")
            }.onFailure { error ->
                onResult(false, error.localizedMessage ?: "Error al registrar")
            }
        }
    }

    fun quickLoginDemo() {
        viewModelScope.launch(Dispatchers.IO) {
            var user = repository.getAnyUser()
            if (user == null) {
                val registered = repository.registerUser("joha", "123", "Johan Mancebo")
                user = registered.getOrNull()
            }
            _currentUser.value = user
            _userMessage.emit("Sesión iniciada como ${user?.name ?: "Joha"}")
        }
    }

    fun logout() {
        _currentUser.value = null
        viewModelScope.launch {
            _userMessage.emit("Sesión cerrada")
        }
    }

    fun createTask(
        title: String,
        content: String,
        category: String,
        priority: String,
        reminderMinutes: Long?,
        context: Context
    ) {
        val user = _currentUser.value ?: return
        if (title.isBlank()) return

        viewModelScope.launch(Dispatchers.IO) {
            val reminderTimeMillis = reminderMinutes?.let {
                System.currentTimeMillis() + (it * 60 * 1000L)
            }

            val task = TaskItem(
                userId = user.id,
                title = title.trim(),
                content = content.trim(),
                category = category,
                priority = priority,
                isCompleted = false,
                reminderTime = reminderTimeMillis
            )
            val newId = repository.insertTask(task).toInt()

            if (reminderTimeMillis != null) {
                NotificationHelper.scheduleAlarmReminder(
                    context = context,
                    taskId = newId,
                    title = task.title,
                    content = task.content,
                    triggerAtMillis = reminderTimeMillis
                )
            }
            _userMessage.emit("Apunte \"${task.title}\" guardado")
        }
    }

    fun updateTask(
        task: TaskItem,
        newTitle: String,
        newContent: String,
        newCategory: String,
        newPriority: String,
        newReminderMinutes: Long?,
        context: Context
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val reminderTimeMillis = if (newReminderMinutes != null) {
                System.currentTimeMillis() + (newReminderMinutes * 60 * 1000L)
            } else {
                task.reminderTime
            }

            val updated = task.copy(
                title = newTitle.trim(),
                content = newContent.trim(),
                category = newCategory,
                priority = newPriority,
                reminderTime = reminderTimeMillis
            )
            repository.updateTask(updated)

            if (newReminderMinutes != null && reminderTimeMillis != null) {
                NotificationHelper.scheduleAlarmReminder(
                    context = context,
                    taskId = task.id,
                    title = updated.title,
                    content = updated.content,
                    triggerAtMillis = reminderTimeMillis
                )
            }
            _userMessage.emit("Apunte actualizado")
        }
    }

    fun toggleTaskCompletion(task: TaskItem, context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val newCompleted = !task.isCompleted
            repository.setTaskCompleted(task.id, newCompleted)
            if (newCompleted) {
                // Cancel scheduled notification if completed
                NotificationHelper.cancelAlarmReminder(context, task.id)
                _userMessage.emit("¡Tarea \"${task.title}\" completada! 🎉")
            } else {
                _userMessage.emit("Tarea marcada como pendiente")
            }
        }
    }

    fun deleteTask(task: TaskItem, context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            NotificationHelper.cancelAlarmReminder(context, task.id)
            repository.deleteTask(task)
            _userMessage.emit("Apunte eliminado")
        }
    }

    // Trigger immediate reminder notification for a specific task
    fun triggerTaskNotificationNow(context: Context, task: TaskItem) {
        NotificationHelper.showTaskReminderNotification(
            context = context,
            taskId = task.id,
            title = task.title,
            content = task.content
        )
        viewModelScope.launch {
            _userMessage.emit("🔔 Notificación enviada: \"${task.title}\"")
        }
    }

    // Trigger general pending tasks reminder alert ("No has hecho X cosas")
    fun notifyPendingTasksNow(context: Context) {
        val user = _currentUser.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            val pending = repository.getPendingTasks(user.id)
            if (pending.isEmpty()) {
                _userMessage.emit("🎉 ¡Excelente! No tienes ninguna tarea pendiente.")
            } else {
                val firstTask = pending.first()
                val details = if (pending.size > 1) {
                    "Otras pendientes: " + pending.drop(1).take(3).joinToString(", ") { it.title }
                } else ""

                NotificationHelper.showPendingTasksAlertNotification(
                    context = context,
                    pendingCount = pending.size,
                    firstTaskTitle = firstTask.title,
                    details = details
                )
                _userMessage.emit("🔔 Notificación enviada para ${pending.size} tarea(s) pendiente(s)")
            }
        }
    }
}
