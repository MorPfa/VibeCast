package app.vibecast.domain.repository

import app.vibecast.domain.entity.User
import app.vibecast.domain.entity.Weather
import kotlinx.coroutines.flow.Flow


interface UserRepository {

    fun getUser(userId : Long) : Flow<User>

    suspend fun addUser(user: User)

    suspend fun deleteUser(user: User)
}