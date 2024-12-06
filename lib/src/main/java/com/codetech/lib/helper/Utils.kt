package com.codetech.lib.helper

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition

object Utils {

    fun isEmptyStr(str: String?): Boolean {
        return str.isNullOrEmpty() || str == "" || str.isBlank()
    }

    fun hasPermission(context: Context, per: String): Boolean {
        return ContextCompat.checkSelfPermission(context, per) == PackageManager.PERMISSION_GRANTED
    }

    fun loadBitmapFromUrl(context: Context, url: String?,callBack:(Bitmap?)->Unit){
         try {
             Glide.with(context)
                 .asBitmap()
                 .load(url)
                 .into(object : CustomTarget<Bitmap>() {
                     override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                         callBack(resource)
                     }

                     override fun onLoadCleared(placeholder: Drawable?) {

                     }
                 })
        } catch (e: Exception) {
            e.printStackTrace()
             callBack(null)
        }
    }

}