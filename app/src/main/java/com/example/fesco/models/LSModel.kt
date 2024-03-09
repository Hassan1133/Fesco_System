package com.example.fesco.models

data class LSModel(
    var id: String = "",
    var name: String = "",
    var email: String = "",
    var city: String = "",
    var subDivision : String = "",
    var work : List<String> = emptyList<String>()
)
