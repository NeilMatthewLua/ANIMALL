package com.mobdeve.s15.animall

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class MainActivity : AppCompatActivity() {

    private lateinit var createListingBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        createListingBtn = findViewById(R.id.createListingBtn)

        createListingBtn.setOnClickListener{
            val intent = Intent(this, CreateListingActivity::class.java)
            startActivity(intent)
        }
    }
}