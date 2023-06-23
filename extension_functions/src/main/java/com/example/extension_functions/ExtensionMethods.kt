package com.example.extension_functions

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.SystemClock
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar

fun Context.showToastShort(msg:String){
    Toast.makeText(this,msg,Toast.LENGTH_SHORT).show()
}
fun Context.showToastLong(msg:String){
    Toast.makeText(this,msg,Toast.LENGTH_LONG).show()
}

fun View.setSafeOnClickListener(debounceTime: Long = 1500L, action: () -> Unit) {
    this.setOnClickListener(object : View.OnClickListener {
        private var lastClickTime: Long = 0
        override fun onClick(v: View) {
            if (SystemClock.elapsedRealtime() - lastClickTime < debounceTime) return
            else{
                //Will always invoke else statement because system clock time is not the same since we have delayed by 1.5 seconds
                action.invoke()
                Log.d("TAG99", "onClick: Time not delayed Double CLick Occurred")

            }
            lastClickTime = SystemClock.elapsedRealtime()
        }

    })
}

fun String.isValidEmail() = !TextUtils.isEmpty(this) && Patterns.EMAIL_ADDRESS.matcher(this).matches()

fun <T> Context.openCustomActivity(it: Class<T>) {
    val intent = Intent(this, it)
    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
    startActivity(intent)
}

/**
 *
 * Usage
   For example, In the MainActivity, how to call this?

    if(isNetworkAvailable()){
        //Perform your task
    }else{
        showToast("Internet is not available, Try Later")
    }

 *
 */
fun Context.isNetworkAvailable(): Boolean {
    val manager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val capabilities = manager.getNetworkCapabilities(manager.activeNetwork)
        if (capabilities != null) {
            if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                return true
            } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                return true
            } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                return true
            }
        }
    } else {
        try {
            val activeNetworkInfo = manager.activeNetworkInfo
            if (activeNetworkInfo != null && activeNetworkInfo.isConnected) {
                return true
            }
        } catch (e: Exception) {
            e.message
        }
    }
    return false
}

fun EditText.onChange(textChanged: ((String) -> Unit)) {
    this.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {

        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            textChanged.invoke(s.toString())
        }
    })
}

fun View.showSnackMessage(
    message: String?,
    anchorView: View? = null,
    backgroundColor: Int,
    textColor: Int,
    length: Int = Snackbar.LENGTH_SHORT
) {
    message?.let {
        try {
            val snack = Snackbar.make(this, it, length)
            snack.setBackgroundTint(ContextCompat.getColor(context, backgroundColor))
            snack.setTextColor(ContextCompat.getColor(context, textColor))
            snack.anchorView = anchorView
            snack.show()
        } catch (ex: Exception) {
            ex.message
        }
    }
}

/**

//Usage
binding.loadingPb.hide()

 */
fun View.show() {
    visibility = View.VISIBLE
}
fun View.hide() {
    visibility = View.GONE
}
fun View.invisible() {
    visibility = View.INVISIBLE
}

//Function To Convert View Type to a Bitmap
fun viewToBitmap(binding: ConstraintLayout): Bitmap {
    val view: ConstraintLayout = binding
    val myBitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(myBitmap)
    view.draw(canvas)
    return myBitmap
}

fun View.showKeyboard() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    this.requestFocus()
    imm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
}
fun View.hideKeyboard() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(windowToken, 0)
}

//Permissions
fun <T : Activity> T.requestPermissionsFast(vararg permissions: String, REQUEST_CODE: Int) {
    val permissionsToRequest = permissions.filter {
        ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
    }

    if (permissionsToRequest.isEmpty()) {
        // All permissions are already granted
        return
    }

    val shouldShowRationaleList = permissionsToRequest.map {
        ActivityCompat.shouldShowRequestPermissionRationale(this, it)
    }

    if (shouldShowRationaleList.contains(true)) {
        // Show the rationale dialog
        showPermissionRationaleDialog(permissionsToRequest.toTypedArray(), REQUEST_CODE)
    } else {
        // Request the permissions directly
        ActivityCompat.requestPermissions(this, permissionsToRequest.toTypedArray(), REQUEST_CODE)
    }
}
private fun <T : Activity> T.showPermissionRationaleDialog(permissions: Array<String>, REQUEST_CODE: Int) {
    val builder = AlertDialog.Builder(this)
        .setTitle("Permissions Required")
        .setMessage("This app requires certain permissions to function properly.")
        .setPositiveButton("Grant Permissions") { dialog, _ ->
            ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE)
            dialog.dismiss()
        }
        .setNegativeButton("Deny") { dialog, _ ->
            dialog.dismiss()
        }
        .setCancelable(false)
        .show()
}

//Alert Dialog
fun Context.alert(
    title: String? = null,
    message: String? = null,
    positiveButtonText: String,
    positiveButtonAction: () -> Unit = {},
    negativeButtonText: String? = null,
    negativeButtonAction: () -> Unit = {},
    neutralButtonText: String? = null,
    neutralButtonAction: () -> Unit = {},
) {
    val builder = AlertDialog.Builder(this)
    builder.setTitle(title)
    builder.setMessage(message)
    builder.setPositiveButton(positiveButtonText) { dialog, which ->
        positiveButtonAction()
    }
    if (negativeButtonText != null) {
        builder.setNegativeButton(negativeButtonText) { dialog, which ->
            negativeButtonAction()
        }
    }
    if (neutralButtonText != null) {
        builder.setNeutralButton(neutralButtonText) { dialog, which ->
            neutralButtonAction()
        }
    }
    val dialog = builder.create()
    dialog.show()
}

//RV
fun <T> RecyclerView.setRecyclerViewData(
    data: List<T>,
    itemLayoutRes: Int,
    bindItem: View.(T) -> Unit
) {
    adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): RecyclerView.ViewHolder {
            val itemView = LayoutInflater.from(parent.context)
                .inflate(itemLayoutRes, parent, false)
            return object : RecyclerView.ViewHolder(itemView) {}
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val item = data[position]
            holder.itemView.bindItem(item)
            holder.itemView.setOnClickListener {
                context.showToastShort("$item clicked")
            }
        }

        override fun getItemCount(): Int = data.size
    }
}

