package com.example.sunnyweather.ui.weather

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.drawerlayout.widget.DrawerLayout
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
        // 使窗口和状态栏更加吻合
        translucentStatusBar(this,true)
//        val decorView = window.decorView
////        这里被弃用了
//        decorView.systemUiVisibility=View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//        window.statusBarColor= Color.TRANSPARENT

        setContentView(R.layout.activity_weather)

        // 使用滑动窗口进行城市的搜索和切换
        navBtn.setOnClickListener{
            drawLayout.openDrawer(GravityCompat.START)
        }
        drawLayout.addDrawerListener(object:DrawerLayout.DrawerListener{
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
            }

            override fun onDrawerOpened(drawerView: View) {
            }

            override fun onDrawerClosed(drawerView: View) {
                // 滑动窗口关闭时将键盘也关闭
                val manager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                manager.hideSoftInputFromWindow(drawerView.windowToken,InputMethodManager.HIDE_NOT_ALWAYS)
            }

            override fun onDrawerStateChanged(newState: Int) {
            }

        })

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
            // 请求结束将下拉刷新取消
            swipeRefresh.isRefreshing=false
        })
        swipeRefresh.setColorSchemeResources(R.color.purple_500)
        // 观察数据信息是否有变化
        refreshWeather()
        // 进行下拉更新监控
        swipeRefresh.setOnRefreshListener {
            refreshWeather()
        }

    }

     fun refreshWeather(){
        viewModel.refreshWeather(viewModel.locationLng,viewModel.locationLat)
        swipeRefresh.isRefreshing=true
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
            val tempText = "${temperature.min.toInt()}~${temperature.max.toInt()}℃"
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

    // 将状态栏改变成透明颜色
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun translucentStatusBar(activity: Activity, hideStatusBarBackground: Boolean) {
        val window = activity.window
        //添加Flag把状态栏设为可绘制模式
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        if (hideStatusBarBackground) {
            //如果为全透明模式，取消设置Window半透明的Flag
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            //设置状态栏为透明
            window.statusBarColor = Color.TRANSPARENT
            //设置window的状态栏不可见
            window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        } else {
            //如果为半透明模式，添加设置Window半透明的Flag
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            //设置系统状态栏处于可见状态
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
        }
        //view不根据系统窗口来调整自己的布局
        val mContentView = window.findViewById(Window.ID_ANDROID_CONTENT) as ViewGroup
        val mChildView = mContentView.getChildAt(0)
        if (mChildView != null) {
            ViewCompat.setFitsSystemWindows(mChildView, false)
            ViewCompat.requestApplyInsets(mChildView)
        }
    }


}