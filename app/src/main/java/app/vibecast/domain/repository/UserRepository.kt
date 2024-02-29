package app.vibecast.domain.repository

import app.vibecast.domain.model.User
import kotlinx.coroutines.flow.Flow


interface UserRepository {

    fun getUser(userId : Long) : Flow<User>

    suspend fun addUser(user: User)

    suspend fun deleteUser(user: User)
}