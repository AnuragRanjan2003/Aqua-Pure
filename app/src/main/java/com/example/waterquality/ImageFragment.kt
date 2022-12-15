package com.example.waterquality

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log.d
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.waterquality.databinding.FragmentImageBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.storage.FirebaseStorage
import ui.ProgressButton
import viewModels.AnalysisFragmentViewModel
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ImageFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
private const val FILE_NAME = "photo.jpg"

class ImageFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var photoFile: File
    private lateinit var binding: FragmentImageBinding
    private lateinit var fuser: FirebaseUser
    private lateinit var storage: FirebaseStorage
    private lateinit var viewModel: AnalysisFragmentViewModel
    private lateinit var communicator: Communicator
    private var uri: Uri? = null
    private lateinit var pbtn: View
    private lateinit var dialog: AlertDialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentImageBinding.inflate(inflater, container, false)
        communicator = activity as Communicator

        pbtn = binding.root.findViewById(R.id.analyze_btn)
        makeDialog()

        dialog.show()

        dialog.findViewById<AppCompatButton>(R.id.ad_ok).setOnClickListener {
            dialog.dismiss()
        }
        viewModel = ViewModelProvider(this)[AnalysisFragmentViewModel::class.java]
        binding.cameraBtn.setOnClickListener {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            photoFile = getPhotoFile(FILE_NAME)

            val fileProvider = FileProvider.getUriForFile(
                container!!.context,
                "com.example.fileprovider",
                photoFile
            )
            intent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider)

            startActivityForResult(intent, 100)

        }

        storage = FirebaseStorage.getInstance()
        fuser = FirebaseAuth.getInstance().currentUser!!

        binding.galleryBtn.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 101)
        }




        pbtn.setOnClickListener {
            val progressBtn =
                ProgressButton((activity as AppCompatActivity).applicationContext, pbtn, "Analyse")
            progressBtn.activateButton()
            if (uri == null) {
                snackBar("No Image")
                progressBtn.deactivateButton()
                return@setOnClickListener
            } else {
                val ref = storage.getReference(
                    "posts/" + fuser.uid + "/" + getName() + "." + getFileExtension(uri!!)
                )
                ref.putFile(uri!!).addOnSuccessListener {
                    ref.downloadUrl.addOnSuccessListener {
                        progressBtn.finish()
                        communicator.passUri(it.toString())
                    }.addOnFailureListener {
                        progressBtn.deactivateButton()
                        d("Error", it.message.toString())
                    }
                }
                    .addOnFailureListener {
                        progressBtn.deactivateButton()
                        snackBar(it.message.toString())
                    }


            }
        }


        return binding.root
    }

    private fun getPhotoFile(fileName: String): File {
        val storage = activity?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(fileName, ".jpg", storage)
    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ImageFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ImageFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 100 && resultCode == Activity.RESULT_OK) {
            Glide.with(this).load(BitmapFactory.decodeFile(photoFile.absolutePath))
                .into(binding.imageView)
            uri = photoFile.toUri()
        } else if (requestCode == 101 && resultCode == Activity.RESULT_OK) {
            Glide.with(this).load(data?.data).into(binding.imageView)
            uri = data?.data
        } else super.onActivityResult(requestCode, resultCode, data)
    }

    private fun snackBar(text: String) {
        Snackbar.make(binding.layout, text, Snackbar.LENGTH_LONG).show()

    }

    private fun getFileExtension(uri: Uri): String? {
        val cr = activity?.contentResolver
        val map = MimeTypeMap.getSingleton()
        return map.getExtensionFromMimeType(cr?.getType(uri))
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getName(): String {
        val formater = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        return LocalDateTime.now().format(formater)
    }

    private fun makeDialog() {
        val view = LayoutInflater.from((activity as AppCompatActivity))
            .inflate(R.layout.image_alert_dialog, null)
        dialog = AlertDialog.Builder((activity as AppCompatActivity)).setView(view).create()
        dialog.window?.attributes?.windowAnimations = R.style.fadeAnim
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCancelable(false)

    }
}