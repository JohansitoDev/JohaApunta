package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.TaskItem
import com.example.data.model.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [User::class, TaskItem::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun taskDao(): TaskDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "joha_apunta_db"
                )
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Seed initial demo user and tasks
                        CoroutineScope(Dispatchers.IO).launch {
                            val database = getDatabase(context)
                            val userId = database.userDao().insertUser(
                                User(
                                    username = "joha",
                                    password = "123",
                                    name = "Johan Mancebo"
                                )
                            ).toInt()

                            database.taskDao().insertTask(
                                TaskItem(
                                    userId = userId,
                                    title = "Revisar apuntes del examen",
                                    content = "Repasar los temas 3 y 4 de la guía y hacer el resumen de fórmulas.",
                                    category = "Estudio",
                                    priority = "Alta",
                                    isCompleted = false
                                )
                            )
                            database.taskDao().insertTask(
                                TaskItem(
                                    userId = userId,
                                    title = "Comprar cuadernos y bolígrafos",
                                    content = "Ir a la librería por libretas cuadriculadas y post-its de colores.",
                                    category = "Personal",
                                    priority = "Media",
                                    isCompleted = false
                                )
                            )
                            database.taskDao().insertTask(
                                TaskItem(
                                    userId = userId,
                                    title = "Enviar reporte de avance",
                                    content = "Adjuntar la documentación en PDF y enviar al tutor.",
                                    category = "Trabajo",
                                    priority = "Alta",
                                    isCompleted = true,
                                    completedAt = System.currentTimeMillis() - 3600000
                                )
                            )
                        }
                    }
                })
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
