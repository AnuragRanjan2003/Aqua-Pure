package com.example.waterquality

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.util.Log.d
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import models.colorApi.Dominant
import models.colorApi.Response
import models.processedInfo
import viewModels.AnalysisFragmentViewModel
import java.math.RoundingMode
import java.text.DecimalFormat
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
    ): View {
        binding = FragmentAnalysisBinding.inflate(inflater, container, false)
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

        makeDialog()

        viewModel.getResponse(url)
        viewModel.getPrediction((activity as AppCompatActivity), uri)
        viewModel.observeResponse().observe(viewLifecycleOwner, Observer { t -> putValues(t) })
        viewModel.observeInfo().observe(viewLifecycleOwner, Observer { t -> putValues(t) })
        viewModel.observePrediction().observe(viewLifecycleOwner, Observer { t -> putValues(t) })

        d("url", arguments?.getString("url")!!)

        binding.imgMore.setOnClickListener {
            dialog.show()
        }


        return binding.root
    }

    private fun putValues(output: FloatArray) {
        // setting the values
        binding.tvDrk.text = formatDecimal(output[0])
        binding.tvUnfit.text = formatDecimal(output[1])

        // pb
        binding.pbDrk.visibility = View.INVISIBLE
        binding.pbUnfit.visibility = View.INVISIBLE
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


        //pb
        binding.pb3.visibility = View.INVISIBLE

    }

    private fun getRgbString(dominant: Dominant): String {
        return "( " + dominant.r.toString() + ", " + dominant.g.toString() + ", " + dominant.b.toString() + " )"
    }

    private fun formatWL(d: Double): String {
        return round(d).toInt().toString() + " nm"
    }

    private fun formatDecimal(num: Float):String{
        val df = DecimalFormat("##.##")
        df.roundingMode = RoundingMode.CEILING
        return df.format(num)
    }

    private fun makeDialog() {
        val view = LayoutInflater.from((activity as AppCompatActivity))
            .inflate(R.layout.more_alert_dialog, null)
        dialog = AlertDialog.Builder(activity as AppCompatActivity).setView(view).create()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.attributes?.windowAnimations = R.style.fadeAnim
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