package com.example.pj4test.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
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
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.pj4test.ProjectConfiguration
import com.example.pj4test.audioInference.StopClassifier
import com.example.pj4test.databinding.FragmentSpeedometerBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import java.util.Timer
import java.util.TimerTask
import kotlin.concurrent.scheduleAtFixedRate
import kotlin.concurrent.schedule

class SpeedometerFragment : Fragment(), SensorEventListener, StopClassifier.DetectorListener {
    private val TAG = "SpeedometerFragment"

    private var _fragmentSpeedometerBinding: FragmentSpeedometerBinding? = null
    private val fragmentSpeedometerBinding
        get() = _fragmentSpeedometerBinding!!

    var sensorManager: SensorManager?=null
    var sensor: Sensor?=null

    private var savedInstanceState: Bundle? = null

    private lateinit var tvSpeed :TextView
    private lateinit var tvListening :TextView
    private lateinit var tvStop :TextView



    private var _lastTick = System.currentTimeMillis()
    private var steps = 0
//    private var initial_steps = 0

    lateinit var stopClassifier: StopClassifier
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var task: TimerTask? = null
    private var startedListening = false

    override fun onCreate(savedInstanceState: Bundle?) {
        this.savedInstanceState = savedInstanceState
        super.onCreate(savedInstanceState)

        sensorManager = requireActivity().getSystemService(AppCompatActivity.SENSOR_SERVICE) as SensorManager
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorManager?.unregisterListener(this)
        task?.cancel()
        task = null
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _fragmentSpeedometerBinding = FragmentSpeedometerBinding.inflate(inflater, container, false)

        sensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
        stopClassifier = StopClassifier()
        stopClassifier.initialize(requireContext())
        stopClassifier.setDetectorListener(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        return fragmentSpeedometerBinding.root
    }

    private fun startSpeedTracking() {
        if (sensor != null) {
            sensorManager?.registerListener(this, sensor, SensorManager.SENSOR_DELAY_FASTEST)
            if (task == null) {
                // Each 750 ms we update the speed
                task = Timer().scheduleAtFixedRate(0, 750) {
                    val tick = System.currentTimeMillis()
                    val localPeriod: Long = tick - _lastTick
                    _lastTick = tick
                    // One step is about 0.8 meters
                    val speed = (steps.toFloat() * 0.8f * 3600000.0f / localPeriod) / 1000.0f
                    tvSpeed.text = "$speed km/h"
                    steps = 0

                    if(speed > SPEED_THRESHOLD && !startedListening) {
                        stopClassifier.start()
                        startedListening = true

                        tvListening.text = "LISTENING"
                        tvListening.setBackgroundColor(ProjectConfiguration.activeBackgroundColor)
                        tvListening.setTextColor(ProjectConfiguration.activeTextColor)

                        Timer().schedule(60000) {
                            stopClassifier.stop()
                            startedListening = false

                            tvListening.text = "NOT LISTENING"
                            tvListening.setBackgroundColor(ProjectConfiguration.idleBackgroundColor)
                            tvListening.setTextColor(ProjectConfiguration.idleTextColor)
                        }
                    }
                }
            }
        } else {
            // This will give a toast message to the user if there is no sensor in the device
            Toast.makeText(requireContext(), "No sensor detected on this device", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvSpeed = fragmentSpeedometerBinding.speedometerTextView
        tvListening = fragmentSpeedometerBinding.listeningTextView
        tvStop = fragmentSpeedometerBinding.stopTextView

        startSpeedTracking()
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
//        TODO("Not yet implemented")
        Log.d(TAG, "accuracy changed: $p1")
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event?.sensor?.type == Sensor.TYPE_STEP_DETECTOR) {
            steps ++
//            if(initial_steps == 0) {
//                initial_steps = event.values[0].toInt();
//            }
//            steps += event.values[0].toInt() - initial_steps;
        }
    }

    override fun onPause() {
        super.onPause()
//        Log.d(TAG, "unregistering sensor listener!");
//        sensorManager?.unregisterListener(this)
//        task?.cancel()
//        task = null
//        initial_steps = -1
    }

    override fun onResume() {
        super.onResume()
//        Log.d(TAG, "registering sensor listener!");
    }

    private fun call(){
        val phoneIntent = Intent(Intent.ACTION_CALL)
        phoneIntent.data = Uri.parse("tel:+821096016349")
        startActivity(phoneIntent)
    }

    @SuppressLint("MissingPermission")
    override fun onResults(score: Float, score2:Float){
        Log.d(TAG, "shouting: $score, stop: $score2")
        if (score > StopClassifier.THRESHOLD && score2 > StopClassifier.THRESHOLD2){
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location : Location? ->
                    // Got last known location. In some rare situations this can be null.
                    Log.d(TAG, "her ehrer ")
                    val lat = location?.latitude.toString()
                    val lon = location?.longitude.toString()
                    val smsManager: SmsManager = requireActivity().getSystemService(SmsManager::class.java)
                    val msg =  "Current Location: \n Latitude: $lat \n Longitude: $lon"
                    Toast.makeText(requireContext(), "Sending message: $msg", Toast.LENGTH_LONG).show()
//                    smsManager.sendTextMessage("+821097550759", null, msg, null, null)
                }
            call()
        }
        activity?.runOnUiThread {
            if (score > StopClassifier.THRESHOLD && score2 > StopClassifier.THRESHOLD2) {
                tvStop.text = "STOP"
                tvStop.setBackgroundColor(ProjectConfiguration.activeBackgroundColor)
                tvStop.setTextColor(ProjectConfiguration.activeTextColor)
            } else {
                tvStop.text = "NO STOP"
                tvStop.setBackgroundColor(ProjectConfiguration.idleBackgroundColor)
                tvStop.setTextColor(ProjectConfiguration.idleTextColor)
            }
        }
    }

    companion object {
        const val SPEED_THRESHOLD = 6  // 6 km/h
    }
}
