package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.TaskItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

val CategoriesList = listOf("General", "Estudio", "Trabajo", "Personal", "Urgente")
val PrioritiesList = listOf("Alta", "Media", "Baja")

data class ReminderOption(val label: String, val minutes: Long?)

val ReminderOptions = listOf(
    ReminderOption("Sin recordatorio", null),
    ReminderOption("En 5 min", 5),
    ReminderOption("En 15 min", 15),
    ReminderOption("En 1 hora", 60),
    ReminderOption("En 24 horas", 1440)
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TaskEditorDialog(
    taskToEdit: TaskItem? = null,
    onDismiss: () -> Unit,
    onSave: (title: String, content: String, category: String, priority: String, reminderMinutes: Long?) -> Unit
) {
    var title by remember { mutableStateOf(taskToEdit?.title ?: "") }
    var content by remember { mutableStateOf(taskToEdit?.content ?: "") }
    var selectedCategory by remember { mutableStateOf(taskToEdit?.category ?: "General") }
    var selectedPriority by remember { mutableStateOf(taskToEdit?.priority ?: "Media") }
    var selectedReminder by remember { mutableStateOf(ReminderOptions[0]) }
    var titleError by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (taskToEdit == null) "Nuevo Apunte / Tarea" else "Editar Apunte",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Title field
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        titleError = false
                    },
                    label = { Text("Título de la tarea o apunte *") },
                    placeholder = { Text("Ej. Terminar informe de química") },
                    isError = titleError,
                    supportingText = if (titleError) {
                        { Text("El título no puede estar vacío") }
                    } else null,
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("task_title_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Content / Notes field
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Detalles o notas adicionales") },
                    placeholder = { Text("Escribe aquí todos los apuntes, pasos o recordatorios...") },
                    minLines = 3,
                    maxLines = 6,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("task_content_input")
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Category chips
                Text(
                    text = "Categoría",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    CategoriesList.forEach { category ->
                        FilterChip(
                            selected = selectedCategory == category,
                            onClick = { selectedCategory = category },
                            label = { Text(category, fontSize = 13.sp) },
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Priority chips
                Text(
                    text = "Prioridad",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PrioritiesList.forEach { priority ->
                        val isSelected = selectedPriority == priority
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedPriority = priority },
                            label = { Text(priority, fontSize = 13.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = when (priority) {
                                    "Alta" -> Color(0xFFFEE2E2)
                                    "Media" -> Color(0xFFFEF3C7)
                                    else -> Color(0xFFE0F2FE)
                                },
                                selectedLabelColor = when (priority) {
                                    "Alta" -> Color(0xFF991B1B)
                                    "Media" -> Color(0xFF92400E)
                                    else -> Color(0xFF075985)
                                }
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Reminder option
                Text(
                    text = "🔔 Notificación de Recordatorio",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    ReminderOptions.forEach { option ->
                        FilterChip(
                            selected = selectedReminder == option,
                            onClick = { selectedReminder = option },
                            label = { Text(option.label, fontSize = 12.sp) },
                            leadingIcon = if (option.minutes != null) {
                                { Icon(Icons.Default.Alarm, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            } else null,
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (title.isBlank()) {
                                titleError = true
                            } else {
                                onSave(
                                    title,
                                    content,
                                    selectedCategory,
                                    selectedPriority,
                                    selectedReminder.minutes
                                )
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("task_save_button")
                    ) {
                        Text(if (taskToEdit == null) "Guardar Tarea" else "Actualizar")
                    }
                }
            }
        }
    }
}

@Composable
fun TaskDetailDialog(
    task: TaskItem,
    onDismiss: () -> Unit,
    onToggleComplete: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onNotifyNow: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp)
            ) {
                // Header with category badge and close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = task.category,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }

                        val (priorityColor, priorityText) = when (task.priority) {
                            "Alta" -> Color(0xFFEF4444) to "Prioridad Alta"
                            "Media" -> Color(0xFFF59E0B) to "Prioridad Media"
                            else -> Color(0xFF10B981) to "Prioridad Baja"
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = priorityColor.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = priorityText,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = priorityColor,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Title
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Status chip
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (task.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                        contentDescription = null,
                        tint = if (task.isCompleted) Color(0xFF10B981) else Color(0xFFF59E0B),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (task.isCompleted) "Completada" else "Pendiente por hacer",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = if (task.isCompleted) Color(0xFF10B981) else Color(0xFFF59E0B)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))

                // Content / Notes
                Text(
                    text = "Apunte o Contenido:",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (task.content.isNotBlank()) task.content else "(Sin notas adicionales)",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Date
                Text(
                    text = "Creado: ${dateFormat.format(Date(task.createdAt))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (task.reminderTime != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "🔔 Recordatorio programado: ${dateFormat.format(Date(task.reminderTime))}",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action to test reminder notification immediately
                OutlinedButton(
                    onClick = onNotifyNow,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("notify_now_button")
                ) {
                    Icon(Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("🔔 Notificarme esta tarea pendiente ahora")
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Primary complete/uncomplete toggle
                Button(
                    onClick = onToggleComplete,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (task.isCompleted) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("toggle_complete_button")
                ) {
                    Icon(
                        imageVector = if (task.isCompleted) Icons.Default.RadioButtonUnchecked else Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (task.isCompleted) "Marcar como pendiente" else "Marcar como completada")
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Edit & Delete row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onEdit,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("edit_task_button")
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Editar")
                    }

                    OutlinedButton(
                        onClick = onDelete,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("delete_task_button")
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Eliminar")
                    }
                }
            }
        }
    }
}

@Composable
fun DeleteConfirmationDialog(
    taskTitle: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Eliminar apunte", fontWeight = FontWeight.Bold) },
        text = { Text("¿Estás seguro de que deseas eliminar \"$taskTitle\"? Esta acción no se puede deshacer.") },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Eliminar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
