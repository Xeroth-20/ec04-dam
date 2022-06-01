package dev.jx.ec04.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import dev.jx.ec04.R
import dev.jx.ec04.databinding.ActivityPostDetailsBinding
import dev.jx.ec04.entity.Post
import dev.jx.ec04.entity.User
import dev.jx.ec04.util.UserUtils

class PostDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPostDetailsBinding
    private lateinit var usersReference: DatabaseReference
    private var post: Post? = null
    private var author: User? = null
    private var currentUser: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPostDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        currentUser = UserUtils.getUserFromSharedPreferences(this)
        usersReference = Firebase.database.getReference(getString(R.string.users_reference))

        post = intent.extras?.getParcelable("post")

        setupView()
    }

    private fun setupView() {
        binding.topAppBar.setNavigationOnClickListener {
            finish()
        }

        post?.let { post ->
            Picasso.get().load(post.imageUrl).into(binding.petImageView)
            binding.petNameTextView.text = post.petName
            binding.locationTextView.text =
                getString(R.string.post_details_location, post.state, post.city)
            binding.petAgeTextView.text = getString(R.string.post_details_pet_age, post.petAge)
            binding.petSexTextView.text = post.petSex
            binding.petWeightTextView.text =
                getString(R.string.post_details_pet_weight, post.petWeight.toString())
            binding.petStoryTextView.text = post.petStory

            usersReference.child(post.authorId!!).get()
                .addOnSuccessListener { dataSnapshot ->
                    Log.d(TAG, "Retrieving author data: success")
                    author = dataSnapshot.getValue(User::class.java)
                    binding.ownerFullNameTextView.text = "${author!!.firstname} ${author!!.lastname}"
                    if (author!!.email == currentUser!!.email) {
                        binding.contactMeBtn.isEnabled = false
                    }
                }.addOnFailureListener { ex ->
                    Log.w(TAG, "Retrieving author data: failure", ex)
                }

            binding.contactMeBtn.setOnClickListener { callOwner(post.contact!!) }
        }
    }

    private fun callOwner(phoneNumber: String) {
        val callIntent = Intent(Intent.ACTION_DIAL)
            .setData(Uri.parse("tel:$phoneNumber"))
        startActivity(callIntent)
    }

    companion object {
        private const val TAG = "PostDetailsActivity"
    }
}