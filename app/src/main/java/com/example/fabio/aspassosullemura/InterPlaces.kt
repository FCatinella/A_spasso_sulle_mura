package com.example.fabio.aspassosullemura

import android.location.Location

class InterPlaces (name:String, photoDrawAr :Int, descrAr : String, lat: Double, longi: Double, audioRawId : Int): Comparable<InterPlaces> {
    private var name : String = name
    private var photoDraw : Int = photoDrawAr
    private  var descr : String  = descrAr
    private  var locat : Location
    private var distance : Float
    private var realIndex : Int = -1
    private var audioId : Int = audioRawId

    init {
        locat = Location(name)
        locat.latitude=lat
        locat.longitude=longi
        distance= 0.0F
    }

    fun setNewName (name2 : String) : String {
        name = name2
        return name
    }

    fun getName () : String {
        return this.name
    }

    fun getRealIndex() : Int{
        return realIndex
    }

    fun setRealIndex(new : Int){
        realIndex = new
    }

    fun getPhotoDraw() : Int {
        return photoDraw
    }

    fun getDescr() : String {
        return descr
    }

    fun getLoc() : Location{
        return locat
    }

    fun updateDist(new: Float){
        distance = new
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

    fun getAudioId() : Int{
        return audioId
    }

    override fun compareTo(other: InterPlaces): Int {
        if (this.distance<=other.distance) return -1
        return 1
    }
}