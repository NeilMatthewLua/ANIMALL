package com.mobdeve.s15.animall

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.activity_navbar.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_navbar)

        //Initialize the bottom navigation view
        //create bottom navigation view object
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        findViewById<BottomNavigationView>(R.id.bottom_navigatin_view)
            .setupWithNavController(navController)

//        bottom_navigatin_view.setOnItemSelectedListener {
//            when(it.itemId) {
//                R.id.landingFragment -> {
//                    val transaction = supportFragmentManager.beginTransaction()
//                    transaction.replace(R.id.nav_host_fragment, ViewListingFragment())
//                    transaction.commit()
//
//                }
//                else {
//
//                }
//            }
//        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
    }
}