package com.example.pj4test

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class StatsViewModel : ViewModel() {
    private val mutableSpeed = MutableLiveData<Float>()
    val speed: LiveData<Float> get() = mutableSpeed

    fun setSpeed(input:Float){
        mutableSpeed.postValue(input)
    }

}