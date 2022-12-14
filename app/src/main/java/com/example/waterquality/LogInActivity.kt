package com.example.waterquality

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.ViewModelProvider
import com.example.waterquality.databinding.ActivityLogInBinding
import viewModels.LogInActivityViewModel

class LogInActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLogInBinding
    private lateinit var viewModel: LogInActivityViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLogInBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewModel = ViewModelProvider(this)[LogInActivityViewModel::class.java]
        binding.etLogInEmail.doAfterTextChanged {
            viewModel.setEmail(it.toString().trim())
        }
        binding.etLogInPassword.doAfterTextChanged {
            viewModel.setPass(it.toString().trim())
        }


        binding.btnLogIn.setOnClickListener {
            viewModel.logIn(this, binding)
        }
    }


}








