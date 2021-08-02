package com.example.tradego.retrofit

import com.example.tradego.models.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

interface RetrofitInterface {
    @FormUrlEncoded
    @POST("/register")
    fun registerUser(
        @Field("name")name:String,
        @Field("email")email:String,
        @Field("password")password:String,
        @Field("phone")phone:String
    ): Call<RegisterResponse>


    @FormUrlEncoded
    @POST("/login")
    fun loginUser(@Field("email")email:String,
                  @Field("password")password:String
    ): Call<LoginResponse>

    @FormUrlEncoded
    @POST("/request-reset-password-link")
    fun requestPasswordToken(
        @Field("user_id") id:String,
        @Field("email") mail:String
    ):Call<RequestPasswordTokenResponse>

    @FormUrlEncoded
    @PATCH("/reset-password")
    fun resetPassword(
        @Field("user_id") id:String,
        @Field("token_id") token:String,
        @Field("email") mail:String,
        @Field("n_password") newPass:String,
    ):Call<PasswordResetResponse>


    @Multipart
    @POST("/upload-products")
    fun uploadProduct(
        @Part("p_name") p_name:RequestBody,
        @Part("p_price") p_price:RequestBody,
        @Part("p_category") p_category:RequestBody,
        @Part("p_stock") p_stock:RequestBody,
        @Part("p_description") p_description:RequestBody,
        @Part("user_id") user_id:RequestBody,
        @Part("p_image") p_img:RequestBody,
        @Part p_image1:MultipartBody.Part,
        @Part p_image2:MultipartBody.Part,
        @Part p_image3:MultipartBody.Part,

        ):Call<ProductResponse>

    @FormUrlEncoded
    @POST("/get-user-products")
    fun getUserProducts(
        @Field("user_id") uid:String
    ):Call<GetUserProductsResponse>

    @FormUrlEncoded
    @PATCH("/update-product-stock")
     fun updateStock(
        @Field("p_id") id:String,
        @Field("p_stock") stock:String
     ):Call<UpdateStockResponse>


     @FormUrlEncoded
     @HTTP(method = "DELETE",path = "/delete-product",hasBody = true)
     fun deleteProduct(
         @Field("p_id") id:String
     ):Call<DeleteProductResponse>

     @FormUrlEncoded
     @POST("/get-all-products")
     fun getAllProducts(
         @Field("user_id") id:String
     ):Call<GetAllProductsResponse>

    @FormUrlEncoded
    @POST("/add-to-cart")
    fun addToCart(
        @Field("p_id") pid:String,
        @Field("user_id") id:String
    ):Call<AddToCartResponse>

    @FormUrlEncoded
    @POST("/retrieve-cart-products")
    fun getCartProducts(
        @Field("user_id") uid:String
    ):Call<RetrieveCartProductsResponse>

    @FormUrlEncoded
    @HTTP(method = "DELETE",path = "/delete-cart-item",hasBody = true)
    fun deleteCartProduct(
        @Field("p_id") id:String
    ):Call<DeleteCartProductsResponse>
    //for demo purpose only
    @Multipart
    @POST("/upload")
    fun up(
        @Part img:MultipartBody.Part,
        @Part ("name") name:RequestBody,
        @Part ("price") price:RequestBody,
    ):Call<ProductResponse>

}