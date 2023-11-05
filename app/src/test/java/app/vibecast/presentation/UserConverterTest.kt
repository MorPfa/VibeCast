package app.vibecast.presentation

import android.content.Context
import app.vibecast.domain.entity.User
import app.vibecast.domain.usecase.GetUserUseCase
import app.vibecast.presentation.user.UserConverter
import app.vibecast.presentation.user.UserModel
import org.junit.Assert
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import app.vibecast.R

class UserConverterTest {

    private val context = mock<Context>()
    private val converter = UserConverter(context)

    @Test
    fun testConvertSuccess() {
        val response = GetUserUseCase.Response(
            user = User(
                userId = 1L,
                name = "name",
                userName = "username",
                email = "email",
                profilePicture = "profilePicture"
            )
        )
        val formattedName = "formattedName"
        val formattedUsername = "formattedUsername"
        val formattedEmail = "formattedEmail"
        val profilePicture = "profilePicture"
        whenever(context.getString(R.string.name, "name")).thenReturn(formattedName)
        whenever(context.getString(R.string.username, "username")).thenReturn(formattedUsername)
        whenever(context.getString(R.string.email, "email")).thenReturn(formattedEmail)
        val result = converter.convertSuccess(response)
        Assert.assertEquals(UserModel(formattedName, formattedUsername, formattedEmail, profilePicture), result)
    }
}