package com.jg.android_recyclerview.ui.base

import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.jg.android_recyclerview.viewmodel.StateFlowViewModel

abstract class BaseFragment : Fragment() {
    protected val viewModel: StateFlowViewModel by activityViewModels()
}
