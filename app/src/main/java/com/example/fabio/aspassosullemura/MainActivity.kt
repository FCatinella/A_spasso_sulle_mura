package com.example.fabio.aspassosullemura

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
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
import android.widget.Toast
import com.google.gson.Gson
import kotlinx.android.synthetic.main.info_layout.*


class MainActivity : AppCompatActivity(), YouTubePlayer.OnInitializedListener {
    // variabili globali
    private lateinit var viewlist:ArrayList<View>
    private var firsttime :Boolean = true
    private var justInstalled:Boolean = true
    private lateinit var lm :LocationManager
    private lateinit var interplacesList : ArrayList<InterPlaces>
    lateinit var editor : SharedPreferences.Editor



    //Listener della navigation bar
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

        //cosa succede quando instanzio una pagina del pageadapter
        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            container.addView(viewlist.get(position))
            //sennò NULL POINTER EXCEPTION, non posso "toccare" view contenuti in layout non ancora instanziati
            if(position==0){

                //listener bottone Inizia
                button2?.setOnClickListener{view ->

                    //controllo permessi
                    if (ContextCompat.checkSelfPermission(applicationContext,Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_DENIED) {
                        if(Build.VERSION.SDK_INT>=23){
                            ActivityCompat.requestPermissions(this@MainActivity,arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),0)
                        }
                    }
                    else {
                        if(!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) Toast.makeText(applicationContext,resources.getText(R.string.AttivaGPS), Toast.LENGTH_LONG).show()
                        else {
                            val intent = Intent(applicationContext, VisitActivity::class.java)
                            startActivity(intent)
                        }
                    }
                }
            }

            if(position ==1){
                infotextView13.setOnClickListener {
                    //apre il dialer se compone il numero de
                    val callIntent = Intent(Intent.ACTION_DIAL)
                    callIntent.data= Uri.parse(("tel:+390500987480"))
                    startActivity(callIntent)
                }
            }

            return viewlist.get(position)
        }
    }
    //-----------------------------------------------------------------------------------------------


    //PageChangeListener ( cosa succede quando cambiamo pagina nella home )
    private val pageChangeListener: ViewPager.OnPageChangeListener = object : ViewPager.OnPageChangeListener {

        //------------
        override fun onPageScrollStateChanged(state: Int) {}
        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
        //---------------

        override fun onPageSelected(position: Int) {
            when (position) {
                0 -> navigation.selectedItemId = R.id.navigation_home
                1 -> navigation.selectedItemId = R.id.navigation_info
            }
            // cambio il titolo in base a cosa è selezionato
            if(position==1){
                mytext?.text=resources.getText(R.string.InfoeOrari)
                supportActionBar?.title=resources.getText(R.string.InfoeOrari)
            }
            else {
                mytext?.text = resources.getString(R.string.app_name)
                supportActionBar?.title=resources.getString(R.string.app_name)
            }
        }

    }

    //BottomBar initializer
    private fun initBottomView(){
        //parte riguardante la bottombar
        //creo i due layout da aggiungere all view pager
        val infoview=layoutInflater.inflate(R.layout.info_layout,null)
        val homeview=layoutInflater.inflate(R.layout.home_layout,null)

        viewlist=ArrayList()
        viewlist.add(homeview)
        viewlist.add(infoview)
        viewpager.adapter=pagerAdapter // gli assegno l'adapter
        viewpager.addOnPageChangeListener(pageChangeListener)

        navigation.elevation=0F // azzero "l'altezza" della bottombar
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
    }



    // Parte playerYoutube----------------------------------------------------------------------------------------
    // Funzioni da implementare perchè il main implementa l'interfaccia YouTubePlayer.OnInitializedListener

    override fun onInitializationSuccess(p0: YouTubePlayer.Provider?, p1: YouTubePlayer?, p2: Boolean) {
        p1?.cueVideo("zgJ3mHPPxcI") //carica il video senza farlo partire
    }

    override fun onInitializationFailure(p0: YouTubePlayer.Provider?, p1: YouTubeInitializationResult?) {
        Log.w("YoutubePlayer",p1.toString())

    }
    //-------------------------------------------------------------------------------------------------------------



    // Controlla se siamo connessi ad internet--------------
     fun isConnected () : Boolean{
        val cm=getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager //ottengo il CONNECTIVITY SERVICE
        if(cm.activeNetworkInfo!= null && cm.activeNetworkInfo.isConnectedOrConnecting) return true //è connesso
        return false //non è connesso
    }
    //-------------------------------------------------------





    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportActionBar?.elevation= 0F // elimino l'ombra sotto l'action bar

        initBottomView()

        lm = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        //controllo se l'applicazione è stata appena installata
        val pref= getSharedPreferences("myprefs",Context.MODE_PRIVATE)
        editor = pref.edit()
        justInstalled=pref.getBoolean("AppenaInstallata",true)

        editor.putInt("Settato", 0)
        editor.apply()

        //Tutto quello che riguarda il primissimo avvio dopo l'installazione
        if(justInstalled) {
            editor.putBoolean("AppenaInstallata", false)
            editor.apply()

            //Riguardante Oreo+
            // ottengo il service delle notifiche
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            // Creo i canali delle notifiche (spero di trovare un modo per farlo solo una volta)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val nch = NotificationChannel("tutte", "Tutte", NotificationManager.IMPORTANCE_DEFAULT)
                val posChan = NotificationChannel("posizione", "Posizione", NotificationManager.IMPORTANCE_LOW)
                val audioChan = NotificationChannel("AudioGuida", "AudioGuida", NotificationManager.IMPORTANCE_LOW)
                notificationManager.createNotificationChannel(nch)
                notificationManager.createNotificationChannel(posChan)
                notificationManager.createNotificationChannel(audioChan)
            }

        }
        //popolo la lista dei monumenti
        inizializeList()

        //inizializzo il fragment usando la api key
        val frag = supportFragmentManager.findFragmentById(R.id.youtubeplayer) as YouTubePlayerSupportFragment
        frag.initialize(R.string.keyApiYoutube.toString(), this) // dove this è inteso per YouTubePlayer.OnInitializedListener

        //-------------------------------

       //se ho appena aperto l'applicazione e non sono connesso ad internet visualizzo uno Snackbar
        if(firsttime){
            firsttime = false
            if(!isConnected()){
                Snackbar.make(viewpager, "Connettiti ad Internet", Snackbar.LENGTH_LONG).show() //mostra avviso in caso non ci sia internet
            }
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater?.inflate(R.menu.home_menu,menu)
        return super.onCreateOptionsMenu(menu)
    }

    //gestisco il "menù" sulla actionbar
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



    fun inizializeList(){
        //inserisco tutti i punti d'interesse nella lista
        interplacesList = ArrayList()
        interplacesList.add(InterPlaces("Torre di Santa Maria",R.drawable.torresantamaria,resources.getString(R.string.TorSanMarDescFull),43.72442,10.3936933,R.raw.torresantamaria))
        interplacesList.add(InterPlaces("Cimitero Ebraico",R.drawable.cimiteroebraico,resources.getString(R.string.CimiEbraDesc),43.7240329,10.393226,R.raw.cimiteroebraico))
        interplacesList.add(InterPlaces("Battistero di San Giovanni",R.drawable.pisa_battistero,resources.getString(R.string.BattDesc),43.7232127,10.3940551,R.raw.battistero))
        interplacesList.add(InterPlaces("Camposanto Monumentale",R.drawable.camposanto,resources.getString(R.string.CamSantDesc),43.724005,10.3948948,R.raw.camposanto))
        interplacesList.add(InterPlaces("Cattedrale di Santa Maria Assunta",R.drawable.duomo_pisa_torre,resources.getString(R.string.DuomoDesc),43.7233676,10.39557566,R.raw.duomo))
        interplacesList.add(InterPlaces("Torre Pendente",R.drawable.torrependente,resources.getString(R.string.TorPendDesc),43.72309347,10.39668073,R.raw.torrependente))
        interplacesList.add(InterPlaces("Bagni di Nerone",R.drawable.bagnidinerone,resources.getString(R.string.BagnNeroneDesc),43.7222934,10.4019646,R.raw.bagninerone))
        interplacesList.add(InterPlaces("Chiesa e convento di San Torpè",R.drawable.chiesasantorpe,resources.getString(R.string.ChieSanTorpeDesc),43.72206459,10.4022598,R.raw.santorpe))
        interplacesList.add(InterPlaces("Chiesa di San Zeno",R.drawable.chiesasanzeno,resources.getString(R.string.ChieSanZenDesc),43.72303073,10.40757626,R.raw.sanzeno))
        interplacesList.add(InterPlaces("Polo Fibonacci",R.drawable.fibonacci,resources.getString(R.string.FiboDesc),43.72112263,10.40778232,R.raw.fibonacci))
        interplacesList.add(InterPlaces("Torre Piezometrica",R.drawable.torrepiezometrica,resources.getString(R.string.TorPiezoDescFull),43.72002347,10.40882788,R.raw.piezometrica))
        interplacesList.add(InterPlaces("Chiesa di San Francesco",R.drawable.chiesasanfrancesco,resources.getString(R.string.ChieSanFranDesc),43.71881031,10.40716524,R.raw.sanfrancesco))
        interplacesList.add(InterPlaces("Piazza delle Gondole",R.drawable.piazzagondole,resources.getString(R.string.PiazGondDescFull),43.7166266,10.40917109999998,R.raw.gondole))
        interplacesList.add(InterPlaces("Torre di Legno",R.drawable.torrelegno,resources.getString(R.string.TorLegnDescFull),43.7132137,10.410173,R.raw.torrelegno))

        val interPlacesJson = Gson().toJson(interplacesList)
        editor.putString("InterPlacesJson",interPlacesJson).apply()
    }
}
