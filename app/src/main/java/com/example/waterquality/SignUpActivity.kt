package com.example.waterquality

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.registerForActivityResult
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.waterquality.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.database.FirebaseDatabase
import models.User

const val PICK_CODE=100

class SignUpActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignUpBinding

    private lateinit var mAuth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private  lateinit var uri: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = FirebaseDatabase.getInstance()
        val pd = ProgressDialog(this)
        pd.setMessage("Creating Account")
        mAuth = FirebaseAuth.getInstance()
        binding.profileImage.setOnClickListener{
            val intent = Intent(Intent.ACTION_PICK)
            intent.type="image/*"
            launcher.launch(intent)
        }
        binding.btnSignIn.setOnClickListener {
            pd.show()
            val email = binding.etSignInEmail.text.toString().trim()
            val pass = binding.etSignInPassword.text.toString().trim()
            val name = binding.etSignInName.text.toString().trim()
            val uri:Uri? = Uri.parse(binding.profileImage.tag.toString())
            if (email.isNullOrBlank()) {
                binding.etSignInEmail.error = "No Email"
                pd.dismiss()
                return@setOnClickListener
            } else if (name.isNullOrBlank()) {
                binding.etSignInName.error = "No Name"
                pd.dismiss()
                return@setOnClickListener
            } else if (pass.isNullOrBlank()) {
                binding.etSignInPassword.error = "No Password"
                pd.dismiss()
                return@setOnClickListener
            } else if(uri== null){
                Toast.makeText(this,"No Image",Toast.LENGTH_SHORT).show()
            }
            else {
                mAuth.createUserWithEmailAndPassword(email, pass).addOnSuccessListener {
                    database.getReference("users").child(it.user!!.uid)
                        .setValue(User(email, name, it.user!!.uid))
                        .addOnCompleteListener {
                            pd.dismiss()
                            if (it.isSuccessful) {
                                val update = userProfileChangeRequest {
                                    displayName = name
                                    photoUri=uri
                                }
                                mAuth.currentUser?.updateProfile(update)?.addOnCompleteListener {
                                    if (it.isSuccessful) startActivity(
                                        Intent(
                                            this@SignUpActivity,
                                            MainActivity::class.java
                                        )
                                    )
                                    else Toast.makeText(
                                        this,
                                        it.exception?.message,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }

                            } else {
                                Toast.makeText(
                                    this@SignUpActivity,
                                    it.exception?.message,
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                }

            }

        }


    }
    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            if(it.resultCode == Activity.RESULT_OK){
                val uri = it.data?.data!!
                Glide.with(this).load(uri).into(binding.profileImage)

            }
    }



}










