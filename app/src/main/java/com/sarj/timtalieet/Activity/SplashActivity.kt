package com.sarj.timtalieet.Activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AnimationUtils
import com.sarj.timtalieet.R
import com.sarj.timtalieet.databinding.ActivitySplashBinding

class SplashActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Logo animasyonunu uygula
        val logoAnimation = AnimationUtils.loadAnimation(this, R.anim.logo_fade_in_out)
        binding.arka.startAnimation(logoAnimation)

        // Araç animasyonunu uygula
        val carAnimation = AnimationUtils.loadAnimation(this, R.anim.slide_in_right)
        binding.cingan.startAnimation(carAnimation)

        // Animasyonların süresini al (her ikisi de aynı süre içinde tamamlanacak şekilde ayarlanmış varsayalım)
        val animationDuration = 1000L

        // Ana aktiviteye geçiş için bir intent oluştur ve belirtilen süre sonunda başlat
        Handler().postDelayed({
            val intent = Intent(this@SplashActivity, MainActivity::class.java)
            startActivity(intent)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        }, animationDuration)
    }
}