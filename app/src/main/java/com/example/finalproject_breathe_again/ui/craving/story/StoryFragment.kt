package com.example.finalproject_breathe_again.ui.craving.story

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.finalproject_breathe_again.databinding.FragmentStoryBinding
import com.google.firebase.firestore.FirebaseFirestore

class StoryFragment : Fragment() {

    private var _binding: FragmentStoryBinding? = null
    private val binding get() = _binding!!

    private val firestore = FirebaseFirestore.getInstance()
    private lateinit var storyAdapter: StoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        setupRecyclerView()
        checkAndSaveStories()
        loadStoriesFromFirestore()
    }

    private fun setupRecyclerView() {
        storyAdapter = StoryAdapter { story ->
            Toast.makeText(requireContext(), "Clicked: ${story.title}", Toast.LENGTH_SHORT).show()
        }
        binding.storyRVList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = storyAdapter
        }
    }


    private fun checkAndSaveStories() {
        val defaultStories = DataStory.generateStoryList()

        firestore.collection("stories")
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (querySnapshot.isEmpty) {
                    saveStoriesToFirestore(defaultStories)
                }
            }
            .addOnFailureListener { exception ->
                Log.e("StoryFragment", "Error checking stories: ${exception.message}")
            }
    }


    private fun saveStoriesToFirestore(stories: List<Story>) {
        val batch = firestore.batch()
        val collectionRef = firestore.collection("stories")

        stories.forEach { story ->
            val documentRef = collectionRef.document(story.id)
            batch.set(documentRef, story)
        }

        batch.commit()
            .addOnSuccessListener {
                Log.d("StoryFragment", "Stories saved successfully!")
            }
            .addOnFailureListener { exception ->
                Log.e("StoryFragment", "Error saving stories: ${exception.message}")
            }
    }


    private fun loadStoriesFromFirestore() {
        firestore.collection("stories")
            .get()
            .addOnSuccessListener { querySnapshot ->
                val storyList = querySnapshot.toObjects(Story::class.java)
                storyAdapter.updateStories(storyList)
            }
            .addOnFailureListener { exception ->
                Log.e("StoryFragment", "Error loading stories: ${exception.message}")
                Toast.makeText(
                    requireContext(),
                    "Failed to load stories. Please try again.",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
