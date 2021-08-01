package com.example.tradego.activities


import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.tradego.R
import com.example.tradego.adapters.YourProductsAdapter
import com.example.tradego.models.GetUserProductsResponse
import com.example.tradego.adapterdataclasses.YourProductsAdapterDataClass
import com.example.tradego.retrofit.RetrofitClient
import com.example.tradego.retrofit.snackbar
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_your_products.*
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.collections.ArrayList


class YourProducts : AppCompatActivity() {
    private val sharedPref="Auth_check"
    private val list= ArrayList<YourProductsAdapterDataClass>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_your_products)
        onStart()
        setSupportActionBar(toolbar_product_section as Toolbar?)
        supportActionBar?.title="My Products"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        getMyProducts()
        add_products.setOnClickListener{
            val intent= Intent(this,ProductAddSection::class.java)
            intent.flags=Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }
    }

    private fun getMyProducts(){
        val sh: SharedPreferences =this.getSharedPreferences(sharedPref, Context.MODE_PRIVATE)
        val id:String= sh.getString("Uid","")!!
        val retrofitInterface=RetrofitClient.getInstance()
        val call: Call<GetUserProductsResponse> =retrofitInterface.getUserProducts(id)
        Log.e("after call", "onCreate: " )
        call.enqueue(object :Callback<GetUserProductsResponse>{
            override fun onResponse(call: Call<GetUserProductsResponse>, response: Response<GetUserProductsResponse>) {

                when {
                    (response.code()==200 ||response.isSuccessful)->{
                        list.clear()
                        val res= response.body()!!
                        Log.e("inside ", "onResponse: 200" )
                        val productArray=res.data
                        val jsonObject=JSONObject(productArray)
                        val jsonArray=jsonObject.optJSONArray("products")
                        Log.e("", "Array : $productArray")

                        shimmer_effect_layout.stopShimmer()
                        shimmer_effect_layout.visibility=View.GONE
                        if (res.count.toInt()>0){
                            no_image_relativelayout.visibility=View.INVISIBLE
                            no_image_view.visibility=View.INVISIBLE
                            my_products_list.visibility=View.VISIBLE
                            user_product_display.snackbar(res.msg)

                            for (i in 0 until jsonArray!!.length()) {
                                Log.e("", "onResponse: I value :$i")
                                val obj = jsonArray.getJSONObject(i)
                                val pid=obj.optString("p_id")
                                val name=obj.optString("name")
                                val price=obj.optString("price")
                                val stock=obj.optString("stock")
                                val description=obj.optString("desc")
                                val category=obj.optString("category")
                                val url1=obj.optString("product_url_1")
                                Log.e("", "url :$url1" )
                                //val url2=obj.optString("product_url_2")
                                //val url3=obj.optString("product_url_3")
                                my_products_list.adapter= YourProductsAdapter(this@YourProducts,list)
                                my_products_list.layoutManager=LinearLayoutManager(this@YourProducts)
                                my_products_list.setHasFixedSize(true)
                                //(my_products_list.adapter )?.notifyDataSetChanged()
                                list.add(YourProductsAdapterDataClass(pid,name,price,stock,description,category,url1))
                            }
                        }else{
                            no_image_relativelayout.visibility=View.VISIBLE
                            no_image_view.visibility=View.VISIBLE
                            Picasso.get()
                                .load(R.drawable.no_results_image)
                                .error(R.drawable.ic_placeholder_slider)
                                .fit()
                                .into(no_image_view)
                        }
                    }
                    response.code()==408->{
                        user_product_display.snackbar("Error getting products . Close the application and try again !")
                    }
                }
            }

            override fun onFailure(call: Call<GetUserProductsResponse>, t: Throwable) {
                user_product_display.snackbar("Connection timed out . Please try again ! : "+t.message)
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
       // super.onBackPressed()
        val intent= Intent(this,Home::class.java)
        intent.flags=Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }
}