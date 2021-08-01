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
import com.example.tradego.models.RegisterResponse
import com.example.tradego.retrofit.RetrofitClient

import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_register.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class Register : AppCompatActivity() {

    private val emailPattern :Regex=Regex( "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+")
    private val sharedPref="Auth_check"
    private lateinit var sharedStorage: SharedPreferences
    private  lateinit var editor: SharedPreferences.Editor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        //supportActionBar?.hide()
        navigation_login.setOnClickListener {
            startActivity(Intent(this, Login::class.java))
            finish()
        }

        button_register.setOnClickListener(View.OnClickListener {
            if (!validateName() or !validatePassword() or !validatePhoneNo() or !validateEmail() or !validateConfirmPassword()) {
                return@OnClickListener
            }
            registerUser(name_register.text.toString().trim(),email_register.text.toString().trim(),password_register.text.toString().trim(),Phone_register.text.toString().trim())

        })
    }

    private fun registerUser(name: String, email: String, password: String, phone: String) {
        //ProgressBar
        button_register.isEnabled=false
        relativeLayout_pgbar_register.visibility=View.VISIBLE
        pgBar_register.visibility=View.VISIBLE

        val retrofitInterface= (RetrofitClient.getInstance())
        val call: Call<RegisterResponse> =retrofitInterface.registerUser(name,email,password,phone)
        call.enqueue(object : Callback<RegisterResponse>{
            override fun onResponse(call: Call<RegisterResponse>, response: Response<RegisterResponse>) {

                when{
                    response.code()==200 ->{
                        val res=response.body()!!
                        sharedStorage=this@Register.getSharedPreferences(sharedPref, Context.MODE_PRIVATE)
                        editor =sharedStorage.edit()
                        editor.putString("Uid",res.id)
                        editor.putString("Email",res.email)
                        editor.putString("Name",res.name)
                        editor.putBoolean("Register",true)
                        editor.apply()
                            relativeLayout_pgbar_register.visibility=View.INVISIBLE
                            pgBar_register.visibility=View.INVISIBLE
                            Toast.makeText(this@Register,res.msg,Toast.LENGTH_LONG).show()
                            val intent=Intent(applicationContext,Home::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                    }
                    response.code()==408->{
                        button_register.isEnabled=true
                        relativeLayout_pgbar_register.visibility=View.INVISIBLE
                        pgBar_register.visibility=View.INVISIBLE
                        Toast.makeText(this@Register,"Failed to create User !. Try again",Toast.LENGTH_LONG).show()
                    }
                    response.code()==405->{
                        button_register.isEnabled=true
                        relativeLayout_pgbar_register.visibility=View.INVISIBLE
                        pgBar_register.visibility=View.INVISIBLE
                        Toast.makeText(this@Register,"Sign In failed : User Already exists !",Toast.LENGTH_LONG).show()
                    }
                }

            }

            override fun onFailure(call: Call<RegisterResponse>, t: Throwable) {
                button_register.isEnabled=true
                relativeLayout_pgbar_register.visibility=View.INVISIBLE
                pgBar_register.visibility=View.INVISIBLE
                Toast.makeText(this@Register,"ERROR: "+t.message,Toast.LENGTH_LONG).show()
            }

        })


    }

    private fun validateName(): Boolean {
        val name: String = name_register.text.toString().trim()
        return when {
            name.isEmpty() -> {
                name_register.error = "Field cannot be empty"
                false
            }
            name.length >= 20 -> {
                name_register.error = "Name Too Long"
                false
            }
            else -> {
                name_register.error = null

                true
            }
        }
    }
    private fun validateEmail(): Boolean {
        val `val`: String = email_register.text.toString().trim()
        return if (`val`.isEmpty()) {
            email_register.error = "Field cannot be empty"
            false
        } else if (!`val`.matches(emailPattern)) {
            email_register.error = "Invalid email address"
            false
        } else {
            email_register.error = null

            true
        }
    }

    private fun validatePassword(): Boolean {
        val `val`: String = password_register.text.toString().trim()
        return when {
            `val`.isEmpty() -> {
                password_register.error = "Field cannot be empty"
                false
            }
            `val`.length <= 5 -> {
                password_register.error = "Password must be at least 6 characters"
                false
            }
            else -> {
                password_register.error = null
                true
            }
        }
    }

    private fun validatePhoneNo(): Boolean {
        val `val`: String = Phone_register.text.toString().trim()
        return when {
            `val`.isEmpty() -> {
                Phone_register.error = "Field cannot be empty"
                false
            }
            `val`.length>10||`val`.length<10 -> {
                Phone_register.error="Enter a valid number"
                false
            }
            else -> {
                Phone_register.error = null
                true
            }
        }
    }

    private fun validateConfirmPassword(): Boolean {
        val val1: String = confirmPassword_register.text.toString().trim()
        val val2: String = password_register.text.toString().trim()
        return when {
            val1.isEmpty() -> {
                confirmPassword_register.error = "Field cannot be empty"
                false
            }
            val2 != val1 -> {
                confirmPassword_register.error = "Password doesn't match"
                false
            }
            else -> {
                confirmPassword_register.error = null
                true
            }
        }
    }

}