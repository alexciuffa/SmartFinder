package com.example.camera2_v04

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnOpenCamera.setOnClickListener{
            openCameraActivity()
        }
    }

    private fun openCameraActivity() {
        val intent = Intent(this, Camera2Activity::class.java)
        startActivity(intent)
    }
}