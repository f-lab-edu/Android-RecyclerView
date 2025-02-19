package com.jg.android_recyclerview

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.jg.android_recyclerview.databinding.FragmentSecondBinding
import com.jg.android_recyclerview.ui.adapter.MainAdapter
import com.jg.android_recyclerview.viewmodel.StateFlowViewModel
import kotlinx.coroutines.launch

/**
 * 휴지통 목록
 */
class SecondFragment : Fragment() {

    private var _binding: FragmentSecondBinding? = null
    private val binding get() = _binding!!
    private val mainAdapter = MainAdapter()
    private val viewModel: StateFlowViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentSecondBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupButton()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        binding.recyclerViewTrash.adapter = mainAdapter

        mainAdapter.setOnItemClickListener { item ->
            // TODO 복구 시 리스트 한개가 더 생김 처리 필요
            viewModel.restoreItem(item)
        }
    }

    private fun setupButton() {
        binding.btnShowNormal.setOnClickListener {
            viewModel.switchToTrashOrNormal()
            findNavController().popBackStack()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.displayItems.collect { items ->
                mainAdapter.submitList(items)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}