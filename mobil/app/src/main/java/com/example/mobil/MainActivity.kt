package com.example.mobil

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.example.mobil.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // NavHostFragment'ı buluyoruz
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.main_navHostFragment) as NavHostFragment

        // NavController'ı değişkene atıyoruz
        navController = navHostFragment.navController

        // Bottom Navigation'ı NavController'a bağlıyoruz
        NavigationUI.setupWithNavController(binding.bottomNav, navController)

        //menu dosyasındaki item id'leri ile nav_graph dosyasındaki fragment id'lerini aynı isimlendirerek eşleştirme yapılır

        navController.addOnDestinationChangedListener { _, destination, _ ->

            // Buraya alt menünün görüneceği sayfalar
            if (destination.id == R.id.kullaniciFavFragment
                || destination.id == R.id.kullaniciHomeFragment
                || destination.id == R.id.kullaniciProfilFragment
                || destination.id == R.id.kullaniciSepetFragment) {
                binding.bottomNav.visibility = View.VISIBLE
            }
            else{
                binding.bottomNav.visibility = View.GONE
            }
        }
    }
}