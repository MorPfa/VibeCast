package app.vibecast.data.data_repository.data_source.local


import app.vibecast.domain.entity.User
import kotlinx.coroutines.flow.Flow


interface LocalUserDataSource {

    fun getUSer(userId : Long) : Flow<User>

    suspend fun addUser(user: User)

    suspend fun deleteUser(user: User)
}