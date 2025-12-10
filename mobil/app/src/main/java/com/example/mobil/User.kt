package com.example.mobil

data class User (
    val uid: String = "",
    var name: String = "",
    var email: String = "",
    //rol tabanlı erişim
    val role: String = "User"){
}