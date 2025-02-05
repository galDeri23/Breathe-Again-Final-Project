package com.example.finalproject_breathe_again.ui.craving

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.finalproject_breathe_again.R
import com.example.finalproject_breathe_again.databinding.FragmentCravingBinding

class CravingFragment : Fragment() {

    private var _binding: FragmentCravingBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCravingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set click listeners for buttons
        binding.btnStories.setOnClickListener {
            findNavController().navigate(R.id.action_cravingFragment_to_storyFragment)
        }

        //binding.buttonBreathing.setOnClickListener {
          //  findNavController().navigate(R.id.action_cravingFragment_to_breathingFragment)
       // }

       // binding.buttonChallenge.setOnClickListener {
          //  findNavController().navigate(R.id.action_cravingFragment_to_challengeFragment)
      //  }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
