package com.jg.android_recyclerview.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.jg.android_recyclerview.databinding.FragmentTrashBinding
import com.jg.android_recyclerview.ui.adapter.MainAdapter
import com.jg.android_recyclerview.ui.base.BaseFragment
import kotlinx.coroutines.launch

/**
 * 휴지통 목록
 */
class TrashFragment : BaseFragment() {

    private var _binding: FragmentTrashBinding? = null
    private val binding get() = _binding!!
    private val mainAdapter = MainAdapter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentTrashBinding.inflate(inflater, container, false)
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

        // 중복 방지
        var isProcessing = false

        mainAdapter.setOnItemClickListener { item ->
            if (!isProcessing) {
                isProcessing = true
                viewModel.restoreItem(item)
                isProcessing = false
            }
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