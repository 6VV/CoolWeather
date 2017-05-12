package vv.coolweather.db;

import org.litepal.crud.DataSupport;

/**
 * Created by Administrator on 2017/5/12.
 */

public class County extends DataSupport {
    private int id;
    private String mName;
    private String mWeatherId;
    private int mCityId;

    public int getId() {
        return id;
    }

    public String getName() {
        return mName;
    }

    public String getWeatherId() {
        return mWeatherId;
    }

    public int getCityId() {
        return mCityId;
    }

    public void setId(int id) {
        id = id;
    }

    public void setName(String name) {
        mName = name;
    }

    public void setWeatherId(String weatherId) {
        mWeatherId = weatherId;
    }

    public void setCityId(int cityId) {
        mCityId = cityId;
    }
}
