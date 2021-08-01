@file:Suppress("DEPRECATION")
package com.example.tradego.activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.tradego.R
import com.example.tradego.models.LoginResponse
import com.example.tradego.retrofit.RetrofitClient
import kotlinx.android.synthetic.main.activity_login.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class Login : AppCompatActivity() {

    private val emailPattern :Regex=Regex( "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+")
    private val sharedPref="Auth_check"
    private lateinit var sharedStorage:SharedPreferences
    private  lateinit var editor:SharedPreferences.Editor
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        //supportActionBar?.hide()

        val sh:SharedPreferences=this.getSharedPreferences(sharedPref, Context.MODE_PRIVATE)
        val uid=sh.getString("Uid",null)
        if(uid!=null){
            val intent=Intent(applicationContext,Home::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        } else{
            Toast.makeText(applicationContext,"You are logged out !. Login to continue.",Toast.LENGTH_LONG).show()
        }
        navigation_register.setOnClickListener {
            startActivity(Intent(this, Register::class.java))
            finish()
        }

        button_login.setOnClickListener(View.OnClickListener {
            if (!validateEmail() or !validatePassword()) {
                return@OnClickListener
            }
            login(email_login.text.toString().trim(),password_login.text.toString().trim())

        })
        forgot_password_login.setOnClickListener {
            val intent = Intent(this, RequestPasswordToken::class.java)
            intent.putExtra("Login",true)
            intent.putExtra("Home",false)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }
    }

    private fun login(email1: String, password: String) {
        /*val map=HashMap<String,String>()
        map["email"] = email
        map["password"] = password*/
        //progressBar
        button_login.isEnabled=false
        relativeLayout_pgbar_login.visibility=View.VISIBLE
        pgBar_login.visibility=View.VISIBLE


        val retrofitInterface= RetrofitClient.getInstance()
        val call:Call<LoginResponse> =retrofitInterface.loginUser(email1,password)

        call.enqueue(object :Callback<LoginResponse>{
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {

                when{
                    response.code()==200->{
                        val res=response.body()!!
                        sharedStorage=this@Login.getSharedPreferences(sharedPref, Context.MODE_PRIVATE)
                        editor =sharedStorage.edit()
                        editor.clear()
                        editor.apply()
                        editor.putString("Uid",res.id)
                        editor.putString("Email",res.email)
                        editor.putString("Name",res.name)
                        editor.commit()
                        Toast.makeText(applicationContext,res.msg,Toast.LENGTH_LONG).show()
                            val intent=Intent(applicationContext,Home::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            relativeLayout_pgbar_login.visibility=View.INVISIBLE
                            pgBar_login.visibility=View.INVISIBLE
                    }
                    response.code()==405->{
                        button_login.isEnabled=true
                        relativeLayout_pgbar_login.visibility=View.INVISIBLE
                        pgBar_login.visibility=View.INVISIBLE
                        Toast.makeText(this@Login,"Authentication failed, wrong password",Toast.LENGTH_LONG).show()
                    }
                    response.code()==408->{
                        button_login.isEnabled=true
                        relativeLayout_pgbar_login.visibility=View.INVISIBLE
                        pgBar_login.visibility=View.INVISIBLE
                        Toast.makeText(this@Login,"Authentication failed ! Error occurred",Toast.LENGTH_LONG).show()
                    }
                    response.code()==406->{
                        button_login.isEnabled=true
                        relativeLayout_pgbar_login.visibility=View.INVISIBLE
                        pgBar_login.visibility=View.INVISIBLE
                        Toast.makeText(this@Login,"User does not exist !",Toast.LENGTH_LONG).show()
                    }
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                button_login.isEnabled=true
                relativeLayout_pgbar_login.visibility=View.INVISIBLE
                pgBar_login.visibility=View.INVISIBLE
                Toast.makeText(applicationContext,t.message,Toast.LENGTH_LONG).show()
            }

        })

    }

    private fun validateEmail(): Boolean {
        val email: String = email_login.text.toString().trim()
        return when {
            email.isEmpty() -> {
                email_login.error = "Field cannot be empty"
                false
            }
            !email.matches(emailPattern) -> {
                email_login.error = "Invalid email address"
                false
            }
            else -> {
                email_login.error = null
                true
            }
        }
    }

    private fun validatePassword(): Boolean {
        val `val`: String =password_login.text.toString().trim()
        return when {
            `val`.isEmpty() -> {
                password_login.error = "Field cannot be empty"
                false
            }
            `val`.length <= 5 -> {
                password_login.error = "Password must be at least 6 characters"
                false
            }
            else -> {
                password_login.error = null
                true
            }
        }
    }

}