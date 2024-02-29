package app.vibecast.data.local_data.data_source.user

import app.vibecast.data.local_data.db.user.UserDao
import app.vibecast.data.local_data.db.user.UserEntity
import app.vibecast.domain.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class LocalUserDataSourceImpl @Inject constructor(private val userDao: UserDao) :
    LocalUserDataSource {

    override fun getUSer(userId: Long): Flow<User> = userDao.getUser(userId).map {
        User(it.id, it.name, it.userName, it.email, it.profilePicture)
    }

    override suspend fun addUser(user: User) = userDao.addUser(
        UserEntity(user.userId, user.name, user.userName, user.email, user.profilePicture)
    )

    override suspend fun deleteUser(user: User) = userDao.deleteUser(
        UserEntity(user.userId, user.name, user.userName, user.email, user.profilePicture)
    )
}