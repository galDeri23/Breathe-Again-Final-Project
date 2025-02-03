package com.example.finalproject_breathe_again.ui.share

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.finalproject_breathe_again.databinding.FragmentShareBinding

class ShareFragment : Fragment() {

    private var _binding: FragmentShareBinding? = null
    private val binding get() = _binding!!
    private lateinit var shareViewModel: ShareViewModel
    private lateinit var shareAdapter: ShareAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d("ShareFragment", "onCreateView: Starting initialization")

        _binding = FragmentShareBinding.inflate(inflater, container, false)
        val root: View = binding.root

        setupRecyclerView()

        shareViewModel = ViewModelProvider(this)[ShareViewModel::class.java]


        shareViewModel.shareItems.observe(viewLifecycleOwner) { items ->
            Log.d("ShareFragment", "Observed data change: $items")
            if (items.isEmpty()) {
                binding.recyclerView.visibility = View.GONE
                binding.tvNoData.visibility = View.VISIBLE
            } else {
                binding.recyclerView.visibility = View.VISIBLE
                binding.tvNoData.visibility = View.GONE
                shareAdapter.submitList(items)
            }
        }

        return root
    }

    private fun setupRecyclerView() {
        Log.d("ShareFragment", "Setting up RecyclerView")
        shareAdapter = ShareAdapter()
        binding.recyclerView.apply {
            adapter = shareAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d("ShareFragment", "onDestroyView: Cleaning up binding")
        _binding = null
    }
}
