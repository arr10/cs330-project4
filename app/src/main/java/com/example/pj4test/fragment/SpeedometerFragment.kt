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
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.example.pj4test.ProjectConfiguration
import com.example.pj4test.StatsViewModel
import com.example.pj4test.audioInference.StopClassifier
import com.example.pj4test.databinding.FragmentSpeedometerBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import java.util.Timer
import java.util.TimerTask
import kotlin.concurrent.scheduleAtFixedRate
import kotlin.concurrent.schedule

class SpeedometerFragment : Fragment(), SensorEventListener {
    private val TAG = "SpeedometerFragment"

    private var _fragmentSpeedometerBinding: FragmentSpeedometerBinding? = null
    private val fragmentSpeedometerBinding
        get() = _fragmentSpeedometerBinding!!

    var sensorManager: SensorManager?=null
    var sensor: Sensor?=null

    private var savedInstanceState: Bundle? = null

    private lateinit var tvSpeed :TextView



    private var _lastTick = System.currentTimeMillis()
    private var steps = 0
//    private var initial_steps = 0

    lateinit var stopClassifier: StopClassifier
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var task: TimerTask? = null
    private var startedListening = false

    private val viewModel: StatsViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _fragmentSpeedometerBinding = FragmentSpeedometerBinding.inflate(inflater, container, false)

        sensorManager = requireActivity().getSystemService(AppCompatActivity.SENSOR_SERVICE) as SensorManager
        sensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)

        return fragmentSpeedometerBinding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        stopClassifier.stop()
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
                    viewModel.setSpeed(speed)
                    tvSpeed.text = "$speed km/h"
                    steps = 0
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

        startSpeedTracking()
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
//        TODO("Not yet implemented")
        Log.d(TAG, "accuracy changed: $p1")
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event?.sensor?.type == Sensor.TYPE_STEP_DETECTOR) {
            steps ++
        }
    }

    override fun onPause() {
        super.onPause()

    }

    override fun onResume() {
        super.onResume()
    }
}
