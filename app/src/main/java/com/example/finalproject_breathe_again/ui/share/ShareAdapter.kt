package com.example.finalproject_breathe_again.ui.share

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.finalproject_breathe_again.databinding.ItemShareBinding

class ShareAdapter : ListAdapter<ShareItem, ShareAdapter.ShareViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShareViewHolder {
        Log.d("ShareAdapter", "Creating new ViewHolder")
        val binding = ItemShareBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ShareViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ShareViewHolder, position: Int) {
        val item = getItem(position)
        Log.d("ShareAdapter", "Binding item at position $position: $item")
        holder.bind(item)
    }

    class ShareViewHolder(private val binding: ItemShareBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ShareItem) {
            binding.tvUsername.text = item.username
            binding.tvDetails.text = "${item.daysSmokeFree} Days Smoke-Free\n\$${item.moneySaved} Saved"
            Log.d("ShareViewHolder", "Bound item: $item")
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<ShareItem>() {
        override fun areItemsTheSame(oldItem: ShareItem, newItem: ShareItem): Boolean {
            return oldItem.username == newItem.username
        }

        override fun areContentsTheSame(oldItem: ShareItem, newItem: ShareItem): Boolean {
            return oldItem == newItem
        }
    }
}
