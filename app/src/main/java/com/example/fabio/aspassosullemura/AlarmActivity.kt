package com.example.fabio.aspassosullemura

import android.app.*
import android.app.AlarmManager.RTC
import android.app.AlarmManager.RTC_WAKEUP
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.NotificationCompat
import android.view.View
import kotlinx.android.synthetic.main.activity_alarm.*
import kotlinx.android.synthetic.main.activity_alarm_set.*
import android.os.CountDownTimer
import android.os.SystemClock
import android.support.design.widget.Snackbar
import android.util.Log
import android.view.MenuItem
import android.widget.*
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.gson.Gson
import java.time.Year
import java.util.*
import java.util.prefs.PreferenceChangeListener

class AlarmActivity : AppCompatActivity(),LocationListener {
    //variabili
    var setted : Int = 0
    lateinit var view : View
    lateinit var position : Location
    lateinit var locProv : String
    lateinit var lm : LocationManager
    var chosenIngress : Location = Location("Ingresso")
    var chosenDateAnno :Int = 2018
    var chosenDateMese: Int = 1
    var chosenDateGiornodelMese: Int = 1

    //Listener Date Picker-------------------------
    val datepickerdialoglistener = DatePickerDialog.OnDateSetListener{ datePicker: DatePicker, i: Int, i1: Int, i2: Int ->

        // assegno alle variabili i parametri impostati dall'utente
        chosenDateAnno=i
        chosenDateMese=i1
        chosenDateGiornodelMese=i2


        //visualizzo uno snackbar ed esco se il mese o l'anno non sono quello corrente
        if(i!=Calendar.getInstance().get(Calendar.YEAR) || i1!=Calendar.getInstance().get(Calendar.MONTH)){
            Snackbar.make(addfab, "Puoi scegliere date solo nel mese corrente!", Snackbar.LENGTH_LONG).show()
            return@OnDateSetListener
        }

        // chiamo il selettore di orario
        TimePickerDialog(this, timepickerdialoglistener, 8, 0, true).show()
    }
    //-------------------------------------------------------


    //Cosa succede alla modifica di un valore nelle SharedPreferences -----------------------
    val msharedPreferenceChangeListener = SharedPreferences.OnSharedPreferenceChangeListener{ sharedPreferences: SharedPreferences, s: String ->
        if(s.equals("Settato")) setSwitch() //Switcho il layout se il parametro "Settato" è stato cambiato nell preferenze
    }
    //------------------------------


    //Time picker Listener -------------------------------
    val timepickerdialoglistener = TimePickerDialog.OnTimeSetListener { timePicker: TimePicker, i: Int, i1: Int ->

        //Prendo l'ora da visualizzare e la trasformo in String
        var ora ="$i"
        var minuti = "$i1"
        if(i<10) ora="0$i"
        if (i1<10) minuti="0$i1"


        //Cambio "Settato" in modo che cambi il layout
        var pref= getSharedPreferences("myprefs",Context.MODE_PRIVATE)
        var editor = pref.edit()
        editor.putInt("Settato",1)



        //facciamo due conti
        var quantomancaGiorno = chosenDateGiornodelMese - (Calendar.getInstance().get(Calendar.DAY_OF_MONTH))
        var quantomancaOra= i - (Calendar.getInstance().get(Calendar.HOUR_OF_DAY))
        var quantomancaMinuto = i1 - (Calendar.getInstance().get(Calendar.MINUTE))

        var delay= (quantomancaMinuto*60+quantomancaOra*60*60+quantomancaGiorno*24*60*60)*1000


        //Visualizzo uno snackbar in caso di ora o data sbagliata e ritorno
        if(delay<0) {
            Snackbar.make(addfab, "Non posso viaggiare nel tempo. Per ora.", Snackbar.LENGTH_LONG).show()
            return@OnTimeSetListener
        }


        //aggiorno i valori nelle preferenze
        editor.putInt("chosenDateAnno",chosenDateAnno)
        .putInt("chosenDateMese",chosenDateMese)
        .putInt("chosenDateGiornodelMese",chosenDateGiornodelMese)
        .putInt("chosenDateOra",i)
        .putInt("chosenDateMinuto",i1)
                .putString("chosenDateOraS",ora)
                .putString("chosenDateMinutoS",minuti)
        .commit()

        /*
        - guardo cosa ha selezionato l'utente come ingresso
        - imposta la differenza di tempo come timer
        - scade il timer ->
            - se è più lontano di quanto manca, suona e avviso relativo ( sei in ritardo ). Tempo massimo 1 ora
            - se è abbastanza vicino imposta la metà del tempo che manca fino a > 10 minuti
            - ripeti
         */

        var locationGsonString = pref.getString("Ingresso scelto","")
        var chosenLocation = Gson().fromJson(locationGsonString,Location::class.java)


        var distance = chosenLocation.distanceTo(position)
        var distanceTime = distance*1000

        Log.w("Ingresso scelto",distance.toString())


        var intent = Intent(applicationContext,Receiver::class.java)
        var alarmmanager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        var pintent = PendingIntent.getBroadcast(this,1,intent,0)
        alarmmanager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,SystemClock.elapsedRealtime()+(delay-(distanceTime.toLong()))/2,pintent)




    }
    //--------------------------------------------






    fun setSwitch() {
        var pref = getSharedPreferences("myprefs",Context.MODE_PRIVATE)
        setted = pref.getInt("Settato", 0)
        if (setted == 0) {
            //allarme da impostare
            setContentView(R.layout.activity_alarm)
            var fb= findViewById<FloatingActionButton>(R.id.addfab)
            fb.setOnClickListener { view ->
                //appare dialog per scegliere data e orario
                DatePickerDialog(this,datepickerdialoglistener,Calendar.getInstance().get(Calendar.YEAR),Calendar.getInstance().get(Calendar.MONTH),Calendar.getInstance().get(Calendar.DAY_OF_MONTH))
                        .show()

            }
            var ingrButton = findViewById<Button>(R.id.buttonIngr)
            ingrButton.setOnClickListener { view ->
                var popup = PopupMenu(this,view)
                popup.menuInflater.inflate(R.menu.ingressi,popup.menu)
                popup.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item :MenuItem? ->
                    when (item!!.itemId){
                        R.id.ingr_TorreSmaria -> {
                            ingrButton.text="Torre Santa Maria"
                            chosenIngress.latitude=43.72436654869315
                            chosenIngress.longitude=10.393975675106049
                        }
                        R.id.ingr_PzzaGond -> {
                            ingrButton.text="Piazza Gondole"
                            chosenIngress.latitude=43.7166266
                            chosenIngress.longitude=10.40917109999998
                        }
                        R.id.ingr_TorreLegno -> {
                            ingrButton.text="Torre di Legno"
                            chosenIngress.latitude=43.7132137
                            chosenIngress.longitude=10.410172999999986
                        }
                        R.id.ingr_TorrePiezo -> {
                            ingrButton.text="Torre Piezometrica"
                            chosenIngress.latitude=43.71998561247473
                            chosenIngress.longitude=10.408865085923253
                        }
                    }
                    var editor =pref.edit()
                    var locationGsonString = Gson().toJson(chosenIngress)
                    editor.putString("Ingresso scelto",locationGsonString)
                    editor.commit()
                    true
                })
                popup.show()
            }

        } else {
            //allarme impostato
            setContentView(R.layout.activity_alarm_set)
            var bu= findViewById<Button>(R.id.disableAlarmButton)
            var avvisoTime = findViewById<TextView>(R.id.textViewTimeSet)
            var avvisoData = findViewById<TextView>(R.id.textViewDateSet)

            var orasettata=pref.getString("chosenDateOraS","10")
            var minutosettato=pref.getString("chosenDateMinutoS","00")
            var giornosettato=pref.getInt("chosenDateGiornodelMese",1)
            var mesesettato= pref.getInt("chosenDateMese",1)
            var annosettato= pref.getInt("chosenDateAnno",2000)

            avvisoTime.text=(orasettata+":"+minutosettato)
            avvisoData.text="$giornosettato/$mesesettato/$annosettato"
            bu.setOnClickListener { view ->
                var alarmmanager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
                var intent = Intent(applicationContext,Receiver::class.java)
                var pintent = PendingIntent.getBroadcast(this,1,intent,0)
                alarmmanager.cancel(pintent)
                var editor = pref.edit()
                editor.putInt("Settato", 0)
                editor.commit()
            }
        }
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.title="Allarmi"
        supportActionBar?.elevation=0F
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        var pref= getSharedPreferences("myprefs",Context.MODE_PRIVATE)
        pref.registerOnSharedPreferenceChangeListener(msharedPreferenceChangeListener)
        setSwitch()
        //localizzazione
        var crit = Criteria()
        crit.accuracy=Criteria.ACCURACY_FINE
        crit.powerRequirement=Criteria.POWER_MEDIUM
        lm = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locProv= lm.getBestProvider(crit,true)
        updateLocation(lm.getLastKnownLocation(locProv))

    }


    //Funzioni riguardanti l'interfaccia LocationListener
    fun updateLocation (newLoc : Location){
        position = Location(newLoc)
    }

    override fun onLocationChanged(p0: Location?) {
        updateLocation(p0!!)
    }
    override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {}

    override fun onProviderDisabled(p0: String?) {}

    override fun onProviderEnabled(p0: String?) {}
    //------------------------------------


    override fun onResume() {
        super.onResume()
        lm.requestLocationUpdates(locProv,2000,10.toFloat(),this)
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
