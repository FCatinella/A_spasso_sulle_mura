package com.example.fabio.aspassosullemura

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import android.content.ComponentName
import com.example.fabio.aspassosullemura.AudioService.LocalBinder
import android.content.ServiceConnection
import android.support.v7.app.AppCompatActivity
import com.example.fabio.aspassosullemura.R.id.fab


//servizio che si occupa di visualizzare/ gestire tutta la parte relativa alle descrizioni audio
class AudioService : Service() {
    lateinit var audioNotification: Notification
    lateinit var song : MediaPlayer
    var songPrepared = false
    lateinit var callerActivity : Monu_detailsActivity
    lateinit var intentCpy: Intent
    lateinit var pendingIntent: PendingIntent


    // Binder da dare ai client
    private val mBinder = LocalBinder()



    inner class LocalBinder: Binder() {
         fun getService () : AudioService {
            return this@AudioService
        }
    }

    //ritono il Binder alla attività che ha chiamato questa funzione
    override fun onBind(p0: Intent?): IBinder {
        return mBinder
    }


    fun showNotification(){
        startForeground(5,audioNotification)
    }

    fun stopMusic(){
        if(song.isPlaying)
            song.stop()
    }


    fun prepareAndPlayMusic(audioId : Int){
        val songFd= resources.openRawResourceFd(audioId)
        song.setAudioStreamType(AudioManager.STREAM_MUSIC)
        //imposto la sorgente dell'audio da riprodurre
        song.setDataSource(songFd.fileDescriptor, songFd.startOffset, songFd.length)

        //quando è pronto
        song.setOnPreparedListener {
            songPrepared=true
            it.start() //avvia l'audio
            pendingIntent = PendingIntent.getActivity(applicationContext,10,intentCpy,PendingIntent.FLAG_CANCEL_CURRENT)

            audioNotification = NotificationCompat.Builder(applicationContext,"AudioGuida")
                    .setSmallIcon(R.drawable.ic_audiotrack_white_24dp)
                    .setContentTitle(callerActivity.titolo)
                    .setContentText(resources.getText(R.string.AudioDescr))
                    .setContentIntent(pendingIntent)
                    .build()
            showNotification()

            //easterEgg
            callerActivity.aaaaaahhhh=true
        }

        //quando l'audio è terminato
        song.setOnCompletionListener { it ->
            stopForeground(true)
            callerActivity.updateFabIcon()
        }

        song.prepareAsync()

    }


    override fun onCreate() {
        super.onCreate()
        song = MediaPlayer()
    }
}