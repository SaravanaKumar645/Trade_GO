package com.example.tradego.models

data class AddToCartResponse (
    val success:Boolean,
    val msg:String,
    val productDetails:String
        )