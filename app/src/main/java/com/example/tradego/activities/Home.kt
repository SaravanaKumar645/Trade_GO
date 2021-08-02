package com.example.tradego.activities


import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.util.Log

import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.tradego.R
import com.example.tradego.adapters.HomeProductsAdapter
import com.example.tradego.models.GetAllProductsResponse

import com.example.tradego.adapterdataclasses.HomeAdapterDataClass

import com.example.tradego.retrofit.RetrofitClient
import com.example.tradego.retrofit.snackbar
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.logout_profile_alertdialog.view.*
import kotlinx.android.synthetic.main.toolbar.*
import kotlinx.android.synthetic.main.welcome_email_notify.view.*
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class Home : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private val sharedPref = "Auth_check"
    private var pressedTime: Long = 0
    private var registerValidate: Boolean = false
    private lateinit var sharedStorage: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private val list= ArrayList<HomeAdapterDataClass>()
    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        val sh: SharedPreferences = this.getSharedPreferences(sharedPref, Context.MODE_PRIVATE)
        val email: String = sh.getString("Email", "error")!!
        val name: String = sh.getString("Name", "error")!!
        registerValidate = sh.getBoolean("Register", false)
        showWelcomeDialog()

        navigation_view.bringToFront()
        navigation_view.isNestedScrollingEnabled = true
        setSupportActionBar(toolbar_id21)

        val toggle = ActionBarDrawerToggle(
            this,
            drawer_layout21,
            toolbar_id21,
            R.string.open_drawer,
            R.string.close_drawer
        )
        drawer_layout21.addDrawerListener(toggle)
        toggle.syncState()
        navigation_view.setNavigationItemSelectedListener(this)
        navigation_view.setCheckedItem(R.id.home_fg)
        navigation_view.isClickable=false
        val navDetails = navigation_view.getHeaderView(0)

        getProducts()
        //val profileImg: ImageView = navDetails.findViewById(R.id.profile_logo_navigationBar)
        val profileName: TextView = navDetails.findViewById(R.id.profile_name_navigationBar)
        val profileEmail: TextView = navDetails.findViewById(R.id.profile_email_navigationBar)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            profileEmail.text = Html.fromHtml(
                "Logged in as  <font color=#1565C0><u><b>$email</b></u></font",
                Html.FROM_HTML_MODE_COMPACT
            )
            profileName.text = Html.fromHtml(
                "Hi, <font color=#1565C0><b>$name</b></font>",
                Html.FROM_HTML_MODE_COMPACT
            )
        } else {
            profileEmail.text =
                Html.fromHtml("Logged in as  <font color=#1565C0><u><b>$email</b></u></font",Html.FROM_HTML_MODE_COMPACT)
            profileName.text =
                Html.fromHtml("Hi, <b>$name</b>\nLogged in as  <font color=#1565C0><u><b>$email</b></u></font",Html.FROM_HTML_MODE_COMPACT)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.logout_home -> {
                drawer_layout21.closeDrawer(navigation_view)
                val alertDialog = AlertDialog.Builder(this, R.style.customAlertDialog)
                val customView = View.inflate(this, R.layout.logout_profile_alertdialog, null)
                alertDialog.setView(customView)
                alertDialog.setCancelable(true)
                val dialog = alertDialog.create()
                customView.logout_profile_NO_btn.setOnClickListener {
                    dialog.dismiss()
                }
                customView.logout_profile_Yes_btn.setOnClickListener {
                    val sh: SharedPreferences = this.getSharedPreferences(sharedPref, MODE_PRIVATE)
                    val editor: SharedPreferences.Editor = sh.edit()
                    val id = sh.getString("Uid", null)
                    if (id == null) {
                        Toast.makeText(
                            applicationContext,
                            "Successfully logged out !",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    editor.clear()
                    editor.apply()
                    val intent = Intent(applicationContext, Login::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    dialog.dismiss()

                }
                dialog.show()
                true
            }
            R.id.changePass_fg -> {
                drawer_layout21.closeDrawer(navigation_view)
                val intent = Intent(applicationContext, RequestPasswordToken::class.java)
                intent.putExtra("Login", false)
                intent.putExtra("Home", true)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)

                return true
            }
            R.id.your_products_fg -> {
                drawer_layout21.closeDrawer(navigation_view)
                val intent = Intent(applicationContext, YourProducts::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                return true
            }

            R.id.cart_fg->{
                drawer_layout21.closeDrawer(navigation_view)
                val intent = Intent(applicationContext, UserCart::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                return true
            }
            else -> {
                return true
            }
        }

    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onBackPressed() {
        if (drawer_layout21.isDrawerOpen(navigation_view)) {
            drawer_layout21.closeDrawer(navigation_view)
            return
        }
        if (pressedTime + 2000 >= System.currentTimeMillis()) {
            super.onBackPressed()
            finish()
        } else {
            snackBar()
        }
        pressedTime = System.currentTimeMillis()

    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun snackBar() {
        val v: View = findViewById(android.R.id.content)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val snackbar = Snackbar.make(
                v,
                Html.fromHtml("<b>Press Back again to Exit</b>", Html.FROM_HTML_MODE_COMPACT),
                Snackbar.LENGTH_LONG
            )
            val view = snackbar.view
            val tv: TextView = view.findViewById(com.google.android.material.R.id.snackbar_text)
            tv.textAlignment = (View.TEXT_ALIGNMENT_CENTER)
            snackbar.show()
        } else {
            val snackbar = Snackbar.make(
                v,
                Html.fromHtml("<b>Press Back again to Exit</b>",Html.FROM_HTML_MODE_COMPACT),
                Snackbar.LENGTH_LONG
            )
            val view = snackbar.view
            val tv: TextView = view.findViewById(com.google.android.material.R.id.snackbar_text)
            tv.textAlignment = (View.TEXT_ALIGNMENT_CENTER)
            snackbar.show()
        }
    }
    @RequiresApi(Build.VERSION_CODES.N)
    private fun showWelcomeDialog(){
        if (registerValidate) {
            val alertDialog = AlertDialog.Builder(this, R.style.customAlertDialog)
            val customView = View.inflate(this, R.layout.welcome_email_notify, null)
            alertDialog.setView(customView)
            alertDialog.setCancelable(true)
            customView.welcome_email_text2.text = Html.fromHtml(
                "We have sent a <font color=#1565C0><b>Welcome email</b></font> for you to get know more about us. The mail will be in the <font color=#1565C0><b>spam folder.</b></font><br><br> Kindly report that mail as <b><font color=#1565C0>Not Spam</font></b> to get further updates received directly to your inbox.  ",
                Html.FROM_HTML_MODE_COMPACT
            )
            val dialog = alertDialog.create()
            sharedStorage = this.getSharedPreferences(sharedPref, Context.MODE_PRIVATE)
            editor = sharedStorage.edit()
            customView.welcome_email_CHECKOUT.setOnClickListener {
                editor.putBoolean("Register", false)
                editor.apply()
                if (isAppInstalled(this,"com.google.android.gm")) {
                    val intent = packageManager.getLaunchIntentForPackage("com.google.android.gm")
                    startActivity(intent)
                }else{
                    dialog.dismiss()
                    Toast.makeText(this,"Gmail Application not found ! Open another client.",Toast.LENGTH_LONG).show()
                }
            }

            customView.welcome_email_CANCEL.setOnClickListener {
                editor.putBoolean("Register", false)
                editor.apply()
                dialog.dismiss()
            }
            dialog.show()

        }
    }
    private fun isAppInstalled(context: Context, packageName: String): Boolean {
        return try {
            context.packageManager.getApplicationInfo(packageName, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    private fun getProducts(){
        sharedStorage=this@Home.getSharedPreferences(sharedPref, Context.MODE_PRIVATE)
        val uid=sharedStorage.getString("Uid","")!!

        val retrofitInterface= RetrofitClient.getInstance()
        val call: Call<GetAllProductsResponse> =retrofitInterface.getAllProducts(uid)
        Log.e("after call", "onCreate: " )
        call.enqueue(object : Callback<GetAllProductsResponse> {
            override fun onResponse(call: Call<GetAllProductsResponse>, response: Response<GetAllProductsResponse>) {

                when {

                    (response.code()==200 ||response.isSuccessful)->{

                        val res= response.body()!!
                        Log.e("inside ", "onResponse: 200" )
                        val productArray=res.data
                        val jsonObject= JSONObject(productArray)
                        val jsonArray=jsonObject.optJSONArray("products")
                        Log.e("", "Array : $productArray")

                        shimmer_effect_layout_home_1.stopShimmer()
                        shimmer_effect_layout_home_1.visibility=View.GONE
                        if (res.count.toInt()>0){
                            home_no_image_relativelayout.visibility=View.GONE
                            no_image_view_home.visibility=View.GONE
                            relativelayout_searchHolder.visibility=View.VISIBLE
                            horizontal_scroll_category_home.visibility=View.VISIBLE
                            recyclerView_relativeLayout_home.visibility=View.VISIBLE
                            home_product_recycler_view.visibility=View.VISIBLE
                            drawer_layout21.snackbar(res.msg)

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
                                val url2=obj.optString("product_url_2")
                                val url3=obj.optString("product_url_3")
                                home_product_recycler_view.adapter=
                                    HomeProductsAdapter(this@Home,list)
                                home_product_recycler_view.layoutManager=LinearLayoutManager(this@Home)
                                home_product_recycler_view.setHasFixedSize(true)
                                //(my_products_list.adapter )?.notifyDataSetChanged()
                                list.add(HomeAdapterDataClass(pid,name,price,stock,description,category,url1,url2,url3))
                            }
                        }else{
                            horizontal_scroll_category_home.visibility=View.VISIBLE
                            relativelayout_searchHolder.visibility=View.VISIBLE
                            home_no_image_relativelayout.visibility=View.VISIBLE
                            no_image_view_home.visibility=View.VISIBLE
                            Picasso.get()
                                .load(R.drawable.no_results_image)
                                .error(R.drawable.ic_placeholder_slider)
                                .fit()
                                .into(no_image_view_home)
                        }
                    }
                    response.code()==408->{
                        horizontal_scroll_category_home.visibility=View.VISIBLE
                        relativelayout_searchHolder.visibility=View.VISIBLE
                        home_no_image_relativelayout.visibility=View.VISIBLE
                        no_image_view_home.visibility=View.VISIBLE
                        drawer_layout21.snackbar("Error getting products . Close the application and try again !")
                    }
                }
            }

            override fun onFailure(call: Call<GetAllProductsResponse>, t: Throwable) {
                horizontal_scroll_category_home.visibility=View.VISIBLE
                relativelayout_searchHolder.visibility=View.VISIBLE
                home_no_image_relativelayout.visibility=View.VISIBLE
                no_image_view_home.visibility=View.VISIBLE
                drawer_layout21.snackbar("Connection timed out . Please try again ! : "+t.message)
            }
        })
    }
}