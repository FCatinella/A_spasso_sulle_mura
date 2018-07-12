package com.example.fabio.aspassosullemura

import android.Manifest
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.PorterDuff
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.view.View
import kotlinx.android.synthetic.main.activity_alarm.*
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.MenuItem
import android.widget.*
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_alarm_set.*
import java.util.*

class AlarmActivity : AppCompatActivity(),LocationListener {
    //variabili
    var setted : Int = 0  //indica se l'allarme è stato settato oppure no
    lateinit var view : View
    lateinit var position : Location
    lateinit var locProv : String
    lateinit var allarmIntent : Intent
    lateinit var lm : LocationManager
    lateinit var pref : SharedPreferences
    var chosenIngress : Location = Location("Ingresso")
    var chosenDateAnno :Int = 2018
    var chosenDateMese: Int = 1
    var chosenDateGiornodelMese: Int = 1

    //Listener Date Picker
    private val datepickerdialoglistener = DatePickerDialog.OnDateSetListener{ datePicker: DatePicker, i: Int, i1: Int, i2: Int ->

        // assegno alle variabili i parametri impostati dall'utente
        chosenDateAnno=i
        chosenDateMese=i1+1 //I mesi sono contati da 0 (boh)
        chosenDateGiornodelMese=i2


        //visualizzo uno snackbar ed esco se il mese o l'anno non sono quello corrente
        if(i!=Calendar.getInstance().get(Calendar.YEAR) || i1!=Calendar.getInstance().get(Calendar.MONTH)){
            val addFab = findViewById<FloatingActionButton>(R.id.addfab)
            Snackbar.make(addFab, resources.getText(R.string.meseCorr), Snackbar.LENGTH_LONG).show()
            return@OnDateSetListener
        }

        // chiamo il selettore di orario
        TimePickerDialog(this, timepickerdialoglistener, Calendar.getInstance().get(Calendar.HOUR_OF_DAY), Calendar.getInstance().get(Calendar.MINUTE),true)
                .show()
    }




    //Cosa succede alla modifica di un valore nelle SharedPreferences
    val msharedPreferenceChangeListener = SharedPreferences.OnSharedPreferenceChangeListener{ sharedPreferences: SharedPreferences, s: String ->
        if(s.equals("Settato")) setSwitch() //Switcho il layout se il parametro "Settato" è stato cambiato nell preferenze
    }



    //Time picker Listener -------------------------------
    val timepickerdialoglistener = TimePickerDialog.OnTimeSetListener { timePicker: TimePicker, i: Int, i1: Int ->

        //Prendo l'ora da visualizzare e la trasformo in String
        var ora ="$i"
        var minuti = "$i1"
        if(i<10) ora="0$i"
        if (i1<10) minuti="0$i1"

        //Cambio "Settato" in modo che cambi il layout
        val editor = pref.edit()
        editor.putInt("Settato",1) //va committato

        //calcolo la differenza che c'è tra l'orario attuale e l'orario scelto
        val quantomancaGiorno = chosenDateGiornodelMese - (Calendar.getInstance().get(Calendar.DAY_OF_MONTH))
        val quantomancaOra= i - (Calendar.getInstance().get(Calendar.HOUR_OF_DAY))
        val quantomancaMinuto = i1 - (Calendar.getInstance().get(Calendar.MINUTE))
        //sommo e trasformo in millisecondi
        val delay= (quantomancaMinuto*60+quantomancaOra*60*60+quantomancaGiorno*24*60*60)*1000

        Log.w("Delay","$delay")
        //Visualizzo uno snackbar in caso di ora o data sbagliata ed esco
        if(delay<0) {
            val addFab = findViewById<FloatingActionButton>(R.id.addfab)
            Snackbar.make(addFab, resources.getText(R.string.viaggiTempo), Snackbar.LENGTH_LONG).show()
            return@OnTimeSetListener
        }


        //aggiorno i valori nelle preferenze. Mi servono nella setSwitch()
        editor.putInt("chosenDateAnno",chosenDateAnno)
                .putInt("chosenDateMese",chosenDateMese)
                .putInt("chosenDateGiornodelMese",chosenDateGiornodelMese)
                .putInt("chosenDateOra",i)
                .putInt("chosenDateMinuto",i1)
                .putString("chosenDateOraS",ora)
                .putString("chosenDateMinutoS",minuti)
                .apply()

        allarmIntent = Intent(applicationContext,AlarmService::class.java)

        //avvio il servizio che si occuperà del resto (AlarmService)
        startService(allarmIntent)
    }




    //imposta il layout in base alla "situazione"
    fun setSwitch() {
        val ingressoNome = pref.getString("Ingresso scelto","null")
        setted = pref.getInt("Settato", 0)

        if (setted == 0) {  //allarme da impostare

            //imposto il layout "Allarme non settato"
            setContentView(R.layout.activity_alarm)
            val aal= findViewById<View>(R.id.activity_alarm_layout)
            aal.backgroundTintMode= PorterDuff.Mode.DARKEN
            val fb= findViewById<FloatingActionButton>(R.id.addfab) // usare la classica abbreviazione alla Kotlin non funziona

            //listener del FAB
            fb.setOnClickListener { view ->
                //appare dialog per scegliere data e orario
                DatePickerDialog(this,datepickerdialoglistener,Calendar.getInstance().get(Calendar.YEAR),Calendar.getInstance().get(Calendar.MONTH),Calendar.getInstance().get(Calendar.DAY_OF_MONTH))
                        .show()

            }
            val ingrButton = findViewById<Button>(R.id.buttonIngr)
            // il FAB non si può premete finchè non è stato scelto un ingresso
            fb.isClickable=false
            if(ingressoNome.equals("null")==false){
                // ingresso già scelto
                ingrButton.text=Gson().fromJson(ingressoNome,Location::class.java).provider
                fb.isClickable=true
            }
            ingrButton.setOnClickListener { view ->  //listener del pulsante vero e proprio
                val popup = PopupMenu(this,view)
                popup.menuInflater.inflate(R.menu.ingressi,popup.menu) // ""Gonfio"" il menù
                popup.setOnMenuItemClickListener{ item :MenuItem? -> //listener dei vari pulsanti del menu
                    when (item!!.itemId){
                        //setto le coordinate degli ingressi e salvo tutto nelle preferenze (usando Json)
                        R.id.ingr_TorreSmaria -> {
                            ingrButton.text="Torre Santa Maria"
                            chosenIngress.provider="Torre Santa Maria"
                            chosenIngress.latitude=43.72436654869315
                            chosenIngress.longitude=10.393975675106049
                        }
                        R.id.ingr_PzzaGond -> {
                            ingrButton.text="Piazza Gondole"
                            chosenIngress.provider="Piazza Gondole"
                            chosenIngress.latitude=43.7166266
                            chosenIngress.longitude=10.40917109999998
                        }
                        R.id.ingr_TorreLegno -> {
                            ingrButton.text="Torre di Legno"
                            chosenIngress.provider="Torre di Legno"
                            chosenIngress.latitude=43.7132137
                            chosenIngress.longitude=10.410172999999986
                        }
                        R.id.ingr_TorrePiezo -> {
                            ingrButton.text="Torre Piezometrica"
                            chosenIngress.provider="Torre Piezometrica"
                            chosenIngress.latitude=43.71998561247473
                            chosenIngress.longitude=10.408865085923253
                        }
                    }
                    val editor =pref.edit()
                    val locationGsonString = Gson().toJson(chosenIngress)
                    editor.putString("Ingresso scelto",locationGsonString)
                    editor.apply()
                    fb.isClickable=true
                    true
                }
                popup.show() // lo mostro ( questo fa parte del primo listener
            }

        } else {
            //allarme impostato
            setContentView(R.layout.activity_alarm_set)

            val bu= findViewById<Button>(R.id.disableAlarmButton)
            val avvisoTime = findViewById<TextView>(R.id.textViewTimeSet)
            val avvisoData = findViewById<TextView>(R.id.textViewDateSet)
            val avvisoIngr = findViewById<TextView>(R.id.textViewIngressoScelto)
            val avvisoIngrDesc = findViewById<TextView>(R.id.textViewDescIngr)

            val ingrSceltoJson=Gson().fromJson(ingressoNome, Location::class.java)

            if(ingressoNome.equals("null")==false) {
                // ingresso già scelto
                avvisoIngr.text = ingrSceltoJson.provider
                //visualizzo una breve descrizione dell'ingresso scelto
                when (avvisoIngr.text) {
                    "Piazza Gondole" -> avvisoIngrDesc.text = resources.getString(R.string.PiazGondDesc)
                    "Torre Piezometrica" -> avvisoIngrDesc.text = resources.getString(R.string.TorPiezoDesc)
                    "Torre Santa Maria" -> avvisoIngrDesc.text = resources.getString(R.string.TorSanMarDesc)
                    "Torre di Legno" -> avvisoIngrDesc.text = resources.getString(R.string.TorLegnDesc)
                }
            }

            val orasettata=pref.getString("chosenDateOraS","10")
            val minutosettato=pref.getString("chosenDateMinutoS","00")
            val giornosettato=pref.getInt("chosenDateGiornodelMese",1)
            val mesesettato= pref.getInt("chosenDateMese",1)
            val annosettato= pref.getInt("chosenDateAnno",2000)

            //imposto i testi nelle Textview riguardanti l'orario e la data scelti
            var temp = "$orasettata:$minutosettato"
            avvisoTime.text=temp
            temp ="$giornosettato/$mesesettato/$annosettato"
            avvisoData.text=temp

            bu.setOnClickListener {
                //listener del tasto "annulla"
                val alarmmanager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
                val pintent = PendingIntent.getService(this,1,allarmIntent,0)
                //stoppo il servizio (se attivo)
                val statusDebug = stopService(allarmIntent)
                Log.w("DEBUG","Ho stoppato il servizio: "+statusDebug.toString())
                //cancello gli allarmi registrati
                alarmmanager.cancel(pintent)
                val editor = pref.edit()
                editor.putInt("Settato", 0)
                editor.apply()
            }

            //pulsante indicazioni
            val indButt= findViewById<Button>(R.id.buttonIndication)
            indButt.setOnClickListener{
                //invio un intent con le coordinate dell'ingresso scelto
                //mode=w -> navigazione impostata a piedi
                val gmmIntentUri = Uri.parse("google.navigation:q="+ingrSceltoJson.latitude+","+ingrSceltoJson.longitude+"&mode=w")
                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                startActivity(mapIntent)
            }
        }
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //cambio il titolo dell'activity
        supportActionBar?.title=resources.getText(R.string.Allarmi)
        supportActionBar?.elevation=0F
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        allarmIntent = Intent(applicationContext,AlarmService::class.java)
        pref= getSharedPreferences("myprefs",Context.MODE_PRIVATE)
        // MODE_PRIVATE modo default di creare il file, può essere acceduto soltanto dall'applicazione che chiama il metodo

        //registro un listener che scatti al variare di un elemento chiave-valore
        pref.registerOnSharedPreferenceChangeListener(msharedPreferenceChangeListener)
        setSwitch()

        //nascondo il fab per aggiungere la sveglia, sarà di nuovo visibile quando l'applicazione avrà una posizione valida
        if((pref.getInt("Settato",0))!=1){ //solo se non già impostata
            val fb = findViewById<FloatingActionButton>(R.id.addfab)
            fb.visibility=View.INVISIBLE
        }


        //localizzazione
        val crit = Criteria()
        crit.accuracy=Criteria.ACCURACY_FINE
        crit.powerRequirement=Criteria.POWER_MEDIUM
        lm = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locProv= lm.getBestProvider(crit,true) //location provvider
        Log.w("PROVVIDER SCELTO",locProv)

        //se il provvider migliore in quel momento non è il GPS, questa funzione non si può usare
        if(!(locProv.equals(LocationManager.GPS_PROVIDER))) {
            //avviso con un messaggio toast
            val toast = Toast.makeText(applicationContext,resources.getText(R.string.AttivaGPS), Toast.LENGTH_LONG)
            toast.show()
            finish()
        }
        // se per qualche motivo non ho i permessi di localizzazione, termina male.
        if (ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_DENIED)
            finish()

    }


    //Funzioni riguardanti l'interfaccia LocationListener-------------

    fun updateLocation (newLoc : Location){
        position = Location(newLoc)
    }

    override fun onLocationChanged(p0: Location?) {
        //aggiorno la posizione globale
        updateLocation(p0!!)
        Log.w("POSIZIONE","cambiata")
        //mostro il FAB
        addfab?.visibility=View.VISIBLE
        lm.removeUpdates(this) //disattivo il listener
    }

    override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {}

    override fun onProviderDisabled(p0: String?) {
        //in caso di spegnimento del gps prima di aver trovato una posizione termino ( non apparirebbe mai il fab )
        finish()
    }

    override fun onProviderEnabled(p0: String?) {
        if (ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED)
        lm.requestLocationUpdates(locProv,5000,0.toFloat(),this)
    }
    //------------------------------------------------------


    override fun onResume() {
        super.onResume()
        //controllo i permessi
        if (ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_DENIED) finish()
        lm.requestLocationUpdates(locProv,5000,1.toFloat(),this) //attivo il listener
        if(lm.getLastKnownLocation(locProv)!=null){
            //devo avere una posizione valida
            updateLocation(lm.getLastKnownLocation(locProv))
            pref= getSharedPreferences("myprefs",Context.MODE_PRIVATE)
            if((pref.getInt("Settato",0)!=1)){ // se ho gìa una posizione valida rendo visibile il FAB fin da subito
                addfab.visibility=View.VISIBLE
            }
            else  Toast.makeText(applicationContext, resources.getText(R.string.StocercandoPos), Toast.LENGTH_LONG).show()
        }
        else {
            //cerco la posizione altrimenti
            val toast = Toast.makeText(applicationContext, resources.getText(R.string.StocercandoPos), Toast.LENGTH_LONG)
            toast.show()
        }

    }

    override fun onPause() {
        super.onPause()
        lm.removeUpdates(this) //disattivo il listener
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        lm.removeUpdates(this) //disattivo il listener
        return true
    }
}
