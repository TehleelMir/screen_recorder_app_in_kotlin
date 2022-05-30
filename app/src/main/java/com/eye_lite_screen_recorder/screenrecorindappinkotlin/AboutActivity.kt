package com.eye_lite_screen_recorder.screenrecorindappinkotlin

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.Window
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class AboutActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
        supportActionBar!!.hide()
        val imageView = findViewById<ImageView>(R.id.avatar)
        imageView.setOnClickListener { Toast.makeText(this, "Hello there!", Toast.LENGTH_SHORT).show() }
        var textView = findViewById<TextView>(R.id.tv_reddit)
        textView.movementMethod = LinkMovementMethod.getInstance()
        textView = findViewById(R.id.tv_insta)
        textView.movementMethod = LinkMovementMethod.getInstance()
        textView = findViewById(R.id.tv_twitter)
        textView.movementMethod = LinkMovementMethod.getInstance()
        textView = findViewById(R.id.icon_tv_link)
        textView.movementMethod = LinkMovementMethod.getInstance()
    }
}