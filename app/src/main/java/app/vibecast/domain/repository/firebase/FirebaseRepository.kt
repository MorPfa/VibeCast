package app.vibecast.domain.repository.firebase

interface FirebaseRepository {

    fun createAccount(email : String, password : String)
}