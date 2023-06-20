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
import androidx.core.app.ActivityCompat
import androidx.core.content.getSystemService
import androidx.fragment.app.Fragment
import com.example.pj4test.ProjectConfiguration
import com.example.pj4test.audioInference.SnapClassifier
import com.example.pj4test.databinding.FragmentAudioBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

class AudioFragment: Fragment(), SnapClassifier.DetectorListener {
    private val TAG = "AudioFragment"

    private var _fragmentAudioBinding: FragmentAudioBinding? = null

    private val fragmentAudioBinding
        get() = _fragmentAudioBinding!!

    // classifiers
    lateinit var snapClassifier: SnapClassifier
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // views
    lateinit var snapView: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _fragmentAudioBinding = FragmentAudioBinding.inflate(inflater, container, false)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        return fragmentAudioBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        snapView = fragmentAudioBinding.SnapView

        snapClassifier = SnapClassifier()
        snapClassifier.initialize(requireContext())
        snapClassifier.setDetectorListener(this)
    }

    override fun onPause() {
        super.onPause()
//        snapClassifier.stopInferencing()

    }

    override fun onResume() {
        super.onResume()
//        snapClassifier.stopInferencing()

        snapClassifier.startInferencing()
    }
    private fun call(){
        val phoneIntent = Intent(Intent.ACTION_CALL)
        phoneIntent.data = Uri.parse("tel:+821097550759")
        startActivity(phoneIntent)
    }
    @SuppressLint("MissingPermission")
    override fun onResults(score: Float, score2:Float){
        Log.d(TAG, "shouting: $score, stop: $score2")
        if (score > SnapClassifier.THRESHOLD && score2 > SnapClassifier.THRESHOLD){
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location : Location? ->
                    // Got last known location. In some rare situations this can be null.
                    val lat = location?.latitude.toString()
                    val lon = location?.longitude.toString()
                    val smsManager:SmsManager = requireActivity().getSystemService(SmsManager::class.java)
                    val msg =  "Current Location: \n Latitude: $lat \n Longitude: $lon"
//                    smsManager.sendTextMessage("+821097550759", null, msg, null, null)
                }
//            call()
        }
        activity?.runOnUiThread {
            if (score > SnapClassifier.THRESHOLD && score2 > SnapClassifier.THRESHOLD2) {
                snapView.text = "STOP"
                snapView.setBackgroundColor(ProjectConfiguration.activeBackgroundColor)
                snapView.setTextColor(ProjectConfiguration.activeTextColor)
            } else {
                snapView.text = "NO STOP"
                snapView.setBackgroundColor(ProjectConfiguration.idleBackgroundColor)
                snapView.setTextColor(ProjectConfiguration.idleTextColor)
            }
        }
    }
}