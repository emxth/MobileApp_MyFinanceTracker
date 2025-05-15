package com.example.myfinancetracker.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import data.AppDatabase
import data.entity.User
import data.repository.UserRepository
import kotlinx.coroutines.launch

class UserViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: UserRepository

    init {
        val userDao = AppDatabase.getDatabase(application).userDao()
        repository = UserRepository(userDao)
    }

    fun loginUser(username: String, password: String, callback: (User?) -> Unit) {
        viewModelScope.launch {
            val user = repository.getUserByUsernameAndPassword(username, password)
            callback(user)
        }
    }

    fun registerUser(user: User, callback: () -> Unit) {
        viewModelScope.launch {
            repository.insertUser(user)
            callback()
        }
    }
}