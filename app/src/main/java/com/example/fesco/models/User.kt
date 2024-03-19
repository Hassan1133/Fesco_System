package com.example.fesco.models

data class User(
    var consumerID: String = "",
    var name: String = "",
    var phoneNo: String = "",
    var address: String = "",
    var ls: String = "",
    var sdo: String = "",
    var key: String = "",
    var complaints: List<String> = emptyList()
)
