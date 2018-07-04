package com.example.fabio.aspassosullemura

import android.location.Location

class InterPlaces (name:String, photoDrawAr :Int, descrAr : String, lat: Double, longi: Double) {
    private var name : String = name
    private var photoDraw : Int = photoDrawAr
    private  var descr : String  = descrAr
    private  var locat : Location
    init {
        locat = Location(name)
        locat.latitude=lat
        locat.longitude=longi
    }

    fun setNewName (name2 : String) : String {
        name = name2
        return name
    }

    fun getName () : String {
        return this.name
    }

    fun getPhotoDraw() : Int {
        return photoDraw
    }

    fun getDescr() : String {
        return descr
    }

    fun setNewPhotoDraw (photoDraw2 : Int){
        photoDraw = photoDraw2
    }

    fun addDesc(descrnew:String){
        descr=descrnew
    }

    fun addLocat(newLocat : Location){
        locat=Location(newLocat)
    }
}