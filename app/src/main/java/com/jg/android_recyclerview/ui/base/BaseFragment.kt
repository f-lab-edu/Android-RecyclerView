package com.jg.android_recyclerview.ui.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.jg.android_recyclerview.ui.adapter.ListSampleAdapter
import com.jg.android_recyclerview.viewmodel.ListViewModel
import kotlinx.coroutines.launch
import androidx.recyclerview.widget.SimpleItemAnimator

abstract class BaseFragment<VB: ViewBinding>(
    private val bidingInflater: (LayoutInflater) -> VB
) : Fragment() {

    private var _binding: VB? = null
    protected val binding get() = _binding!!

    protected val viewModel: ListViewModel by activityViewModels()
    protected val listSampleAdapter: ListSampleAdapter by lazy { ListSampleAdapter() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = bidingInflater.invoke(inflater)
        _binding?.let {
            return it.root
        } ?: throw IllegalArgumentException("Binding variable is null")
    }

    protected abstract fun getRecyclerView(): RecyclerView
    protected abstract fun setupButton()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeDisplayItems()
        setupButton()
    }

    private fun setupRecyclerView() {
        val recyclerView = getRecyclerView()
        recyclerView.adapter = listSampleAdapter

        val animator = recyclerView.itemAnimator
        if (animator is SimpleItemAnimator) {
            animator.supportsChangeAnimations = false
        }
    }

    private fun observeDisplayItems() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.displayItems.collect { items ->
                    listSampleAdapter.submitList(items)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
