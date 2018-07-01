package com.example.fabio.aspassosullemura

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.os.SystemClock
import android.support.v4.app.NotificationCompat
import android.util.Log
import com.google.gson.Gson
import java.util.*

class AllarmService : Service(),LocationListener {

    lateinit var position : Location
    lateinit var contextCpy : Context
    lateinit var lm : LocationManager
    lateinit var notification2 :Notification

    override fun onLocationChanged(p0: Location?) {
        updateLocation(position)
        //devo passare la posizione al receiver
        Log.w("AllarmService","posizione cambiata")
        handler()
        //stopForeground(true); //true will remove notification
       // onDestroy()
    }

    override fun onProviderDisabled(p0: String?) {
    }

    override fun onProviderEnabled(p0: String?) {
    }

    override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {
    }

    override fun onBind(intent: Intent): IBinder {
        return null!!
    }

    override fun onCreate() {
        super.onCreate()

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        contextCpy= applicationContext
        lm = contextCpy.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        var crit = Criteria()
        crit.accuracy= Criteria.ACCURACY_FINE
        crit.powerRequirement= Criteria.POWER_MEDIUM
        var locProv = lm.getBestProvider(crit,true)
        lm.requestLocationUpdates(locProv,300000,0.toFloat(),this)
        updateLocation(lm.getLastKnownLocation(locProv))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notification2 = NotificationCompat.Builder(contextCpy!!,"tutte")
                    .setSmallIcon(R.drawable.ic_location_searching_white_24dp)
                    .setContentTitle("Servizio Posizione")
                    .setContentText("Controllerò la tua posizione ogni 5 minuti")
                    .build()
            startForeground(2, notification2);
        }
        Log.w("AllarmService","partito")

        return super.onStartCommand(intent, flags, startId)
    }

    fun updateLocation (newLoc : Location){
        position = Location(newLoc)
    }



    fun handler(){
        /*- guardo cosa ha selezionato l'utente come ingresso
       - imposta la differenza di tempo come timer
       - scade il timer ->
       - se è più lontano di quanto manca, suona e avviso relativo ( sei in ritardo ).
       - se è abbastanza vicino imposta la metà del tempo che manca fino a > 5 minuti
               - ripeti
       */

        //estraggo l'ingresso scelto (JSon)
        var pref= contextCpy.getSharedPreferences("myprefs",Context.MODE_PRIVATE) as SharedPreferences
        var locationGsonString = pref.getString("Ingresso scelto","")
        var chosenLocation = Gson().fromJson(locationGsonString, Location::class.java)

        //calcolo la differenza che c'è tra l'orario attuale e l'orario scelto
        var quantomancaGiorno = pref.getInt("chosenDateGiornodelMese",0) - (Calendar.getInstance().get(Calendar.DAY_OF_MONTH))
        var quantomancaOra= pref.getInt("chosenDateOra",0) - (Calendar.getInstance().get(Calendar.HOUR_OF_DAY))
        var quantomancaMinuto = pref.getInt("chosenDateMinuto",0) - (Calendar.getInstance().get(Calendar.MINUTE))
        //sommo e trasformo in millisecondi
        var delay= (quantomancaMinuto*60+quantomancaOra*60*60+quantomancaGiorno*24*60*60)*1000

        //distanza in tempo ( linea d'aria)
        var distanceTime = (chosenLocation.distanceTo(position))*1000

        if(delay-distanceTime<=300000){ //se la differenza è < di 5 minuti allora invia notifica
            //creo la notifica
            val notification2 = NotificationCompat.Builder(contextCpy!!,"tutte")
                    .setSmallIcon(R.drawable.ic_info_black_24dp)
                    .setContentTitle("E' ora!")
                    .setContentText("Incamminati verso le mura per non fare tardi.")
                    .build()
            val nm= contextCpy.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.notify(3,notification2) //invio la notifica vera e propria
            lm.removeUpdates(this)
            stopForeground(true)

            //siccome la notifica è stata mandanta, posso reimpostare un altro allarme
            var editor = pref.edit()
            editor.putInt("Settato",0)
            editor.commit()
            onDestroy()

        }
       /* else {
            //reimposto l'allarme + 5 minuti
            Log.w("Timer", "scattato e reimpostato")
            var intent = Intent(contextCpy,AllarmService::class.java)
            var alarmmanager = contextCpy.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            var pintent = PendingIntent.getService(contextCpy,1,intent,0)
            alarmmanager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime()+60000,pintent)
            //alarmmanager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime()+(delay-(distanceTime.toLong()))/2,pintent)
        }
        lm.removeUpdates(this) //disattivo il listener della posizione*/
    }

}
