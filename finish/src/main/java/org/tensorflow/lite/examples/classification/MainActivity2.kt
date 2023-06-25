package org.tensorflow.lite.examples.classification

import android.Manifest
import android.app.NotificationManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import org.tensorflow.lite.examples.classification.databinding.ActivityMain2Binding

//import org.tensorflow.lite.examples.classification.binding.ActivityMainBinding


class MainActivity2 : AppCompatActivity() {



private lateinit var binding: ActivityMain2Binding

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMain2Binding.inflate(layoutInflater)
        AssetsDatabaseManager.initManager(applicationContext)
        // Request permission
        requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_REQUEST_CODE)

        setContentView(binding.root)
        replaceFragment(Home())

        binding.bottomNavigationView.setOnNavigationItemSelectedListener {
            when(it.itemId){
                R.id.home->replaceFragment(Home())
                R.id.newPlant->replaceFragment(NewPlantForm())
                R.id.scan->replaceFragment(Scan())
                R.id.map->replaceFragment(Map())
                R.id.setting->replaceFragment(Setting())

                else->{


                }



            }
            true
        }




    }

    private  fun replaceFragment(fragment:Fragment)
    {
        val fragmentManager=supportFragmentManager
        val fragmentTransition=fragmentManager.beginTransaction()
        fragmentTransition.replace(R.id.frame_layout,fragment)
        fragmentTransition.commit()
    }
}