package com.example.tradego.activities

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Html
import android.text.SpannableString
import android.text.style.BulletSpan
import android.util.Log
import android.view.MenuItem
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.denzcoskun.imageslider.constants.ScaleTypes
import com.denzcoskun.imageslider.models.SlideModel
import com.example.tradego.R
import com.example.tradego.models.AddToCartResponse
import com.example.tradego.retrofit.RetrofitClient
import com.example.tradego.retrofit.snackbar
import kotlinx.android.synthetic.main.activity_single_product_view_page.*
import kotlinx.android.synthetic.main.activity_user_cart.*
import kotlinx.android.synthetic.main.notification_added_to_cart.*
import kotlinx.android.synthetic.main.notification_added_to_cart.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.net.URL

class SingleProductViewPage : AppCompatActivity() {

    private val CHANNEL_ID="ProductAddedToCart"
    private lateinit var notifyManager:NotificationManagerCompat
    private val sharedPref = "Auth_check"
    private lateinit var sharedStorage: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private val imageList=ArrayList<SlideModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_single_product_view_page)

        setSupportActionBar(toolbar_singleProduct_view)
        toolbar_singleProduct_view.title=null
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        notifyManager= NotificationManagerCompat.from(this@SingleProductViewPage)
        imageList.clear()
        val id=intent.getStringExtra("p_id")!!
        val name=intent.getStringExtra("p_name")!!
        val price=intent.getStringExtra("p_price")
        val stock=intent.getStringExtra("p_stock")
        val desc=intent.getStringExtra("p_desc")
        val category=intent.getStringExtra("p_category")
        val url1=intent.getStringExtra("p_url1")!!
        val url2=intent.getStringExtra("p_url2")
        val url3=intent.getStringExtra("p_url3")
        val descString= desc!!.split(". ")
        imageList.add(SlideModel(url1))
        imageList.add(SlideModel(url2))
        imageList.add(SlideModel(url3))
        single_product_imageSlider.background=null
        single_product_imageSlider.setImageList(imageList, ScaleTypes.FIT)
        single_product_description.text= descString.toBulletedList()
        single_product_name.text=name
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            desc_title.text=Html.fromHtml("<u>Description :</u>",Html.FROM_HTML_MODE_COMPACT)
            single_product_price.text=Html.fromHtml("Only for <u><b><font color=red>Rs.${price}</font></b></u>",Html.FROM_HTML_MODE_COMPACT)
            single_product_stock.text=Html.fromHtml("Hurry up ! Only <u><b><font color=#1565C0> $stock</u> left in stock </font></b>.",Html.FROM_HTML_MODE_COMPACT)
        }else{
            desc_title.text=Html.fromHtml("<u>Description :</u>")
            single_product_price.text=Html.fromHtml("Only for <b>Rs.<font color=red>${price}</font></b>")
            single_product_stock.text=Html.fromHtml("Hurry up ! Only <font color=#1565C0>${stock} left in stock .")
        }

        single_product_addCart_btn.setOnClickListener{
            singleProduct_view_relativeLayout.snackbar("Adding to cart...")
            addtoCart(id,name,url1)
        }


    }


    private fun List<String>.toBulletedList(): CharSequence {
        return SpannableString(this.joinToString("\n")).apply {
            this@toBulletedList.foldIndexed(0) { index, acc, span ->
                val end = acc + span.length + if (index != this@toBulletedList.size - 1) 1 else 0
                this.setSpan(BulletSpan(30), acc, end, 0)
                end
            }
        }
    }

    private fun addtoCart(pid: String, name: String, url1: String){
        sharedStorage=this.getSharedPreferences(sharedPref, Context.MODE_PRIVATE)
        val uid=sharedStorage.getString("Uid","")!!
        val retrofitInterface= RetrofitClient.getInstance()
        val call: Call<AddToCartResponse> =retrofitInterface.addToCart(pid,uid)
        Log.e("after call", "onCreate: " )
        call.enqueue(object : Callback<AddToCartResponse> {
            override fun onResponse(call: Call<AddToCartResponse>, response: Response<AddToCartResponse>) {

                when {
                    (response.code()==200 ||response.isSuccessful)->{
                        val res= response.body()!!
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            sendNotification(name, res.msg, url1)
                        }else{
                            Log.e("", "sendNotification: ", )
                            singleProduct_view_relativeLayout.snackbar(res.msg)
                        }

                    }
                    response.code()==405->{
                       singleProduct_view_relativeLayout.snackbar("Cannot add to cart .Try again !")
                    }
                    response.code()==408->{
                        singleProduct_view_relativeLayout.snackbar("No product found . Try login again !")
                    }
                }
            }
            override fun onFailure(call: Call<AddToCartResponse>, t: Throwable) {
                   singleProduct_view_relativeLayout.snackbar("Error - "+t.message)
            }
        })
    }
    override fun onBackPressed() {
        Intent.FLAG_ACTIVITY_CLEAR_TASK
        super.onBackPressed()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            android.R.id.home->{
                super.onBackPressed()
                true
            }
            else -> {
                super.onBackPressed()
                return true
            }
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun sendNotification(title: String, msg: String, url1: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val intent=Intent(this,UserCart::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            val openActivity= PendingIntent.getActivity(this,10,intent,0)
            val customView = RemoteViews(packageName, R.layout.notification_added_to_cart)

            customView.setTextViewText(R.id.notify_cart_title,title)
            customView.setTextViewText(R.id.notify_cart_desc,msg)
            customView.setOnClickPendingIntent(R.id.notify_cart_view_btn,openActivity)
            val builder: NotificationCompat.Builder = NotificationCompat.Builder(this, "cart")
                .setSmallIcon(R.drawable.ic_outline_shopping_cart_24)
                .setStyle(NotificationCompat.DecoratedCustomViewStyle())
                .setContent(customView)
                //.setContentIntent(openActivity)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
            val manager = NotificationManagerCompat.from(this)
            manager.notify(0, builder.build())
        }
    }
}