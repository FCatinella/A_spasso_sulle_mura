package com.example.fabio.aspassosullemura

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.SystemClock
import android.support.v4.app.NotificationCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.google.gson.Gson
import java.util.*
import java.util.prefs.Preferences


class Receiver : BroadcastReceiver(),LocationListener{
    lateinit var position : Location
    lateinit var contextCpy : Context
    lateinit var lm : LocationManager



    override fun onReceive(p0: Context?, p1: Intent?) {
        /*- guardo cosa ha selezionato l'utente come ingresso
        - imposta la differenza di tempo come timer
        - scade il timer ->
        - se è più lontano di quanto manca, suona e avviso relativo ( sei in ritardo ). Tempo massimo 1 ora
        - se è abbastanza vicino imposta la metà del tempo che manca fino a > 10 minuti
                - ripeti
        */


        contextCpy=p0!!
        lm = p0.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        var crit = Criteria()
        crit.accuracy= Criteria.ACCURACY_FINE
        crit.powerRequirement= Criteria.POWER_MEDIUM
        var locProv = lm.getBestProvider(crit,true)
        lm.requestLocationUpdates(locProv,2000,10.toFloat(),this)
        updateLocation(lm.getLastKnownLocation(locProv))




    }



    //Funzioni riguardanti l'interfaccia LocationListener
    fun updateLocation (newLoc : Location){
        position = Location(newLoc)
    }


    override fun onLocationChanged(p0: Location?) {
        updateLocation(p0!!)

        Log.w("POSITION","posizione cambiata"+p0.latitude.toString())
        //estraggo l'ingresso scelto
        var pref= contextCpy.getSharedPreferences("myprefs",Context.MODE_PRIVATE) as SharedPreferences
        var locationGsonString = pref.getString("Ingresso scelto","")
        var chosenLocation = Gson().fromJson(locationGsonString, Location::class.java)

        //facciamo due conti
        var quantomancaGiorno = pref.getInt("chosenDateGiornodelMese",0) - (Calendar.getInstance().get(Calendar.DAY_OF_MONTH))
        var quantomancaOra= pref.getInt("chosenDateOra",0) - (Calendar.getInstance().get(Calendar.HOUR_OF_DAY))
        var quantomancaMinuto = pref.getInt("chosenDateMinuto",0) - (Calendar.getInstance().get(Calendar.MINUTE))



        var delay= (quantomancaMinuto*60+quantomancaOra*60*60+quantomancaGiorno*24*60*60)*1000

        var distanceTime = (chosenLocation.distanceTo(position))*1000

        if(delay-distanceTime<=300000){
            //suona
            val notification2 = NotificationCompat.Builder(contextCpy!!,"tutte")
                    .setSmallIcon(R.drawable.ic_info_black_24dp)
                    .setContentTitle("E' ora!")
                    .setContentText("Incamminati verso le mura per non fare tardi.")
                    .build()
            val nm= contextCpy.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.notify(3,notification2) //invio la notifica vera e propria

            var editor = pref.edit()
            editor.putInt("Settato",0)
            editor.commit()
        }
        else {
            Log.w("Timer", "scattato e reimpostato")
            var intent = Intent(contextCpy,Receiver::class.java)
            var alarmmanager = contextCpy.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            var pintent = PendingIntent.getBroadcast(contextCpy,1,intent,0)
            alarmmanager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime()+(delay-(distanceTime.toLong()))/2,pintent)
        }
        lm.removeUpdates(this)



    }
    override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {}

    override fun onProviderDisabled(p0: String?) {}

    override fun onProviderEnabled(p0: String?) {}
    //------------------------------------



}