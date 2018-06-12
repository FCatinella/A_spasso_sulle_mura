package com.example.fabio.aspassosullemura

import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.math.absoluteValue

class MainActivity : AppCompatActivity() {

    //Listener della navigation bar ( ritorna una funzione )
    /*private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_home -> {

                return@OnNavigationItemSelectedListener true
            }

            R.id.navigation_info -> {

                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }
*/
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_main)
        setContentView(R.layout.monu_info_det)

        //centro il titolo
        /*supportActionBar?.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
        supportActionBar?.setCustomView(R.layout.abs_layout) //uso un layout ad-hoc
        supportActionBar?.elevation= 0F // elimino l'ombra sotto l'action bar ( la "schiaccio a terra" )
        */

        //navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
    }
}
