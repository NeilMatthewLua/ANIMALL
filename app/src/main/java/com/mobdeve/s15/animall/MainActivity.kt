package com.mobdeve.s15.animall

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction


class MainActivity : AppCompatActivity() {

    private lateinit var viewProfileBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewProfileBtn = findViewById(R.id.viewProfileBtn)

        viewProfileBtn.setOnClickListener{
//            val manager: FragmentManager = getSupportFragmentManager()
//            val transaction: FragmentTransaction = manager.beginTransaction()
//            transaction.add(R.id.container, UserProfileFragment, "UserProfileFragment")
//            transaction.addToBackStack(null)
//            transaction.commit()
        }
    }
}