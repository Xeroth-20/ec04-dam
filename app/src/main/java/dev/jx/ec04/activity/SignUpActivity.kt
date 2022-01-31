package dev.jx.ec04.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import dev.jx.ec04.databinding.ActivitySignUpBinding
import dev.jx.ec04.entity.User
import dev.jx.ec04.validation.UserValidationUtils
import dev.jx.ec04.validation.ValidationResult

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var usersReference: DatabaseReference
    private var isFormSubmitted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        usersReference = Firebase.database.getReference("users")

        setupView()
    }

    private fun setupView() {
        binding.topAppBar.setNavigationOnClickListener {
            finish()
        }

        binding.firstnameInputEditText.setOnKeyListener { v, keyCode, event ->
            if (isFormSubmitted) {
                val firstname = binding.firstnameInputEditText.text.toString().trim()
                val validationResult = UserValidationUtils.validateFirstname(this, firstname)
                applyValidation(binding.firstnameInputLayout, validationResult)
            }

            false
        }

        binding.lastnameInputEditText.setOnKeyListener { v, keyCode, event ->
            if (isFormSubmitted) {
                val lastname = binding.lastnameInputEditText.text.toString().trim()
                val validationResult = UserValidationUtils.validateLastname(this, lastname)
                applyValidation(binding.lastnameInputLayout, validationResult)
            }

            false
        }

        binding.emailInputEditText.setOnKeyListener { v, keyCode, event ->
            if (isFormSubmitted) {
                val email = binding.emailInputEditText.text.toString().trim()
                val validationResult = UserValidationUtils.validateEmail(this, email)
                applyValidation(binding.emailInputLayout, validationResult)
            }

            false
        }

        binding.passwordInputEditText.setOnKeyListener { v, keyCode, event ->
            if (isFormSubmitted) {
                val password = binding.passwordInputEditText.text.toString()
                val validationResult = UserValidationUtils.validatePassword(this, password)
                applyValidation(binding.passwordInputLayout, validationResult)
            }

            false
        }

        binding.confirmPasswordInputEditText.setOnKeyListener { v, keyCode, event ->
            if (isFormSubmitted) {
                val confirmPassword = binding.confirmPasswordInputEditText.text.toString()
                val password = binding.passwordInputEditText.text.toString()
                val validationResult =
                    UserValidationUtils.validateConfirmPassword(this, confirmPassword, password)
                applyValidation(binding.confirmPasswordInputLayout, validationResult)
            }

            false
        }

        binding.phoneNumberInputEditText.setOnKeyListener { v, keyCode, event ->
            if (isFormSubmitted) {
                val phoneNumber = binding.phoneNumberInputEditText.text.toString().trim()
                val validationResult = UserValidationUtils.validatePhoneNumber(this, phoneNumber)
                applyValidation(binding.phoneNumberInputLayout, validationResult)
            }

            false
        }

        binding.joinBtn.setOnClickListener {
            val firstname = binding.firstnameInputEditText.text.toString().trim()
            val lastname = binding.lastnameInputEditText.text.toString().trim()
            val email = binding.emailInputEditText.text.toString().trim()
            val password = binding.passwordInputEditText.text.toString()
            val confirmPassword = binding.confirmPasswordInputEditText.text.toString()
            val phoneNumber = binding.phoneNumberInputEditText.text.toString().trim()

            val firstnameResult = UserValidationUtils.validateFirstname(this, firstname)
            val lastnameResult = UserValidationUtils.validateLastname(this, lastname)
            val emailResult = UserValidationUtils.validateEmail(this, email)
            val passwordResult = UserValidationUtils.validatePassword(this, password)
            val confirmPasswordResult =
                UserValidationUtils.validateConfirmPassword(this, confirmPassword, password)
            val phoneNumberResult = UserValidationUtils.validatePhoneNumber(this, phoneNumber)

            val isFormValid = mapOf(
                binding.firstnameInputLayout to firstnameResult,
                binding.lastnameInputLayout to lastnameResult,
                binding.emailInputLayout to emailResult,
                binding.passwordInputLayout to passwordResult,
                binding.confirmPasswordInputLayout to confirmPasswordResult,
                binding.phoneNumberInputLayout to phoneNumberResult
            ).run {
                onEach { (txtInputLyt, vr) -> applyValidation(txtInputLyt, vr) }
                all { (_, vr) -> vr.ok }
            }

            if (isFormValid) {
                createUser(
                    User(firstname, lastname, email, phoneNumber),
                    password
                )
            }

            isFormSubmitted = true
        }

        binding.loginHereTextView.setOnClickListener {
            finish()
        }
    }

    private fun createUser(user: User, password: String) {
        auth.createUserWithEmailAndPassword(user.email!!, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val uid = task.result.user!!.uid
                    usersReference.child(uid).setValue(user)
                        .addOnCompleteListener {
                            if (it.isSuccessful) {
                                navigateToMain()
                            } else {
                                Log.w(TAG, "Create user in database: failure", it.exception)
                                task.result.user!!.delete()
                            }
                        }
                } else {
                    Log.w(TAG, "Create user with email and password: failure", task.exception)
                    showUserCreationFailureToast()
                }
            }
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finishAffinity()
    }

    private fun showUserCreationFailureToast() {
        Toast.makeText(this, "Ocurrió un error al crear la cuenta", Toast.LENGTH_SHORT).show()
    }

    private fun applyValidation(txtInputLyt: TextInputLayout, vr: ValidationResult) {
        if (vr.ok) {
            txtInputLyt.error = null
        } else {
            txtInputLyt.error = vr.reason
        }
    }

    companion object {
        private const val TAG = "SignUpActivity"
    }
}