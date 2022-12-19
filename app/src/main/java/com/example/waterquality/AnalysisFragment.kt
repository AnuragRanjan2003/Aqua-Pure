package com.example.waterquality

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.Log.d
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.waterquality.databinding.FragmentAnalysisBinding
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import models.Quality
import models.Report
import models.colorApi.Dominant
import models.colorApi.Response
import models.processedInfo
import ui.ReportProgressButton
import viewModels.AnalysisFragmentViewModel
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.floor
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
    private lateinit var moredialog: AlertDialog
    private lateinit var reportDialog: AlertDialog
    private lateinit var databaseReference: DatabaseReference
    private lateinit var fuser: FirebaseUser
    private var drinkable: Float = 0.00f
    private var dwl: Int = 0
    private var long: Double = 0.00
    private var lat: Double = 0.00
    private var algae: Float = 0.00f
    private var dirty: Float = 0.00f
    private lateinit var locationRequest: LocationRequest


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

        locationRequest = LocationRequest.create()
        locationRequest.priority = Priority.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 5000
        locationRequest.fastestInterval = 2000

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
        viewModel.observeQuality().observe(viewLifecycleOwner, Observer { t -> putValues(t) })

        d("url", arguments?.getString("url")!!)

        binding.imgMore.setOnClickListener {
            moredialog.show()
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
                checkLocationPermissionAndProceed()
            }
            reportDialog.setOnCancelListener {
                reportDialog.dismiss()
                reportDialog.setCancelable(true)
            }
        }

        binding.backButton.setOnClickListener {
            (activity as Communicator).passBack()
        }
        moredialog.setOnShowListener {
            moredialog.findViewById<TextView>(R.id.tv_algae).text = (algae * 100).toString()
            moredialog.findViewById<TextView>(R.id.tv_dirty).text = (dirty * 100).toString()

            //pb
            moredialog.findViewById<ProgressBar>(R.id.pb_algae).visibility = View.INVISIBLE
            moredialog.findViewById<ProgressBar>(R.id.pb_dirty).visibility = View.INVISIBLE
        }





        return binding.root
    }

    private fun putValues(t: Quality) {
        algae = t.algae!!
        dirty = t.dirty!!
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun report() {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val format = DateTimeFormatter.ofPattern("yyyy MM dd HH mm")
        val latS = formatToName(lat)
        val lonS = formatToName(long)
        val report =
            Report(fuser.uid, drinkable, algae, dirty, LocalDate.now().format(formatter), lat, long)
        if (latS.isBlank() || lonS.isBlank()) {
            Toast.makeText(
                (activity as AppCompatActivity),
                "Location Could not be fetched",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            databaseReference.child(latS).child(lonS)
                .child(fuser.uid + " " + LocalDateTime.now().format(format))
                .setValue(report)
                .addOnSuccessListener {
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

    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun getLocation() {
        if (ActivityCompat.checkSelfPermission(
                (activity as AppCompatActivity),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            if (isGPSOn()) {
                LocationServices.getFusedLocationProviderClient((activity as AppCompatActivity))
                    .lastLocation.addOnSuccessListener {
                        if (it != null) {
                            lat = it!!.latitude
                            long = it!!.longitude
                            Log.e("lat", "latitude : $lat")
                            Log.e("long", "longitude : $long")
                            report()
                        }else{
                            getLocation()
                        }
                    }

            } else {
                reportDialog.dismiss()
                turnOnGPS()
            }
        } else {
            reportDialog.dismiss()
            checkLocationPermissionAndProceed()
        }
        return

    }

    private fun isGPSOn(): Boolean {
        var locationManager: LocationManager? = null
        if (locationManager == null) {
            locationManager =
                (activity as AppCompatActivity).getSystemService(Context.LOCATION_SERVICE) as LocationManager
        }
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    private fun turnOnGPS() {
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        builder.setAlwaysShow(true)

        val request =
            LocationServices.getSettingsClient((activity as AppCompatActivity).applicationContext)
                .checkLocationSettings(builder.build())

        request.addOnCompleteListener {
            try {
                val response = it.getResult(ApiException::class.java)
            } catch (e: ResolvableApiException) {
                when (e.statusCode) {
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED ->
                        try {
                            val resolvableApiException: ResolvableApiException = e
                            resolvableApiException.startResolutionForResult(
                                (activity as AppCompatActivity),
                                2
                            )
                            startIntentSenderForResult(
                                resolvableApiException.resolution.intentSender,
                                2,
                                null,
                                0,
                                0,
                                0,
                                null
                            )
                        } catch (e: IntentSender.SendIntentException) {
                            e.printStackTrace()
                        }
                    else -> Toast.makeText(
                        (activity as AppCompatActivity),
                        "no location",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
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
        //alert more dialog
        val view = LayoutInflater.from((activity as AppCompatActivity))
            .inflate(R.layout.more_alert_dialog, null)
        moredialog = AlertDialog.Builder(activity as AppCompatActivity).setView(view).create()
        moredialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        moredialog.window?.attributes?.windowAnimations = R.style.fadeAnim

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

    private fun formatToName(num: Double): String {
        return floor(num).toInt().toString()
    }

    private fun checkLocationPermissionAndProceed() {
        Dexter.withContext((activity as AppCompatActivity).applicationContext)
            .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
            .withListener(object : PermissionListener {
                @RequiresApi(Build.VERSION_CODES.O)
                override fun onPermissionGranted(p0: PermissionGrantedResponse?) {
                    getLocation()
                }

                override fun onPermissionDenied(p0: PermissionDeniedResponse?) {
                    Toast.makeText(
                        (activity as AppCompatActivity),
                        "Permission Needed",
                        Toast.LENGTH_SHORT
                    ).show()
                    reportDialog.dismiss()
                }

                override fun onPermissionRationaleShouldBeShown(
                    p0: PermissionRequest?,
                    p1: PermissionToken?
                ) {
                    p1?.continuePermissionRequest()
                }
            }).check()
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 2 && resultCode == Activity.RESULT_OK) {
            Toast.makeText((activity as AppCompatActivity), "Gps On", Toast.LENGTH_SHORT).show()
            binding.reportBtn.visibility = View.VISIBLE
            Handler(Looper.getMainLooper()).postDelayed({
                checkLocationPermissionAndProceed()
            }, 5000)

        }
    }

}