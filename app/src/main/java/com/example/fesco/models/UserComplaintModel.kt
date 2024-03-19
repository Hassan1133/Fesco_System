package com.example.fesco.models

import java.io.Serializable

data class UserComplaintModel(
    var consumerID: String = "",
    var userName: String = "",
    var phoneNo: String = "",
    var address: String = "",
    var dateTime: String = "",
    var status: String = "",
    var lm: String = "",
    var complaintType: String = "",
    var id: String = ""
) : Serializable
