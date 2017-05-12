package vv.coolweather.util;

import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import vv.coolweather.db.City;
import vv.coolweather.db.County;
import vv.coolweather.db.Province;

/**
 * Created by Administrator on 2017/5/12.
 */

public class Utility {

    public static boolean handleProvinceResponse(String response){
        if (!TextUtils.isEmpty(response)){
            try {
                JSONArray allProvinces=new JSONArray(response);
                for (int i=0;i<allProvinces.length();++i){
                    JSONObject jsonObject=allProvinces.getJSONObject(i);
                    Province province=new Province();
                    province.setName(jsonObject.getString("name"));
                    province.setCode(jsonObject.getInt("id"));
                    province.save();
                    return true;
                }
            } catch (JSONException e) {
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
                    return true;
                }
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
                    county.setWeatherId(jsonObject.getInt("weather_id"));
                    county.setCityId(cityId);
                    county.save();
                    return true;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return false;
    }
}
