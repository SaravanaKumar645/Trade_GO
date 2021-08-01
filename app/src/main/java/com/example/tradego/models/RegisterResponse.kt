package com.example.tradego.models

data class RegisterResponse (
    var success:Boolean,
    var msg:String,
    var id:String,
    var email:String,
    var name:String
    )