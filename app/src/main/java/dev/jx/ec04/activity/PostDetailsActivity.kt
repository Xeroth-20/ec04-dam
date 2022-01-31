package dev.jx.ec04.activity

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

class PostDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPostDetailsBinding
    private lateinit var usersReference: DatabaseReference
    private var post: Post? = null
    private var author: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPostDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        usersReference = Firebase.database.getReference(getString(R.string.users_reference))

        post = intent.extras?.let {
            it.getParcelable("post")
        }

        setupView()
    }

    private fun setupView() {
        binding.topAppBar.setNavigationOnClickListener {
            finish()
        }

        post?.let {
            Picasso.get().load(it.imageUrl).into(binding.petImageView)
            binding.petNameTextView.text = it.petName
            binding.locationTextView.text =
                getString(R.string.post_details_location, it.state, it.city)
            binding.petAgeTextView.text = getString(R.string.post_details_pet_age, it.petAge)
            binding.petSexTextView.text = it.petSex
            binding.petWeightTextView.text =
                getString(R.string.post_details_pet_weight, it.petWeight.toString())
            binding.petStoryTextView.text = it.petStory

            usersReference.child(it.authorId!!).get()
                .addOnSuccessListener { dataSnapshot ->
                    Log.d(TAG, "Retrieving author data: success")
                    author = dataSnapshot.getValue(User::class.java)
                    binding.ownerFullNameTextView.text = "${author!!.firstname} ${author!!.lastname}"
                }.addOnFailureListener { ex ->
                    Log.w(TAG, "Retrieving author data: failure", ex)
                }
        }
    }

    companion object {
        private const val TAG = "PostDetailsActivity"
    }
}