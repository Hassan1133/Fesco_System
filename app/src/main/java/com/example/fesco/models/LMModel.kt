package com.example.fesco.models

import java.io.Serializable

data class LMModel(
    var id: String = "",
    var name: String = "",
    var email: String = "",
    var city: String = "",
    var ls: String = "",
    var lmFCMToken: String = "",
    var subDivision: String = ""
) : Serializable
