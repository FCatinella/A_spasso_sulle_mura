package com.example.fabio.aspassosullemura

import android.content.Context
import android.content.SharedPreferences
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.widget.ListView
import android.widget.ArrayAdapter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.inte_places_layout.*
import java.util.*
import kotlin.collections.ArrayList


class VisitActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager
    lateinit var pref : SharedPreferences
    private lateinit var interplacesList : ArrayList<InterPlaces>




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.inte_places_layout)

        supportActionBar?.title="Monumenti vicini"
        supportActionBar?.elevation=0F
        supportActionBar?.setDisplayHomeAsUpEnabled(true)


        var rv = findViewById<RecyclerView>(R.id.inter_places_recycler_view)

        rv.setHasFixedSize(true)
        viewManager =LinearLayoutManager(this)

        rv.layoutManager=viewManager

        pref= getSharedPreferences("myprefs", Context.MODE_PRIVATE)

        //trovo il tipo da passare a fromJson
        val interPlacesListType = object : TypeToken<List<InterPlaces>>(){}.type
        interplacesList = Gson().fromJson(pref.getString("InterPlacesJson",""),interPlacesListType)

        rv.adapter= InterPlacesAdapter(interplacesList,this)

        button3.setOnClickListener { view ->
            /*var a = interplacesList[0]
            var b = interplacesList[2]
            interplacesList[0]=b
            interplacesList[2]=a*/
            rv.adapter.notifyItemMoved(0, 2)
           // rv.adapter.notifyDataSetChanged()// trigghera onBindViewHolder che aggiorna la recycle view
        }

    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
