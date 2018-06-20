package com.example.fabio.aspassosullemura

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.os.SystemClock

class NotificationService : Service() {

    override fun onCreate() {
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        var alarmmanager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        var intent2 =  Intent(applicationContext,Receiver::class.java)

        var pintent = PendingIntent.getBroadcast(this,1,intent2,0)
       // alarmmanager.set(AlarmManager.RTC_WAKEUP,calendario.timeInMillis,pintent) //funziona?

        //FUNZIONA
        alarmmanager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime()+5000,pintent)
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent): IBinder {
        var a = null as IBinder
        return a
    }


}
