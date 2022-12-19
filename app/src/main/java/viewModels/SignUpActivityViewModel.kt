package viewModels

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.waterquality.MainActivity
import com.example.waterquality.databinding.ActivitySignUpBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import models.User


const val invisible = View.INVISIBLE

class SignUpActivityViewModel : ViewModel() {
    private val email: MutableLiveData<String> by lazy { MutableLiveData<String>() }
    private val pass: MutableLiveData<String> by lazy { MutableLiveData<String>() }
    private val name: MutableLiveData<String> by lazy { MutableLiveData<String>() }
    private val uri: MutableLiveData<Uri> by lazy { MutableLiveData<Uri>() }
    private val dRef = FirebaseDatabase.getInstance().getReference("users")
    private val storage = FirebaseStorage.getInstance()
    private val mAuth = FirebaseAuth.getInstance()

    fun setEmail(email: String) {
        this.email.value = email
    }

    fun setName(name: String) {
        this.name.value = name
    }

    fun setPass(pass: String) {
        this.pass.value = pass
    }

    fun setUri(uri: Uri) {
        this.uri.value = uri
    }

    fun signUp(binding: ActivitySignUpBinding, context: Context) {
        if (email.value.isNullOrBlank()) {
            binding.etSignInEmail.error = "No Email"
            binding.pb.visibility = invisible
            return
        } else if (name.value.isNullOrBlank()) {
            binding.etSignInName.error = "No Name"
            binding.pb.visibility = invisible
            return
        } else if (pass.value.isNullOrBlank()) {
            binding.etSignInPassword.error = "No Password"
            binding.pb.visibility = invisible
            return
        } else if (uri == null) {
            Toast.makeText(context, "No Image", Toast.LENGTH_SHORT).show()
            binding.pb.visibility = invisible
        } else {
            mAuth.createUserWithEmailAndPassword(email.value!!, pass.value!!).addOnSuccessListener {
                val ref = storage.getReference(
                    "profile/" + mAuth.currentUser!!.uid + "/" + mAuth.currentUser!!.uid + "." + getFileExtension(
                        uri.value!!, context
                    )
                )
                ref.putFile(uri.value!!).addOnSuccessListener {
                    ref.downloadUrl.addOnSuccessListener {
                        dRef.child(mAuth.currentUser!!.uid)
                            .setValue(
                                User(
                                    email.value!!,
                                    name.value!!,
                                    mAuth.currentUser!!.uid,
                                    it.toString()
                                )
                            )
                            .addOnSuccessListener {
                                binding.pb.visibility = invisible
                                context.startActivity(Intent(context, MainActivity::class.java))
                                (context as AppCompatActivity).finishAffinity()
                            }
                            .addOnFailureListener { exception ->
                                binding.pb.visibility = invisible
                                Snackbar.make(
                                    binding.root,
                                    exception.message.toString(),
                                    Snackbar.LENGTH_LONG
                                )
                                    .show()
                            }
                    }
                }
            }
                .addOnFailureListener {
                    binding.pb.visibility = invisible
                    Snackbar.make(binding.root, it.message.toString(), Snackbar.LENGTH_LONG)
                        .show()
                }

        }

    }
}

private fun getFileExtension(uri: Uri, context: Context): String? {
    val cr = context.contentResolver
    val map = MimeTypeMap.getSingleton()
    return map.getExtensionFromMimeType(cr.getType(uri))
}
