package com.example.tradego.models

data class UpdateStockResponse (
    val success: Boolean,
    val msg:String,
    val currentStock:String
        )