package com.example.pj4test.fragment

import android.annotation.SuppressLint
import android.content.IntentFilter
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.pj4test.R
import com.example.pj4test.SpeedometerViewModel
import com.example.pj4test.audioInference.SnapClassifier
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Timer
import java.util.TimerTask
import kotlin.concurrent.scheduleAtFixedRate
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sqrt

class SpeedometerFragment : Fragment(), SensorEventListener {
    private val TAG = "SpeedometerFragment"

    var sensorManager: SensorManager?=null
    var sensor: Sensor?=null

    private var savedInstanceState: Bundle? = null

    private lateinit var tvSpeed :TextView
    private lateinit var viewModel: SpeedometerViewModel

    private var _lastTick = System.currentTimeMillis()
    private var steps = 0
    private var initial_steps = 0


    private var task: TimerTask? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        this.savedInstanceState = savedInstanceState
        super.onCreate(savedInstanceState)

        sensorManager = requireActivity().getSystemService(AppCompatActivity.SENSOR_SERVICE) as SensorManager
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_speedometer, container, false)

        viewModel = ViewModelProvider(this)[SpeedometerViewModel::class.java]

        sensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)

        tvSpeed = view.findViewById(R.id.SpeedometerView)

        return view
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        // Do something here if sensor accuracy changes.
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
        Log.d(TAG, "unregistering sensor listener!");
        tvSpeed.text = "0 km/h"
        sensorManager?.unregisterListener(this)
        task?.cancel()
        task = null
        initial_steps = -1
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "registering sensor listener!");

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
                }
            }
        } else {
            // This will give a toast message to the user if there is no sensor in the device
            Toast.makeText(requireContext(), "No sensor detected on this device", Toast.LENGTH_SHORT).show()
        }
    }

}
