package com.example.tradego.activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.tradego.R
import com.example.tradego.adapterdataclasses.UserCartAdapterDataClass
import com.example.tradego.adapters.UserCartAdapter
import com.example.tradego.models.RetrieveCartProductsResponse
import com.example.tradego.retrofit.RetrofitClient
import com.example.tradego.retrofit.snackbar
import com.squareup.picasso.Picasso

import kotlinx.android.synthetic.main.activity_user_cart.*

import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UserCart : AppCompatActivity(),UserCartAdapter.onItemClickObserver {
    private val sharedPref = "Auth_check"
    private var cartItems=0
    private lateinit var sharedStorage: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private val list= ArrayList<UserCartAdapterDataClass>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_cart)

        setSupportActionBar(toolbar_cart_section as Toolbar?)
        supportActionBar?.title="My Cart"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        getUserCart()

        if (cartItems>0){
            cart_place_order_btn.setOnClickListener{
                cart_main_relativeLayout.snackbar("Not yet implemented")
            }
        }else{
            cart_place_order_btn.isEnabled=false
        }
        cart_shop_now_btn.setOnClickListener{
            val intent= Intent(applicationContext,Home::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    private fun getUserCart() {
        relativeLayout_pgbar_cart.visibility=View.VISIBLE
        val sh: SharedPreferences =this.getSharedPreferences(sharedPref, Context.MODE_PRIVATE)
        val id:String= sh.getString("Uid","")!!

        val retrofitInterface= RetrofitClient.getInstance()
        val call: Call<RetrieveCartProductsResponse> =retrofitInterface.getCartProducts(id)
        Log.e("after call", "onCreate: " )
        call.enqueue(object : Callback<RetrieveCartProductsResponse> {
            @RequiresApi(Build.VERSION_CODES.N)
            override fun onResponse(call: Call<RetrieveCartProductsResponse>, response: Response<RetrieveCartProductsResponse>) {

                when {
                    (response.code()==200 ||response.isSuccessful)->{
                        relativeLayout_pgbar_cart.visibility=View.INVISIBLE
                        var totalPrice=0
                        val res= response.body()!!
                        Log.e("inside ", "onResponse: 200" )
                        val productArray=res.data
                        val jsonObject= JSONObject(productArray)
                        val jsonArray=jsonObject.optJSONArray("products")
                        Log.e("", "Array : $productArray")

                        if (res.count.toInt()>0){
                            my_cart_list.visibility=View.VISIBLE
                            cart_place_order_relativeLayout.visibility=View.VISIBLE
                            for (i in 0 until jsonArray!!.length()) {
                                Log.e("", "onResponse: I value :$i")
                                cartItems+=1
                                val obj = jsonArray.getJSONObject(i)
                                val pid=obj.optString("p_id")
                                val name=obj.optString("name")
                                val price=obj.optString("price")
                                totalPrice+=price.toInt()
                                val stock=obj.optString("stock")
                                val description=obj.optString("desc")
                                val category=obj.optString("category")
                                val url1=obj.optString("product_url_1")
                                Log.e("", "url :$url1" )
                                val url2=obj.optString("product_url_2")
                                val url3=obj.optString("product_url_3")
                                my_cart_list.adapter= UserCartAdapter(this@UserCart,list,this@UserCart)
                                my_cart_list.layoutManager= LinearLayoutManager(this@UserCart)
                                my_cart_list.setHasFixedSize(true)
                                list.add(UserCartAdapterDataClass(pid,name,price,stock,description,category,url1,url2,url3))
                            }
                            cart_total_price_textView.text=Html.fromHtml("Total <b>Rs.$totalPrice</b>",Html.FROM_HTML_MODE_COMPACT)
                        }else{
                            cart_no_image_relativelayout.visibility=View.VISIBLE
                            Picasso.get()
                                .load(R.drawable.no_results_image)
                                .error(R.drawable.ic_placeholder_slider)
                                .fit()
                                .into(cart_no_image_view)
                        }
                    }
                    response.code()==408->{
                        cart_main_relativeLayout.snackbar("No items found .")
                        relativeLayout_pgbar_cart.visibility=View.INVISIBLE
                        cart_no_image_relativelayout.visibility=View.VISIBLE
                        Picasso.get()
                            .load(R.drawable.no_results_image)
                            .error(R.drawable.ic_placeholder_slider)
                            .fit()
                            .into(cart_no_image_view)
                    }
                }
            }

            override fun onFailure(call: Call<RetrieveCartProductsResponse>, t: Throwable) {
                relativeLayout_pgbar_cart.visibility=View.INVISIBLE
                cart_no_image_relativelayout.visibility=View.VISIBLE
                Picasso.get()
                    .load(R.drawable.no_results_image)
                    .error(R.drawable.ic_placeholder_slider)
                    .fit()
                    .into(cart_no_image_view)
                cart_main_relativeLayout.snackbar("Connection timed out . Please try again ! : "+t.message)
            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            android.R.id.home->{
                val intent= Intent(this,Home::class.java)
                intent.flags=Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                true
            }
            else -> {
                super.onBackPressed()
                return true
            }
        }
    }

    override fun onBackPressed() {
        val intent= Intent(this,Home::class.java)
        intent.flags=Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    override fun onItemClick(position: Int) {
        val product= list[position]
        val intent= Intent(this, SingleProductViewPage::class.java)
        intent.putExtra("p_id",product.pid)
        intent.putExtra("p_name",product.p_name)
        intent.putExtra("p_price",product.p_price)
        intent.putExtra("p_stock",product.p_stock)
        intent.putExtra("p_desc",product.p_description)
        intent.putExtra("p_category",product.category)
        intent.putExtra("p_url1",product.url1)
        intent.putExtra("p_url2",product.url2)
        intent.putExtra("p_url3",product.url3)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

}