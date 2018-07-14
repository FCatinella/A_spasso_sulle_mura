package com.example.fabio.aspassosullemura

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_monu_details.*
import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.location.Location
import android.media.MediaPlayer
import android.net.Uri
import android.os.IBinder
import android.util.Log
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.content_scrolling.*



class Monu_detailsActivity : AppCompatActivity(), OnMapReadyCallback{
    lateinit var titolo : String
    lateinit var mService : AudioService
    var mBound : Boolean = false
    lateinit var ee : MediaPlayer
    var aaaaaahhhh : Boolean = false

    //gestisco le riprese della mappa
    override fun onMapReady(p0: GoogleMap?) {
        //recupero le coordinate dall'intent
        val location = this.intent.getParcelableExtra("Posizione") as Location
        val latlng = LatLng(location.latitude,location.longitude)

        val cameraPosition = CameraPosition.builder()
                .target(latlng) // dove deve inquadrare
                .zoom(17f) // il livello di zoom
                .tilt(50f) // l'inclinazione della mapppa (per la visualizzazione 3D)
                .build()

        try{
            p0?.isMyLocationEnabled=true
        }
        catch (e : SecurityException){
            finish()
        }
        p0?.isBuildingsEnabled=true
        //aggiungo il marker sulla mappa
        p0?.addMarker(MarkerOptions().position(latlng))?.title=titolo

        //quando l'utente preme sul marker faccio una ricerca su Google Maps
        p0?.setOnMarkerClickListener{
            //"Pisa" aggiunto per essere il più precisi possibile
            val uri = Uri.parse("geo:${latlng.latitude},${latlng.longitude}?q=$titolo,Pisa")
            val mapIntent = Intent(Intent.ACTION_VIEW,uri)
            startActivity(mapIntent)
            true
        }

        //animo la camera
        p0?.moveCamera(CameraUpdateFactory.newLatLng(latlng))
        p0?.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_monu_details)

        setSupportActionBar(toolbar)
        supportActionBar?.elevation=0F
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        tv_scrolling.text=intent.extras.getString("Descrizione")

        // imposto la foto come sfondo della imageview (questa deve essere variabile in base al monumento)
        // userò le informazioni passate nell'intent per scegliere l'immagine

        image_scrolling_top.setImageResource(intent.extras.getInt("Immagine"))
        image_scrolling_top.imageAlpha=750

        //Imposto le informazioni in base al monumento scelto
        titolo = intent.extras.getString(Intent.EXTRA_TEXT) //estraggo il nome del monumento dall'intent
        toolbar.title= titolo
        toolbar_layout.setExpandedTitleColor(resources.getColor(R.color.colorPrimary))
        setSupportActionBar(toolbar)

        //creo l'intent per il servizio audio
        val audioIntent = Intent(applicationContext,AudioService::class.java)
        audioIntent.putExtra("Titolo",titolo)
        //avvio il servizio e la comunicazione con esso
        bindService(audioIntent, mConnection, Context.BIND_AUTO_CREATE)

        //listener del fab
        fab.setOnClickListener {
            //se il servizio è bindato
            if(mBound && !aaaaaahhhh) {
                //se la traccia è pronta
                if(mService.songPrepared){
                    if(mService.song.isPlaying){
                        fab.setImageDrawable(resources.getDrawable(R.drawable.ic_play_arrow_white_24dp))
                        mService.song.pause()
                    }
                    else {
                         mService.song.start()
                        fab.setImageDrawable(resources.getDrawable(R.drawable.ic_pause_white_24dp))
                        mService.showNotification()
                    }
                }
                else {
                    mService.prepareAndPlayMusic(intent.extras.getInt("AudioId",0))
                    fab.setImageDrawable(resources.getDrawable(R.drawable.ic_pause_white_24dp))
                }

            }

        }

        fab.setOnLongClickListener {
            //AAAAAAAAAHHHHH
            if(titolo=="Polo Fibonacci"){
                aaaaaahhhh = true
                ee = MediaPlayer.create(this,R.raw.aaaahhh)
                ee.setOnCompletionListener {
                    aaaaaahhhh=false
                }
                if(mBound) {
                        if(mService.song.isPlaying){
                            mService.song.pause()
                            ee.start()
                            fab.setImageDrawable(resources.getDrawable(R.drawable.ic_play_arrow_white_24dp))
                            image_scrolling_top.setImageResource(R.drawable.aaaaahhhh)
                        }
                    else {
                            ee.start()
                            image_scrolling_top.setImageResource(R.drawable.aaaaahhhh)
                        }
                }
            }
            true
        }


        //WorkAround per la mappa
        map_overlay.setOnTouchListener { _, _ ->
            //uso una imageview invisibile per interagire con la mappa
            // disabilito la ricezione dei tocchi sulla scrollview in modo che solo la mappa acquisisca i tocchi
            mainScrollView.requestDisallowInterceptTouchEvent(true)
            false
        }

        //recupero la mappa
        val mapFragment = supportFragmentManager.findFragmentById(R.id.monumap) as SupportMapFragment
        mapFragment.getMapAsync(this) //-> chiamerà onMapReady

    }



    override fun onDestroy() {
        super.onDestroy()
        if(mBound) mService.stopMusic()
        if(aaaaaahhhh) ee.stop()
        unbindService(mConnection)
    }



    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    fun updateFabIcon(){
        Log.w("AUDIO","Audio Terminato")
        fab.setImageDrawable(resources.getDrawable(R.drawable.ic_play_arrow_white_24dp))
    }




    private val mConnection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            // Recupero la connessione con il servizio avviato
            val binder = service as AudioService.LocalBinder
            mService = binder.getService()
            mBound = true
            mService.callerActivity=this@Monu_detailsActivity
            mService.intentCpy=intent
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            mBound = false
        }
    }







}