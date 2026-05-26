package com.example.repository

import com.example.model.TaskDao
import com.example.model.TaskItem
import kotlinx.coroutines.flow.Flow

class TaskRepository(private val taskDao: TaskDao) {
    val allTasks: Flow<List<TaskItem>> = taskDao.getAllTasks()

    suspend fun insertTask(task: TaskItem): Long = taskDao.insertTask(task)
    
    suspend fun updateTask(task: TaskItem) = taskDao.updateTask(task)

    suspend fun deleteTask(id: Int) = taskDao.deleteTaskById(id)
}
