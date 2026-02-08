package com.example.jobfinderapp.views.activities

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import com.example.jobfinderapp.ApiConst
import com.example.jobfinderapp.R
import com.example.jobfinderapp.RetrofitService
import com.example.jobfinderapp.databinding.ActivityMainBinding
import kotlinx.coroutines.launch
import androidx.navigation.findNavController
import com.example.jobfinderapp.App
import javax.inject.Inject

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    lateinit var navController: NavController

    @Inject
    lateinit var retrofitService: RetrofitService

    override fun onCreate(savedInstanceState: Bundle?) {

        App.instance.appComponent.inject(this)

        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        navController = this.findNavController(R.id.fragment_cont_view)

        val options = NavOptions.Builder()
            .setLaunchSingleTop(true)
            .setRestoreState(true)
            .setPopUpTo(navController.graph.startDestinationId, saveState = true, inclusive = false)
            .build()

        binding.botNavView.setOnItemSelectedListener {
            when(it.itemId) {
                R.id.fragment_home -> {
                    navController.navigate(R.id.homeFragment, null, options)
                    true
                }

                R.id.fragment_saved -> {
                    navController.navigate(R.id.savedFragment, null, options)
                    true
                }

                R.id.fragment_settings -> {
                    navController.navigate(R.id.settingsFragment, null, options)
                    true
                }

                else -> false
            }

        }


        lifecycleScope.launch {
            val res = retrofitService.getExample(ApiConst.APP_ID, ApiConst.API_KEY)
            if (res.isSuccessful) {
                val body = res.body()
                println("!!! $body")
            } else {
                println("!!! ${res.errorBody()}")
            }
        }
    }
}