package com.jg.android_recyclerview.ui

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.jg.android_recyclerview.databinding.ActivityListBinding
import com.jg.android_recyclerview.databinding.ActivityMainBinding
import com.jg.android_recyclerview.model.ItemType
import com.jg.android_recyclerview.model.ViewMode
import com.jg.android_recyclerview.ui.adapter.MainAdapter
import com.jg.android_recyclerview.viewmodel.StateFlowViewModel
import kotlinx.coroutines.launch

class ListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityListBinding
    private val mainAdapter = MainAdapter()
    private val viewModel: StateFlowViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setRecyclerView()
        observeViewModel()
        setupBtn()
    }

    private fun setRecyclerView() {
        binding.recyclerView.adapter = mainAdapter

        mainAdapter.setOnItemClickListener { item ->
            when (item.type) {
                ItemType.NORMAL -> viewModel.moveToTrash(item)
                ItemType.TRASH -> viewModel.restoreItem(item)
            }
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.currentMode.collect { mode ->
                binding.btnToggle.text = when (mode) {
                    ViewMode.NORMAL -> "휴지통 보기"
                    ViewMode.TRASH -> "목록 보기"
                }
            }
        }

        lifecycleScope.launch {
            viewModel.displayItems.collect {
                mainAdapter.submitList(it)
            }
        }
    }

    private fun setupBtn() {
        binding.btnToggle.setOnClickListener {
            viewModel.switchToTrashOrNormal()
        }
    }


}