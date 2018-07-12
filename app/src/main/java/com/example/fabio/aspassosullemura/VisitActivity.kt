package com.example.fabio.aspassosullemura

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.*
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.v7.util.DiffUtil
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.widget.ListView
import android.widget.ArrayAdapter
import android.widget.ProgressBar
import android.widget.Toast
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.content_scrolling.*
import kotlinx.android.synthetic.main.inte_places_layout.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
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
    var initialized : Boolean = false
    var firstTime : Boolean = true
    lateinit var mCurrentPhotoPath : String




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.inte_places_layout)

        //sistemo l'actionbar ( titolo cambiato, pulsante back abilitato e 0 elevazione)
        supportActionBar?.title=resources.getText(R.string.MonumentiVicini)
        supportActionBar?.elevation=0F
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        //recupero il RecyclerView
        rv = findViewById(R.id.inter_places_recycler_view)
        rv.setHasFixedSize(true)
        rv.visibility=View.INVISIBLE
        viewManager =LinearLayoutManager(this)

        rv.layoutManager=viewManager

        pref= getSharedPreferences("myprefs", Context.MODE_PRIVATE)

        //trovo il tipo da passare a fromJson
        val interPlacesListType = object : TypeToken<List<InterPlaces>>(){}.type
        interplacesList = Gson().fromJson(pref.getString("InterPlacesJson",""),interPlacesListType)

        //pulsante per vedere subito i monumenti senza aspettare la posizione esatta
        showNowButton.setOnClickListener {
            //nascondo loader, messaggio e pulsante
            progress_loader.visibility= View.INVISIBLE
            textViewPosMess.visibility = View.INVISIBLE
            showNowButton.visibility=View.INVISIBLE

            rv.adapter= InterPlacesAdapter(interplacesList,this)
            initialized = true
            rv.visibility=View.VISIBLE
        }

        val crit = Criteria()
        crit.accuracy= Criteria.ACCURACY_FINE
        crit.powerRequirement= Criteria.POWER_MEDIUM
        lm = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locProv= lm.getBestProvider(crit,true) //location provvider
        Log.w("VisitActivity","provvider scelto: $locProv")

    }


    override fun onResume() {
        super.onResume()

        try {
            lm.requestLocationUpdates(locProv, 10000, 0.toFloat(), this) //attivo il listener
            val lastLoc = lm.getLastKnownLocation(locProv)
            if(lastLoc!=null) updateDistAll(lastLoc)
        }
        catch ( e : SecurityException ){
            finish()
        }
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


    //Funzione che aggiorna le distanze dei monumenti dalla posizione attuale
    private fun updateDistAll(currentPos : Location){
        for (i in interplacesList.indices){
            val place = interplacesList[i]
            place.updateDist(place.getLoc().distanceTo(currentPos))
            if(initialized){
                rv.adapter.notifyItemChanged(i)
            }
        }
        //clono la lista, la ordino e calcolo le differenze
        val oldList = interplacesList.clone() as List<InterPlaces>
        interplacesList.sort()
        val newList = interplacesList.clone() as List<InterPlaces>

       if(initialized){ //l'adapter è già stato collegato alla recyclerView
           //avviso l'adapter delle differenze
           val diffResult = DiffUtil.calculateDiff(MyDiffCallback(newList,oldList))
           diffResult.dispatchUpdatesTo(rv.adapter)
       }
        //aggiorno la lista vera e propria
        interplacesList.removeAll(oldList)
        interplacesList.addAll(newList)

        if(!initialized){ // se l'adapter non è stato ancora collegato alla view
            progress_loader.visibility= View.INVISIBLE
            textViewPosMess.visibility = View.INVISIBLE
            showNowButton.visibility=View.INVISIBLE
            rv.adapter= InterPlacesAdapter(interplacesList,this)
            initialized = true
            firstTime=false
            rv.visibility=View.VISIBLE
        }
        if(firstTime){
            firstTime = false
            rv.adapter.notifyDataSetChanged()
        }
    }


    override fun onLocationChanged(p0: Location?) {
        //aggiorno le posizioni quando cambia quella attuale
        updateDistAll(p0!!)
    }

    override fun onProviderDisabled(p0: String?) {
        finish()
    }

    override fun onProviderEnabled(p0: String?) {}

    override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {}

    override fun onDestroy() {
        super.onDestroy()
        //salvo la lista nelle preferenze condivise
        val editor = pref.edit()
        val interPlacesJson = Gson().toJson(interplacesList)
        editor.putString("InterPlacesJson",interPlacesJson).apply()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.w("onActivityResult","arrivato")

        if (requestCode==1 && resultCode!=0){
            //se è terminata l'attività chiamata dall'intent della camera
            val photoIntent = Intent()
            photoIntent.type="image/*"
            photoIntent.action=Intent.ACTION_SEND
            //recupero il path dove è salvata la foto
            val photoPath = pref.getString("mCurrentPhotoPath","")
            //prendo il logo dell'app
            val waterBitmap = BitmapFactory.decodeResource(resources,R.drawable.watermark)
            //e lo applico sulla foto
            val photoNew = addWaterMark(photoPath,waterBitmap)
            //salvo la foto
            val bitmapPath = MediaStore.Images.Media.insertImage(contentResolver, photoNew,"title", null)

            //invio l'intent di condivisione
            photoIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(bitmapPath))
            startActivity(Intent.createChooser(photoIntent, resources.getText(R.string.CondCon)))
        }
    }




   private fun addWaterMark(ImageSrcPath: String, watermarkBitmap: Bitmap): Bitmap? {
        val bitmapOptions = BitmapFactory.Options()
        //scala della bitmap
        bitmapOptions.inSampleSize = 1
        var imageSet = false

        while (!imageSet) {
            try {
                val photoBitmap = BitmapFactory.decodeFile(ImageSrcPath, bitmapOptions)

                //creo il canvas dove "disegnare"
                val w = photoBitmap.width
                val h = photoBitmap.height
                val result = Bitmap.createBitmap(w, h, Bitmap.Config.RGB_565)
                val canvas = Canvas(result)
                //disegno il logo
                canvas.drawBitmap(photoBitmap,0f,0f, null)

                // e il nome del monumento selezionato
                val paint = Paint()
                paint.color=Color.WHITE
                paint.textSize=100.toFloat()
                paint.typeface= Typeface.SERIF
                paint.textAlign=Paint.Align.LEFT
                paint.isAntiAlias=true
                val monuName = pref.getString("ShareButtonName","")
                canvas.drawBitmap(watermarkBitmap, 50f,( canvas.height-watermarkBitmap.height).toFloat(), null)
                canvas.drawText(monuName,480f,canvas.height.toFloat()-120,paint)
                imageSet = true
                return result

            } catch (E: OutOfMemoryError) {
                //dimezzo la scala
                bitmapOptions.inSampleSize *= 2
            }
        }
        return null
    }










}
