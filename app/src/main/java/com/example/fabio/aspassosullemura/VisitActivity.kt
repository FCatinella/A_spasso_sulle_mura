package com.example.fabio.aspassosullemura

import android.content.Context
import android.content.SharedPreferences
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.util.DiffUtil
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.widget.ListView
import android.widget.ArrayAdapter
import android.widget.Toast
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.content_scrolling.*
import kotlinx.android.synthetic.main.inte_places_layout.*
import java.util.*
import kotlin.collections.ArrayList

//Activity dei monumenti nelle vicinanze

class VisitActivity : AppCompatActivity(),LocationListener {
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager
    lateinit var pref : SharedPreferences
    private lateinit var interplacesList : ArrayList<InterPlaces>
    lateinit var currentLocation : Location
    lateinit var rv : RecyclerView
    lateinit var locProv : String
    lateinit var lm : LocationManager
    var locationupdated : Boolean = false




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.inte_places_layout)


        //sistemo l'actionbar ( titolo cambiato, pulsante back abilitato e 0 elevazione)
        supportActionBar?.title="Monumenti vicini"
        supportActionBar?.elevation=0F
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        //recupero il RecyclerView
        rv = findViewById(R.id.inter_places_recycler_view)
        rv.setHasFixedSize(true)
        viewManager =LinearLayoutManager(this)

        rv.layoutManager=viewManager

        pref= getSharedPreferences("myprefs", Context.MODE_PRIVATE)

        //trovo il tipo da passare a fromJson
        val interPlacesListType = object : TypeToken<List<InterPlaces>>(){}.type
        interplacesList = Gson().fromJson(pref.getString("InterPlacesJson",""),interPlacesListType)

        rv.adapter= InterPlacesAdapter(interplacesList,this)

        for(i in interplacesList.indices) {
            interplacesList[i].setRealIndex(i)
        }



        var crit = Criteria()
        crit.accuracy= Criteria.ACCURACY_FINE
        crit.powerRequirement= Criteria.POWER_MEDIUM
        lm = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locProv= lm.getBestProvider(crit,true) //location provvider
        Log.w("VisitActivity","provvider scelto: $locProv")



    }


    override fun onResume() {
        super.onResume()
        lm.requestLocationUpdates(locProv,30000,10.toFloat(),this) //attivo il listener

    }

    override fun onPause() {
        super.onPause()
        lm.removeUpdates(this)
    }

    override fun onSupportNavigateUp(): Boolean {
        lm.removeUpdates(this)
        finish()
        return true
    }


    fun updateDistAll(currentPos : Location){
        for (place in interplacesList){
            place.updateDist(place.getLoc().distanceTo(currentPos))
        }
        val oldList = interplacesList.clone() as List<InterPlaces>
        interplacesList.sort()
        val newList = interplacesList.clone() as List<InterPlaces>

        var diffResult = DiffUtil.calculateDiff(MyDiffCallback(newList,oldList))
        diffResult.dispatchUpdatesTo(rv.adapter)
        interplacesList.clear()
        interplacesList.addAll(newList)

    }


    override fun onLocationChanged(p0: Location?) {
        updateDistAll(p0!!)
        Toast.makeText(this,"Posizone aggiornata",Toast.LENGTH_LONG).show()
    }

    override fun onProviderDisabled(p0: String?) {
        finish()
    }

    override fun onProviderEnabled(p0: String?) {
    }

    override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {

    }

    override fun onDestroy() {
        super.onDestroy()
        val editor = pref.edit()
        var interPlacesJson = Gson().toJson(interplacesList)
        editor.putString("InterPlacesJson",interPlacesJson).commit()
    }
}
