package com.example.jobfinderapp.views.activities

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.GravityCompat
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
import androidx.transition.Slide
import com.example.jobfinderapp.App
import com.example.jobfinderapp.utils.AppPrefs
import com.example.jobfinderapp.utils.NetworkMonitor
import com.example.jobfinderapp.utils.ReselectedScroll
import javax.inject.Inject

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var navOptions: NavOptions
    private lateinit var navHostFragment: NavHostFragment

    private var isBottomNavVisible = true

    @Inject
    lateinit var networkMonitor: NetworkMonitor

    @Inject
    lateinit var appPrefs: AppPrefs

    override fun onCreate(savedInstanceState: Bundle?) {
        App.instance.appComponent.inject(this)

        val isDarkMode = appPrefs.isDarkTheme()
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_NO)
        }

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

        navHostFragment = supportFragmentManager.findFragmentById(R.id.fragment_cont_view) as NavHostFragment
        navController = navHostFragment.navController

        setSupportActionBar(binding.maToolbar)

        navOptions = NavOptions.Builder()
            .setRestoreState(true)
            .setLaunchSingleTop(true)
            .setPopUpTo(navController.graph.startDestinationId, false, true)
            .build()

        val fragmentsWithBotNavView = setOf(R.id.homeFragment, R.id.appliedFragment, R.id.savedFragment, R.id.settingsFragment, R.id.withdrawDialogFragment, R.id.countrySelectorDialogFragment)
        val fragmentsWithAlarmBtn = setOf(R.id.homeFragment, R.id.savedFragment, R.id.appliedFragment, R.id.withdrawDialogFragment, R.id.filterBottomSheetFragment, R.id.settingsFragment, R.id.notificationsFragment, R.id.countrySelectorDialogFragment)

        val topLevelDestination = setOf(R.id.homeFragment, R.id.appliedFragment, R.id.savedFragment, R.id.settingsFragment)

        val appBarConfiguration = AppBarConfiguration(
            topLevelDestination
        )

        binding.maToolbar.setupWithNavController(navController, appBarConfiguration)

        binding.maToolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.botNavView.setupWithNavController(navController)

        binding.botNavView.setOnItemReselectedListener { item ->
            when(item.itemId) {
                R.id.homeFragment -> notifyCurrentFragmentToScrollTop()
                R.id.savedFragment -> notifyCurrentFragmentToScrollTop()
                R.id.appliedFragment -> notifyCurrentFragmentToScrollTop()
            }
        }

        navController.addOnDestinationChangedListener { _, dest, _ ->

            when(dest.id) {
                in fragmentsWithBotNavView -> showBottomNavAnimated()
                else -> hideBottomNavAnimated()
            }

            binding.maAppBarLayout.visibility =
                if (dest.id in fragmentsWithAlarmBtn) View.VISIBLE
                else View.GONE

            binding.maAppBarDivider.visibility =
                if (dest.id in fragmentsWithAlarmBtn) View.VISIBLE
                else View.INVISIBLE


            val menu = binding.maToolbar.menu
            val item = menu.findItem(R.id.ma_toolbar_notification)

            item?.isVisible = dest.id != R.id.notificationsFragment
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

    private fun notifyCurrentFragmentToScrollTop() {
        val currentFragment = navHostFragment.childFragmentManager.primaryNavigationFragment

        if (currentFragment is ReselectedScroll) {
            currentFragment.smoothScrollToStart()
        }
    }

    fun hideBotNavViewExclusive(onEnd: () -> Unit) {
        val nav = binding.botNavView
        nav.animate().cancel()

        if (!isBottomNavVisible) {
            onEnd()
            return
        }
        isBottomNavVisible = false

        nav.animate()
            .translationY(nav.height.toFloat())
            .alpha(0f)
            .setDuration(80)
            .setInterpolator(AccelerateInterpolator())
            .withEndAction { onEnd() }
            .start()
    }

    fun hideBottomNavAnimated() {
        val nav = binding.botNavView
        if (!isBottomNavVisible) return

        isBottomNavVisible = false

        nav.animate().cancel()
        nav.animate()
            .translationY(nav.height.toFloat())
            .alpha(0f)
            .setDuration(200)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()
    }

    fun showBottomNavAnimated() {
        val nav = binding.botNavView
        if (isBottomNavVisible) return

        isBottomNavVisible = true

        nav.animate().cancel()
        nav.animate()
            .translationY(0f)
            .alpha(1f)
            .setDuration(220)
            .setStartDelay(100)
            .setInterpolator(DecelerateInterpolator())
            .start()
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.ma_toolbar_menu, menu)
        
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.ma_toolbar_notification -> {
                //navigate to reminders screen
                navController.navigate(R.id.notificationsFragment, null, navOptions)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }

    }

}