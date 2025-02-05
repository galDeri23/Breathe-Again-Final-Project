package com.example.finalproject_breathe_again.ui.craving.story

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.finalproject_breathe_again.R
import com.example.finalproject_breathe_again.databinding.ItemStoryBinding


class StoryAdapter(
    private val onItemClick: (Story) -> Unit
) : RecyclerView.Adapter<StoryAdapter.StoryViewHolder>() {

    private val stories: MutableList<Story> = mutableListOf()

    inner class StoryViewHolder(private val binding: ItemStoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(story: Story) {
            binding.storyTitle.text = story.title
            binding.storyAuthor.text = "By: ${story.author}"
            binding.storyDate.text = story.date
            binding.movieLBLOverview.text = story.overview
            ImageLoader.getInstance().loadImage(
                R.drawable.ic_stories,
                binding.storyIMGPoster
            )


            if (story.isExpanded) {
                binding.movieLBLOverview.maxLines = Int.MAX_VALUE
            } else {
                binding.movieLBLOverview.maxLines = 3
            }


            binding.root.setOnClickListener {
                story.isExpanded = !story.isExpanded
                notifyItemChanged(adapterPosition)
                onItemClick(story)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoryViewHolder {
        val binding = ItemStoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return StoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StoryViewHolder, position: Int) {
        holder.bind(stories[position])
    }

    override fun getItemCount(): Int = stories.size

    fun updateStories(newStories: List<Story>) {
        stories.clear()
        stories.addAll(newStories)
        notifyDataSetChanged() // Refresh entire RecyclerView
    }
}
