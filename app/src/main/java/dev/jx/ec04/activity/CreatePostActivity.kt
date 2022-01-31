package dev.jx.ec04.activity

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AppCompatActivity
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.squareup.picasso.Picasso
import dev.jx.ec04.R
import dev.jx.ec04.databinding.ActivityCreatePostBinding
import dev.jx.ec04.entity.Post
import java.text.SimpleDateFormat
import java.util.*

class CreatePostActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreatePostBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private lateinit var postsStorageReference: StorageReference
    private var postImageUri: Uri? = null

    private val selectPictureResultLauncher =
        registerForActivityResult(StartActivityForResult()) { result ->
            val resultCode = result.resultCode
            val data = result.data?.data

            when (resultCode) {
                RESULT_OK -> {
                    postImageUri = data!!
                    Picasso.get().load(postImageUri).into(binding.petImageView)
                }
                ImagePicker.RESULT_ERROR -> {
                    Toast.makeText(this, ImagePicker.getError(result.data), Toast.LENGTH_SHORT)
                        .show()
                }
                else -> {
                    Toast.makeText(this, "Se canceló la operación", Toast.LENGTH_SHORT).show()
                }
            }

        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreatePostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        databaseReference = Firebase.database.reference
        postsStorageReference =
            Firebase.storage.getReference(getString(R.string.posts_storage_reference))

        setupView()
    }

    private fun setupView() {
        val petAnimalItems = resources.getStringArray(R.array.pet_animals).toList()
        val petAnimalAdapter = ArrayAdapter(this, R.layout.dropdown_item, petAnimalItems)

        val petSexItems = resources.getStringArray(R.array.pet_sexs).toList()
        val petSexAdapter = ArrayAdapter(this, R.layout.dropdown_item, petSexItems)

        val petStageItems = resources.getStringArray(R.array.pet_stages).toList()
        val petStageAdapter = ArrayAdapter(this, R.layout.dropdown_item, petStageItems)

        binding.petAnimalAutoComplete.setAdapter(petAnimalAdapter)
        binding.petSexAutoComplete.setAdapter(petSexAdapter)
        binding.petStageAutoComplete.setAdapter(petStageAdapter)

        binding.topAppBar.setNavigationOnClickListener {
            finish()
        }

        binding.selectPictureBtn.setOnClickListener {
            ImagePicker.with(this)
                .crop(3f, 2f)
                .compress(1024 * 3)
                .maxResultSize(1920, 1080)
                .createIntent { intent ->
                    selectPictureResultLauncher.launch(intent)
                }
        }

        binding.mainBtn.setOnClickListener {
            binding.mainBtn.isEnabled = false

            val petName = binding.petNameInputEditText.text.toString()
            val petAnimal = binding.petAnimalAutoComplete.text.toString()
            val petAge = binding.petAgeInputEditText.text.toString()
            val petWeight = binding.petWeightInputEditText.text.toString()
            val petSex = binding.petSexAutoComplete.text.toString()
            val petStage = binding.petStageAutoComplete.text.toString()
            val petStory = binding.petStoryInputEditText.text.toString()
            val state = binding.stateInputEditText.text.toString()
            val city = binding.cityInputEditText.text.toString()
            val createdAt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:sss'Z'").format(Date())

            createPost(
                Post(
                    auth.currentUser!!.uid,
                    petName,
                    petAnimal,
                    petAge.toInt(),
                    petSex,
                    petStage,
                    petWeight.toFloat(),
                    petStory,
                    state,
                    city,
                    createdAt = createdAt
                ),
                postImageUri!!
            )
        }
    }

    private fun createPost(post: Post, postImageUri: Uri) {
        val postsPath = getString(R.string.posts_reference)
        val userPostsPath = "${getString(R.string.users_posts_reference)}/${auth.currentUser!!.uid}"
        val postId = UUID.randomUUID().toString()
        val postData = mapOf(
            "$postsPath/$postId" to post,
            "$userPostsPath/$postId" to post
        )

        val handleOnCreatePost = OnCompleteListener<Void> { task ->
            if (task.isSuccessful) {
                Log.d(TAG, "Create post in database: success")
                Toast.makeText(this, "Publicación creada", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Log.w(TAG, "Create post in database: failure", task.exception)
                showPostCreationFailureToast()
                binding.mainBtn.isEnabled = true
            }
        }

        postsStorageReference.child(UUID.randomUUID().toString()).putFile(postImageUri)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "Upload post image to storage: success")
                    task.result.storage.downloadUrl
                        .addOnCompleteListener {
                            if (it.isSuccessful) {
                                post.imageUrl = it.result.toString()
                                databaseReference.updateChildren(postData)
                                    .addOnCompleteListener(handleOnCreatePost)
                            } else {
                                Log.w(TAG, "Get post image url: failure", it.exception)
                            }
                        }

                } else {
                    Log.w(TAG, "Upload post image to storage: failure", task.exception)
                    showPostCreationFailureToast()
                    binding.mainBtn.isEnabled = true
                }
            }
    }

    private fun showPostCreationFailureToast() {
        Toast.makeText(
            this,
            "Ocurrió un error al crear la publicación",
            Toast.LENGTH_SHORT
        )
    }

    companion object {
        private const val TAG = "CreatePostActivity"
    }
}