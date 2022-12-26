package com.shubham.tasktrackerapp

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentContainerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.shubham.tasktrackerapp.dashboard.DashboardFragment
import com.shubham.tasktrackerapp.data.local.Task
import com.shubham.tasktrackerapp.data.local.TaskDao
import com.shubham.tasktrackerapp.data.local.TaskDatabase
import com.shubham.tasktrackerapp.newtask.NewTaskFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import java.util.*

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    companion object {
        const val TAG = "taskTag"
    }

    private var fragmentContainer: FragmentContainerView? = null
    private var taskDao: TaskDao? = null
    private lateinit var bottomNavBar: BottomNavigationView

    @SuppressLint("ObjectAnimatorBinding")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bottomNavBar = findViewById(R.id.bottom_nav)
        fragmentContainer = findViewById(R.id.fragment_container)

        supportFragmentManager.beginTransaction()
            .add(R.id.fragment_container, HomeFragment(), "Home")
            .addToBackStack(null)
            .commit()

        bottomNavBar.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.nav_home -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, HomeFragment())
                        .commit()
                    return@setOnItemSelectedListener true
                }
                R.id.nav_new_task -> {
                    if (supportFragmentManager.backStackEntryCount > 1) {
                        onBackPressed()
                        return@setOnItemSelectedListener true
                    }
                    supportFragmentManager
                        .beginTransaction()
                        .setCustomAnimations(
                            R.anim.slide_in,
                            0,
                            0,
                            R.anim.slide_out
                        )
                        .add(R.id.fragment_container, NewTaskFragment(this))
                        .addToBackStack(null)
                        .commit()
                    return@setOnItemSelectedListener true
                }
                R.id.nav_dashboard -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, DashboardFragment(), "Dashboard")
                        .commit()
                    return@setOnItemSelectedListener true
                }
            }
            true
        }
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 1) {
            supportFragmentManager.popBackStack()
            val fragment = supportFragmentManager.fragments.first()

            // We are checking for home and dashboard because if we are in this block
            // then it means that we are removing the new task frag
            if (fragment.tag == "Home") {
                // TODO change selected item of bottom navbar
                bottomNavBar.selectedItemId = R.id.nav_home
            } else {

            }
        } else {
            finish()
        }
    }
}