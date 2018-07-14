package com.example.fabio.aspassosullemura

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.language_activity_layout.*
import java.util.*

class LanguageActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.language_activity_layout)
        val intent = Intent(this,MainActivity::class.java)
        supportActionBar?.elevation=0F
        supportActionBar?.hide()


        /*
        - imposto la lingua
        - avvio la mainActivity
        - termino
         */

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

    //imposto la lingua
    fun setLocale(lang : String){
        val myLocale = Locale(lang)
        val dm= resources.displayMetrics
        val conf = resources.configuration
        conf.locale = myLocale
        //aggiorno la configurazione con quella nuova ( che è uguale a quella vecchia tranne per la lingua )
        resources.updateConfiguration(conf,dm)
    }
}