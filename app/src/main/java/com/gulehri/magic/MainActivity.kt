package com.gulehri.magic

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.gulehri.magicutils.Utilities

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Utilities.Customization.customizeText()
    }
}