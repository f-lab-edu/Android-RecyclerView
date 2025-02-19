package com.jg.android_recyclerview

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.jg.android_recyclerview.databinding.FragmentFirstBinding
import com.jg.android_recyclerview.ui.adapter.MainAdapter
import com.jg.android_recyclerview.viewmodel.StateFlowViewModel
import kotlinx.coroutines.launch

/**
 * 목록 리스트
 */
class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null
    private val binding get() = _binding!!
    private val mainAdapter = MainAdapter()
    private val viewModel: StateFlowViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentFirstBinding.inflate(inflater, container, false)
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
            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
        }
    }

    private fun setupButton() {
        binding.btnShowTrash.setOnClickListener {
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