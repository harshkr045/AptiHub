package com.harshkr.aptihub

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.harshkr.aptihub.databinding.ItemTopicBinding

class TopicAdapter(
    private var topics: List<Topic>,
    private val onTopicClick: (Topic) -> Unit
) : RecyclerView.Adapter<TopicAdapter.TopicViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TopicViewHolder {
        val binding = ItemTopicBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TopicViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TopicViewHolder, position: Int) {
        val topic = topics[position]
        holder.bind(topic)
    }

    override fun getItemCount(): Int = topics.size

    fun updateTopics(newTopics: List<Topic>) {
        topics = newTopics
        notifyDataSetChanged()
    }

    inner class TopicViewHolder(private val binding: ItemTopicBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(topic: Topic) {
            binding.topicTitle.text = topic.title
            binding.topicIcon.setImageResource(topic.iconResource)
            // Set the click listener on the entire item view.
            // This single listener correctly handles navigation.
            binding.root.setOnClickListener {
                onTopicClick(topic)
            }
        }
    }
}
