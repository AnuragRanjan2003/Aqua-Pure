package com.example.waterquality

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import java.lang.Thread.sleep

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Thread {
            try {
                sleep(3000)
            } catch (e: Exception) {
            } finally {
                startActivity(Intent(this, AskActivity::class.java))
                finish()
            }

        }.start()

    }
}