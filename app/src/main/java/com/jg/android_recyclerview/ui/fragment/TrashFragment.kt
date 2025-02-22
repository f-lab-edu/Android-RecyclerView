package com.jg.android_recyclerview.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import com.jg.android_recyclerview.databinding.FragmentTrashBinding
import com.jg.android_recyclerview.ui.base.BaseFragment

/**
 * 휴지통 목록
 */
class TrashFragment : BaseFragment<FragmentTrashBinding>(
    FragmentTrashBinding::inflate
) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        listSampleAdapter.setOnItemClickListener { item ->
            viewModel.restoreItem(item)
        }
    }

    override fun getRecyclerView() = binding.recyclerViewTrash

    override fun setupButton() {
        binding.btnShowNormal.setOnClickListener {
            viewModel.switchToTrashOrNormal()
            findNavController().popBackStack()
        }
    }
}