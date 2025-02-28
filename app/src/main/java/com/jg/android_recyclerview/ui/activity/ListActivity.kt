package com.jg.android_recyclerview.ui.activity

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.jg.android_recyclerview.databinding.ActivityListBinding
import com.jg.android_recyclerview.model.ViewMode
import com.jg.android_recyclerview.ui.adapter.ListSampleAdapter
import com.jg.android_recyclerview.viewmodel.ListViewModel
import kotlinx.coroutines.launch

class ListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityListBinding
    private val listSampleAdapter = ListSampleAdapter()
    private val viewModel: ListViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 초기 설정 메서드들 호출
        setRecyclerView()  
        observeViewModel() 
        setupBtn()       
    }

    // RecyclerView 초기 설정
    private fun setRecyclerView() {
        binding.recyclerView.adapter = listSampleAdapter

        listSampleAdapter.setOnItemClickListener { item ->
            viewModel.switchItemType(type = item.type, item = item)
        }
    }

    // ViewModel의 데이터 변화를 관찰
    private fun observeViewModel() {
        // 현재 모드(NORMAL/TRASH)를 관찰하여 버튼 텍스트 업데이트
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.currentMode.collect { mode ->
                    binding.btnToggle.text = when (mode) {
                        ViewMode.NORMAL -> "휴지통 보기" // 일반 모드일 때 버튼 텍스트
                        ViewMode.TRASH -> "목록 보기"   // 휴지통 모드일 때 버튼 텍스트
                    }
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.displayItems.collect {
                    listSampleAdapter.submitList(it) // 새로운 리스트로 UI 업데이트
                }
            }
        }
    }

    private fun setupBtn() {
        // 버튼 클릭시 일반/휴지통 모드 전환
        binding.btnToggle.setOnClickListener {
            viewModel.switchToTrashOrNormal()
        }
    }
}