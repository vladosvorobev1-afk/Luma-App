package com.example.formamind.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Главная база данных приложения.
 *
 * Таблицы:
 *  - sessions   — выполненные упражнения (история)
 *  - courses    — кэш курсов из сети
 *  - exercises  — кэш упражнений из сети
 *
 * version = 2:
 *  - v1: только sessions
 *  - v2: добавлены courses и exercises для сетевого кэша
 *
 * fallbackToDestructiveMigration():
 *  при изменении схемы старая база стирается, создаётся новая.
 *  Для MVP это ок — данные заново скачаются из сети.
 */
@Database(
    entities = [
        SessionEntity::class,
        CourseEntity::class,
        ExerciseEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun sessionDao(): SessionDao
    abstract fun courseDao(): CourseDao
    abstract fun exerciseDao(): ExerciseDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Получить экземпляр базы данных.
         * Singleton — на всё приложение одна база.
         */
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "formamind.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}