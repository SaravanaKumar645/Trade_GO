package com.example.tradego.activities
import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.provider.Settings
import android.util.Log
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View.OnTouchListener
import android.widget.RemoteViews
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.denzcoskun.imageslider.constants.ScaleTypes
import com.denzcoskun.imageslider.models.SlideModel
import com.example.tradego.R
import com.example.tradego.models.ProductResponse
import com.example.tradego.retrofit.*
import com.skydoves.powerspinner.SpinnerGravity
import kotlinx.android.synthetic.main.activity_product_add_section.*
import kotlinx.android.synthetic.main.activity_single_product_view_page.*
import kotlinx.android.synthetic.main.activity_your_products.*
import kotlinx.android.synthetic.main.custom_progressbar_dialog.view.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream


class ProductAddSection : AppCompatActivity() {
     private val imageList=ArrayList<SlideModel>()
    private val imageUri=ArrayList<Uri>()
    private val sharedPref="Auth_check"
    private val category=ArrayList<String>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_add_section)

        setSupportActionBar(toolbar_add_product_area as Toolbar?)
        supportActionBar?.title="Add Products"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)


            category.add("Fashion")
            category.add("Electronics")
            category.add("Groceries")
            category.add("Furniture")
            category.add("Sports")
            category.add("Beauty")
            category.add("Others")
            category_dropdownList.arrowAnimate = true
            category_dropdownList.setItems(category)
            category_dropdownList.arrowGravity = SpinnerGravity.END
            // category_dropdownList.selectItemByIndex(0)
            category_dropdownList.lifecycleOwner = this@ProductAddSection


        add_images_btn.setOnClickListener {
                if (!askForPermissions()) {
                    return@setOnClickListener
                }
                openGalleryForImages()
            }

        clear_imageSlider.setOnClickListener{
            if(imageList.isEmpty()){
                Toast.makeText(applicationContext,"No images !. Add a few.",Toast.LENGTH_LONG).show()
            }
            imageList.clear()
            image_Slider.setImageList(imageList)
        }


        add_product_submit.setOnClickListener{
            if(!validateProductName() or !validateProductPrice() or !validateProductStock() or !validateProductCategory() or !validateProductDescription() or !validateProductImagecount()){
                return@setOnClickListener
            }
            add_product_submit.isEnabled=false
            val index=category_dropdownList.selectedIndex
            val p_category=category[index]
            val p_name=add_p_name.text.toString().trim()
            val p_price=add_p_price.text.toString().trim()
            val p_stock=add_p_stock.text.toString().trim()
            val p_description=add_p_description.text.toString().trim()
            val dialog= customProgressBar(this)
           uploadFile(imageUri[0],imageUri[1],imageUri[2],p_name,p_category,p_price,p_stock,p_description,dialog)


        }
        add_p_description.setOnTouchListener(OnTouchListener { v, event ->
            if (v.id == R.id.add_p_description) {
                v.parent.requestDisallowInterceptTouchEvent(true)
                when (event.action and MotionEvent.ACTION_MASK) {
                    MotionEvent.ACTION_UP -> v.parent.requestDisallowInterceptTouchEvent(false)
                    else->{
                        performClick()
                    }
                }
            }
            return@OnTouchListener false
        })

    }
    private fun performClick() {
        //add_p_description.isEnabled=true
    }

    private fun openGalleryForImages() {

        // For latest versions API LEVEL 19+
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "image/*"
        startActivityForResult(intent, 200)

    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(resultCode==Activity.RESULT_OK && requestCode==200){
            if(data?.clipData!=null){
                image_Slider.background=null
                //multiple images are selected

                if(imageList.size>2){
                        Toast.makeText(this,"You can add up to only 3 images.",Toast.LENGTH_LONG).show()
                    }else{
                        val count = data.clipData!!.itemCount

                        for(i in 0 until count){
                            if(imageList.size==3){
                                Toast.makeText(this,"You can add up to only 3 images.",Toast.LENGTH_LONG).show()
                                break
                            }
                            val imgUri= data.clipData!!.getItemAt(i).uri.toString()
                            val path= data.clipData!!.getItemAt(i).uri

                            imageUri.add(path)
                            imageList.add(SlideModel(imgUri))
                        }
                    if(imageList.size<3){
                        Toast.makeText(this,"Select at least 3 images.",Toast.LENGTH_LONG).show()
                    }
                        image_Slider.setImageList(imageList,ScaleTypes.FIT)

                    }

            }else if(data?.data!=null){
                //single image is selected
                image_Slider.background=null
                    if(imageList.size<2){
                        Toast.makeText(this,"Select at least 3 images.",Toast.LENGTH_LONG).show()
                    }
                    if(imageList.size>2){
                        Toast.makeText(this,"You can add up to only 3 images.",Toast.LENGTH_LONG).show()
                    }else{
                        val path=data.data!!
                        imageUri.add(path)
                        imageList.add(SlideModel(path.toString()))
                        image_Slider.setImageList(imageList,ScaleTypes.FIT)
                    }
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
                return true
            }
        }
    }



    //permission area
    private fun isPermissionsAllowed(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }
    private fun askForPermissions(): Boolean {
        if (!isPermissionsAllowed()) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this as Activity,Manifest.permission.READ_EXTERNAL_STORAGE)) {
                showPermissionDeniedDialog()
            } else {
                ActivityCompat.requestPermissions(this as Activity,arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),200)
            }
            return false
        }
        return true
    }

    override fun onRequestPermissionsResult(requestCode: Int,permissions: Array<String>,grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            200 -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                   print("success") // permission is granted, you can perform your operation here
                }
                return
            }
        }
    }

    private fun showPermissionDeniedDialog() {
        AlertDialog.Builder(this)
            .setTitle("Permission Denied")
            .setMessage("Permission is denied, Please allow permissions from App Settings.")
            .setPositiveButton("App Settings"
            ) { _, _ ->
                // send to app settings if permission is denied permanently
                val intent = Intent()
                intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                val uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivity(intent)
            }
            .setNegativeButton("Cancel",null)
            .show()
    }

    //upload file function
    private fun uploadFile(uri1:Uri,uri2:Uri,uri3:Uri,name:String,category:String,price:String,stock:String,description:String,dialog: AlertDialog){
        //Getting user id to pass with product
        val sh: SharedPreferences =this.getSharedPreferences(sharedPref, Context.MODE_PRIVATE)
        val id:String= sh.getString("Uid","")!!
        val parcelFileDescriptor_file1=contentResolver.openFileDescriptor(uri1,"r",null)?:return
        val parcelFileDescriptor_file2=contentResolver.openFileDescriptor(uri1,"r",null)?:return
        val parcelFileDescriptor_file3=contentResolver.openFileDescriptor(uri1,"r",null)?:return
    //preparing file i.e to store it in the cache storage
        val file1=prepareFile(uri1,parcelFileDescriptor_file1)
        val file2=prepareFile(uri2,parcelFileDescriptor_file2)
        val file3=prepareFile(uri3,parcelFileDescriptor_file3)
    //creating multipart form data
        val requestFile1:RequestBody= RequestBody.create(MediaType.parse("image/*"),file1)
        val requestFile2:RequestBody= RequestBody.create(MediaType.parse("image/*"),file2)
        val requestFile3:RequestBody= RequestBody.create(MediaType.parse("image/*"),file3)


        val p_image1:MultipartBody.Part=MultipartBody.Part.createFormData("p_image",file1.name,requestFile1)
        val p_image2:MultipartBody.Part=MultipartBody.Part.createFormData("p_image",file2.name,requestFile2)
        val p_image3:MultipartBody.Part=MultipartBody.Part.createFormData("p_image",file3.name,requestFile3)
        val p_name=RequestBody.create(MediaType.parse("multipart/form-data"),name)
        val p_price=RequestBody.create(MediaType.parse("multipart/form-data"),price)
        val p_category=RequestBody.create(MediaType.parse("multipart/form-data"),category)
        val p_stock=RequestBody.create(MediaType.parse("multipart/form-data"),stock)
        val p_description=RequestBody.create(MediaType.parse("multipart/form-data"),description)
        val user_id=RequestBody.create(MediaType.parse("multipart/form-data"),id)
        val img_desc=RequestBody.create(MediaType.parse("multipart/form-data"),"p_image")


        dialog.show()
        val retrofitInterface= RetrofitClient.getInstance()
        val call: Call<ProductResponse> =retrofitInterface.uploadProduct(p_name,p_price,p_category,p_stock,p_description,user_id,img_desc,p_image1,p_image2, p_image3)
        layout_root.snackbar("Adding your Product . It will take a few seconds...")
         call.enqueue(object:Callback<ProductResponse>{
            override fun onResponse(call: Call<ProductResponse>, response: Response<ProductResponse>) {
              when{
                   response.code()==200->{
                       dialog.dismiss()
                       val res=response.body()!!
                       if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                           sendNotification(name,"Your Product has been successfully uploaded.")
                       }else{
                           layout_root.snackbar(res.msg)
                           Log.e("CHECK   ::: ", "success: "+response.body()?.msg!! )
                       }
                      Log.e("CHECK   ::: ", "success: "+response.body()?.msg!! )
                       val intent=Intent(applicationContext,YourProducts::class.java)
                       intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                       startActivity(intent)

                   }
                  response.code()==408->{
                      dialog.dismiss()
                      add_product_submit.isEnabled=true
                      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                          sendNotification(name,response.body()?.msg!!)
                      }else{
                          dialog.dismiss()
                          layout_root.snackbar(response.body()?.msg!!)
                          Log.e("CHECK   ::: ", "success: "+response.body()?.msg!! )
                      }
                      Log.e("CHECK   ::: ", "Failure: "+response.body()?.msg!! )
                  }
              }
            }

            override fun onFailure(call: Call<ProductResponse>, t: Throwable) {
                add_product_submit.isEnabled=true
                dialog.dismiss()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    sendNotification(name,t.message!!)
                }else{
                    layout_root.snackbar(t.message!!)
                    Log.e("CHECK   ::: ", "Failed call: "+t.message )
                }
            }

        })
    }


    private fun prepareFile(uri:Uri,parcelFileDescriptor:ParcelFileDescriptor):File{

        val inputStream=FileInputStream(parcelFileDescriptor.fileDescriptor)
        val file=File(cacheDir,contentResolver.getFileName((uri)))
        val outputStream=FileOutputStream(file)
        inputStream.copyTo(outputStream)
        return file
    }

    //validation area
    private fun validateProductName():Boolean{
        val name=add_p_name.text.toString().trim()
        return when{
            name.isEmpty()->{
                add_p_name.error="Field cannot be empty."
                false
            }
            name.length>26->{
                add_p_name.error="Try providing a short name."
                return false
            }

            else->{
                add_p_name.error=null
                return true
            }
        }
    }
    private fun validateProductPrice():Boolean{
        val price=add_p_price.text.toString().trim()

        return when{
            price.isEmpty()->{
                add_p_price.error="Field cannot be empty."
                false
            }
            (price.toInt()>200000)->{
                add_p_price.error="Cannot trade this much valuable products."
                return false
            }
            else->{
                add_p_price.error=null
                return true
            }
        }
    }
    private fun validateProductStock():Boolean{
        val stock=add_p_stock.text.toString().trim()
        return when{
            stock.isEmpty()->{
                add_p_stock.error="Field cannot be empty."
                false
            }
            else->{
                add_p_stock.error=null
                return true
            }
        }
    }
    private fun validateProductDescription():Boolean{
        val desc=add_p_description.text.toString().trim()
        return when{
            desc.isEmpty()->{
                add_p_description.error="Field cannot be empty."
                false
            }
            desc.length>250->{
                add_p_description.error="Limit  250 characters"
                return false
            }
            else->{
                add_p_description.error=null
                return true
            }
        }
    }
    private fun validateProductImagecount():Boolean{
        val count=imageList.size

        return when{
            count<3->{
                layout_root.snackbar("Add at least 3 images of your Product !")
                false
            }
            else->{
                return true
            }
        }
    }
    private fun validateProductCategory():Boolean{
        val range=0..6
        val cat=category_dropdownList.selectedIndex
        return when{
           (range.contains(cat))->{
                 true
            }
            else->{
                layout_root.snackbar("Choose the Product Category !")
                return false
            }
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun sendNotification(title: String,msg:String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val intent=Intent(this,YourProducts::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            val openActivity= PendingIntent.getActivity(this,10,intent,0)
            val customView = RemoteViews(packageName, R.layout.notification_added_to_cart)
            customView.setTextViewText(R.id.notify_cart_title,title)
            customView.setTextViewText(R.id.notify_cart_desc,msg)
            customView.setOnClickFillInIntent(R.id.notify_cart_view_btn,intent)
            val builder: NotificationCompat.Builder = NotificationCompat.Builder(this, "cart")
                .setSmallIcon(R.drawable.ic_outline_shopping_cart_24)
                .setStyle(NotificationCompat.DecoratedCustomViewStyle())
                .setContent(customView)
                .setContentIntent(openActivity)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
            val manager = NotificationManagerCompat.from(this)
            manager.notify(0, builder.build())
        }
    }
//    private fun uploadDummy(uri:Uri){
//
//        layout_root.snackbar("Uploading your Product . It will take a few seconds...")
//        val parcelFileDescriptor=contentResolver.openFileDescriptor(uri,"r",null)?:return
//        val inputStream=FileInputStream(parcelFileDescriptor.fileDescriptor)
//        val file1=File(cacheDir,contentResolver.getFileName((uri)))
//        val outputStream=FileOutputStream(file1)
//        inputStream.copyTo(outputStream)
//
//
//        val requestFile1:RequestBody= RequestBody.create(MediaType.parse("image/*"),file1)
//        //val requestFile2:RequestBody= RequestBody.create(MediaType.parse("image/*"),file1)
//        //val requestFile3:RequestBody= RequestBody.create(MediaType.parse("image/*"),file1)
//
//        val p_image:MultipartBody.Part=MultipartBody.Part.createFormData("hi",file1.name,requestFile1)
//        val name=RequestBody.create(MediaType.parse("multipart/form-data"),"testProduct")
//        val price=RequestBody.create(MediaType.parse("multipart/form-data"),"Rs. 1000")
//        val retrofitInterface= RetrofitClient.getInstance().create(RetrofitInterface::class.java)
//        val call: Call<ProductResponse> =retrofitInterface.up(p_image,name,price)
//        Toast.makeText(this@ProductAddSection,"after call",Toast.LENGTH_LONG).show()
//        call.enqueue(object:Callback<ProductResponse>{
//            override fun onResponse(call: Call<ProductResponse>, response: Response<ProductResponse>) {
//                val res=response.body()
//                Log.e("CHECK   ::: ", "In call: " )
//                layout_root.snackbar(res?.msg!!)
//                //Toast.makeText(this@ProductAddSection,"Message : ${res?.msg} and Product Id: ${res?.pid}",Toast.LENGTH_LONG).show()
//            }
//
//            override fun onFailure(call: Call<ProductResponse>, t: Throwable) {
//                layout_root.snackbar(t.message!!)
//                //Toast.makeText(this@ProductAddSection,"Failure . Try Again "+t.message,Toast.LENGTH_LONG).show()
//                Log.e("CHECK   ::: ", "Failed call: "+t.message )
//
//
//            }
//
//        })
//    }
}