package dev.jx.ec04.recycler

import android.content.Intent
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.squareup.picasso.Picasso
import dev.jx.ec04.R
import dev.jx.ec04.activity.PostDetailsActivity
import dev.jx.ec04.databinding.CardPostBinding
import dev.jx.ec04.entity.Post

class PostCardViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    private val binding = CardPostBinding.bind(view)
    private lateinit var post: Post

    fun bind(post: Post) {
        this.post = post
        Picasso.get().load(post.imageUrl).into(binding.petImageView)
        binding.petNameTextView.text = post.petName
        binding.petSexTextView.text = post.petSex
        binding.petStageTextView.text = post.petStage
        binding.petStoryTextView.text = post.petStory
        binding.detailsBtn.setOnClickListener {
            val intent = Intent(itemView.context, PostDetailsActivity::class.java).apply {
                putExtra("post", post)
            }
            itemView.context.startActivity(intent)
        }
    }

    fun setItemListener(listener: PostCardAdapter.OnItemListener) {
        binding.root.isClickable = true
        binding.root.isFocusable = true
        binding.root.setOnClickListener {
            listener.onItemLongClick(post)
        }
    }
}