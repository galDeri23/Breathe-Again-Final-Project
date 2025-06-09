package com.example.finalproject_breathe_again.ui.chat

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.finalproject_breathe_again.databinding.FragmentChatBotBinding

class ChatBotFragment : Fragment() {

    private var _binding: FragmentChatBotBinding? = null
    private val binding get() = _binding!!

    private val messages = mutableListOf<ChatMessage>()
    private lateinit var adapter: ChatAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBotBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Init RecyclerView with LayoutManager
        adapter = ChatAdapter(messages)
        binding.chatRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.chatRecyclerView.adapter = adapter

        // Greeting message
        addMessage("Hi! How can I support you today? 😊", isUser = false)

        // Send message button
        binding.btnSend.setOnClickListener {
            val userMessage = binding.etMessage.text.toString().trim()
            if (userMessage.isNotEmpty()) {
                addMessage(userMessage, isUser = true)
                binding.etMessage.setText("")
                respondToMessage(userMessage)
            }
        }
    }

    private fun addMessage(text: String, isUser: Boolean) {
        messages.add(ChatMessage(text, isUser))
        adapter.notifyItemInserted(messages.size - 1)
        binding.chatRecyclerView.scrollToPosition(messages.size - 1)
    }

    private fun respondToMessage(message: String) {
        val response = when {
            message.contains("how are you", ignoreCase = true) -> "I'm here and ready to help you 💙"
            message.contains("thank", ignoreCase = true) -> "You're very welcome 😊"
            message.contains("want to smoke", ignoreCase = true) -> "Take a breath. You’ve come so far! Try a breathing exercise 💨"
            message.contains("help", ignoreCase = true) -> "I'm right here with you. You got this! 💪"
            message.contains("hi") || message.contains("hello") -> "Hi there! Ready to breathe again today? 🌞"
            // רגשות
            message.contains("sad") || message.contains("depressed") -> "It's okay to feel down. You're not alone. Maybe try a short breathing exercise? 💨"
            message.contains("anxious") || message.contains("nervous") || message.contains("stressed") -> "Deep breaths help a lot. Let's calm the storm together. 💙"
            message.contains("bored") -> "Staying busy helps with cravings. How about reading a short inspiring story? 📖"

            // מוטיבציה
            message.contains("why quit") || message.contains("is it worth it") -> "Absolutely worth it! Your lungs, heart, and wallet all thank you 🫁❤️💰"
            message.contains("benefits") -> "Less coughing, better breathing, more energy, and saving money. That's just the start!"

            // גמילה / פיתוי
            message.contains("craving") || message.contains("tempted") || message.contains("urge") -> "Cravings pass. Drink water, go for a walk, or breathe for 2 minutes 🧘‍♂️"
            message.contains("relapse") -> "Slipping is part of the process. Don't give up — today is a new day 💪"

            // הודעות חיוביות
            message.contains("thank") || message.contains("thanks") -> "You're very welcome! 😊"
            message.contains("i did it") || message.contains("success") || message.contains("good job") -> "👏 Amazing! Every smoke-free moment is a victory."

            else -> "I'm still learning 🧠. Try asking about cravings, breathing, or staying motivated!"
        }

        Handler(Looper.getMainLooper()).postDelayed({
            addMessage(response, isUser = false)
        }, 600)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
