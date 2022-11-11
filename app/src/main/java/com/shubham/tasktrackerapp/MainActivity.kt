package com.shubham.tasktrackerapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.shubham.tasktrackerapp.Fragments.DashboardFragment
import com.shubham.tasktrackerapp.Fragments.HomeFragment
import com.shubham.tasktrackerapp.Fragments.NewTaskFragment

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val bottomNavBar = findViewById<BottomNavigationView>(R.id.bottom_nav)
        bottomNavBar.setOnItemSelectedListener {
            when(it.itemId){
                R.id.nav_home -> {
                    supportFragmentManager.beginTransaction().replace(R.id.fragment_container , HomeFragment()).commit()
                    return@setOnItemSelectedListener true
                }
                R.id.nav_new_task -> {
                    supportFragmentManager.beginTransaction().replace(R.id.fragment_container , NewTaskFragment()).commit()
                    return@setOnItemSelectedListener true
                }
                R.id.nav_dashboard -> {
                    supportFragmentManager.beginTransaction().replace(R.id.fragment_container , DashboardFragment()).commit()
                    return@setOnItemSelectedListener true
                }
            }
            true
        }
    }
}