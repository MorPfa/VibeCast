package app.vibecast.data.data_repository.repository

import app.vibecast.data.data_repository.data_source.local.LocalUserDataSource
import app.vibecast.domain.entity.User
import app.vibecast.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UserRepositoryImpl@Inject constructor(
    private val localUserDataSource: LocalUserDataSource
): UserRepository {
    override fun getUser(userId: Long): Flow<User> = localUserDataSource.getUSer(userId)

    override suspend fun addUser(user: User) = localUserDataSource.addUser(user)

    override suspend fun deleteUser(user: User) = localUserDataSource.deleteUser(user)
}