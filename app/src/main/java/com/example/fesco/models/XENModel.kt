package com.example.fesco.models

data class XENModel(
    var id: String = "",
    var name: String = "",
    var email: String = "",
    var division: String = "",
    var city: String = "",
    var SDO : List<String> = emptyList<String>()
)
