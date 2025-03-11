package com.example.finalproject_breathe_again.ui.craving.breathing

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.finalproject_breathe_again.databinding.FragmentBreathingBinding

class BreathingFragment : Fragment() {

    private var _binding: FragmentBreathingBinding? = null
    private val binding get() = _binding!!

    private var isBreathing = false
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentBreathingBinding.inflate(inflater, container, false)

        setupUI()
        return binding.root
    }

    private fun setupUI() {

        binding.btnStartBreathing.setOnClickListener {
            if (!isBreathing) {
                startBreathingExercise()
            }
        }


        binding.btnStopBreathing.setOnClickListener {
            stopBreathingExercise()
        }
    }

    private fun startBreathingExercise() {
        isBreathing = true
        binding.lottieBreathing.playAnimation()

    }

    private fun stopBreathingExercise() {
        isBreathing = false
        binding.lottieBreathing.pauseAnimation()
        handler.removeCallbacksAndMessages(null)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        stopBreathingExercise()
        _binding = null
    }
}
