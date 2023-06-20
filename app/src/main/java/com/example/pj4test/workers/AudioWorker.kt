package com.example.pj4test.workers

import android.content.Context
import android.util.Log
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.pj4test.audioInference.SnapClassifier

private const val TAG = "AudioWorker"

class AudioWorker(ctx: Context, params: WorkerParameters) : Worker(ctx, params), SnapClassifier.DetectorListener {
    lateinit var snapClassifier: SnapClassifier
    override fun doWork(): Result {
        val appContext = applicationContext

        snapClassifier = SnapClassifier()
        snapClassifier.initialize(appContext)
        snapClassifier.setDetectorListener(this)
        snapClassifier.startInferencing()

        return try {
//            val score = snapClassifier.inference()
            Log.e(TAG, "Good")
            Thread.sleep(10000)
            snapClassifier.stopInferencing()
            Result.success();
        }
        catch (throwable: Throwable) {
            Log.e(TAG, "Error")
            Result.failure()
        }
    }

    override fun onResults(score: Float) {
        Log.d(TAG,"score: $score")
    }
}