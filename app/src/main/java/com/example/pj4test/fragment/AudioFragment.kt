package com.example.pj4test.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.telephony.SmsManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.getSystemService
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.example.pj4test.ProjectConfiguration
import com.example.pj4test.StatsViewModel
import com.example.pj4test.audioInference.StopClassifier
import com.example.pj4test.databinding.FragmentAudioBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

class AudioFragment: Fragment(), StopClassifier.DetectorListener {
    private val TAG = "AudioFragment"

    private var _fragmentAudioBinding: FragmentAudioBinding? = null

    private val fragmentAudioBinding
        get() = _fragmentAudioBinding!!

    // classifiers
    lateinit var stopClassifier: StopClassifier
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // views
    lateinit var stopView: TextView
    var current_speed: Float = 0.0f

    private val viewModel: StatsViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _fragmentAudioBinding = FragmentAudioBinding.inflate(inflater, container, false)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        return fragmentAudioBinding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        stopClassifier.stop()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        stopView = fragmentAudioBinding.stopTextView

        stopClassifier = StopClassifier()
        stopClassifier.initialize(requireContext())
        stopClassifier.setDetectorListener(this)
        stopClassifier.start()

        viewModel.speed.observe(viewLifecycleOwner, Observer {speed ->
            Log.d(TAG, "changing speed to $speed")
            current_speed = speed
        })
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
    }
    private fun call(){
        val phoneIntent = Intent(Intent.ACTION_CALL)
        phoneIntent.data = Uri.parse("tel:+821097550759")
        startActivity(phoneIntent)
    }
    @SuppressLint("MissingPermission")
    override fun onResults(score: Float, score2:Float){
        Log.d(TAG, "shouting: $score, stop: $score2, current speed: $current_speed")
        if (score > StopClassifier.THRESHOLD && score2 > StopClassifier.THRESHOLD2 && current_speed > SPEED_THRESHOLD){
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location : Location? ->
                    // Got last known location. In some rare situations this can be null.
                    val lat = location?.latitude.toString()
                    val lon = location?.longitude.toString()
                    val smsManager:SmsManager = requireActivity().getSystemService(SmsManager::class.java)
                    val msg =  "Current Location: \n Latitude: $lat \n Longitude: $lon"

                    Toast.makeText(requireContext(), "Sending message: $msg", Toast.LENGTH_LONG).show()

//                    smsManager.sendTextMessage("+821097550759", null, msg, null, null)
                }
//            call()
        }
        activity?.runOnUiThread {
            if (score > StopClassifier.THRESHOLD && score2 > StopClassifier.THRESHOLD2) {
                stopView.text = "STOP"
                stopView.setBackgroundColor(ProjectConfiguration.activeBackgroundColor)
                stopView.setTextColor(ProjectConfiguration.activeTextColor)
            } else {
                stopView.text = "NO STOP"
                stopView.setBackgroundColor(ProjectConfiguration.idleBackgroundColor)
                stopView.setTextColor(ProjectConfiguration.idleTextColor)
            }
        }
    }

    companion object {
        const val SPEED_THRESHOLD = 6  // 6 km/h
    }
}