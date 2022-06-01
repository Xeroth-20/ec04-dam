package dev.jx.ec04.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import dev.jx.ec04.R
import dev.jx.ec04.databinding.ActivitySearchResultBinding
import dev.jx.ec04.entity.Post
import dev.jx.ec04.recycler.PostCardAdapter
import dev.jx.ec04.recycler.PostCardItemDecoration
import java.lang.IllegalArgumentException

class SearchResultActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchResultBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var postsReference: DatabaseReference
    private lateinit var usersPostsReference: DatabaseReference
    private var searchFor: SearchFor? = null
    private var payload: String? = null
    private val postCardAdapter = PostCardAdapter(mutableListOf(), null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        postsReference = Firebase.database.getReference(getString(R.string.posts_reference))
        usersPostsReference =
            Firebase.database.getReference(getString(R.string.users_posts_reference))

        intent.extras?.let {
            try {
                searchFor = SearchFor.valueOf(it.getString("search_for") ?: "")
            } catch (ex: IllegalArgumentException) {
                Log.w(TAG, ex.message, ex)
            }
            payload = it.getString("payload")
        }

        setupView()
    }

    private fun setupView() {
        val postCardHorizontalSpacing =
            resources.getDimensionPixelSize(R.dimen.app_horizontal_spacing)
        val postCardBottomSpacing =
            resources.getDimensionPixelSize(R.dimen.card_post_bottom_spacing)

        binding.noResultFoundFrameLayout.visibility = View.INVISIBLE

        binding.topAppBar.setNavigationOnClickListener {
            finish()
        }

        binding.cardPostRecyclerView.apply {
            setHasFixedSize(true)
            addItemDecoration(
                PostCardItemDecoration(
                    postCardHorizontalSpacing,
                    postCardBottomSpacing
                )
            )
            layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            adapter = postCardAdapter
        }

        when (searchFor) {
            SearchFor.CUSTOM -> {
                searchForPets(payload)
            }
            SearchFor.DOGS -> {
                searchForAnimalPosts("Perro", getString(R.string.search_result_title_dogs))
            }
            SearchFor.CATS -> {
                searchForAnimalPosts("Gato", getString(R.string.search_result_title_cats))
            }
            SearchFor.RABBITS -> {
                searchForAnimalPosts("Conejo", getString(R.string.search_result_title_rabbits))
            }
            SearchFor.HAMSTERS -> {
                searchForAnimalPosts("Hamster", getString(R.string.search_result_title_hamsters))
            }
            SearchFor.BIRDS -> {
                searchForAnimalPosts("Ave", getString(R.string.search_result_title_birds))
            }
            SearchFor.MY_POSTS -> {
                searchForMyPosts()
            }
            else -> {
                Log.w(TAG, "search_for extra is null")
            }
        }
    }

    private fun setSearchResult(posts: List<Post>, myPosts: Boolean = false) {
        if (posts.isNotEmpty()) {
            val listener = if (myPosts) object : PostCardAdapter.OnItemListener {
                override fun onItemLongClick(post: Post) {
                    openPostMenu(post)
                }
            } else null
            binding.noResultFoundFrameLayout.visibility = View.GONE
            binding.recyclerScrollView.visibility = View.VISIBLE
            postCardAdapter.setItems(posts.toMutableList())
            postCardAdapter.setOnItemListener(listener)
        } else {
            binding.recyclerScrollView.visibility = View.GONE
            binding.noResultFoundFrameLayout.visibility = View.VISIBLE
        }
    }

    private fun searchForPets(search: String?) {
        search?.let {
            postsReference.orderByChild("petName").equalTo(search).get()
                .addOnSuccessListener { dataSnapshot ->
                    Log.d(TAG, "Retrieving posts data for $search query: success")
                    val posts = dataSnapshot.children.map { it.getValue(Post::class.java)!! }
                    setSearchResult(posts)
                }.addOnFailureListener { ex ->
                    Log.w(TAG, "Retrieving posts data for $search query: failure", ex)
                }
        }
    }

    private fun searchForAnimalPosts(animal: String, title: String) {
        binding.topAppBar.title = title
        postsReference.orderByChild("petAnimal").equalTo(animal).get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "Retrieving $animal posts data: success")
                    val posts = task.result.children
                        .map { it.getValue(Post::class.java)!! }
                    setSearchResult(posts)
                } else {
                    Log.d(TAG, "Retrieving $animal posts data: failure", task.exception)
                }
            }
    }

    private fun searchForMyPosts() {
        binding.topAppBar.title = getString(R.string.search_result_title_my_posts)
        usersPostsReference.child(auth.currentUser!!.uid).orderByChild("createdAt").get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "Retrieving user posts data: success")
                    val posts = task.result.children
                        .map { it.getValue(Post::class.java)!! }
                        .reversed()
                    setSearchResult(posts, true)
                } else {
                    Log.w(TAG, "Retrieving user posts data: failure", task.exception)
                }
            }
    }

    private fun openPostMenu(post: Post) {
        MaterialAlertDialogBuilder(this)
            .setItems(arrayOf("Editar publicación", "Borrar publicación")) { dialog, which ->
                when (which) {
                    0 -> navigateToEditPost(post)
                    1 -> deletePost(post)
                    else -> dialog.dismiss()
                }
            }.show()
    }

    private fun navigateToEditPost(post: Post) {
        val intent = Intent(this, CreatePostActivity::class.java)
            .putExtra("isEditMode", true)
            .putExtra("post", post)
        startActivity(intent)
    }

    private fun deletePost(post: Post) {
        val databaseReference = Firebase.database.reference
        val postsPath = getString(R.string.posts_reference)
        val userPostsPath = "${getString(R.string.users_posts_reference)}/${auth.currentUser!!.uid}"
        val postData = mapOf(
            "$postsPath/${post.id!!}" to null,
            "$userPostsPath/${post.id!!}" to null
        )

        databaseReference.updateChildren(postData)
            .addOnSuccessListener {
                postCardAdapter.deleteItem(post)
            }.addOnFailureListener { ex ->
                Log.w(TAG, "Deleting post: failure", ex)
            }
    }

    companion object {
        private const val TAG = "SearchResultActivity"
    }

    enum class SearchFor {
        CUSTOM,
        DOGS,
        CATS,
        RABBITS,
        HAMSTERS,
        BIRDS,
        MY_POSTS,
    }
}