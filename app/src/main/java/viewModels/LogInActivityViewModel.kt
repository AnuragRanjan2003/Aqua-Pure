package viewModels

import android.content.Intent
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.waterquality.LogInActivity
import com.example.waterquality.MainActivity
import com.example.waterquality.databinding.ActivityLogInBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth

class LogInActivityViewModel : ViewModel() {
    private var email = MutableLiveData<String>()
    private var pass = MutableLiveData<String>()
    private val mAuth = FirebaseAuth.getInstance()

    fun setEmail(inEmail: String) {
        email.value = inEmail

    }

    fun setPass(inPass: String) {
        pass.value = inPass
    }

    fun observeEmail(): LiveData<String> {
        return email
    }

    fun observePass(): LiveData<String> {
        return pass
    }

    fun logIn(context: LogInActivity, binding: ActivityLogInBinding) {
        binding.pbLogin.visibility = View.VISIBLE
        if (email.value.isNullOrBlank()) {
            binding.etLogInEmail.error = "No Email"
            return
        } else if (pass.value.isNullOrBlank()) {
            binding.etLogInPassword.error = "No Password"
            return
        } else {
            mAuth.signInWithEmailAndPassword(email.value!!, pass.value!!).addOnSuccessListener {
                context.startActivity(Intent(context, MainActivity::class.java))
            }.addOnFailureListener {
                Snackbar.make(binding.layout, it.message.toString(), Snackbar.LENGTH_LONG).show()
            }
        }
    }
}