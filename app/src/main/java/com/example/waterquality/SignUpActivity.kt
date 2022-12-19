package com.example.waterquality


import android.app.Activity
import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.waterquality.databinding.ActivitySignUpBinding
import viewModels.SignUpActivityViewModel


class SignUpActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignUpBinding

    private lateinit var uri: Uri
    private lateinit var viewModel: SignUpActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewModel = ViewModelProvider(this)[SignUpActivityViewModel::class.java]

        uri = Uri.parse(
            ContentResolver.SCHEME_ANDROID_RESOURCE +
                    "://" + resources.getResourcePackageName(R.drawable.ic_launcher_background)
                    + '/' + resources.getResourceTypeName(R.drawable.ic_launcher_background) + '/' + resources.getResourceEntryName(
                R.drawable.ic_launcher_background
            )
        )
        viewModel.setUri(uri)
        binding.profileImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            launcher.launch(intent)
        }

        binding.etSignInEmail.doAfterTextChanged { viewModel.setEmail(it.toString().trim()) }
        binding.etSignInName.doAfterTextChanged { viewModel.setName(it.toString().trim()) }
        binding.etSignInPassword.doAfterTextChanged { viewModel.setPass(it.toString().trim()) }

        binding.btnSignIn.setOnClickListener {
            binding.pb.visibility = View.VISIBLE
            viewModel.signUp(binding,this@SignUpActivity)
        }


    }

    private val launcher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                val uri1 = it.data?.data!!
                uri = uri1
                viewModel.setUri(uri1)
                Glide.with(this).load(uri).into(binding.profileImage)

            }
        }


}










