package com.example.fabio.aspassosullemura

import android.app.*
import android.app.AlarmManager.RTC
import android.app.AlarmManager.RTC_WAKEUP
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.NotificationCompat
import android.view.View
import android.widget.Button
import android.widget.TimePicker
import kotlinx.android.synthetic.main.activity_alarm.*
import kotlinx.android.synthetic.main.activity_alarm_set.*
import android.os.CountDownTimer
import android.os.SystemClock
import android.support.design.widget.Snackbar
import android.widget.DatePicker
import android.widget.TextView
import java.time.Year
import java.util.*
import java.util.prefs.PreferenceChangeListener


class AlarmActivity : AppCompatActivity() {
    var setted : Int = 0
    lateinit var view : View
    lateinit var view2 : View

    var chosenDateAnno :Int = 2018
    var chosenDateMese: Int = 1
    var chosenDateGiornodelMese: Int = 1


    val datepickerdialoglistener = DatePickerDialog.OnDateSetListener{ datePicker: DatePicker, i: Int, i1: Int, i2: Int ->
        chosenDateAnno=i
        chosenDateMese=i1
        chosenDateGiornodelMese=i2
        TimePickerDialog(this, timepickerdialoglistener, 8, 0, true).show()
    }


    val msharedPreferenceChangeListener = SharedPreferences.OnSharedPreferenceChangeListener{ sharedPreferences: SharedPreferences, s: String ->
        if(s.equals("Settato")) setSwitch()
    }


    //Time picker Listener -------------------------------
    val timepickerdialoglistener = TimePickerDialog.OnTimeSetListener { timePicker: TimePicker, i: Int, i1: Int ->

        var ora ="$i"
        var minuti = "$i1"
        if(i<=10) ora="0$i"
        if (i1<10) minuti="0$i1"



        var pref= getSharedPreferences("myprefs",Context.MODE_PRIVATE)
        var editor = pref.edit()
        editor.putInt("Settato",1)


        //facciamo due conti


        var quantomancaAnno = chosenDateAnno - (Calendar.getInstance().get(Calendar.YEAR))
        var quantomancaMese = chosenDateMese - (Calendar.getInstance().get(Calendar.MONTH))
        var quantomancaGiorno = chosenDateGiornodelMese - (Calendar.getInstance().get(Calendar.DAY_OF_MONTH))
        var quantomancaOra= i - (Calendar.getInstance().get(Calendar.HOUR_OF_DAY))
        var quantomancaMinuto = i1 - (Calendar.getInstance().get(Calendar.MINUTE))

        var delay= (quantomancaMinuto*60+quantomancaOra*60*60+quantomancaGiorno*24*60*60)*1000
        if(delay<0){
            Snackbar.make(addfab, "Non posso viaggiare nel tempo. Per ora.", Snackbar.LENGTH_LONG).show()
            return@OnTimeSetListener
        }


        var calendario = Calendar.getInstance()
        calendario.set(Calendar.YEAR,chosenDateAnno)
        calendario.set(Calendar.MONTH,chosenDateMese)
        calendario.set(Calendar.DAY_OF_MONTH,chosenDateGiornodelMese)
        calendario.set(Calendar.HOUR_OF_DAY,i)
        calendario.set(Calendar.MINUTE,i1)

        editor.putInt("chosenDateAnno",chosenDateAnno)
        editor.putInt("chosenDateMese",chosenDateMese)
        editor.putInt("chosenDateGiornodelMese",chosenDateGiornodelMese)
        editor.putString("chosenDateOra",ora)
        editor.putString("chosenDateMinuto",minuti)
        editor.commit()



        var intent = Intent(applicationContext,Receiver::class.java)
        var alarmmanager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        var pintent = PendingIntent.getBroadcast(this,1,intent,0)
        alarmmanager.set(RTC_WAKEUP,calendario.timeInMillis,pintent) //funziona?

        //disableAlarmButton.text=("ora: $quantomancaOra minuto: $quantomancaMinuto giorno:$quantomancaGiorno")

        //FUNZIONA
       //alarmmanager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,SystemClock.elapsedRealtime()+delay,pintent)
        //




    }
    //--------------------------------------------





    fun setSwitch() {
        var pref = getSharedPreferences("myprefs",Context.MODE_PRIVATE)
        setted = pref.getInt("Settato", 0)
        if (setted == 0) {
            setContentView(R.layout.activity_alarm)
            var fb= findViewById<FloatingActionButton>(R.id.addfab)
            fb.setOnClickListener { view ->
                //appare dialog per scegliere data e orario
                DatePickerDialog(this,datepickerdialoglistener,Calendar.getInstance().get(Calendar.YEAR),Calendar.getInstance().get(Calendar.MONTH),Calendar.getInstance().get(Calendar.DAY_OF_MONTH))
                        .show()

            }



        } else {
            setContentView(R.layout.activity_alarm_set)
            var bu= findViewById<Button>(R.id.disableAlarmButton)
            var avvisoTime = findViewById<TextView>(R.id.textViewTimeSet)
            var avvisoData = findViewById<TextView>(R.id.textViewDateSet)

            var orasettata=pref.getString("chosenDateOra","10")
            var minutosettato=pref.getString("chosenDateMinuto","00")
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



    }



    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
