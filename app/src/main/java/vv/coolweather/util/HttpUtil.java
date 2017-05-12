package vv.coolweather.util;

import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * Created by Administrator on 2017/5/12.
 */

public class HttpUtil {
    public static void sendOkHttpRequest(String address,okhttp3.Callback callback){
        OkHttpClient client=new OkHttpClient();
        Request request=new Request.Builder().url(address).build();
        client.newCall(request).enqueue(callback);
    }

    public static String getWeatherUrl(String weatherId){
        return "http://guolin.tech/api/weather?cityid=" + weatherId + "&key=bc0418b57b2d4918819d3974ac1285d9";
//        return  "https://free-api.heweather.com/v5/weather?city="+weatherId+"&key="+"9a227f91fff347dbbd2cf1a60ee0e2c0";
    }

    public static String weatherResponseHeader(){
        return "HeWeather";
    }
}
