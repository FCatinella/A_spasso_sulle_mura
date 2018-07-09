package com.example.fabio.aspassosullemura

import android.Manifest
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.os.SystemClock
import android.support.v4.app.NotificationCompat
import android.support.v4.content.ContextCompat
import android.util.Log
import com.google.gson.Gson
import java.util.*

class AlarmService : Service(),LocationListener {

    lateinit var position : Location
    lateinit var contextCpy : Context
    lateinit var intentCpy : Intent
    lateinit var lm : LocationManager
    lateinit var notification2 :Notification
    lateinit var pref : SharedPreferences
    lateinit var editor : SharedPreferences.Editor
    lateinit var nm : NotificationManager
    lateinit var mapPintent : PendingIntent

    override fun onLocationChanged(p0: Location?) {
        updateLocation(p0!!)
        //devo passare la posizione al receiver
        Log.w("AlarmService","posizione cambiata")
        handler()
        stopForeground(true) //rimuovo le notifiche in foreground
        stopSelf() // termino il servizio
    }

    override fun onProviderDisabled(p0: String?) {
        //in caso di disattivazione del GSP mentre è attivo il servizio, invio una notifica e lo termino
        notification2 = NotificationCompat.Builder(contextCpy,"posizione")
            .setSmallIcon(R.drawable.ic_location_searching_white_24dp)
            .setContentTitle(resources.getText(R.string.AlarmServiceNot))
            .setContentText(resources.getText(R.string.MisonoDis))
            .build()
        nm.notify(3,notification2)
        lm.removeUpdates(this)
        stopForeground(true)
        //trigghero il listener delle preferenze per ripristinare il layout e quindi poter reimpostare un allarme
        editor.putInt("Settato",0)
        editor.apply()
        stopSelf()
    }

    override fun onProviderEnabled(p0: String?) {
    }

    override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {
    }


    override fun onCreate() {
        super.onCreate()
        val pref = getSharedPreferences("myprefs",Context.MODE_PRIVATE)
        val ingrSceltoJson = Gson().fromJson<Location>((pref.getString("Ingresso scelto","")),Location::class.java)
        val gmmIntentUri = Uri.parse("google.navigation:q="+ingrSceltoJson.latitude+","+ingrSceltoJson.longitude+"&mode=w")
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        mapPintent = PendingIntent.getActivity(this,5,mapIntent,0)
    }

    override fun onBind(intent: Intent): IBinder {
        return null!!
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intentCpy =intent!!
        contextCpy= applicationContext
        pref= contextCpy.getSharedPreferences("myprefs",Context.MODE_PRIVATE) as SharedPreferences
        editor = pref.edit()
        lm = contextCpy.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        nm= contextCpy.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager


        val locProv=LocationManager.GPS_PROVIDER
        Log.w("DEBUG","Provvider attivo - "+lm.isProviderEnabled(locProv).toString())
        if(!(lm.isProviderEnabled(locProv))){
            // se il GPS è stato disattivato invio una notifica e termino
            notification2 = NotificationCompat.Builder(contextCpy,"posizione")
                    .setSmallIcon(R.drawable.ic_location_searching_white_24dp)
                    .setContentTitle(resources.getText(R.string.AlarmServiceNot))
                    .setContentText(resources.getText(R.string.MisonoDis))
                    .build()
            nm.notify(3,notification2)
            stopForeground(true)
            editor.putInt("Settato",0)
            editor.apply()
            stopSelf()
        }
        //controllo i permessi
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_DENIED){
            notification2 = NotificationCompat.Builder(contextCpy,"posizione")
                    .setSmallIcon(R.drawable.ic_location_searching_white_24dp)
                    .setContentTitle(resources.getText(R.string.AlarmServiceNot))
                    .setContentText(resources.getText(R.string.MisonoDis))
                    .build()
            nm.notify(3,notification2)
            stopForeground(true)
            editor.putInt("Settato",0)
            editor.apply()
            stopSelf()
        }
        //inizio a richiedere aggiornamenti sulla posizione (1 solo in realtà -> guardare la onLocationChanged)
        lm.requestLocationUpdates(locProv,2000,0.toFloat(),this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //visualizzo la notifica sui dispositivi con Android 8+
            //per via delle restrizioni che Google ha inserito
            notification2 = NotificationCompat.Builder(contextCpy,"posizione")
                    .setSmallIcon(R.drawable.ic_location_searching_white_24dp)
                    .setContentTitle(resources.getText(R.string.AlarmServiceNot))
                    .setContentText(resources.getText(R.string.StocercandoPos))
                    .build()
            startForeground(2, notification2)
        }
        Log.w("AlarmService","partito")

        return  START_NOT_STICKY// Non ripartire in caso di uccisione brutale
    }

    fun updateLocation (newLoc : Location){
        position = Location(newLoc)
    }



    fun handler(){

        /*- guardo cosa ha selezionato l'utente come ingresso
       - imposta la differenza di tempo come timer (diviso 2)
       - scade il timer ->
            - se è più lontano di quanto manca, suona e avviso relativo.
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
                    .setSmallIcon(R.drawable.ic_info_white_24dp)
                    .setContentTitle(resources.getText(R.string.EOra))
                    .setContentText(contextCpy.resources.getText(R.string.Incammi))
                    .addAction(R.drawable.navigation_empty_icon,resources.getText(R.string.Indicazioni),mapPintent)
                    .build()
            nm.notify(3,notification2) //invio la notifica vera e propria
            lm.removeUpdates(this)
            stopForeground(true)

            //siccome la notifica è stata mandanta, posso reimpostare un altro allarme
            editor.putInt("Settato",0)
            editor.apply()

        }
       else {
            //reimposto l'allarme a metà del tempo
            Log.w("Timer", "scattato e reimpostato")
            val alarmmanager = contextCpy.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val pintent = PendingIntent.getService(contextCpy,1,intentCpy,0)
            //TEST   alarmmanager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime()+6000,pintent)
            alarmmanager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime()+delay/*-(distanceTime.toLong()))*//2,pintent)
        }
        lm.removeUpdates(this) //disattivo il listener della posizione
    }


    override fun onDestroy() {
        lm.removeUpdates(this)
        super.onDestroy()
    }
}
