package com.example.waterquality

import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.registerForActivityResult
import com.example.waterquality.databinding.ActivityLogInBinding
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class LogInActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLogInBinding
    private lateinit var mAuth:FirebaseAuth



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLogInBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mAuth = FirebaseAuth.getInstance()
        binding.btnLogIn.setOnClickListener {
            binding.pbLogin.visibility= View.VISIBLE
            val email=binding.etLogInEmail.text.toString().trim()
            val pass=binding.etLogInPassword.text.toString().trim()
             if(email.isNullOrBlank()){
                 binding.etLogInEmail.error="No Email"
                 binding.pbLogin.visibility=View.GONE
                 return@setOnClickListener
             }
            else if(pass.isNullOrBlank()){
                binding.etLogInPassword.error="No Pass"
                 binding.pbLogin.visibility=View.GONE
                 return@setOnClickListener
            }else{
                mAuth.signInWithEmailAndPassword(email,pass).addOnCompleteListener {
                    binding.pbLogin.visibility=View.GONE
                    if(it.isSuccessful){
                        startActivity(Intent(this@LogInActivity,MainActivity::class.java))
                    }else{
                        Toast.makeText(this,it.exception?.message,Toast.LENGTH_LONG).show()
                    }
                }
            }
        }







    }








}