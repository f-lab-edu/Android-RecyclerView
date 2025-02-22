package com.jg.android_recyclerview.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import com.jg.android_recyclerview.R
import com.jg.android_recyclerview.databinding.FragmentNormalBinding
import com.jg.android_recyclerview.ui.base.BaseFragment

/**
 * 목록 리스트
 */
class NormalFragment : BaseFragment<FragmentNormalBinding>(
    FragmentNormalBinding::inflate
) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        listSampleAdapter.setOnItemClickListener { item ->
            viewModel.moveToTrash(item)
        }
    }

    override fun getRecyclerView() = binding.recyclerViewNormal

    override fun setupButton() {
        binding.btnShowTrash.setOnClickListener {
            viewModel.switchToTrashOrNormal()
            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
        }
    }
}