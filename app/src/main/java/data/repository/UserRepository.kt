package data.repository

import data.dao.UserDao
import data.entity.User

class UserRepository(private val userDao: UserDao) {

    suspend fun getUserByUsernameAndPassword(username: String, password: String): User? {
        return userDao.getUserByUsernameAndPassword(username, password)
    }

    suspend fun insertUser(user: User) {
        userDao.insertUser(user)
    }
}