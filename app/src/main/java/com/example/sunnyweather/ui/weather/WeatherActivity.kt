package com.example.sunnyweather.ui.weather

import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.sunnyweather.R
import com.example.sunnyweather.logic.model.Weather
import com.example.sunnyweather.logic.model.getSky
import kotlinx.android.synthetic.main.activity_weather.*
import kotlinx.android.synthetic.main.forcast.*
import kotlinx.android.synthetic.main.life_index.*
import kotlinx.android.synthetic.main.now.*
import java.text.SimpleDateFormat
import java.util.*

class WeatherActivity : AppCompatActivity() {
    private val TAG = "WeatherActivity"
    val viewModel by lazy { ViewModelProvider(this).get(WeatherViewModel::class.java) }
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weather)
        val decorView = window.decorView
//        这里被弃用了
//        decorView.systemUiVisibility=View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//        window.statusBarColor= Color.TRANSPARENT
        Log.d(TAG, "onCreate: 开始对位置赋值")
        if (viewModel.locationLng.isEmpty()){
            Log.d(TAG, "onCreate: 对经度进行赋值")
            viewModel.locationLng= intent.getStringExtra("location_lng")?:""
            Log.d(TAG, "onCreate:${viewModel.locationLng}")
        }
        if(viewModel.locationLat.isEmpty()){
            Log.d(TAG, "onCreate: 对维度进行赋值")
            viewModel.locationLat=intent.getStringExtra("location_lat")?:""
            Log.d(TAG, "onCreate:${viewModel.locationLat}")
        }
        if (viewModel.placeName.isEmpty()){
            viewModel.placeName=intent.getStringExtra("place_name")?:""
        }
        Log.d(TAG, "onCreate: ${viewModel.locationLng},${viewModel.locationLat}")

        viewModel.weatherLiveData.observe(this, Observer { result ->
            val weather = result.getOrNull()
            if (weather!=null){
                showWeatherInfo(weather)
            }else{
                Toast.makeText(this,"无法成功获取天气信息",Toast.LENGTH_SHORT).show()
                result.exceptionOrNull()?.printStackTrace()
            }
        })
        viewModel.refreshWeather(viewModel.locationLng,viewModel.locationLat)

    }

    private fun showWeatherInfo(weather: Weather) {
        placeName.text=viewModel.placeName
        val realtime=weather.realtime
        val daily = weather.daily
        // 填充now.xml布局中的数据
        val currentTempText = "${realtime.temperature.toInt()} ℃"
        currentTmp.text=currentTempText
        currentSky.text= getSky(realtime.skycon).info
        val currentPM25Text="空气指数${realtime.airQuality.aqi.chn.toInt()}"
        currentAQI.text=currentPM25Text
        nowLayout.setBackgroundResource(getSky(realtime.skycon).bg)
        // 填充forecast布局中的数据
        forecastLayout.removeAllViews()
        val days = daily.skycon.size
        for (i in 0 until days){
            val skycon=daily.skycon[i]
            val temperature = daily.temperature[i]
            val view = LayoutInflater.from(this).inflate(R.layout.forecast_item,forecastLayout,false)
            // 子View实列化
            val dateInfo=view.findViewById(R.id.dateInfo) as TextView
            val skyIcon = view.findViewById(R.id.skyIcon) as ImageView
            val skyInfo = view.findViewById(R.id.skyInfo) as TextView
            val temperatureInfo = view.findViewById(R.id.temperatureInfo) as TextView
            // 确定子View中需要填充的值
            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            dateInfo.text=simpleDateFormat.format(skycon.date)
            val sky = getSky(skycon.value)
            skyIcon.setImageResource(sky.icon)
            skyInfo.text=sky.info
            val tempText = "${temperature.min.toInt()}$~${temperature.max.toInt()}℃"
            temperatureInfo.text=tempText
            forecastLayout.addView(view)
        }
        // 填充life_index.xml布局中的数据
        val lifeIndex=daily.lifeIndex
        coldRiskText.text=lifeIndex.coldRisk[0].desc
        dressingText.text=lifeIndex.dressing[0].desc
        ultravioletText.text=lifeIndex.ultraviolet[0].desc
        carWashingText.text=lifeIndex.carWashing[0].desc
        weatherLayout.visibility=View.VISIBLE

    }
}