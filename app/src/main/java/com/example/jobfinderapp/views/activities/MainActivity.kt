package com.example.jobfinderapp.views.activities

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import com.example.jobfinderapp.R
import com.example.jobfinderapp.databinding.ActivityMainBinding
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.example.jobfinderapp.App
import com.example.jobfinderapp.utils.NetworkMonitor
import javax.inject.Inject

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    lateinit var navController: NavController

    @Inject
    lateinit var networkMonitor: NetworkMonitor

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.bot_nav_view)) { v, insets ->
            val navInsets = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
            v.setPadding(0, 0, 0, navInsets.bottom)
            insets
        }

        setSupportActionBar(binding.maToolbar)

        App.instance.appComponent.inject(this)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragment_cont_view) as NavHostFragment
        navController = navHostFragment.navController

        val topLevelDestination = setOf(R.id.homeFragment, R.id.appliedFragment, R.id.savedFragment, R.id.settingsFragment, R.id.withdrawDialogFragment)

        val appBarConfiguration = AppBarConfiguration(
            topLevelDestination
        )

        binding.maToolbar.setupWithNavController(navController, appBarConfiguration)

        binding.botNavView.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, dest, _ ->
            binding.botNavView.visibility =
                if (dest.id in topLevelDestination) View.VISIBLE
                else View.GONE
        }

        networkMonitor.isConnected.observe(this) { connected ->

            if (!connected) {
                binding.maNetworkProblemTv.apply {
                    isVisible = true
                    alpha = 0f
                    animate()
                        .alpha(1f)
                        .setDuration(200)
                        .start()
                }
            } else {
                binding.maNetworkProblemTv.animate()
                    .alpha(0f)
                    .setDuration(200)
                    .withEndAction { binding.maNetworkProblemTv.isVisible = false }
                    .start()
            }

        }

    }
}