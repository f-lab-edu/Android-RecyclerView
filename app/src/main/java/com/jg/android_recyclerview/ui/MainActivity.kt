package com.jg.android_recyclerview.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import com.jg.android_recyclerview.R
import com.jg.android_recyclerview.databinding.ActivityMainBinding
import com.jg.android_recyclerview.model.ItemType
import com.jg.android_recyclerview.model.ViewMode
import com.jg.android_recyclerview.ui.adapter.MainAdapter
import com.jg.android_recyclerview.viewmodel.StateFlowViewModel
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupBtn()
    }

    private fun setupBtn() {
        binding.btnActivity.setOnClickListener {
            // Navigation 대신 Intent 사용
            startActivity(Intent(this, ListActivity::class.java))
        }

        binding.btnFragment.setOnClickListener {
            setContentView(R.layout.content_main)
            findNavController(R.id.nav_host_fragment_content_main)
                .navigate(R.id.action_global_FirstFragment)
        }
    }
}