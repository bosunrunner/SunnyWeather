package com.example.sunnyweather.logic.model

import com.google.gson.annotations.SerializedName

data class RealTimeResponse(val status:String,val result:Result){
    data class Result(val realtime:Realtime)

    data class Realtime(val temperature:Float,val skycon:String,
    @SerializedName("air_quality") val airQuality:AirQuality)

    data class AirQuality(val aqi:AQI)
    data class AQI(val chn:Float)
}

