package app.vibecast.presentation.user

import android.content.Context
import app.vibecast.R
import app.vibecast.domain.usecase.GetUserUseCase
import app.vibecast.presentation.state.CommonResultConverter
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class UserConverter @Inject constructor(@ApplicationContext private val context: Context) :
    CommonResultConverter<GetUserUseCase.Response, UserModel>() {

    override fun convertSuccess(data: GetUserUseCase.Response): UserModel {
        return UserModel(
            context.getString(R.string.name, data.user.name),
            context.getString(R.string.username, data.user.userName),
            context.getString(R.string.email, data.user.email),
            data.user.profilePicture
        )
    }
}