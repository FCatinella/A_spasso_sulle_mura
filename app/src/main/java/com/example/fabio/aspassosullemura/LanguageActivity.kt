package com.example.fabio.aspassosullemura

import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.language_activity_layput.*
import java.util.*

class LanguageActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.language_activity_layput)
        var intent = Intent(this,MainActivity::class.java)
        supportActionBar?.elevation=0F
        supportActionBar?.hide()



        itaButton.setOnClickListener{
            setLocale("it")
            startActivity(intent)
            finish()
        }
        engButton.setOnClickListener{
            setLocale("en")
            startActivity(intent)
            finish()
        }
        frButton.setOnClickListener{
            setLocale("fr")
            startActivity(intent)
            finish()
        }
    }

    fun setLocale(lang : String){
        val myLocale = Locale(lang)
        val dm= resources.displayMetrics
        val conf = resources.configuration
        conf.locale = myLocale
        resources.updateConfiguration(conf,dm)
    }
}