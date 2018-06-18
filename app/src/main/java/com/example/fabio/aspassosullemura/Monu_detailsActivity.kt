package com.example.fabio.aspassosullemura

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.NotificationCompat
import android.support.v7.app.AppCompatActivity
import android.view.WindowManager
import android.widget.ImageView
import kotlinx.android.synthetic.main.activity_monu_details.*
import android.support.v4.app.NotificationManagerCompat
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.TimePickerDialog
import android.content.Context
import android.os.Build
import android.widget.TimePicker
import kotlinx.android.synthetic.main.timepicker_layout.*


class Monu_detailsActivity : AppCompatActivity() {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_monu_details)

        /*imposto la foto come sfondo della imageview (questa deve essere variabile in base al monumento)

          userò le informazioni passate nell'intent per scegliere l'immagine */
        image_scrolling_top.setImageDrawable(getDrawable(R.drawable.mura1))
        image_scrolling_top.imageAlpha=750

        //Imposto le informazioni in base al monumento scelto
        toolbar.title=intent.extras.getString(Intent.EXTRA_TEXT) //estraggo il nome del monumento dall'intent
        toolbar_layout.setExpandedTitleColor(resources.getColor(R.color.colorPrimary))
        setSupportActionBar(toolbar)

        //listener del fab
        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }

        // ottengo il service delle notifiche
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        //Riguardante Oreo+
        // Creo il canale delle notifiche (spero di trovare un modo per farlo solo una volta)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nch = NotificationChannel("tutte", "Tutte", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(nch)
        }
        //---------------------------------
    }
}
