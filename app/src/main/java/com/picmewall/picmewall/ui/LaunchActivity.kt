package com.picmewall.picmewall.ui

import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import com.picmewall.picmewall.AppGlobalValues
import com.picmewall.picmewall.MainActivity
import com.picmewall.picmewall.R
import com.picmewall.picmewall.utils.ImageUtils
import kotlinx.android.synthetic.main.activity_launch.*
import kotlinx.coroutines.*

class LaunchActivity : AppCompatActivity() {
    private lateinit var animation: Animation

    override fun onCreate(savedInstanceState: Bundle?) {
        updateStatusBarBackgroundColor("#E2372C")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launch)

        animation = AnimationUtils.loadAnimation(this, R.anim.logo_animation)
        animation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(p0: Animation?) {

            }

            override fun onAnimationEnd(p0: Animation?) {
                runBlocking {
                    launch {
                        deleteAllPreviousCreatedTiles()
                        delay(1200L)
                    }
                    val intent = Intent(this@LaunchActivity, MainActivity::class.java)
                    startActivity(intent)
                }
            }

            override fun onAnimationStart(p0: Animation?) {

            }
        })

        launch_screen_logo.startAnimation(animation)
    }

    suspend fun deleteAllPreviousCreatedTiles() {
        withContext(Dispatchers.IO) {
            ImageUtils.deleteFolderWithAllFilesFromExternalStorage(this@LaunchActivity,
                AppGlobalValues.TILES_FOLDER_NAME
            )
        }
    }

    fun updateStatusBarBackgroundColor(color: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            try {
                window.statusBarColor = Color.parseColor(color)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}