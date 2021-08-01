package com.example.tradego.adapters

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.tradego.R
import com.example.tradego.activities.UserCart
import com.example.tradego.adapterdataclasses.UserCartAdapterDataClass
import com.example.tradego.models.DeleteCartProductsResponse
import com.example.tradego.retrofit.RetrofitClient
import com.google.android.material.snackbar.Snackbar
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.user_cart_adapter_layout.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class UserCartAdapter(val c: Context,private val cartList:List<UserCartAdapterDataClass>,private val listener:onItemClickObserver) :
    RecyclerView.Adapter<UserCartAdapter.CartProductViewHolder>(){
    private var handler: Handler = Handler()
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartProductViewHolder {
        val itemView=LayoutInflater.from(parent.context).inflate(
            R.layout.user_cart_adapter_layout,
            parent,false
        )

        return CartProductViewHolder((itemView))
    }

    override fun onBindViewHolder(holder: CartProductViewHolder, position: Int) {
        val currentProduct=cartList[position]
        val v=holder.itemView
        Picasso.get()
            .load(currentProduct.url1)
            .error(R.drawable.no_image_icon)
            .fit()
            .into(holder.productImage)
        holder.productName.text=currentProduct.p_name
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            holder.productPrice.text=Html.fromHtml("Rs.${currentProduct.p_price}",Html.FROM_HTML_MODE_COMPACT)
        }else{
            holder.productPrice.text=Html.fromHtml("Rs.${currentProduct.p_price}")
        }
        holder.cartRemoveButton.setOnClickListener{

            val alertDialog = AlertDialog.Builder(c)
            alertDialog.setTitle("Remove Item")
            alertDialog.setMessage("Are you sure want to remove this product ?")
            alertDialog.setCancelable(true)

            alertDialog.setPositiveButton("Yes") { _, _ ->
                v.snackbar("Removing...")
                removeCartItems(currentProduct.pid, v)
            }
            alertDialog.setNegativeButton("No") { d, _ ->
                d.dismiss()
            }
            val dialog=alertDialog.create()
            dialog.show()

        }
    }

    override fun getItemCount(): Int {
        return  cartList.size
    }
    fun View.snackbar(message: String) {
        Snackbar.make(
            (context as Activity).findViewById(android.R.id.content),
            message,
            Snackbar.LENGTH_LONG
        ).also { snackbar ->

            snackbar.setAction("Ok") {
                snackbar.dismiss()
            }
        }.show()
    }
    fun removeCartItems(id:String, v:View){
        Log.e("", "removeCartItems: inideddededed", )
        val call : Call<DeleteCartProductsResponse> = RetrofitClient.getInstance().deleteCartProduct(id)
        call.enqueue(object: Callback<DeleteCartProductsResponse> {
            override fun onResponse(call: Call<DeleteCartProductsResponse>, response: Response<DeleteCartProductsResponse>) {

                when{
                    response.code()==200 || response.isSuccessful->{
                        val res=response.body()!!
                        v.snackbar(res.msg)
                        handler.postDelayed({
                            val intent= Intent(c, UserCart::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            c.startActivity(intent)

                        },2000)

                        Log.e("", "onResponse: success"+res.msg )
                    }
                    response.code()==408->{
                        v.snackbar("Cannot remove product . Try again !")
                    }
                    response.code()==405->{

                        v.snackbar("Unexpected error ! ")
                    }
                }
            }

            override fun onFailure(call: Call<DeleteCartProductsResponse>, t: Throwable) {
                Log.e("", "onResponse: Failed"+t.message )
                v.snackbar(t.message!!)
            }
        })
    }

    inner class CartProductViewHolder( itemView: View):RecyclerView.ViewHolder(itemView),View.OnClickListener{
        val productImage:ImageView= itemView.cart_product_image
        val productName:TextView= itemView.cart_product_name
        val productPrice:TextView= itemView.cart_product_price
        val cartRemoveButton:Button= itemView.cart_product_remove_btn
        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            val position:Int= absoluteAdapterPosition
            if (position!=RecyclerView.NO_POSITION){
                listener.onItemClick(position)
            }
        }
    }
    interface onItemClickObserver{
        fun onItemClick(position: Int)
    }

}