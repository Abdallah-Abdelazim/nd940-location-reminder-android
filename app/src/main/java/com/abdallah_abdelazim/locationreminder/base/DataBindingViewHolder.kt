package com.abdallah_abdelazim.locationreminder.base

import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import com.abdallah_abdelazim.locationreminder.BR

/**
 * View Holder for the Recycler View to bind the data item to the UI
 */
class DataBindingViewHolder<T>(private val binding: ViewDataBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(item: T) {
        binding.setVariable(BR.item, item)
        binding.executePendingBindings()
    }
}