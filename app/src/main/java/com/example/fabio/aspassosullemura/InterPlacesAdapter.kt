package com.example.fabio.aspassosullemura

import android.Manifest
import android.app.Activity
import android.content.ContentProvider
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat.startActivity
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import android.support.v4.app.ActivityCompat.startActivityForResult
import android.support.v4.content.ContextCompat
import android.support.v4.content.FileProvider
import android.util.Log
import android.widget.*
import java.io.File
import java.nio.file.Files
import java.text.SimpleDateFormat
import java.util.*


class InterPlacesAdapter (lista : ArrayList<InterPlaces>,contextAr: Activity): RecyclerView.Adapter<InterPlacesAdapter.InterPlacesViewHolder>() {

    class InterPlacesViewHolder internal constructor(itemView:View) : RecyclerView.ViewHolder(itemView) {
        internal var cd: CardView
        internal var placeName: TextView
        internal var placePhoto: ImageView
        internal var shareButt: ImageView
        internal var distanceText : TextView


        init {

            cd = itemView.findViewById(R.id.card_view)
            placeName = itemView.findViewById(R.id.cv_place_name)
            placePhoto = itemView.findViewById(R.id.cv_place_photo)
            shareButt = itemView.findViewById(R.id.cv_share_imageview)
            distanceText = itemView.findViewById(R.id.textViewDistanceTo)

        }
    }

    internal var interplaces : List <InterPlaces>
    internal var contextCpy : Activity
    lateinit var mCurrentPhotoPath : String
    lateinit var pref : SharedPreferences
    lateinit var editor : SharedPreferences.Editor

    init {
        interplaces = lista
        contextCpy = contextAr

    }

    override fun getItemCount(): Int {
        return interplaces.size
    }


    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InterPlacesAdapter.InterPlacesViewHolder {
        pref = contextCpy.getSharedPreferences("myprefs", Context.MODE_PRIVATE)
        editor = pref.edit()
        var cardView = LayoutInflater.from(parent.context).inflate(R.layout.places_card_list,parent,false)
        return InterPlacesViewHolder(cardView)
    }


    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: InterPlacesViewHolder, position: Int) {
        val luogo = interplaces[position]
        holder.placePhoto.setImageDrawable(null)
        holder.placeName.text = luogo.getName()
        val distanzatesto = luogo.getDistance().toInt().toString()
        holder.distanceText.text = "- "+ distanzatesto+"m"

        //Glide rende il caricamento delle immagini molto più efficente
        Glide.with(contextCpy)
                .load(luogo.getPhotoDraw())
                .into(holder.placePhoto)

        holder.placePhoto.adjustViewBounds=true
        holder.placePhoto.scaleType=ImageView.ScaleType.CENTER_CROP

        holder.placePhoto.setOnClickListener { view ->
            val intent = Intent(contextCpy,Monu_detailsActivity::class.java)

            intent.putExtra(Intent.EXTRA_TEXT,luogo.getName())
            intent.putExtra("Descrizione",luogo.getDescr())
            intent.putExtra("Immagine",luogo.getPhotoDraw())
            intent.putExtra("Posizione",luogo.getLoc())
            intent.putExtra("AudioId",luogo.getAudioId())
            contextCpy.startActivity(intent)
        }

        //tasto condividi
        holder.shareButt.setOnClickListener { view ->

            if (ContextCompat.checkSelfPermission(contextCpy, Manifest.permission.WRITE_EXTERNAL_STORAGE)== PackageManager.PERMISSION_DENIED) {
                if(Build.VERSION.SDK_INT>=23){
                    ActivityCompat.requestPermissions(contextCpy,arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),0)
                }
            }
            else {

                val takePictureIntent = Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)
                editor.putString("ShareButtonName", luogo.getName()).apply()
                var photofile = createImageFile()
                // controllare le eccezioni
                val photoUri = FileProvider.getUriForFile(contextCpy, "com.example.fabio.fileprovider", photofile)
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                Log.w("PATH", mCurrentPhotoPath)
                contextCpy.startActivityForResult(takePictureIntent, 1)
            }
        }


        holder.shareButt.setOnLongClickListener { view ->
            Toast.makeText(contextCpy,contextCpy.resources.getText(R.string.ScatteCond),Toast.LENGTH_SHORT).show()
            return@setOnLongClickListener true
        }
    }





    //funzione per creare il file dove andrà la foto
    //@Throws(IOException::class)
    private fun createImageFile() : File {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "JPEG_"+timestamp+"_"
        val storageDir= contextCpy.getExternalFilesDir (Environment.DIRECTORY_PICTURES)
        val image = File.createTempFile(imageFileName,".jpg",storageDir)
        mCurrentPhotoPath = image.absolutePath
        editor.putString("mCurrentPhotoPath",mCurrentPhotoPath).apply()
        return image
    }


}