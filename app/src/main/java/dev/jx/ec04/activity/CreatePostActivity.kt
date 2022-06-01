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
import dev.jx.ec04.entity.User
import dev.jx.ec04.util.UserUtils
import java.text.SimpleDateFormat
import java.util.*

class CreatePostActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreatePostBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private lateinit var postsStorageReference: StorageReference
    private var postImageUri: Uri? = null
    private var user: User? = null
    private var currentPost: Post? = null
    private var isEditMode = false

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

        isEditMode = intent.getBooleanExtra("isEditMode", false)
        user = UserUtils.getUserFromSharedPreferences(this)

        if (isEditMode) {
            binding.topAppBar.title = "Editar publicación"
            binding.mainBtn.text = "Actualizar publicación"
            currentPost = intent.getParcelableExtra("post")
        }

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

        if (isEditMode) {
            currentPost?.let {
                Picasso.get().load(it.imageUrl).error(R.drawable.default_image)
                    .into(binding.petImageView)
                binding.petNameInputEditText.setText(it.petName)
                binding.petAnimalAutoComplete.setText(it.petAnimal)
                binding.petSexAutoComplete.setText(it.petSex)
                binding.petStageAutoComplete.setText(it.petStage)
                binding.petStoryInputEditText.setText(it.petStory)
                binding.petAgeInputEditText.setText(it.petAge.toString())
                binding.petWeightInputEditText.setText(it.petWeight.toString())
                binding.stateInputEditText.setText(it.state)
                binding.cityInputEditText.setText(it.city)
                binding.phoneNumberInputEditText.setText(it.contact)
            }
        } else {
            binding.phoneNumberInputEditText.setText(user?.phoneNumber)
        }

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

            val id = currentPost?.id
            val petName = binding.petNameInputEditText.text.toString()
            val petAnimal = binding.petAnimalAutoComplete.text.toString()
            val petAge = binding.petAgeInputEditText.text.toString()
            val petWeight = binding.petWeightInputEditText.text.toString()
            val petSex = binding.petSexAutoComplete.text.toString()
            val petStage = binding.petStageAutoComplete.text.toString()
            val petStory = binding.petStoryInputEditText.text.toString()
            val state = binding.stateInputEditText.text.toString()
            val city = binding.cityInputEditText.text.toString()
            val contact = binding.phoneNumberInputEditText.text.toString()
            val createdAt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:sss'Z'").format(Date())

            savePost(
                Post(
                    id,
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
                    contact,
                    imageUrl = currentPost?.imageUrl,
                    createdAt = if (isEditMode) currentPost?.createdAt else createdAt
                ),
                postImageUri,
                binding.phoneNumberCheckbox.isChecked
            )
        }
    }

    private fun savePost(post: Post, postImageUri: Uri?, saveContact: Boolean) {
        val userPhoneNumberPath =
            "${getString(R.string.users_reference)}/${auth.currentUser!!.uid}/phoneNumber"
        val postsPath = getString(R.string.posts_reference)
        val userPostsPath = "${getString(R.string.users_posts_reference)}/${auth.currentUser!!.uid}"

        if (!isEditMode) {
            val postId = UUID.randomUUID().toString()
            post.id = postId
        }

        val postData = mutableMapOf<String, Any?>(
            "$postsPath/${post.id!!}" to post,
            "$userPostsPath/${post.id!!}" to post
        )

        if (saveContact) {
            postData[userPhoneNumberPath] = post.contact
        }

        val handleOnSavePost = OnCompleteListener<Void> { task ->
            if (task.isSuccessful) {
                Log.d(TAG, "Save post in database: success")
                Toast.makeText(this, "Publicación guardada", Toast.LENGTH_SHORT).show()

                if (saveContact) {
                    user?.phoneNumber = post.contact
                    UserUtils.saveUserToSharedPreferences(this, user!!)
                }

                if (!isEditMode) finish()
                else binding.mainBtn.isEnabled = true
            } else {
                Log.w(TAG, "Save post in database: failure", task.exception)
                showPostCreationFailureToast()
                binding.mainBtn.isEnabled = true
            }
        }

        if (postImageUri != null) {
            postsStorageReference.child(UUID.randomUUID().toString()).putFile(postImageUri)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "Upload post image to storage: success")
                        task.result.storage.downloadUrl
                            .addOnSuccessListener {
                                post.imageUrl = it.toString()
                                databaseReference.updateChildren(postData)
                                    .addOnCompleteListener(handleOnSavePost)
                            }
                    } else {
                        Log.w(TAG, "Upload post image to storage: failure", task.exception)
                        showPostCreationFailureToast()
                        binding.mainBtn.isEnabled = true
                    }
                }
        } else {
            databaseReference.updateChildren(postData)
                .addOnCompleteListener(handleOnSavePost)
        }
    }

    private fun showPostCreationFailureToast() {
        Toast.makeText(
            this,
            "Ocurrió un error al crear la publicación",
            Toast.LENGTH_SHORT
        ).show()
    }

    companion object {
        private const val TAG = "CreatePostActivity"
    }
}