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
import android.content.Context
import android.os.Build


class Monu_detailsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_monu_details)

        //imposto la foto come sfondo della imageview (questa deve essere variabile in base al monumento)
        image_scrolling_top.setImageDrawable(getDrawable(R.drawable.mura1))
        image_scrolling_top.imageAlpha=750

        //questa cosa è allucinante!
        toolbar.title=intent.extras.getString(Intent.EXTRA_TEXT) //estraggo il nome del monumento dall'intent
        toolbar_layout.setExpandedTitleColor(resources.getColor(R.color.colorPrimary))
        setSupportActionBar(toolbar)

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }

        //notifiche prova-------
        var builder= NotificationCompat.Builder(this,"tutte")
                .setSmallIcon(R.drawable.ic_info_black_24dp)
                .setContentTitle("Allarme")
                .setContentText("Devi partire, anzi no è una burla")
        var notification = builder.build()
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val pri = NotificationManager.IMPORTANCE_LOW // Priorità

        //Accidenti ad Oreo
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nch = NotificationChannel("tutte", "cazzo", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(nch)
        } else {
        }
        notificationManager?.notify(3,notification)
        //---------------------------------
    }
}
