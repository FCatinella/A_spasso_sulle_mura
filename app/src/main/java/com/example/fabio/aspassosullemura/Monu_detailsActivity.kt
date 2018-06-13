package com.example.fabio.aspassosullemura

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.view.WindowManager
import android.widget.ImageView
import kotlinx.android.synthetic.main.activity_monu_details.*

class Monu_detailsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_monu_details)

        //imposto la foto come sfondo della imageview (questa deve essere variabile in base al monumento)
        image_scrolling_top.setImageDrawable(getDrawable(R.drawable.mura1))
        image_scrolling_top.imageAlpha=750



        //questa cosa è allucinante!
        toolbar.title="Mura di Pisa"
        toolbar_layout.setExpandedTitleColor(resources.getColor(R.color.colorPrimary))
        setSupportActionBar(toolbar)

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }
    }
}
