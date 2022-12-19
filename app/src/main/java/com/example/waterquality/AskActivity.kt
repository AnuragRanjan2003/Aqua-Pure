package com.example.waterquality

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.waterquality.databinding.ActivityAskBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class AskActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAskBinding
    private lateinit var mAuth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var googleSignInClient: GoogleSignInClient
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAskBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().getReference("users")

        checkUser()
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()


        googleSignInClient = GoogleSignIn.getClient(this, gso)


        binding.logIn.setOnClickListener {
            startActivity(
                Intent(
                    this@AskActivity,
                    LogInActivity::class.java
                )
            )
        }
        binding.signUp.setOnClickListener {
            startActivity(
                Intent(
                    this@AskActivity,
                    SignUpActivity::class.java
                )
            )
        }
        binding.btnGSignIn.setOnClickListener {
            googleSignIn()
        }

    }

    private fun googleSignIn() {
        val intent = googleSignInClient.signInIntent
        launcher.launch(intent)
    }

    private val launcher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(it.data)
                handleResult(task)
            }
        }

    private fun handleResult(task: Task<GoogleSignInAccount>) {
        if (task.isSuccessful) {
            val account: GoogleSignInAccount? = task.result
            if (account != null) {
                updateUI(account)
            } else {
                Toast.makeText(this, task.exception?.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun updateUI(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        mAuth.signInWithCredential(credential).addOnCompleteListener {
            if (it.isSuccessful) {
                saveUserData(it.result.user!!)


            } else {
                Toast.makeText(this, it.exception?.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun saveUserData(user: FirebaseUser) {
        val user2 = models.User(user.email, user.displayName, user.uid, user.photoUrl.toString())
        database.child(user.uid).setValue(user2)
            .addOnSuccessListener { startActivity(Intent(this, MainActivity::class.java)) }
            .addOnFailureListener {
                Toast.makeText(this@AskActivity, it.message, Toast.LENGTH_LONG).show()
            }
    }

    private fun checkUser() {
        val user: FirebaseUser? = mAuth.currentUser
        if (user != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finishAffinity()
        }
    }
}