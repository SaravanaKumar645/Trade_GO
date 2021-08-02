package com.example.tradego.models

data class RequestPasswordTokenResponse(
    var success:Boolean,
    var msg:String,
    var token_id:String
)