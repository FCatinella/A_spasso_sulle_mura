package com.example.fabio.aspassosullemura

import android.os.Bundle
import android.support.constraint.ConstraintSet
import android.support.design.widget.BottomNavigationView
import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity
import android.transition.ChangeBounds
import android.transition.TransitionManager
import android.view.animation.AnticipateOvershootInterpolator
import android.view.animation.OvershootInterpolator
import com.example.fabio.aspassosullemura.R.id.monu_info_id
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.monu_info.*
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

    // questa roba va nell'activity delle info sul monumento-----------

    private var show = false

    private fun showComponents(){
        show=true
        val constraintSet = ConstraintSet()
        constraintSet.clone(this, R.layout.monu_info_det)

        val transition = ChangeBounds()
        transition.interpolator = OvershootInterpolator(1.0f)
                // AnticipateOvershootInterpolator(1.0f)
        transition.duration = 1200

        TransitionManager.beginDelayedTransition(monu_info_id, transition) //ci vuole l'id del viewgroup
        constraintSet.applyTo(monu_info_id) //idem
    }

    private fun hideComponents(){
        show=false
        val constraintSet = ConstraintSet()
        constraintSet.clone(this, R.layout.monu_info)

        val transition = ChangeBounds()
        transition.interpolator = OvershootInterpolator(1.0f)
        // AnticipateOvershootInterpolator(1.0f)
        transition.duration = 1200

        TransitionManager.beginDelayedTransition(monu_info_id, transition) //ci vuole l'id del viewgroup
        constraintSet.applyTo(monu_info_id) //idem
    }

    //--------------------------------------------------------------------


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_main)
        setContentView(R.layout.monu_info)
        backgroundImage.setOnClickListener{
            if(!show)
                showComponents()
            else
                hideComponents()
        }

        //centro il titolo
        /*supportActionBar?.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
        supportActionBar?.setCustomView(R.layout.abs_layout) //uso un layout ad-hoc
        supportActionBar?.elevation= 0F // elimino l'ombra sotto l'action bar ( la "schiaccio a terra" )
        */

        //navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
    }
}
