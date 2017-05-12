package vv.coolweather;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import vv.coolweather.db.City;
import vv.coolweather.db.County;
import vv.coolweather.db.Province;
import vv.coolweather.util.HttpUtil;
import vv.coolweather.util.Utility;

/**
 * Created by Administrator on 2017/5/12.
 */

public class ChooseAreaFragment extends Fragment {

    private static final String TAG = "ChooseAreaFragment";

    private static final int LEVEL_PROVINCE=0;
    private static final int LEVEL_CITY=1;
    private static final int LEVEL_COUNTY=2;

    private ProgressDialog mProgressDialog;
    private TextView mTitleTextView;
    private Button mBackButton;
    private ListView mListView;

    private ArrayAdapter<String> mArrayAdapter;
    private List<String> mDataList=new ArrayList<>();
    private List<Province> mProvinceList=new ArrayList<>();
    private List<City> mCityList=new ArrayList<>();
    private List<County> mCountyList=new ArrayList<>();
    private Province mSelectedProvince;
    private City mSelectedCity;

    private int mCurrentLevel=LEVEL_PROVINCE;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.choose_area,container,false);
        mTitleTextView= (TextView) view.findViewById(R.id.title_text_view);
        mBackButton= (Button) view.findViewById(R.id.back_button);
        mListView= (ListView) view.findViewById(R.id.list_view);

        mArrayAdapter=new ArrayAdapter<String>(getContext(),android.R.layout.simple_list_item_1,mDataList);
        mListView.setAdapter(mArrayAdapter);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (mCurrentLevel==LEVEL_PROVINCE){
                    mSelectedProvince=mProvinceList.get(i);
                    queryCities();
                }else if (mCurrentLevel==LEVEL_CITY){
                    mSelectedCity=mCityList.get(i);
                    queryCounties();
                }else if (mCurrentLevel==LEVEL_COUNTY){
                    String weatherId=mCountyList.get(i).getWeatherId();
                    if (getActivity() instanceof MainActivity){
                        WeatherActivity.actionStart(getActivity(),weatherId);
                    }else if (getActivity() instanceof WeatherActivity){
                        WeatherActivity activity= (WeatherActivity) getActivity();
                        activity.drawerLayoutRefresh();
                    }
                }
            }
        });
        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCurrentLevel==LEVEL_COUNTY){
                    queryCities();
                }else if (mCurrentLevel==LEVEL_CITY){
                    queryProvinces();
                }
            }
        });

        queryProvinces();
    }

    private void queryProvinces() {
        mTitleTextView.setText("中国");
        mBackButton.setVisibility(View.GONE);
        mProvinceList= DataSupport.findAll(Province.class);
        if (!updateProvinceList()){
            String address="http://guolin.tech/api/china";
            queryFromServer(address,"province");
        }
    }

    private boolean updateProvinceList(){
        mProvinceList= DataSupport.findAll(Province.class);
        if (mProvinceList.size()>0){
            mDataList.clear();
            for (Province province:mProvinceList){
                mDataList.add(province.getName());
            }
            mArrayAdapter.notifyDataSetChanged();
            mListView.setSelection(0);
            mCurrentLevel=LEVEL_PROVINCE;
            return true;
        }

        return false;
    }

    private void queryCounties() {
        mTitleTextView.setText(mSelectedCity.getName());
        mBackButton.setVisibility(View.VISIBLE);
        if (!updateCountyList()){
            int provinceCode=mSelectedProvince.getCode();
            int cityCode=mSelectedCity.getCode();
            String address="http://guolin.tech/api/china/"+provinceCode+"/"+cityCode;
            Log.d(TAG, "queryCounties: "+address);
            queryFromServer(address,"county");
        }
    }

    private boolean updateCountyList(){
        mCountyList = DataSupport.where("mcityid=?", String.valueOf(mSelectedCity.getId())).find(County.class);
        if (mCountyList.size()>0){
            mDataList.clear();
            for (County county:mCountyList){
                mDataList.add(county.getName());
            }
            mArrayAdapter.notifyDataSetChanged();
            mListView.setSelection(0);
            mCurrentLevel=LEVEL_COUNTY;
            return true;
        }

        return false;
    }

    private void queryCities() {
        mTitleTextView.setText(mSelectedProvince.getName());
        mBackButton.setVisibility(View.VISIBLE);
        if (!updateCityList()){
            int provinceCode=mSelectedProvince.getCode();
            String address="http://guolin.tech/api/china/"+provinceCode;
            queryFromServer(address,"city");
        }
    }

    private boolean updateCityList(){
        mCityList=DataSupport.where("mprovinceid=?",String.valueOf(mSelectedProvince.getId())).find(City.class);
        if (mCityList.size()>0){
            mDataList.clear();
            for (City city:mCityList){
                mDataList.add(city.getName());
            }
            mArrayAdapter.notifyDataSetChanged();
            mListView.setSelection(0);
            mCurrentLevel=LEVEL_CITY;
            return true;
        }

        return false;
    }

    private void queryFromServer(String address, final String type) {
        showProgressDialog();

        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(getContext(),"加载失败",Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText=response.body().string();
                boolean result=false;

                if (type.equals("province")){
                    result= Utility.handleProvinceResponse(responseText);
                }else if (type.equals("city")){
                    result=Utility.handleCityResponse(responseText,mSelectedProvince.getId());
                }else if (type.equals("county")){
                    result=Utility.handleCountyResponse(responseText,mSelectedCity.getId());
                }

                if (result){
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if (type.equals("province")){
                                updateProvinceList();
                            }else if (type.equals("city")){
                                updateCityList();
                            }else if (type.equals("county")){
                                updateCountyList();
                            }
                        }
                    });
                }
            }
        });
    }

    private void showProgressDialog(){
        if (mProgressDialog==null){
            mProgressDialog=new ProgressDialog(getActivity());
            mProgressDialog.setMessage("正在加载...");
            mProgressDialog.setCancelable(false);
        }
        mProgressDialog.show();
    }

    private void closeProgressDialog() {
        if (mProgressDialog!=null){
            mProgressDialog.dismiss();
        }
    }
}
