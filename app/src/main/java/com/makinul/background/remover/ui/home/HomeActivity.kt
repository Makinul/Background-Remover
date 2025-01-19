package com.makinul.background.remover.ui.home

import android.os.Bundle
import androidx.navigation.ui.AppBarConfiguration
import com.makinul.background.remover.base.BaseActivity
import com.makinul.background.remover.databinding.ActivityHomeBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeActivity : BaseActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        setSupportActionBar(binding.toolbar)
//        val bottomNavView: BottomNavigationView = binding.bottomNavView
//        val navController = findNavController(R.id.nav_host_fragment_home)
//        // Passing each menu ID as a set of Ids because each
//        // menu should be considered as top level destinations.
//        appBarConfiguration = AppBarConfiguration(
//            setOf(
//                R.id.home_fragment, R.id.history_fragment, R.id.notifications_fragment
//            )
//        )
//        setupActionBarWithNavController(navController, appBarConfiguration)
//        bottomNavView.setupWithNavController(navController)

        updateHomeIcon()
    }

//    override fun onCreateOptionsMenu(menu: Menu): Boolean {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        menuInflater.inflate(R.menu.menu_main, menu)
//        return true
//    }
//
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        return when (item.itemId) {
//            R.id.action_settings -> true
//            else -> super.onOptionsItemSelected(item)
//        }
//    }

//    override fun onSupportNavigateUp(): Boolean {
//        val navController = findNavController(R.id.nav_host_fragment_home)
//        return navController.navigateUp(appBarConfiguration)
//                || super.onSupportNavigateUp()
//    }

    fun updateHomeIcon() {
//        supportActionBar?.setDisplayHomeAsUpEnabled(true)
//        supportActionBar?.setDisplayShowHomeEnabled(true)
//        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_home_white)
    }

    fun goForPrivacyPolicy() {
        showBrowserDialog(this, "https://bg-remover-5530d.web.app/privacy_policy.html")
    }
}