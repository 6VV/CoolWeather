package vv.coolweather.util;

import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import vv.coolweather.db.City;
import vv.coolweather.db.County;
import vv.coolweather.db.Province;
import vv.coolweather.gson.Weather;

/**
 * Created by Administrator on 2017/5/12.
 */

public class Utility {

    private static final String TAG = "Utility";

    public static boolean handleProvinceResponse(String response){
        if (!TextUtils.isEmpty(response)){
            try {
                JSONArray allProvinces=new JSONArray(response);
                for (int i=0;i<allProvinces.length();++i){
                    JSONObject jsonObject=allProvinces.getJSONObject(i);
                    Province province=new Province();
                    province.setName(jsonObject.getString("name"));
                    province.setCode(jsonObject.getInt("id"));
                    if (!province.save()){
                        Log.e(TAG, "handleProvinceResponse: save false");
                        return false;
                    }
                }

                return true;
            } catch (JSONException e) {
                Log.e(TAG, "handleProvinceResponse: parse error");
                e.printStackTrace();
            }
        }

       return false;
    }

    public static boolean handleCityResponse(String response,int provinceId){
        if (!TextUtils.isEmpty(response)){
            try {
                JSONArray allProvinces=new JSONArray(response);
                for (int i=0;i<allProvinces.length();++i){
                    JSONObject jsonObject=allProvinces.getJSONObject(i);
                    City city=new City();
                    city.setName(jsonObject.getString("name"));
                    city.setCode(jsonObject.getInt("id"));
                    city.setProvinceId(provinceId);
                    city.save();
                }
                return true;

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return false;
    }

    public static boolean handleCountyResponse(String response,int cityId){
        if (!TextUtils.isEmpty(response)){
            try {
                JSONArray allProvinces=new JSONArray(response);
                for (int i=0;i<allProvinces.length();++i){
                    JSONObject jsonObject=allProvinces.getJSONObject(i);
                    County county=new County();
                    county.setName(jsonObject.getString("name"));
                    county.setWeatherId(jsonObject.getString("weather_id"));
                    county.setCityId(cityId);
                    county.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return false;
    }

    public static Weather handleWeatherResponse(String response){
        try {
            JSONObject jsonObject=new JSONObject(response);
            JSONArray jsonArray=jsonObject.getJSONArray("HeWeather");
            String weatherContent=jsonArray.getJSONObject(0).toString();
            return new Gson().fromJson(weatherContent, Weather.class);
        } catch (JSONException e) {
            Log.e(TAG, "handleWeatherResponse: parse failed");
            e.printStackTrace();
        }
        return null;
    }
}
