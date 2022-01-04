package com.example.tradego.activities

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import com.example.tradego.R
import com.example.tradego.models.BuyProductVerifyOTPResponse
import com.example.tradego.retrofit.RetrofitClient
import com.example.tradego.retrofit.snackbar
import kotlinx.android.synthetic.main.activity_buy_product_verify_page.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class BuyProduct_VerifyPage : AppCompatActivity() {
    private val sharedPref="Auth_check"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_buy_product_verify_page)
        setSupportActionBar(toolbar_productBuy_verify)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        val sh: SharedPreferences =this.getSharedPreferences(sharedPref, Context.MODE_PRIVATE)
        val uid=sh.getString("Uid","")!!
        val phone=intent.getStringExtra("Phone")!!
        val curStock=intent.getStringExtra("Current Stock")!!
        val pid=intent.getStringExtra("p_id")!!



        productBuy_submit_btn.setOnClickListener{
            val code=productBuy_edittextVP.text.toString().trim()
            Log.e("Verify code", "My code:${code} ", )
            if (code.isNotEmpty()){
                val call: Call<BuyProductVerifyOTPResponse> = RetrofitClient.getInstance().verifyOTP(pid, phone, curStock, code,uid)
                call.enqueue(object :Callback<BuyProductVerifyOTPResponse> {
                    override fun onResponse(call: Call<BuyProductVerifyOTPResponse>, response: Response<BuyProductVerifyOTPResponse>) {
                        when{
                            response.code()==200||response.isSuccessful ->{
                                verify_otp_RelativeLayout_Main.snackbar("Product ordered Successfully")
                            }
                            response.code()==408->{
                                verify_otp_RelativeLayout_Main.snackbar("Product not ordered . Try Again !")
                            }else->{
                            verify_otp_RelativeLayout_Main.snackbar("Product not ordered . Try Again !")
                        }
                        }
                    }

                    override fun onFailure(call: Call<BuyProductVerifyOTPResponse>, t: Throwable) {
                        verify_otp_RelativeLayout_Main.snackbar("Product not ordered . Try Again !")
                    }
                })
            }else{
                verify_otp_RelativeLayout_Main.snackbar("Code not Valid !")
            }

        }
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
}