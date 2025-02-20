package com.jg.android_recyclerview.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.jg.android_recyclerview.R
import com.jg.android_recyclerview.databinding.FragmentNormalBinding
import com.jg.android_recyclerview.ui.adapter.MainAdapter
import com.jg.android_recyclerview.ui.base.BaseFragment
import kotlinx.coroutines.launch

/**
 * 목록 리스트
 */
class NormalFragment : BaseFragment() {

    private var _binding: FragmentNormalBinding? = null
    private val binding get() = _binding!!
    private val mainAdapter = MainAdapter()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentNormalBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupButton()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        binding.recyclerViewNormal.adapter = mainAdapter

        mainAdapter.setOnItemClickListener { item ->
            viewModel.moveToTrash(item)
        }
    }

    private fun setupButton() {
        binding.btnShowTrash.setOnClickListener {
            viewModel.switchToTrashOrNormal()
            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
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