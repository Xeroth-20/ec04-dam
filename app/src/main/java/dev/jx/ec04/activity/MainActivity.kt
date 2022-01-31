package dev.jx.ec04.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import dev.jx.ec04.R
import dev.jx.ec04.databinding.ActivityMainBinding
import dev.jx.ec04.entity.Post
import dev.jx.ec04.entity.User
import dev.jx.ec04.recycler.PostCardAdapter
import dev.jx.ec04.recycler.PostCardItemDecoration

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var usersReference: DatabaseReference
    private lateinit var postsReference: DatabaseReference
    private lateinit var user: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        usersReference = Firebase.database.getReference(getString(R.string.users_reference))
        postsReference = Firebase.database.getReference(getString(R.string.posts_reference))

        setupView()
    }

    private fun setupView() {
        val postCardHorizontalSpacing =
            resources.getDimensionPixelSize(R.dimen.app_horizontal_spacing)
        val postCardBottomSpacing =
            resources.getDimensionPixelSize(R.dimen.card_post_bottom_spacing)

        binding.topAppBar.setNavigationOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }

        binding.navigationDrawer.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_item_home -> {
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                R.id.menu_item_my_posts -> {
                    navigateToSearchResult(SearchResultActivity.SearchFor.MY_POSTS)
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                    false
                }
                R.id.menu_item_logout -> {
                    auth.signOut()
                    navigateToSignIn()
                    false
                }
                else -> {
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                    false
                }
            }
        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            getPosts { posts ->
                posts?.let {
                    binding.cardPostRecyclerView.swapAdapter(PostCardAdapter(posts), true)
                }
                binding.swipeRefreshLayout.isRefreshing = false
            }
        }

        binding.searchPetsInputLayout.setEndIconOnClickListener {
            val search = binding.searchPetsInputEditText.text.toString()
            if (!search.isNullOrBlank()) {
                navigateToSearchResult(SearchResultActivity.SearchFor.CUSTOM, search)
            }
        }

        binding.searchPetsInputEditText.addTextChangedListener {
            if (!it.toString().isNullOrBlank()) {
                binding.searchPetsInputLayout.endIconDrawable =
                    getDrawable(R.drawable.ic_search_pets)
            } else {
                binding.searchPetsInputLayout.endIconDrawable = null
            }
        }

        binding.searchForDogsBtn.setOnClickListener {
            navigateToSearchResult(SearchResultActivity.SearchFor.DOGS)
        }

        binding.searchForCatsBtn.setOnClickListener {
            navigateToSearchResult(SearchResultActivity.SearchFor.CATS)
        }

        binding.searchForRabbitsBtn.setOnClickListener {
            navigateToSearchResult(SearchResultActivity.SearchFor.RABBITS)
        }

        binding.searchForHamsterBtn.setOnClickListener {
            navigateToSearchResult(SearchResultActivity.SearchFor.HAMSTERS)
        }

        binding.searchForBirdsBtn.setOnClickListener {
            navigateToSearchResult(SearchResultActivity.SearchFor.BIRDS)
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
        }

        binding.createPostFab.setOnClickListener {
            navigateToCreatePost()
        }

        usersReference.child(auth.currentUser!!.uid).get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "Retrieving user data: success")
                    user = task.result.getValue(User::class.java)!!
                    updateUserInfo()
                } else {
                    Log.w(TAG, "Retrieving user data: failure")
                }
            }

        getPosts { posts ->
            posts?.let {
                binding.cardPostRecyclerView.adapter = PostCardAdapter(posts)
            }
        }
    }

    private fun getPosts(cb: (posts: List<Post>?) -> Unit) {
        postsReference.orderByChild("createdAt").limitToFirst(10).get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "Retrieving posts data: success")
                    val posts = task.result.children
                        .map { it.getValue(Post::class.java)!! }
                        .reversed()
                    cb(posts)
                } else {
                    Log.w(TAG, "Retrieving posts data: failure", task.exception)
                    cb(null)
                }
            }
    }

    private fun updateUserInfo() {
        val userFullNameTextView = findViewById<TextView>(R.id.user_full_name_text_view)
        val userEmailTextView = findViewById<TextView>(R.id.user_email_text_view)
        userFullNameTextView.text = "${user.firstname} ${user.lastname}"
        userEmailTextView.text = user.email
        binding.welcomeUserTextView.text =
            getString(R.string.main_welcome_user, "${user.firstname}")
    }

    private fun navigateToSearchResult(
        searchFor: SearchResultActivity.SearchFor,
        payload: String? = null
    ) {
        val intent = Intent(this, SearchResultActivity::class.java).apply {
            putExtra("search_for", searchFor.name)
            putExtra("payload", payload?.trim())
        }
        startActivity(intent)
    }

    private fun navigateToSignIn() {
        val intent = Intent(this, SignInActivity::class.java).apply {
            putExtra("show_splash_screen", false)
        }
        startActivity(intent)
        finish()
    }

    private fun navigateToCreatePost() {
        val intent = Intent(this, CreatePostActivity::class.java)
        startActivity(intent)
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}