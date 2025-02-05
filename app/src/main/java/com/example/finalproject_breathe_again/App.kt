package com.example.finalproject_breathe_again

import android.app.Application
import com.example.finalproject_breathe_again.ui.craving.story.ImageLoader


class App : Application() {
    override fun onCreate() {
        super.onCreate()
        ImageLoader.init(this)
    }
}