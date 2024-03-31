package app.vibecast.domain.repository.firebase

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import javax.inject.Inject


class FirebaseRepositoryImpl @Inject constructor() : FirebaseRepository {


    override fun createAccount(email: String, password: String) {
        TODO("Not yet implemented")
    }
}