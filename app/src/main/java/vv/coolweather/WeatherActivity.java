package vv.coolweather;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import vv.coolweather.gson.Forecast;
import vv.coolweather.gson.Weather;
import vv.coolweather.util.HttpUtil;
import vv.coolweather.util.Utility;

public class WeatherActivity extends AppCompatActivity {

    private static final String TAG=WeatherActivity.class.getSimpleName();

    private static final String WEATHER_ID="weather_id";

    private ScrollView mWeatherLayout;
    private TextView mTitleCity;
    private TextView mTitleUpdateTime;
    private TextView mDegreeText;
    private TextView mWeatherInfoText;
    private LinearLayout mForecastLayout;
    private TextView mAqiText;
    private TextView mPm25Text;
    private TextView mComfortText;
    private TextView mCarWashText;
    private TextView mSportText;

    private ImageView mBackgroundView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private DrawerLayout mDrawerLayout;

    private String mWeatherId;

    public static void actionStart(Context context,String weatherId){
        Intent intent=new Intent(context,WeatherActivity.class);
        intent.putExtra(WEATHER_ID,weatherId);
        context.startActivity(intent);
    }

    public void drawerLayoutRefresh(){
        mDrawerLayout.closeDrawers();
        mSwipeRefreshLayout.setRefreshing(true);
        requestWeather(mWeatherId);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT>21){
            View decorView=getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }

        setContentView(R.layout.activity_weather);

        mWeatherLayout= (ScrollView) findViewById(R.id.weather_layout);
        mTitleCity = (TextView) findViewById(R.id.title_city_text_view);
        mTitleUpdateTime = (TextView) findViewById(R.id.title_update_time_text_view);
        mDegreeText = (TextView) findViewById(R.id.degree_text_view);
        mWeatherInfoText = (TextView) findViewById(R.id.weather_info_text_view);
        mForecastLayout = (LinearLayout) findViewById(R.id.forecast_layout);
        mAqiText = (TextView) findViewById(R.id.aqi_text_view);
        mPm25Text = (TextView) findViewById(R.id.pm25_text_view);
        mComfortText = (TextView) findViewById(R.id.comfort_text_view);
        mCarWashText = (TextView) findViewById(R.id.car_wash_text_view);
        mSportText = (TextView) findViewById(R.id.sport_text_view);
        mBackgroundView= (ImageView) findViewById(R.id.background_view);
        mSwipeRefreshLayout= (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
        mDrawerLayout= (DrawerLayout) findViewById(R.id.drawer_layout);

        SharedPreferences preferences= PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString=preferences.getString("weather",null);
        mWeatherId=getIntent().getStringExtra(WEATHER_ID);
        if (weatherString!=null){
            Log.d(TAG, "file text: "+weatherString);
            Weather weather= Utility.handleWeatherResponse(weatherString);
            if (weather.basic.weatherId.equals(mWeatherId)){
                showWeatherInfo(weather);
            }else{
                mWeatherLayout.setVisibility(View.INVISIBLE);
                requestWeather(mWeatherId);
            }
        }else{
            mWeatherLayout.setVisibility(View.INVISIBLE);
            requestWeather(mWeatherId);
        }

        findViewById(R.id.nav_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDrawerLayout.openDrawer(GravityCompat.START);
            }
        });

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestWeather(mWeatherId);
            }
        });
        loadBingPic();
    }

    private void loadBingPic() {
        String requestBingPic="http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String bingPic=response.body().string();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(WeatherActivity.this).load(bingPic).into(mBackgroundView);
                    }
                });
            }
        });
    }

    private void requestWeather(String weatherId) {
        String weatherUrl = "http://guolin.tech/api/weather?cityid=" + weatherId + "&key=bc0418b57b2d4918819d3974ac1285d9";
//        String weatherUrl="https://free-api.heweather.com/v5/weather?city="+weatherId+"&key="+"9a227f91fff347dbbd2cf1a60ee0e2c0";
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "request failed");
                        Toast.makeText(WeatherActivity.this,"获取天气信息失败",Toast.LENGTH_LONG).show();
                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText=response.body().string();
                Log.d(TAG, "onResponse: "+responseText);
                final Weather weather=Utility.handleWeatherResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (weather!=null && weather.status.equals("ok")){
                            SharedPreferences.Editor editor=PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("weather",responseText);
                            editor.apply();
                            showWeatherInfo(weather);
                        }else{
                            Toast.makeText(WeatherActivity.this,"获取天气信息失败",Toast.LENGTH_LONG).show();
                        }
                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                });
            }
        });
    }

    private void showWeatherInfo(Weather weather) {
        String cityName=weather.basic.cityName;
        String updateTime=weather.basic.update.updateTime;
        String degree=weather.now.temperature+"°C";
        String weatherInfo=weather.now.more.info;
        mTitleCity.setText(cityName);
        mTitleUpdateTime.setText(updateTime);
        mDegreeText.setText(degree);
        mWeatherInfoText.setText(weatherInfo);

        mForecastLayout.removeAllViews();
        for (Forecast forecast:weather.forecastList){
            View view= LayoutInflater.from(this).inflate(R.layout.forecast_item,mForecastLayout,false);
            TextView dateTimeView= (TextView) view.findViewById(R.id.date_text_view);
            TextView infoTextView= (TextView) view.findViewById(R.id.info_text_view);
            TextView maxTextView= (TextView) view.findViewById(R.id.max_text_view);
            TextView minTextView = (TextView) view.findViewById(R.id.min_text_view);
            dateTimeView.setText(forecast.date);
            infoTextView.setText(forecast.more.info);
            maxTextView.setText(forecast.temperature.max);
            minTextView.setText(forecast.temperature.min);
            mForecastLayout.addView(view);
        }

        if (weather.aqi!=null){
            mAqiText.setText(weather.aqi.city.aqi);
            mPm25Text.setText(weather.aqi.city.pm25);
        }

        String comfort="舒适度："+weather.suggestion.comfort.info;
        String carWash="洗车指数："+weather.suggestion.carWash.info;
        String sport="运动建议："+weather.suggestion.sport.info;
        mComfortText.setText(comfort);
        mCarWashText.setText(carWash);
        mSportText.setText(sport);
        mWeatherLayout.setVisibility(View.VISIBLE);
    }
}
