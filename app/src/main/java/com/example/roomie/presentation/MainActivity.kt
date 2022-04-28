package com.example.roomie.presentation

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.*
import com.example.roomie.R
import com.example.roomie.core.sharedpreferences.FlatStorage
import com.example.roomie.core.sharedpreferences.SecureStorage
import com.example.roomie.data.repository.NetworkStatusTracker
import com.example.roomie.databinding.ActivityMainBinding
import com.example.roomie.presentation.notification.BackgroundService
import com.example.roomie.presentation.splashscreen.SplashScreenActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var appBarConfiguration : AppBarConfiguration
    private lateinit var splashScreenSharedPref: SharedPreferences
    private val TAG = MainActivity::class.java.simpleName

    private val viewModel: NetworkStatusViewModel by lazy {
        ViewModelProvider(
            this,
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val networkStatusTracker = NetworkStatusTracker(this@MainActivity)
                    return NetworkStatusViewModel(networkStatusTracker) as T
                }
            },
        ).get(NetworkStatusViewModel::class.java)
    }
    var reloadTrigger = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /**
         * to show SplashScreen only when the user opens the app for the first time
         * it doesn't show the splash screen every time the user goes in the app only if the app is destroy
         */
        splashScreenSharedPref = getSharedPreferences("splashScreenSettings", 0)
        val firstRun = splashScreenSharedPref.getBoolean("splashScreenFirstRun", false)
        if (!firstRun)
        {
            val editor = splashScreenSharedPref.edit()
            editor.putBoolean("splashScreenFirstRun", true)
            editor.apply()
            val splashScreenIntent = Intent(this, SplashScreenActivity::class.java)
            startActivity(splashScreenIntent)
        }


        /**
         * initialize SecureStorage for save the auth token
         */
        SecureStorage.init(this.applicationContext)
        /**
         * initialize FlatStorag to store the users flat id
         */
        FlatStorage.init(this.applicationContext)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        /**
         * there are limits to its use of background services. This does not apply to foreground services,
         * which is why this polling service is implemented as an Foreground Service
         *
         * By default, these restrictions only apply to apps that target Android 8.0 (API level 26) or higher.
         */
        val backgroundServiceIntent = Intent(applicationContext, BackgroundService::class.java)
        applicationContext.startForegroundService(backgroundServiceIntent)

        // alternative is the workmanager, but the data can not be fetched in less time intervals than 15 min.
        // val periodicWorkRequest = PeriodicWorkRequestBuilder<BackgroundWorker>(60, TimeUnit.SECONDS).build()
        // WorkManager.getInstance().enqueue(periodicWorkRequest)

        // Disabled for presentation (no internet error message bug)
//        viewModel.state.observe(this) {
//            when (it) {
//                MyState.Fetched -> {}
//                MyState.Error -> {
//                    CustomSnackbar.showSnackbar(binding.root, "No Internet Connection", CustomSnackbar.SnackbarTime.INFINIE, CustomSnackbar.SnackbarType.ERROR)
//                }
//            }
//        }

        navView = binding.navView

        //val navController = findNavController(R.id.nav_host_fragment_activity_main)
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment
        val navController = navHostFragment.navController

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        // these fragments do not contain the upbutton in the appbar
         appBarConfiguration = AppBarConfiguration
            .Builder(
                R.id.navigation_home,
                R.id.navigation_shopping,
                R.id.navigation_finance,
                R.id.navigation_settings,
                R.id.loginFragment,
                R.id.flatFragment
            ).build()

        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    override fun onSupportNavigateUp(): Boolean {
        return findNavController(R.id.nav_host_fragment_activity_main).navigateUp(appBarConfiguration)
    }


    override fun onDestroy() {
        super.onDestroy()
        val editor = splashScreenSharedPref.edit()
        editor.putBoolean("splashScreenFirstRun", false)
        editor.apply()
    }

    companion object {
        lateinit var navView: BottomNavigationView
    }
}