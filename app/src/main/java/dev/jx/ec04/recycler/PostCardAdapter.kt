package dev.jx.ec04.recycler

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import dev.jx.ec04.R
import dev.jx.ec04.entity.Post

class PostCardAdapter(private val posts: List<Post>) : RecyclerView.Adapter<PostCardViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostCardViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.card_post, parent, false)
        return PostCardViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostCardViewHolder, position: Int) {
        val post = posts[position]
        holder.bind(post)
    }

    override fun getItemCount(): Int = posts.size
}
