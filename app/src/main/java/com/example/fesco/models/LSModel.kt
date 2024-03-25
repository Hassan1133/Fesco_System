package com.example.fesco.models

import java.io.Serializable

data class LSModel(
    var id: String = "",
    var name: String = "",
    var email: String = "",
    var city: String = "",
    var subDivision : String = "",
    var sdo : String = "",
    var lsFCMToken : String = "",
    var lm : List<String> = emptyList(),
    var complaints: List<String> = emptyList()
) : Serializable
