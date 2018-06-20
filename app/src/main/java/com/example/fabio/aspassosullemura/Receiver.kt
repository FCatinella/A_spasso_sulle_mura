package com.example.fabio.aspassosullemura

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.support.v4.app.NotificationCompat
import android.support.v7.app.AppCompatActivity
import java.util.prefs.Preferences


class Receiver : BroadcastReceiver(){

    override fun onReceive(p0: Context?, p1: Intent?) {
        val notification2 = NotificationCompat.Builder(p0!!,"tutte")
                .setSmallIcon(R.drawable.ic_info_black_24dp)
                .setContentTitle("E' ora!")
                .setContentText("Incamminati verso le mura per non fare tardi.")
                .build()
        val nm= p0.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(3,notification2) //invio la notifica vera e propria
        var pref= p0.getSharedPreferences("myprefs",Context.MODE_PRIVATE) as SharedPreferences
        var editor = pref.edit()
        editor.putInt("Settato",0)
        editor.commit()
    }


}