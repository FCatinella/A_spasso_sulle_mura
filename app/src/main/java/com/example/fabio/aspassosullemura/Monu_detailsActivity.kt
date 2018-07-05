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
import android.app.TimePickerDialog
import android.content.Context
import android.location.Location
import android.os.Build
import android.view.MotionEvent
import android.widget.TimePicker
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.content_scrolling.*
import kotlinx.android.synthetic.main.timepicker_layout.*


class Monu_detailsActivity : AppCompatActivity(), OnMapReadyCallback{
    lateinit var titolo : String

    override fun onMapReady(p0: GoogleMap?) {
        var location = intent.getParcelableExtra("Posizione") as Location
        var latlng = LatLng(location.latitude,location.longitude)

        val cameraPosition = CameraPosition.builder()
                .target(latlng)
                .zoom(17f)
                .tilt(50f)
                .build()
        p0?.addMarker(MarkerOptions().position(latlng))?.title=titolo
        p0?.moveCamera(CameraUpdateFactory.newLatLng(latlng))
        p0?.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_monu_details)
        supportActionBar?.elevation=0F
        supportActionBar?.setDisplayHomeAsUpEnabled(true)


        tv_scrolling.text=intent.extras.getString("Descrizione")



        /*imposto la foto come sfondo della imageview (questa deve essere variabile in base al monumento)

          userò le informazioni passate nell'intent per scegliere l'immagine */
        image_scrolling_top.setImageResource(intent.extras.getInt("Immagine"))
        image_scrolling_top.imageAlpha=750

        //Imposto le informazioni in base al monumento scelto
        titolo = intent.extras.getString(Intent.EXTRA_TEXT) //estraggo il nome del monumento dall'intent
        toolbar.title= titolo
        toolbar_layout.setExpandedTitleColor(resources.getColor(R.color.colorPrimary))
        setSupportActionBar(toolbar)

        //listener del fab
        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }


        map_overlay.setOnTouchListener { v, event ->
            val action = event.action
            //uso una imageview invisibile per interagire con la mappa
            mainScrollView.requestDisallowInterceptTouchEvent(true) // disabilito la ricezione dei tocchi sulla scrollview
            false
        }

        val mapFragment = supportFragmentManager.findFragmentById(R.id.monumap) as SupportMapFragment
        mapFragment.getMapAsync(this)






    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}