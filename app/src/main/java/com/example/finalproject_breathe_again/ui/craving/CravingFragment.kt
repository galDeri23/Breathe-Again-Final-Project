package com.example.finalproject_breathe_again.ui.craving

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.finalproject_breathe_again.databinding.FragmentCravingBinding


class CravingFragment : Fragment() {

    private var _binding: FragmentCravingBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val cravingViewModel =
            ViewModelProvider(this).get(CravingViewModel::class.java)

        _binding = FragmentCravingBinding.inflate(inflater, container, false)
        val root: View = binding.root

      //  val textView: TextView = binding.textCraving
      //  cravingViewModel.text.observe(viewLifecycleOwner) {
         //   textView.text = it
       // }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}