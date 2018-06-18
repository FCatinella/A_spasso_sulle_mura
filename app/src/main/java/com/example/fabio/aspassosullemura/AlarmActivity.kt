package com.example.fabio.aspassosullemura

import android.app.ActionBar
import android.app.NotificationManager
import android.app.TimePickerDialog
import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.NotificationCompat
import android.widget.TimePicker
import kotlinx.android.synthetic.main.activity_alarm.*

class AlarmActivity : AppCompatActivity() {


    //Time picker Listener -------------------------------
    val timepickerdialoglistener = TimePickerDialog.OnTimeSetListener { timePicker: TimePicker, i: Int, i1: Int ->

        var ora ="$i"
        var minuti = "$i1"
        if(i<=10) ora="0$i"
        if (i1<10) minuti="0$i1"

        val notification2 = NotificationCompat.Builder(this,"tutte")
                .setSmallIcon(R.drawable.ic_info_black_24dp)
                .setContentTitle("Allarme")
                .setContentText("Devi partire alle ore $ora:$minuti")
                .build()
        val nm= getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(3,notification2) //invio la notifica vera e propria
    }
    //--------------------------------------------


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alarm)
        addfab?.setOnClickListener{ view ->
            //appare dialog per scegliere l'orario
            TimePickerDialog(this,timepickerdialoglistener,8,0,true).show()
        }
        supportActionBar?.title="Allarmi"
        supportActionBar?.elevation=0F
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

    }



    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
