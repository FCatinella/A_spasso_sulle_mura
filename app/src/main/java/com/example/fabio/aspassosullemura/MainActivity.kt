package com.example.fabio.aspassosullemura

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.abs_layout.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.home_layout.*
import java.util.ArrayList


class MainActivity : AppCompatActivity() {

    private lateinit var viewlist:ArrayList<View>

    //Listener della navigation bar ( ritorna una funzione )
    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_home -> {
                viewpager.currentItem=0

                return@OnNavigationItemSelectedListener true
            }

            R.id.navigation_info -> {
                viewpager.currentItem=1
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }


    // questa roba va nell'activity delle info sul monumento-----------
/*
    private var show = false

    private fun showComponents(){
        show=true
        val constraintSet = ConstraintSet()
        constraintSet.clone(this, R.layout.monu_info_det)

        val transition = ChangeBounds()
        //transition.interpolator = OvershootInterpolator(1.0f)

        transition.duration = 500

        TransitionManager.beginDelayedTransition(monu_info_id, transition) //ci vuole l'id del viewgroup
        constraintSet.applyTo(monu_info_id) //idem
    }

    private fun hideComponents(){
        show=false
        val constraintSet = ConstraintSet()
        constraintSet.clone(this, R.layout.monu_info)

        val transition = ChangeBounds()
        //transition.interpolator = OvershootInterpolator(1.0f)

        transition.duration = 500

        TransitionManager.beginDelayedTransition(monu_info_id, transition) //ci vuole l'id del viewgroup
        constraintSet.applyTo(monu_info_id) //idem
    }
*/
    //--------------------------------------------------------------------



    //adapter
    private val pagerAdapter = object : PagerAdapter() {
        override fun getCount(): Int {
            return viewlist.size
        }

        override fun isViewFromObject(view: View, `object`: Any): Boolean {
            return view === `object`
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            container.removeView(viewlist.get(position))
        }

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            container.addView(viewlist.get(position))
            //sennò NULLPOINTER EXCEPTION (questa devo segnarmela)
            if(position==0){
                button?.setOnClickListener({ view ->
                    val intent = Intent(applicationContext,Monu_detailsActivity::class.java)
                    intent.putExtra(Intent.EXTRA_TEXT,"Bottone1")
                    startActivity(intent)
                })

                button2?.setOnClickListener({view ->
                    val intent = Intent(applicationContext,Monu_detailsActivity::class.java)
                    intent.putExtra(Intent.EXTRA_TEXT,"Bottone2")
                    startActivity(intent)
                })
            }

            return viewlist.get(position)
        }
    }


    private val pageChangeListener: ViewPager.OnPageChangeListener = object : ViewPager.OnPageChangeListener {

        override fun onPageScrollStateChanged(state: Int) {

        }

        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

        }


        override fun onPageSelected(position: Int) {
            when (position) {
                0 -> navigation.selectedItemId = R.id.navigation_home
                1 -> navigation.selectedItemId = R.id.navigation_info
            }
            if(position==1){
                mytext.text="Info e Orari"
            }
            else mytext.text=resources.getString(R.string.app_name)
        }

    }







    private fun initview(){
        //parte riguardante la bottombar
        var infoview=layoutInflater.inflate(R.layout.info_layout,null)
        var homeview=layoutInflater.inflate(R.layout.home_layout,null)
        //var homeview
        viewlist=ArrayList()
        viewlist.add(homeview)
        viewlist.add(infoview)
        viewpager.adapter=pagerAdapter
        viewpager.addOnPageChangeListener(pageChangeListener)
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)


    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //centro il titolo-----------
        supportActionBar?.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
        supportActionBar?.setCustomView(R.layout.abs_layout) //uso un layout ad-hoc
        supportActionBar?.elevation= 0F // elimino l'ombra sotto l'action bar ( la "schiaccio a terra" )
        //------------------------

        initview()


        //anche questa parte va nell'activity delle info sul monumento ------
       /* backgroundImage.setOnClickListener{
            if(!show)
                showComponents()
            else
                hideComponents()
        }
        //---------------------- */

    }

}
