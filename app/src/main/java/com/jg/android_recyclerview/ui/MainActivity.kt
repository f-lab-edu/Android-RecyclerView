package com.jg.android_recyclerview.ui

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.jg.android_recyclerview.databinding.ActivityMainBinding
import com.jg.android_recyclerview.ui.adapter.MainAdapter
import com.jg.android_recyclerview.viewmodel.StateFlowViewModel
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val mainAdapter = MainAdapter()
    private val viewModel: StateFlowViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        observeViewModel()
        setupBtn()
    }

    private fun setupRecyclerView() {
        binding.recyclerView.adapter = mainAdapter

        mainAdapter.setOnItemClickListener { item ->
            viewModel.moveToTrash(item)
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.currentMode.collect { mode ->
                binding.btnToggle.text = "휴지통 보기"
            }
        }

        lifecycleScope.launch {
            viewModel.displayItems.collect { itmes ->
                mainAdapter.submitList(itmes)
            }
        }
    }

    private fun setupBtn() {
        binding.btnToggle.setOnClickListener {
            viewModel.switchToTrashOrNormal()
        }
    }
}