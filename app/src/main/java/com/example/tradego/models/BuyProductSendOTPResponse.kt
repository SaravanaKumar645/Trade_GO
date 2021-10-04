package com.example.tradego.models

data class BuyProductSendOTPResponse (
    val success:Boolean,
    val msg:String,
    val phoneNumber:String
        )