package com.shiweihu.vedioplaydemo

import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import android.view.Menu
import android.view.MenuItem
import android.view.Surface
import androidx.lifecycle.lifecycleScope
import com.shiweihu.vedioplaydemo.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {


    private lateinit var binding: ActivityMainBinding



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.play.setOnClickListener {
            val playClient = PlayClient(Surface(binding.PlaybackView.surfaceTexture))
            this.lifecycleScope.launch(Dispatchers.IO) {
                playClient.startPlay(this@MainActivity,"https://player.vimeo.com/external/436041442.sd.mp4?s=59304a9335c0c01843c30208f43ae78b7e3a2355&profile_id=164")
            }

        }
    }

}