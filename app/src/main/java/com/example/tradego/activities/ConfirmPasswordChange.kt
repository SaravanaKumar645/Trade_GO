package com.example.tradego.activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.text.Html
import android.view.View
import com.example.tradego.R
import com.example.tradego.models.PasswordResetResponse
import com.example.tradego.retrofit.RetrofitClient
import com.example.tradego.retrofit.snackbar
import kotlinx.android.synthetic.main.activity_confirm_password_change.*
import kotlinx.android.synthetic.main.activity_register.*
import kotlinx.android.synthetic.main.activity_request_password_token.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ConfirmPasswordChange : AppCompatActivity() {
    private val sharedPref="Auth_check"
    private val handler= Handler()
    private lateinit var token:String
    private lateinit var mail:String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_confirm_password_change)
        token= intent.getStringExtra("Token").toString()
        mail= intent.getStringExtra("Email").toString()
        val login=intent.getBooleanExtra("Login",false)
        val home=intent.getBooleanExtra("Home",false)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            confirm_pass_note_textview3.text= Html.fromHtml("Note: After successful password update ,you will be <font color=#1565C0><b>Logged out.</b></font>",Html.FROM_HTML_MODE_COMPACT)
        }else{
            confirm_pass_note_textview3.text= Html.fromHtml("Note: After successful password update ,you will be <font color=#1565C0><b>Logged out.</b></font>")
        }
        val sh: SharedPreferences =this.getSharedPreferences(sharedPref, Context.MODE_PRIVATE)
        val uid=sh.getString("Uid","")!!

        confirm_password_reset_btn.setOnClickListener{
            if(!validateResetToken() or !validateNewPassword() or !validateConfirmPassword()){
                return@setOnClickListener
            }
            confirm_password_reset_btn.isEnabled=false
            confirm_password_relativeLayout.visibility= View.VISIBLE
            confirm_password_pgBar.visibility=View.VISIBLE
            val newPass=confirm_change_pass_edittext_newPass.text.toString().trim()
            val tokenId=confirm_change_pass_edittext_token.text.toString().trim()

            val call:Call<PasswordResetResponse> =RetrofitClient.getInstance().resetPassword(uid,tokenId,mail,newPass)
            call.enqueue(object:Callback<PasswordResetResponse>{
                override fun onResponse(call: Call<PasswordResetResponse>, response: Response<PasswordResetResponse>) {

                    when{

                        (response.code()==200||response.isSuccessful)->{
                            val res=response.body()!!
                            confirm_password_relativeLayout.visibility= View.INVISIBLE
                            confirm_password_pgBar.visibility=View.INVISIBLE
                            confirm_password_MainLayout.snackbar(res.msg)
                            val editor:SharedPreferences.Editor=sh.edit()
                            editor.clear()
                            editor.apply()

                                handler.postDelayed({
                                    val intent= Intent(this@ConfirmPasswordChange,Login::class.java)
                                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    startActivity(intent)
                                },1800)
                                return

                        }else->{
                        confirm_password_reset_btn.isEnabled=true
                          confirm_password_relativeLayout.visibility= View.INVISIBLE
                          confirm_password_pgBar.visibility=View.INVISIBLE
                          confirm_password_MainLayout.snackbar("Kindly try again . Request failed !")
                        }
                    }
                }

                override fun onFailure(call: Call<PasswordResetResponse>, t: Throwable) {
                    confirm_password_reset_btn.isEnabled=true
                    confirm_password_relativeLayout.visibility= View.INVISIBLE
                    confirm_password_pgBar.visibility=View.INVISIBLE
                    confirm_password_MainLayout.snackbar("Request Failed ! ERROR :"+t.message)
                }
            })

        }

    }
    private fun validateNewPassword(): Boolean {
        val `val`: String = confirm_change_pass_edittext_newPass.text.toString().trim()
        return when {
            `val`.isEmpty() -> {
                confirm_change_pass_edittext_newPass.error = "Field cannot be empty"
                false
            }
            `val`.length <= 5 -> {
                confirm_change_pass_edittext_newPass.error = "Password must be at least 6 characters"
                false
            }
            else -> {
                confirm_change_pass_edittext_newPass.error = null
                true
            }
        }
    }
    private fun validateConfirmPassword(): Boolean {
        val val1: String = confirm_change_pass_edittext_confirmPass.text.toString().trim()
        val val2: String = confirm_change_pass_edittext_newPass.text.toString().trim()
        return when {
            val1.isEmpty() -> {
                confirm_change_pass_edittext_confirmPass.error = "Field cannot be empty"
                false
            }
            val2 != val1 -> {
                confirm_change_pass_edittext_confirmPass.error = "Password doesn't match"
                false
            }
            else -> {
                confirm_change_pass_edittext_confirmPass.error = null
                true
            }
        }
    }
    private fun validateResetToken(): Boolean {
        val `val`: String = confirm_change_pass_edittext_token.text.toString().trim()
        return when {
            `val`.isEmpty() -> {
                confirm_change_pass_edittext_token.error = "Field cannot be empty"
                false
            }
             `val`!=token->{
                 confirm_change_pass_edittext_token.error = "Seems the token not matching.Copy and paste it again !"
                 false
             }
            else -> {
                confirm_change_pass_edittext_token.error = null
                true
            }
        }
    }


}