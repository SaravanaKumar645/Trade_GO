package com.example.tradego.models

import android.text.BoringLayout
import com.google.gson.JsonArray
import org.json.JSONArray
import org.json.JSONObject

data class GetUserProductsResponse (val success:Boolean,val msg:String,val data:String,val count:String)