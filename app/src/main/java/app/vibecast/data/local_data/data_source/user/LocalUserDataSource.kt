package app.vibecast.data.local_data.data_source.user


import app.vibecast.domain.model.User
import kotlinx.coroutines.flow.Flow


interface LocalUserDataSource {

    fun getUSer(userId : Long) : Flow<User>

    suspend fun addUser(user: User)

    suspend fun deleteUser(user: User)
}