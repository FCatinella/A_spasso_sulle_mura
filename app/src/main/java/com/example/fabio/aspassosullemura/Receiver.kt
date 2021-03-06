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


    //Quando ricevo l'allarme
    override fun onReceive(p0: Context?, p1: Intent?) {

        contextCpy=p0!! //copio il contesto per usarlo dopo

        //recupero il location Manager
        lm = p0.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val crit = Criteria()
        crit.accuracy= Criteria.ACCURACY_FINE
        crit.powerRequirement= Criteria.POWER_MEDIUM
        val locProv = lm.getBestProvider(crit,true)
        //attivo gli aggiornamenti delle posizioni
        try {
        lm.requestLocationUpdates(locProv,2000,10.toFloat(),this)
        updateLocation(lm.getLastKnownLocation(locProv))
        }
        catch (e : SecurityException){
        }
    }



    //Funzioni riguardanti l'interfaccia LocationListener
    fun updateLocation (newLoc : Location){
        position = Location(newLoc)
    }

    //appena ricevo una posizione più precisa
    override fun onLocationChanged(p0: Location?) {
        updateLocation(p0!!)
        Log.w("POSITION","posizione cambiata"+p0.latitude.toString())

        handler()

    }
    override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {}

    override fun onProviderDisabled(p0: String?) {}

    override fun onProviderEnabled(p0: String?) {}



    fun handler(){

        /*- guardo cosa ha selezionato l'utente come ingresso
       - imposta la differenza di tempo come timer
       - scade il timer ->
       - se è più lontano di quanto manca, suona e avviso relativo ( sei in ritardo ).
       - se è abbastanza vicino imposta la metà del tempo che manca fino a > 5 minuti
               - ripeti
       */

        //estraggo l'ingresso scelto (JSon)
        val pref= contextCpy.getSharedPreferences("myprefs",Context.MODE_PRIVATE) as SharedPreferences
        val locationGsonString = pref.getString("Ingresso scelto","")
        val chosenLocation = Gson().fromJson(locationGsonString, Location::class.java)

        //calcolo la differenza che c'è tra l'orario attuale e l'orario scelto
        val quantomancaGiorno = pref.getInt("chosenDateGiornodelMese",0) - (Calendar.getInstance().get(Calendar.DAY_OF_MONTH))
        val quantomancaOra= pref.getInt("chosenDateOra",0) - (Calendar.getInstance().get(Calendar.HOUR_OF_DAY))
        val quantomancaMinuto = pref.getInt("chosenDateMinuto",0) - (Calendar.getInstance().get(Calendar.MINUTE))
        //sommo e trasformo in millisecondi
        val delay= (quantomancaMinuto*60+quantomancaOra*60*60+quantomancaGiorno*24*60*60)*1000

        //distanza in tempo ( linea d'aria)
        val distanceTime = (chosenLocation.distanceTo(position))*1000

        if(delay-distanceTime<=300000){ //se la differenza è < di 5 minuti allora invia notifica
            //creo la notifica
            val notification2 = NotificationCompat.Builder(contextCpy,"tutte")
                    .setSmallIcon(R.drawable.ic_info_black_24dp)
                    .setContentTitle(contextCpy.getText(R.string.EOra))
                    .setContentText(contextCpy.getText(R.string.Incammi))
                    .build()
            val nm= contextCpy.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.notify(3,notification2) //invio la notifica vera e propria

            //siccome la notifica è stata mandanta, posso reimpostare un altro allarme
            val editor = pref.edit()
            editor.putInt("Settato",0)
            editor.apply()
        }
        else {
            //reimposto l'allarme a metà del tempo
            Log.w("Timer", "scattato e reimpostato")
            val intent = Intent(contextCpy,Receiver::class.java)
            val alarmmanager = contextCpy.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val pintent = PendingIntent.getBroadcast(contextCpy,1,intent,0)
            alarmmanager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime()+(delay-(distanceTime.toLong()))/2,pintent)
        }
        lm.removeUpdates(this) //disattivo il listener della posizione
    }




}