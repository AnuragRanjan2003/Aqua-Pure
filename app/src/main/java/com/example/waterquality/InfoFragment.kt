package com.example.waterquality

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.waterquality.databinding.FragmentInfoBinding
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsStatusCodes
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import models.Quality
import viewModels.InfoFragmentViewModel
import kotlin.math.floor

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [InfoFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class InfoFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var viewModel: InfoFragmentViewModel
    private lateinit var binding: FragmentInfoBinding
    private lateinit var context: AppCompatActivity
    private lateinit var database: FirebaseDatabase
    private lateinit var fUser: FirebaseUser
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationManager: LocationManager
    private var limit: Double = 0.00

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentInfoBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[InfoFragmentViewModel::class.java]
        database = FirebaseDatabase.getInstance()
        fUser = FirebaseAuth.getInstance().currentUser!!
        context = activity as AppCompatActivity
        locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        checkLocationPermissionAndProceed()





        return binding.root
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment InfoFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            InfoFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    private fun checkLocationPermissionAndProceed() {
        Dexter.withContext(context).withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
            .withListener(object : PermissionListener {
                override fun onPermissionGranted(p0: PermissionGrantedResponse?) {
                    handleLocation()
                }

                override fun onPermissionDenied(p0: PermissionDeniedResponse?) {
                    Toast.makeText(context, "location is needed", Toast.LENGTH_LONG).show()
                }

                override fun onPermissionRationaleShouldBeShown(
                    p0: PermissionRequest?,
                    p1: PermissionToken?
                ) {
                    p1?.continuePermissionRequest()
                }
            }).check()
    }

    private fun handleLocation() {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            if (gpsIsOn()) {
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    5000,
                    0f,
                    object : LocationListener {
                        override fun onLocationChanged(p0: Location) {
                            call(p0)
                        }
                    })
            } else turnOnGPS()
        } else {
            checkLocationPermissionAndProceed()
        }
    }

    private fun call(location: Location) {
        viewModel.getResult(
            database.getReference("Reports").child(formatToName(location.latitude))
                .child(formatToName(location.longitude)),
            floor(location.latitude).toInt()
        )
        limit = viewModel.getLimit()
        viewModel.observeQuality().observe(viewLifecycleOwner,{ t-> putValues(t) })
    }

    private fun putValues(quality: Quality?) {
        binding.status.text = getStatus(quality)
    }

    private fun getStatus(quality: Quality?): CharSequence? {
        TODO("Not yet implemented")
    }

    private fun turnOnGPS() {
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        builder.setAlwaysShow(true)

        LocationServices.getSettingsClient(context.applicationContext)
            .checkLocationSettings(builder.build())
            .addOnCompleteListener {
                try {
                    it.getResult(ApiException::class.java)
                } catch (e: ResolvableApiException) {
                    when (e.statusCode) {
                        LocationSettingsStatusCodes.RESOLUTION_REQUIRED ->
                            try {
                                val resolvableApiException: ResolvableApiException = e
                                resolvableApiException.startResolutionForResult(
                                    context,
                                    2
                                )
                            } catch (e: IntentSender.SendIntentException) {
                                e.printStackTrace()
                            }
                        else -> Toast.makeText(context, "No Gps", Toast.LENGTH_LONG).show()
                    }
                }
            }
    }

    private fun gpsIsOn(): Boolean {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 2 && resultCode == Activity.RESULT_OK) {

        }
    }

    private fun formatToName(num: Double): String {
        return floor(num).toInt().toString()
    }
}