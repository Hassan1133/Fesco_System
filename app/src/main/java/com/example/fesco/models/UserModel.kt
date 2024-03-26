package com.example.fesco.models

data class UserModel(
    var consumerID: String = "",
    var name: String = "",
    var phoneNo: String = "",
    var address: String = "",
    var ls: String = "",
    var key: String = "",
    var complaints: List<String> = emptyList()
)
