package com.example.finalproject_breathe_again.ui.craving.story

import android.content.Context
import android.net.Uri
import androidx.appcompat.widget.AppCompatImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.finalproject_breathe_again.R
import java.lang.ref.WeakReference

class ImageLoader private constructor(context: Context) {

    companion object {

        @Volatile
        private var instance: ImageLoader? = null

        fun getInstance(): ImageLoader {
            return instance
                ?: throw IllegalStateException("ImageLoader must be initialized by calling init(context) before use")
        }

        fun init(context: Context): ImageLoader {
            return instance ?: synchronized(this) {
                instance ?: ImageLoader(context).also { instance = it }
            }
        }
    }

    private val contextRef = WeakReference(context)

    fun loadImage(
        source: Any, // יכול להיות URL או ID של משאב מקומי
        imageView: AppCompatImageView,
        placeholder: Int = R.drawable.ic_launcher_background
    ) {
        contextRef.get()?.let { context ->
            Glide.with(context)
                .load(source) // Glide מזהה אוטומטית בין URL לבין משאב מקומי
                .centerCrop()
                .placeholder(placeholder)
                .into(imageView)
        }
    }

}
