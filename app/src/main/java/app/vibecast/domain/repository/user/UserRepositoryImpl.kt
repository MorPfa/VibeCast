package app.vibecast.domain.repository.user

import app.vibecast.data.local_data.data_source.user.LocalUserDataSource
import app.vibecast.domain.model.User
import app.vibecast.domain.repository.user.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val localUserDataSource: LocalUserDataSource
): UserRepository {
    override fun getUser(userId: Long): Flow<User> = localUserDataSource.getUSer(userId)

    override suspend fun addUser(user: User) = localUserDataSource.addUser(user)

    override suspend fun deleteUser(user: User) = localUserDataSource.deleteUser(user)
}