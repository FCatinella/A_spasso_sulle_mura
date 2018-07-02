package com.example.fabio.aspassosullemura

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v4.app.NotificationCompat
import android.support.v4.content.ContextCompat
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity
import android.support.v7.appcompat.R.id.action_bar_title
import android.view.Menu
import android.view.MenuItem
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
    private var firsttime :Boolean = true
    private var justInstalled:Boolean = true



    //Listener della navigation bar ( lamda ) --------------------------------------------
    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_home -> {
                viewpager.currentItem=0 // cambia la "pagina"
                return@OnNavigationItemSelectedListener true
            }

            R.id.navigation_info -> {
                viewpager.currentItem=1 //cambia la "pagina"
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }
    //----------------------------------------------------


    //Page Adapter ----------------------------------------------------------
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
            //sennò NULL POINTER EXCEPTION, non posso "toccare" view contenuti in layout non ancora instanziati
            if(position==0){
                //listener bottoni (provvisori)
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
            // cambio il titolo in base a cosa è selezionato
            if(position==1){
                mytext?.text="Info e Orari"
                supportActionBar?.title="Info e Orari"
            }
            else {
                mytext?.text = resources.getString(R.string.app_name)
                supportActionBar?.title=resources.getString(R.string.app_name)
            }
        }

    }

    //BottomBar initializer ------------------------------------------
    private fun initview(){
        //parte riguardante la bottombar
        //creo i due layout da aggiungere all view pager
        var infoview=layoutInflater.inflate(R.layout.info_layout,null)
        var homeview=layoutInflater.inflate(R.layout.home_layout,null)

        viewlist=ArrayList() // era lazy
        viewlist.add(homeview)
        viewlist.add(infoview)
        viewpager.adapter=pagerAdapter // gli assegno l'adapter
        viewpager.addOnPageChangeListener(pageChangeListener)
        navigation.elevation=0F // azzero "l'altezza" della bottombar
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
    }
    //----------------------------------------------------------------


    //parte playerYoutube----------------------------------------------------------------------------------------
    // Funzioni da implementare perchè il main implementa l'interfaccia YouTubePlayer.OnInitializedListener

    override fun onInitializationSuccess(p0: YouTubePlayer.Provider?, p1: YouTubePlayer?, p2: Boolean) {
        p1?.cueVideo("zgJ3mHPPxcI") //carica il video senza farlo partire
    }

    override fun onInitializationFailure(p0: YouTubePlayer.Provider?, p1: YouTubeInitializationResult?) {
    }
    //-------------------------------------------------------------------------------------------------------------



    // Controllare se siamo connessi ad internet--------------
     fun isConnected () : Boolean{
        var cm=getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager //ottengo il CONNECTIVITY SERVICE
        if(cm.activeNetworkInfo!= null && cm.activeNetworkInfo.isConnectedOrConnecting) return true //è connesso
        return false //non è connesso
    }
    //-------------------------------------------------------





    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportActionBar?.elevation= 0F // elimino l'ombra sotto l'action bar ( la "schiaccio a terra" )


        initview()

        //controllo se l'applicazione è stata appena installata
        var pref= getSharedPreferences("myprefs",Context.MODE_PRIVATE)
        var editor = pref.edit()
        justInstalled=pref.getBoolean("AppenaInstallata",true)
        editor.putInt("Settato", 0)
        editor.commit()

        //splashscreen etc.
        if(justInstalled){
            editor.putBoolean("AppenaInstallata",false)
            editor.commit()
        }








        // cose da fare al primo avvio ---------------------------
        if(savedInstanceState?.getBoolean("Primo avvio")!=null){
            firsttime=savedInstanceState.getBoolean("Primo avvio") //estraggo la  variabile dal bundle ( se esiste )
        }
        if(firsttime){
            firsttime = false
            savedInstanceState?.putBoolean("Primo avvio",firsttime) // salvo la variabile nel bundle
            if(!isConnected()){
                Snackbar.make(viewpager, "Connettiti ad Internet", Snackbar.LENGTH_LONG).show() //mostra avviso in caso non ci sia internet
            }
        }
        //------------------------------------------------------

        //inizializzo il fragment usando la api key
        val frag = supportFragmentManager.findFragmentById(R.id.youtubeplayer) as YouTubePlayerSupportFragment
        frag.initialize("AIzaSyC6q0mq5it6hcS03_X1dThbl525KvNwXxI", this) // dove this è inteso per YouTubePlayer.OnInitializedListener
        //-------------------------------

        //Riguardante Oreo+
        // ottengo il service delle notifiche
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        // Creo i canali delle notifiche (spero di trovare un modo per farlo solo una volta)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nch = NotificationChannel("tutte", "Tutte", NotificationManager.IMPORTANCE_DEFAULT)
            val posChan = NotificationChannel("posizione", "Posizione", NotificationManager.IMPORTANCE_LOW)
            notificationManager.createNotificationChannel(nch)
            notificationManager.createNotificationChannel(posChan)
        }
        //---------------------------------

    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val mi = menuInflater?.inflate(R.menu.home_menu,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId) {
            R.id.alarm_home -> {
                //gestione permessi per la localizzazione
                if (ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_DENIED) {
                    if(Build.VERSION.SDK_INT>=23){
                        ActivityCompat.requestPermissions(this,arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),0)
                    }
               }
                else {
                    //permesso avuto
                    val intent = Intent(applicationContext, AlarmActivity::class.java)
                    startActivity(intent)
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

}
