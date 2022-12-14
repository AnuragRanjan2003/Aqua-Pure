package com.example.waterquality


import android.app.Activity
import android.app.ProgressDialog
import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.waterquality.databinding.ActivitySignUpBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import models.User


class SignUpActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignUpBinding

    private lateinit var mAuth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var storage: FirebaseStorage
    private lateinit var uri: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = FirebaseDatabase.getInstance()
        val pd = ProgressDialog(this)
        pd.setMessage("Creating Account")
        mAuth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance()
        uri = Uri.parse(
            ContentResolver.SCHEME_ANDROID_RESOURCE +
                    "://" + resources.getResourcePackageName(R.drawable.ic_launcher_background)
                    + '/' + resources.getResourceTypeName(R.drawable.ic_launcher_background) + '/' + resources.getResourceEntryName(
                R.drawable.ic_launcher_background
            )
        )
        binding.profileImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            launcher.launch(intent)
        }
        binding.btnSignIn.setOnClickListener {
            pd.show()
            val email = binding.etSignInEmail.text.toString().trim()
            val pass = binding.etSignInPassword.text.toString().trim()
            val name = binding.etSignInName.text.toString().trim()

            if (email.isBlank()) {
                binding.etSignInEmail.error = "No Email"
                pd.dismiss()
                return@setOnClickListener
            } else if (name.isBlank()) {
                binding.etSignInName.error = "No Name"
                pd.dismiss()
                return@setOnClickListener
            } else if (pass.isBlank()) {
                binding.etSignInPassword.error = "No Password"
                pd.dismiss()
                return@setOnClickListener
            } else if (uri == null) {
                Toast.makeText(this, "No Image", Toast.LENGTH_SHORT).show()
                pd.dismiss()
            } else {
                mAuth.createUserWithEmailAndPassword(email, pass).addOnSuccessListener {
                    val ref = storage.getReference(
                        "profile/" + mAuth.currentUser!!.uid + "/" + mAuth.currentUser!!.uid + "." + getFileExtension(
                            uri
                        )
                    )
                    ref.putFile(uri).addOnSuccessListener {
                        ref.downloadUrl.addOnSuccessListener {
                            database.getReference("users").child(mAuth.currentUser!!.uid)
                                .setValue(User(email, name, mAuth.currentUser!!.uid, it.toString()))
                                .addOnSuccessListener {
                                    pd.dismiss()
                                    startActivity(Intent(this, MainActivity::class.java))
                                    finishAffinity()
                                }
                                .addOnFailureListener {
                                    pd.dismiss()
                                    Snackbar.make(
                                        binding.root,
                                        it.message.toString(),
                                        Snackbar.LENGTH_LONG
                                    )
                                        .show()
                                }
                        }
                    }
                }
                    .addOnFailureListener {
                        pd.dismiss()
                        Snackbar.make(binding.root, it.message.toString(), Snackbar.LENGTH_LONG)
                            .show()
                    }

            }

        }


    }

    private val launcher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                val uri1 = it.data?.data!!
                uri = uri1
                Glide.with(this).load(uri).into(binding.profileImage)

            }
        }

    private fun getFileExtension(uri: Uri): String? {
        val cr = contentResolver
        val map = MimeTypeMap.getSingleton()
        return map.getExtensionFromMimeType(cr.getType(uri))
    }


}










