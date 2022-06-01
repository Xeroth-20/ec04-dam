package dev.jx.ec04.recycler

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import dev.jx.ec04.R
import dev.jx.ec04.entity.Post

class PostCardAdapter(private var posts: MutableList<Post>, private var listener: OnItemListener?) :
    RecyclerView.Adapter<PostCardViewHolder>() {

    fun setItems(items: MutableList<Post>) {
        posts = items
        notifyDataSetChanged()
    }

    fun setOnItemListener(listener: OnItemListener?) {
        this.listener = listener
    }

    fun deleteItem(post: Post) {
        val index = posts.indexOf(post)
        posts.remove(post)
        notifyItemRemoved(index)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostCardViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.card_post, parent, false)
        return PostCardViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostCardViewHolder, position: Int) {
        val post = posts[position]
        holder.bind(post)
        if (listener != null) {
            holder.setItemListener(listener!!)
        }
    }

    override fun getItemCount(): Int = posts.size

    interface OnItemListener {
        fun onItemLongClick(post: Post)
    }
}
