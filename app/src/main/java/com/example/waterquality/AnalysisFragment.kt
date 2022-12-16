package com.example.waterquality

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log.d
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.waterquality.databinding.FragmentAnalysisBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import models.Report
import models.colorApi.Dominant
import models.colorApi.Response
import models.processedInfo
import ui.ReportProgressButton
import viewModels.AnalysisFragmentViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.round

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [AnalysisFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AnalysisFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var binding: FragmentAnalysisBinding
    private lateinit var url: String
    private lateinit var uri: Uri
    private lateinit var viewModel: AnalysisFragmentViewModel
    private lateinit var dialog: AlertDialog
    private lateinit var reportDialog: AlertDialog
    private lateinit var databaseReference: DatabaseReference
    private lateinit var fuser: FirebaseUser
    private var drinkable: Float = 0.00f
    private var dwl: Int = 0


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
        binding = FragmentAnalysisBinding.inflate(inflater, container, false)
        databaseReference = FirebaseDatabase.getInstance().getReference("Reports")
        fuser = FirebaseAuth.getInstance().currentUser!!
        viewModel = ViewModelProvider(this)[AnalysisFragmentViewModel::class.java]

        url = arguments?.getString("url")!!
        uri = Uri.parse(arguments?.getString("uri"))
        Glide.with(this).load(url).listener(object : RequestListener<Drawable?> {
            override fun onLoadFailed(
                e: GlideException?,
                model: Any?,
                target: Target<Drawable?>?,
                isFirstResource: Boolean
            ): Boolean {
                Glide.with(this@AnalysisFragment).load(R.drawable.img_place_holder)
                    .into(binding.image)
                return false
            }

            override fun onResourceReady(
                resource: Drawable?,
                model: Any?,
                target: Target<Drawable?>?,
                dataSource: DataSource?,
                isFirstResource: Boolean
            ): Boolean {
                binding.pbImg.visibility = View.INVISIBLE
                return false
            }
        }).into(binding.image)

        makeDialogs()

        viewModel.getResponse(url)
        viewModel.getPrediction((activity as AppCompatActivity), uri)
        viewModel.observeResponse().observe(viewLifecycleOwner, Observer { t -> putValues(t) })
        viewModel.observeInfo().observe(viewLifecycleOwner, Observer { t -> putValues(t) })
        viewModel.observePrediction().observe(viewLifecycleOwner, Observer { t -> setStatus(t) })

        d("url", arguments?.getString("url")!!)

        binding.imgMore.setOnClickListener {
            dialog.show()
        }


        binding.reportBtn.setOnClickListener {
            reportDialog.show()
        }

        reportDialog.setOnShowListener {
            val reportButton: View? = reportDialog.findViewById(R.id.report_pbtn)
            reportButton?.setOnClickListener {
                val reportProgressButton =
                    ReportProgressButton((activity as AppCompatActivity), reportButton)
                reportProgressButton.activateButton()
                reportDialog.setCancelable(false)
                report()
            }
            reportDialog.setOnCancelListener {
                reportDialog.dismiss()
            }
        }





        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun report() {
        val location = getLocation()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val report =
            Report(fuser.uid, drinkable, dwl, LocalDate.now().format(formatter), location)
        databaseReference.child(location).child(fuser.uid).setValue(report).addOnSuccessListener {
            reportDialog.dismiss()
            Snackbar.make(binding.root, "Report Submitted", Snackbar.LENGTH_SHORT).show()
            binding.reportBtn.visibility = View.INVISIBLE
            reportDialog.setCancelable(true)
        }
            .addOnFailureListener {
                reportDialog.dismiss()
                Snackbar.make(binding.root, it.message.toString(), Snackbar.LENGTH_SHORT).show()
                reportDialog.setCancelable(true)
            }

    }

    private fun getLocation(): String {
        TODO("Return the location of user")
    }

    private fun putValues(response: Response) {
        d("brg", response.brightness.toString())
        //setting values
        binding.tvRgb.text = getRgbString(response.colors.dominant)
        binding.tvBrg.text = response.brightness.toString()


        //pb
        binding.pb1.visibility = View.INVISIBLE
        binding.pb2.visibility = View.INVISIBLE

    }

    private fun putValues(info: processedInfo) {
        d("cdw", formatWL(info.cdw))
        //setting values
        binding.tvDw.text = formatWL(info.dw)
        dwl = info.dw.toInt()

        //pb
        binding.pb3.visibility = View.INVISIBLE

    }

    private fun getRgbString(dominant: Dominant): String {
        return "( " + dominant.r.toString() + ", " + dominant.g.toString() + ", " + dominant.b.toString() + " )"
    }

    private fun formatWL(d: Double): String {
        return round(d).toInt().toString() + " nm"
    }


    private fun makeDialogs() {
        //alert dialog
        val view = LayoutInflater.from((activity as AppCompatActivity))
            .inflate(R.layout.more_alert_dialog, null)
        dialog = AlertDialog.Builder(activity as AppCompatActivity).setView(view).create()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.attributes?.windowAnimations = R.style.fadeAnim

        //report dialog
        val view2 = LayoutInflater.from((activity as AppCompatActivity))
            .inflate(R.layout.report_aler_dialog, null)
        reportDialog = AlertDialog.Builder((activity as AppCompatActivity)).setView(view2).create()
        reportDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        reportDialog.window?.attributes?.windowAnimations = R.style.fadeAnim
    }

    private fun getStatus(output: FloatArray): String {
        val ok = output[0]
        drinkable = ok
        return if (ok >= 0.95) "Drinkable"
        else if (ok >= 0.9) "Risky"
        else "Unfit"
    }

    private fun setStatus(output: FloatArray) {
        val status: String = getStatus(output)
        binding.tvStatus.text = status
        if (status == "Risky") {
            binding.statusCard.setBackgroundColor(
                resources.getColor(
                    R.color.yellow,
                    null
                )
            )
            binding.reportBtn.visibility = View.VISIBLE
        } else if (status == "Unfit") {
            binding.statusCard.setBackgroundColor(
                resources.getColor(
                    R.color.red,
                    null
                )
            )
            binding.reportBtn.visibility = View.VISIBLE
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment AnalysisFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            AnalysisFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }


}