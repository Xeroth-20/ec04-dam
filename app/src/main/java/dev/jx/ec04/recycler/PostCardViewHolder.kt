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
import dev.jx.ec04.entity.Post

class PostCardViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    private val petImageView = itemView.findViewById<ImageView>(R.id.pet_image_view)
    private val petNameTextView = itemView.findViewById<TextView>(R.id.pet_name_text_view)
    private val petSexTextView = itemView.findViewById<TextView>(R.id.pet_sex_text_view)
    private val petStageTextView = itemView.findViewById<TextView>(R.id.pet_stage_text_view)
    private val petStoryTextView = itemView.findViewById<TextView>(R.id.pet_story_text_view)
    private val detailsBtn = itemView.findViewById<MaterialButton>(R.id.details_btn)

    fun bind(post: Post) {
        Picasso.get().load(post.imageUrl).into(petImageView)
        petNameTextView.text = post.petName
        petSexTextView.text = post.petSex
        petStageTextView.text = post.petStage
        petStoryTextView.text = post.petStory
        detailsBtn.setOnClickListener {
            val intent = Intent(itemView.context, PostDetailsActivity::class.java).apply {
                putExtra("post", post)
            }
            itemView.context.startActivity(intent)
        }
    }
}