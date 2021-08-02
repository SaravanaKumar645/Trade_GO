package com.example.tradego.activities


import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import com.example.tradego.R
import com.example.tradego.models.RequestPasswordTokenResponse
import com.example.tradego.retrofit.RetrofitClient
import com.example.tradego.retrofit.snackbar
import kotlinx.android.synthetic.main.activity_request_password_token.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.properties.Delegates

class RequestPasswordToken : AppCompatActivity() {
    private val emailPattern :Regex=Regex( "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+")
    private val sharedPref="Auth_check"
    private val handler=Handler()
    private lateinit var email:String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_request_password_token)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val login=intent.getBooleanExtra("Login",false)
        val home=intent.getBooleanExtra("Home",false)
        val sh: SharedPreferences =this.getSharedPreferences(sharedPref, Context.MODE_PRIVATE)
        val uid=sh.getString("Uid","")!!
        email=sh.getString("Email","")!!

//        if(uid==null){
//            reset_password_MainLayout.snackbar("Error ! . User not found. Try login again")
//            val intent=Intent(applicationContext,Login::class.java)
//            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//            startActivity(intent)
//            return
//        }
        reset_password_btn.setOnClickListener{
            if(!validateEmail()){
                return@setOnClickListener
            }
            reset_password_btn.isEnabled=false
            reset_password_relativeLayout.visibility= View.VISIBLE
            reset_password_pgBar.visibility= View.VISIBLE
            val email: String = reset_password_edittext.text.toString().trim()
            val call:Call<RequestPasswordTokenResponse> =RetrofitClient.getInstance().requestPasswordToken(uid,email)
            call.enqueue(object:Callback<RequestPasswordTokenResponse>{
                override fun onResponse(call: Call<RequestPasswordTokenResponse>, response: Response<RequestPasswordTokenResponse>) {

                    when{
                        response.code()==200||response.isSuccessful->{
                            val res= response.body()!!
                            reset_password_relativeLayout.visibility= View.INVISIBLE
                            reset_password_pgBar.visibility= View.INVISIBLE
                            reset_password_MainLayout.snackbar(res.msg)
                            handler.postDelayed({
                                val intent= Intent(this@RequestPasswordToken,ConfirmPasswordChange::class.java)
                                intent.putExtra("Login",login)
                                intent.putExtra("Home",home)
                                intent.putExtra("Token",res.token_id)
                                intent.putExtra("Email",email)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                startActivity(intent)
                            },3000)


                        }else->{
                           reset_password_btn.isEnabled=true
                           reset_password_relativeLayout.visibility= View.INVISIBLE
                           reset_password_pgBar.visibility= View.INVISIBLE
                           reset_password_MainLayout.snackbar("User not found . Kindly check your email !")
                        }
                    }
                }
                override fun onFailure(call: Call<RequestPasswordTokenResponse>, t: Throwable) {
                    reset_password_btn.isEnabled=true
                      reset_password_relativeLayout.visibility= View.INVISIBLE
                      reset_password_pgBar.visibility= View.INVISIBLE
                      reset_password_MainLayout.snackbar("Request Failed ! ERROR :"+t.message)
                }
            })
        }

    }
    private fun validateEmail(): Boolean {
        val email1: String = reset_password_edittext.text.toString().trim()
        val home=intent.getBooleanExtra("Home",false)
        return when {

            email1.isEmpty() -> {
                reset_password_edittext.error = "Field cannot be empty"
                false
            }

            (!email1.matches(emailPattern)) -> {
                reset_password_edittext.error = "Invalid email address"
               return false
            }
            email1 != email ->{
                if(home){
                    reset_password_edittext.error = "No user found .Check your email !"
                    return false
                }
                return true
            }
            else -> {
                reset_password_edittext.error = null
                return true
            }
        }
    }

}