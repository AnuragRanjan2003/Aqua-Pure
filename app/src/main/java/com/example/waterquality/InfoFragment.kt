package com.example.waterquality

import adapters.CasesRecyclerAdapter
import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log.e
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.waterquality.databinding.FragmentInfoBinding
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import models.Quality
import models.Report
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sqrt

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [InfoFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
private const val AREA_0 = 12391.88

class InfoFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var binding: FragmentInfoBinding
    private lateinit var context: AppCompatActivity
    private lateinit var database: FirebaseDatabase
    private lateinit var fUser: FirebaseUser
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationManager: LocationManager
    private var limit: Double = 0.00
    private var list = ArrayList<Report>()
    private lateinit var adapter: CasesRecyclerAdapter
    private var Var: Float = 0.00f


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
        database = FirebaseDatabase.getInstance()
        fUser = FirebaseAuth.getInstance().currentUser!!
        context = activity as AppCompatActivity
        locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        binding.recCases.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        locationRequest = LocationRequest.create()
        locationRequest.priority = Priority.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 5000
        locationRequest.fastestInterval = 2000

        checkLocationPermissionAndProceed()
        adapter = CasesRecyclerAdapter(list, context)
        binding.recCases.adapter = adapter
        binding.recCases.hasFixedSize()
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
                LocationServices.getFusedLocationProviderClient(context).lastLocation.addOnSuccessListener {
                    try {
                        populateRec(it)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }.addOnFailureListener {
                    Toast.makeText(
                        context,
                        "no location",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } else {
                turnOnGPS()
            }
        } else {
            checkLocationPermissionAndProceed()
        }

    }

    private fun populateRec(location: Location) {
        database.getReference("Reports").child(formatToName(location.latitude))
            .child(formatToName(location.longitude))
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (item in snapshot.children) {
                        val report = item.getValue(Report::class.java)
                        if (report != null)
                            list.add(report)
                    }
                    binding.cases.text = list.size.toString()
                    processQuality(list, floor(location.latitude).toInt())
                    adapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
    }


    private fun putValues(quality: Quality?) {
        if (quality != null) {
            binding.status.text = getStatus(quality)
            when (getStatus(quality)) {
                "Good" -> binding.cl.background = ColorDrawable(context.getColor(R.color.dark_green))
                "Not Good" -> binding.cl.background = ColorDrawable(context.getColor(R.color.red))
            }
        } else binding.status.text = "No Data"

    }


    private fun getStatus(quality: Quality): String {
        return if (quality.qualInd!! / 10000 > limit) "Good"
        else "Not Good"

    }

    private fun processQuality(reportList: MutableList<Report>?, lat: Int) {
        val area = AREA_0 * cos(toRad(lat))
        var qualInd = 0.00f
        var algae = 0.00f
        var dirty = 0.00f
        var sd = 0.00f
        if (!reportList.isNullOrEmpty()) {
            val n = reportList.size
            for (report in reportList) {
                qualInd += report.drinkable!!
                algae += report.algae!!
                dirty += report.dirty!!
                sd += report.drinkable * report.drinkable
            }
            qualInd /= n
            algae /= n
            dirty /= n
            sd /= n
            Var -= qualInd * qualInd
            Var = sqrt(Var / n)
            qualInd = (qualInd * 10000 / area).toFloat()
            algae = (algae / area).toFloat()
            dirty = (dirty / area).toFloat()


            val quality = Quality(qualInd, algae, dirty)
            e("ind", "$qualInd")
            putValues(quality)
            limit = 0.95 - 0.1554 * Var
        }
    }

    private fun toRad(deg: Int): Double {
        return PI * deg / 180
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
            Handler(Looper.getMainLooper()).postDelayed({ handleLocation() }, 4000)
        }
    }

    private fun formatToName(num: Double): String {
        return floor(num).toInt().toString()
    }


}