package app.vibecast.domain.entity

data class User(
    val userId : Long,
    val name : String,
    val userName : String,
    val email : String,
    val profilePicture : String
)