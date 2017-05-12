package vv.coolweather.db;

import org.litepal.crud.DataSupport;

/**
 * Created by Administrator on 2017/5/12.
 */

public class County extends DataSupport {
    private int mId;
    private String mName;
    private int mWeatherId;
    private int mCityId;

    public int getId() {
        return mId;
    }

    public String getName() {
        return mName;
    }

    public int getWeatherId() {
        return mWeatherId;
    }

    public int getCityId() {
        return mCityId;
    }
}
