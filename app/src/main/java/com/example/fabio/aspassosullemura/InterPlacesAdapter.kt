package com.example.fabio.aspassosullemura

import android.content.ContentProvider
import android.content.Context
import android.content.Intent
import android.support.v4.content.ContextCompat.startActivity
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide

class InterPlacesAdapter (lista : ArrayList<InterPlaces>,contextAr: Context): RecyclerView.Adapter<InterPlacesAdapter.InterPlacesViewHolder>() {

    class InterPlacesViewHolder internal constructor(itemView:View) : RecyclerView.ViewHolder(itemView) {
        internal var cd: CardView
        internal var placeName: TextView
        internal var placePhoto: ImageView
        internal var shareButt: ImageView

        init {

            cd = itemView.findViewById(R.id.card_view)
            placeName = itemView.findViewById(R.id.cv_place_name)
            placePhoto = itemView.findViewById(R.id.cv_place_photo)
            shareButt = itemView.findViewById(R.id.cv_share_imageview)
        }
    }

    internal var interplaces : List <InterPlaces>
    internal var contextCpy : Context
    init {
        interplaces = lista
        contextCpy = contextAr

    }

    override fun getItemCount(): Int {
        return interplaces.size
    }


    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InterPlacesAdapter.InterPlacesViewHolder {
        var cardView = LayoutInflater.from(parent.context).inflate(R.layout.places_card_list,parent,false)
        return InterPlacesViewHolder(cardView)
    }


    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: InterPlacesViewHolder, position: Int) {
        val luogo = interplaces[position]
        holder.placePhoto.setImageDrawable(null)
        holder.placeName.text = luogo.getName()

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
            intent.putExtra("Posizione",luogo.getLoc())
            intent.putExtra("AudioId",luogo.getAudioId())
            contextCpy.startActivity(intent)
        }
    }


}