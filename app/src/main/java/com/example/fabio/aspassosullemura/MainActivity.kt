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
import android.location.LocationManager
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
import android.util.Log
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
import android.widget.Toast
import com.google.gson.Gson


class MainActivity : AppCompatActivity(), YouTubePlayer.OnInitializedListener {
    // variabili globali
    private lateinit var viewlist:ArrayList<View>
    private var firsttime :Boolean = true
    private var justInstalled:Boolean = true
    private lateinit var lm :LocationManager

    private lateinit var interplacesList : ArrayList<InterPlaces>



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

                //listener bottone Guida
                button2?.setOnClickListener{view ->

                    //controllo permessi
                    if (ContextCompat.checkSelfPermission(applicationContext,Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_DENIED) {
                        if(Build.VERSION.SDK_INT>=23){
                            ActivityCompat.requestPermissions(this@MainActivity,arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),0)
                        }
                    }
                    else {
                        if(!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) Toast.makeText(applicationContext,"Attiva il GPS (Alta Precisione o Solo Dispositivo) per usare questa funzionalità", Toast.LENGTH_LONG).show()
                        else {
                            val intent = Intent(applicationContext, VisitActivity::class.java)
                            startActivity(intent)
                        }
                    }
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
        Log.w("YoutubePlayer",p1.toString())

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

        lm = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        //controllo se l'applicazione è stata appena installata
        var pref= getSharedPreferences("myprefs",Context.MODE_PRIVATE)
        var editor = pref.edit()
        justInstalled=pref.getBoolean("AppenaInstallata",true)

        //justInstalled=true  //DEBUG

        editor.putInt("Settato", 0)
        editor.commit()

        //splashscreen etc.
        //Tutto quello che riguarda il primissimo avvio dopo l'installazione
        if(justInstalled){
            editor.putBoolean("AppenaInstallata",false)
            editor.commit()


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


            //inserisco tutti i punti d'interesse nella lista
            interplacesList = ArrayList()
            interplacesList.add(InterPlaces("Torre Santa Maria",R.drawable.torresantamaria,resources.getString(R.string.TorSanMarDescFull),43.72442,10.3936933))
            interplacesList.add(InterPlaces("Cimitero Ebraico",R.drawable.cimiteroebraico,resources.getString(R.string.CimiEbraDesc),43.7240329,10.393226))
            interplacesList.add(InterPlaces("Battistero di San Giovanni",R.drawable.pisa_battistero,resources.getString(R.string.BattDesc),43.7232127,10.3940551))
            interplacesList.add(InterPlaces("Camposanto Monumentale",R.drawable.camposanto,resources.getString(R.string.CamSantDesc),43.724005,10.3948948))
            interplacesList.add(InterPlaces("Cattedrale di Santa Maria Assunta",R.drawable.duomo_pisa_torre,resources.getString(R.string.DuomoDesc),43.7233676,10.39557566))
            interplacesList.add(InterPlaces("Torre Pendente",R.drawable.torrependente,resources.getString(R.string.TorPendDesc),43.72309347,10.39668073))
            interplacesList.add(InterPlaces("Bagni di Nerone",R.drawable.bagnidinerone,resources.getString(R.string.BagnNeroneDesc),43.7222934,10.4019646))
            interplacesList.add(InterPlaces("Chiesa e convento di San Torpè",R.drawable.chiesasantorpe,resources.getString(R.string.ChieSanTorpeDesc),43.72206459,10.4022598))
            interplacesList.add(InterPlaces("Chiesa di San Zeno",R.drawable.chiesasanzeno,resources.getString(R.string.ChieSanZenDesc),43.72303073,10.40757626))
            interplacesList.add(InterPlaces("Polo Fibonacci",R.drawable.fibonacci,resources.getString(R.string.FiboDesc),43.72112263,10.40778232))
            interplacesList.add(InterPlaces("Torre Piezometrica",R.drawable.torrepiezometrica,resources.getString(R.string.TorPiezoDescFull),43.72002347,10.40882788))
            interplacesList.add(InterPlaces("Chiesa di San Francesco",R.drawable.chiesasanfrancesco,resources.getString(R.string.ChieSanFranDesc),43.71881031,10.40716524))
            interplacesList.add(InterPlaces("Piazza delle Gondole",R.drawable.piazzagondole,resources.getString(R.string.PiazGondDescFull),43.7166266,10.40917109999998))
            interplacesList.add(InterPlaces("Torre di Legno",R.drawable.torresantamaria,resources.getString(R.string.TorLegnDescFull),43.7132137,10.410173))
            var interPlacesJson = Gson().toJson(interplacesList)
            editor.putString("InterPlacesJson",interPlacesJson).commit()

        }

        //inizializzo il fragment usando la api key
        val frag = supportFragmentManager.findFragmentById(R.id.youtubeplayer) as YouTubePlayerSupportFragment
        frag.initialize(R.string.keyApiYoutube.toString(), this) // dove this è inteso per YouTubePlayer.OnInitializedListener

        //-------------------------------







        // cose da fare una volta ---------------------------
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
