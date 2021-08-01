package com.example.tradego.retrofit

import android.app.Activity
import android.app.AlertDialog
import android.content.ContentResolver
import android.net.Uri
import android.provider.OpenableColumns
import android.view.Gravity
import android.view.View
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.tradego.R
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.delete_product_alertdialog_layout.view.*
import kotlinx.android.synthetic.main.update_product_stock_layout.view.*

fun View.snackbar(message: String) {
    Snackbar.make(
        this,
        message,
        Snackbar.LENGTH_LONG
    ).also { snackbar ->
        snackbar.duration=4000
        snackbar.setAction("Ok") {
            snackbar.dismiss()
        }
    }.show()
}

fun ContentResolver.getFileName(fileUri: Uri): String {
    var name = ""
    val returnCursor = this.query(fileUri, null, null, null, null)
    if (returnCursor != null) {
        val nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        returnCursor.moveToFirst()
        name = returnCursor.getString(nameIndex)
        returnCursor.close()
    }
    return name
}

fun customProgressBar(activity: Activity): AlertDialog {

    val alertDialog = AlertDialog.Builder(activity,R.style.customAlertDialog)
    val customView = View.inflate(activity,R.layout.custom_progressbar_dialog,null)
    alertDialog.setView(customView)
    alertDialog.setCancelable(false)

    return alertDialog.create()!!
}
