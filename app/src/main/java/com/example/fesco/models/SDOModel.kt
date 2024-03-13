package com.example.fesco.models

import java.io.Serializable

data class SDOModel(
    var id: String = "",
    var name: String = "",
    var email: String = "",
    var city: String = "",
    var subDivision : String = "",
    var xen : String = "",
    var ls : List<String> = emptyList<String>(),
    var area : List<String> = emptyList<String>()
) : Serializable