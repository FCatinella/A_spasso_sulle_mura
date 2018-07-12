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




    // Crea le nuove view
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InterPlacesAdapter.InterPlacesViewHolder {
        //copia il contesto per usarlo dopo
        pref = contextCpy.getSharedPreferences("myprefs", Context.MODE_PRIVATE)
        editor = pref.edit()
        //ogni cardview avrà questo layout
        val cardView = LayoutInflater.from(parent.context).inflate(R.layout.places_card_list,parent,false)
        return InterPlacesViewHolder(cardView)
    }


    // Rimpiazza il contenuto di una view
    override fun onBindViewHolder(holder: InterPlacesViewHolder, position: Int) {
        val luogo = interplaces[position]
        holder.placePhoto.setImageDrawable(null)
        holder.placeName.text = luogo.getName()
        val distanzatesto = luogo.getDistance().toInt().toString()
        holder.distanceText.text = "$distanzatesto m"

        //Glide rende il caricamento delle immagini molto più efficente
        // https://bumptech.github.io/glide/
        Glide.with(contextCpy)
                .load(luogo.getPhotoDraw())
                .into(holder.placePhoto)

        holder.placePhoto.adjustViewBounds=true
        holder.placePhoto.scaleType=ImageView.ScaleType.CENTER_CROP

        holder.placePhoto.setOnClickListener {
            //apre i dettagli del luogo
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

            //controllo i permessi di archiviazione
            if (ContextCompat.checkSelfPermission(contextCpy, Manifest.permission.WRITE_EXTERNAL_STORAGE)== PackageManager.PERMISSION_DENIED) {
                if(Build.VERSION.SDK_INT>=23){
                    ActivityCompat.requestPermissions(contextCpy,arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),0)
                }
            }
            else {
                //scatto una foto
                val takePictureIntent = Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)
                editor.putString("ShareButtonName", luogo.getName()).apply()
                //creo un file che conterrà la foto
                val photofile = createImageFile()

                //recupero l'uri da usare per accedere al file e lo metto nell'intent
                val photoUri = FileProvider.getUriForFile(contextCpy, "com.example.fabio.fileprovider", photofile)
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                Log.w("PATH", mCurrentPhotoPath)

                //invio l'intent (prenderò il risultato nell'activity che usa l'adapter)
                contextCpy.startActivityForResult(takePictureIntent, 1)
            }
        }

        //visualizza un toast se l'icona "foto" viene tenuta premuta
        holder.shareButt.setOnLongClickListener { view ->
            Toast.makeText(contextCpy,contextCpy.resources.getText(R.string.ScatteCond),Toast.LENGTH_SHORT).show()
            return@setOnLongClickListener true
        }
    }





    //funzione per creare il file dove andrà la foto
    private fun createImageFile() : File {
        //creo un nome con la data di creazione nel nome
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "JPEG_"+timestamp+"_"
        val storageDir= contextCpy.getExternalFilesDir (Environment.DIRECTORY_PICTURES)
        //creo il file temporaneo
        val image = File.createTempFile(imageFileName,".jpg",storageDir)
        //salvo il path nelle preferenze ( mi servirà per l'intent di condivisione )
        mCurrentPhotoPath = image.absolutePath
        editor.putString("mCurrentPhotoPath",mCurrentPhotoPath).apply()
        return image
    }


}