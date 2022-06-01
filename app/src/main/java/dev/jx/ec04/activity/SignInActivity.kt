package dev.jx.ec04.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast

import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import dev.jx.ec04.R
import dev.jx.ec04.databinding.ActivitySignInBinding
import dev.jx.ec04.entity.User
import dev.jx.ec04.util.UserUtils
import java.util.concurrent.Executor

class SignInActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignInBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var usersReference: DatabaseReference

    private val googleSignInResultLauncher =
        registerForActivityResult(StartActivityForResult()) { result ->
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                auth.signInWithCredential(credential)
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            Log.d(TAG, "Sign in with google: success")
                            val user = User(
                                account.givenName,
                                account.familyName,
                                account.email
                            )

                            if (it.result.additionalUserInfo!!.isNewUser) {
                                usersReference.child(it.result.user!!.uid).setValue(user)
                                    .addOnCompleteListener { usrCreationTask ->
                                        if (usrCreationTask.isSuccessful) {
                                            UserUtils.saveUserToSharedPreferences(this, user)
                                            navigateToMain()
                                        } else {
                                            Log.w(
                                                TAG,
                                                "Create user in database: failure",
                                                usrCreationTask.exception
                                            )
                                            it.result.user!!.delete()
                                        }
                                    }
                            } else {
                                UserUtils.saveUserToSharedPreferences(this, user)
                                navigateToMain()
                            }
                        } else {
                            Log.w(TAG, "Sign in with google: failure", it.exception)
                            Toast.makeText(this, "Error de autenticación", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
            } catch (ex: ApiException) {
                Log.e(TAG, "Cannot recover account from Google", ex)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        usersReference = Firebase.database.getReference("users")

        val showSplashScreen = intent.extras?.getBoolean("show_splash_screen") ?: true

        if (showSplashScreen) {
            showSplash()
            Thread {
                Thread.sleep(2000)
                runOnUiThread {
                    if (auth.currentUser != null) {
                        navigateToMain()
                    } else {
                        hideSplash()
                    }
                }
            }.start()
        } else {
            if (auth.currentUser != null) {
                navigateToMain()
            }
        }

        setupView()
    }

    private fun setupView() {
        binding.loginBtn.setOnClickListener {
            signInWithEmailAndPassword(
                binding.emailInputEditText.text.toString(),
                binding.passwordInputEditText.text.toString()
            )
        }

        binding.loginWithGoogleBtn.setOnClickListener {
            signInWithGoogle()
        }

        binding.createAccountTextView.setOnClickListener {
            navigateToSignUp()
        }
    }

    private fun showSplash() {
        binding.contentLinearLayout.visibility = View.INVISIBLE
        binding.splashScreen.visibility = View.VISIBLE
    }

    private fun hideSplash() {
        binding.contentLinearLayout.apply {
            visibility = View.VISIBLE
            startAnimation(AnimationUtils.loadAnimation(this@SignInActivity, R.anim.fade_in))
        }
        binding.splashScreen.visibility = View.INVISIBLE
    }

    private fun signInWithEmailAndPassword(email: String, password: String) {
        if (email.isEmpty() || password.isEmpty()) {
            showBadAuthenticationToast()
            return;
        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                val id = it.user!!.uid
                usersReference.child(id).get()
                    .addOnSuccessListener { result ->
                        if (result.exists()) {
                            Log.d(TAG, "Sign in with email and password: success")
                            val user = result.getValue(User::class.java)!!
                            UserUtils.saveUserToSharedPreferences(this, user)
                            navigateToMain()
                        } else {
                            showBadAuthenticationToast()
                        }
                    }
            }.addOnFailureListener {
                Log.w(TAG, "Sign in with email and password: failure", it)
                showBadAuthenticationToast()
            }
    }

    private fun signInWithGoogle() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.web_client_id))
            .requestEmail()
            .requestProfile()
            .build()
        val gc = GoogleSignIn.getClient(this, gso).apply { signOut() }
        googleSignInResultLauncher.launch(gc.signInIntent)
    }

    private fun navigateToSignUp() {
        val intent = Intent(this, SignUpActivity::class.java)
        startActivity(intent)
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun showBadAuthenticationToast() {
        Toast.makeText(this, "Correo electrónico o contraseña incorrectos", Toast.LENGTH_SHORT)
            .show()
    }

    companion object {
        private const val TAG = "SignInActivity"
    }
}