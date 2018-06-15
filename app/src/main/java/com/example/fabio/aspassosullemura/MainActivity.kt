package com.example.fabio.aspassosullemura

import android.content.Context
import android.content.Intent
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity
import android.support.v7.appcompat.R.id.action_bar_title
import android.view.View
import android.view.ViewGroup
import com.google.android.youtube.player.YouTubeInitializationResult
import com.google.android.youtube.player.YouTubePlayer
import kotlinx.android.synthetic.main.abs_layout.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.home_layout.*
import java.util.ArrayList
import com.google.android.youtube.player.YouTubePlayerSupportFragment
import android.widget.TextView






class MainActivity : AppCompatActivity(), YouTubePlayer.OnInitializedListener {
    // variabili globali
    private lateinit var viewlist:ArrayList<View>


    //Listener della navigation bar ( ritorna una funzione )--------------------------------------------
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
    //----------------------------------------------------


    //Pageadapter----------------------------------------------------------
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
                button?.setOnClickListener{ view ->
                    val intent = Intent(applicationContext,Monu_detailsActivity::class.java)
                    intent.putExtra(Intent.EXTRA_TEXT,"Bottone1")
                    startActivity(intent)
                }

                button2?.setOnClickListener{view ->
                    val intent = Intent(applicationContext,Monu_detailsActivity::class.java)
                    intent.putExtra(Intent.EXTRA_TEXT,"Bottone2")
                    startActivity(intent)
                }
                homeConstraintLayout.backgroundTintMode=PorterDuff.Mode.DARKEN
            }

            return viewlist.get(position)
        }
    }
    //-----------------------------------------------------------------------------------------------

    //PageChangeListener ( cosa succede quando cambiamo pagina nella home )
    private val pageChangeListener: ViewPager.OnPageChangeListener = object : ViewPager.OnPageChangeListener {
        override fun onPageScrollStateChanged(state: Int) {}
        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
        override fun onPageSelected(position: Int) {
            when (position) {
                0 -> navigation.selectedItemId = R.id.navigation_home
                1 -> navigation.selectedItemId = R.id.navigation_info
            }
            if(position==1){
                mytext?.text="Info e Orari"
                supportActionBar?.title="Info e Orari"
                //supportActionBar?.setBackgroundDrawable(ColorDrawable(resources.getColor(R.color.colorPrimary)))


            }
            else {
                mytext?.text = resources.getString(R.string.app_name)
                supportActionBar?.title=resources.getString(R.string.app_name)
                //supportActionBar?.setBackgroundDrawable(ColorDrawable(resources.getColor(R.color.colorAccent)))
            }
        }

    }

    //BottomBar initializer------------------------------------------
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
        navigation.elevation=0F
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
    }
    //----------------------------------------------------------------

    //parte playerYoutube ----------------------------------------------------------------------------------------
    override fun onInitializationSuccess(p0: YouTubePlayer.Provider?, p1: YouTubePlayer?, p2: Boolean) {
        p1?.cueVideo("zgJ3mHPPxcI")
    }

    override fun onInitializationFailure(p0: YouTubePlayer.Provider?, p1: YouTubeInitializationResult?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
    //-------------------------------------------------------------------------------------------------------------



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //centro il titolo-----------
        //supportActionBar?.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
        //supportActionBar?.setCustomView(R.layout.abs_layout) //uso un layout ad-hoc
        supportActionBar?.elevation= 0F // elimino l'ombra sotto l'action bar ( la "schiaccio a terra" )
        //------------------------

        initview()
        val frag = supportFragmentManager.findFragmentById(R.id.youtubeplayer) as YouTubePlayerSupportFragment
        frag.initialize("AIzaSyC6q0mq5it6hcS03_X1dThbl525KvNwXxI", this)


    }

}
