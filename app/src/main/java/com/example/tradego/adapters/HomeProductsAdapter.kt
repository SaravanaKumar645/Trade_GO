package com.example.tradego.adapters

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.tradego.R
import com.example.tradego.activities.SingleProductViewPage
import com.example.tradego.adapterdataclasses.HomeAdapterDataClass
import com.google.android.material.snackbar.Snackbar
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.home_product_adapter_view.view.*


class HomeProductsAdapter(val c: Context,private val allProductList:List<HomeAdapterDataClass>) :
 RecyclerView.Adapter<HomeProductsAdapter.HomeProductViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeProductViewHolder {
       val itemView=LayoutInflater.from(parent.context).inflate(
           R.layout.home_product_adapter_view,
           parent,false
       )
        return HomeProductViewHolder((itemView))
    }

    override fun onBindViewHolder(holder: HomeProductViewHolder, position: Int) {
        val currentProduct=allProductList[position]
        val v=holder.itemView
        Picasso.get()
            .load(currentProduct.url1)
            .error(R.drawable.no_image_icon)
            .fit()
            .into(holder.productImage)
        holder.productName.text=currentProduct.p_name
        holder.viewProductDetailsButton.setOnClickListener{
            val intent= Intent(c, SingleProductViewPage::class.java)
            intent.putExtra("p_id",currentProduct.pid)
            intent.putExtra("p_name",currentProduct.p_name)
            intent.putExtra("p_price",currentProduct.p_price)
            intent.putExtra("p_stock",currentProduct.p_stock)
            intent.putExtra("p_desc",currentProduct.p_description)
            intent.putExtra("p_category",currentProduct.category)
            intent.putExtra("p_url1",currentProduct.url1)
            intent.putExtra("p_url2",currentProduct.url2)
            intent.putExtra("p_url3",currentProduct.url3)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            c.startActivity(intent)
        }

    }

    override fun getItemCount(): Int {
       return  allProductList.size
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

    inner class HomeProductViewHolder( itemView: View):RecyclerView.ViewHolder(itemView){
         val productImage:ImageView= itemView.home_product_image
         val productName:TextView= itemView.home_product_name
         val viewProductDetailsButton:Button= itemView.home_product_viewDetails_btn


    }

}