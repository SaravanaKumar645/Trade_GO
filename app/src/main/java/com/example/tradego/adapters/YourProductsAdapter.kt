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
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.example.tradego.R
import com.example.tradego.activities.YourProducts
import com.example.tradego.models.DeleteProductResponse
import com.example.tradego.models.UpdateStockResponse
import com.example.tradego.adapterdataclasses.YourProductsAdapterDataClass
import com.example.tradego.retrofit.RetrofitClient
import com.google.android.material.snackbar.Snackbar
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.delete_product_alertdialog_layout.view.*
import kotlinx.android.synthetic.main.your_products_adapter_view.view.*
import kotlinx.android.synthetic.main.update_product_stock_layout.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class YourProductsAdapter(val c:Context, private val productList:List<YourProductsAdapterDataClass>):
    RecyclerView.Adapter<YourProductsAdapter.ProductViewHolder>() {
    private var handler: Handler = Handler()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.your_products_adapter_view,
            parent, false)
        return ProductViewHolder(itemView)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val currentProduct=productList[position]
        val v=holder.itemView
        val context=holder.itemView.context
        holder.productName.text=currentProduct.p_name
        holder.productPrice.text=Html.fromHtml("<u>Rs.</u><font color=#000000><b>  ${currentProduct.p_price}</b></font>",Html.FROM_HTML_MODE_COMPACT)
        holder.productStock.text=Html.fromHtml("<u>In stock</u> :<font color=#1565C0><b> ${currentProduct.p_stock}</b></font>",Html.FROM_HTML_MODE_COMPACT)
        holder.productCategory.text=currentProduct.category
        Picasso.get()
            .load(currentProduct.url1)
            .error(R.drawable.no_image_icon)
            .fit()
            .into(holder.productImg)
        holder.updateStockBtn.setOnClickListener{

            val alertDialog = AlertDialog.Builder(context,R.style.customAlertDialog)
            val customView = View.inflate(context,R.layout.update_product_stock_layout,null)
            alertDialog.setView(customView)
            alertDialog.setCancelable(true)
            val dialog=alertDialog.create()
            customView.update_stock_submit_btn.setOnClickListener{
                val stock =customView.update_stock_edittext.text.toString().trim()
                if(stock.isEmpty()){
                    customView.update_stock_edittext.error="Field cannot be empty"

                }else{
                    Log.e("PID :", "Id : ${currentProduct.pid}" )
                    dialog.dismiss()

                    executeUpdateService(currentProduct.pid,stock,context, v,position)

                }

            }
            dialog.show()
        }
        holder.deleteProductBtn.setOnClickListener{
            val alertDialog = AlertDialog.Builder(context,R.style.customAlertDialog)
            val customView = View.inflate(context,R.layout.delete_product_alertdialog_layout,null)
            alertDialog.setView(customView)
            alertDialog.setCancelable(true)
            val dialog=alertDialog.create()
            customView.delete_product_NO_btn.setOnClickListener{
                dialog.dismiss()
            }
            customView.delete_product_Yes_btn.setOnClickListener{
                dialog.dismiss()
                executeDeleteService(currentProduct.pid, position,context, v)

            }
         dialog.show()
        }

    }

    //Retrofit call methods

    private fun executeDeleteService(pid: String, pos: Int, context: Context, v: View) {
        val call :Call<DeleteProductResponse> =RetrofitClient.getInstance().deleteProduct(pid)
        call.enqueue(object:Callback<DeleteProductResponse>{
            override fun onResponse(call: Call<DeleteProductResponse>, response: Response<DeleteProductResponse>) {
                val res=response.body()!!
                when{
                    response.code()==200 || response.isSuccessful->{
                        v.snackbar(res.msg)
                        handler.postDelayed({
                            val intent= Intent(context, YourProducts::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            context.startActivity(intent)

                        },2000)

                        Log.e("", "onResponse: success"+res.msg )
                    }else->{
                      v.snackbar(res.msg)
                      Log.e("", "onResponse: Failed"+res.msg )
                    }
                }
            }

            override fun onFailure(call: Call<DeleteProductResponse>, t: Throwable) {
                Log.e("", "onResponse: Failed"+t.message )
                v.snackbar(t.message!!)
            }
        })
    }

    private fun executeUpdateService(pid: String, stock: String, context: Context, v: View, position: Int) {
            val call: Call<UpdateStockResponse> =RetrofitClient.getInstance().updateStock(pid,stock)
            call.enqueue(object:Callback<UpdateStockResponse>{
            override fun onResponse(call: Call<UpdateStockResponse>, response: Response<UpdateStockResponse>) {
                val res=response.body()!!
                when{
                    response.code()==200||response.isSuccessful->{
                        v.snackbar(res.msg)

                        handler.postDelayed({
                            val intent= Intent(context, YourProducts::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            context.startActivity(intent)
                        },2000)


                    }
                    else->{
                      v.snackbar(res.msg)
                    }
                }
            }

            override fun onFailure(call: Call<UpdateStockResponse>, t: Throwable) {
                   v.snackbar(t.message!!)
            }
        })
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

    override fun getItemCount()=productList.size

    inner class ProductViewHolder( itemView: View):RecyclerView.ViewHolder(itemView){
         
        val productImg:ImageView=itemView.p_image
        val productName:TextView=itemView.p_name
        val productPrice:TextView=itemView.p_price
        val productStock:TextView=itemView.p_stock
        val productCategory:TextView=itemView.p_category
        val updateStockBtn:Button=itemView.p_update_stock_btn
        val deleteProductBtn:Button=itemView.p_delete_product_btn


    }
}