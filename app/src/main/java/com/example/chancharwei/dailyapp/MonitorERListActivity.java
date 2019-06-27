package com.example.chancharwei.dailyapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;

import com.example.chancharwei.dailyapp.assist.MyItemTouchHelperCallBack;
import com.example.chancharwei.dailyapp.data.ExchangeRateMonitorData;
import com.example.chancharwei.dailyapp.data.ExchangeRateMonitorRecord;

import java.util.ArrayList;

public class MonitorERListActivity extends AppCompatActivity implements MonitorERListAdapter.ExchangeRateAdapterOnclickHandler{
    private static final String TAG = MonitorERListActivity.class.getName();
    private String[] currentData;
    ExchangeRateMonitorRecord exchangeRateMonitorRecord;
    private RecyclerView mRecyclerView;
    private MonitorERListAdapter mExListAdapter;
    private ArrayList<String[]> mCashRateBuyInList, mSpotRateBuyInList, mCashRateSellOutList, mSpotRateSellOutList;
    private ArrayList<Long> mCashRateBuyInList_ID, mSpotRateBuyInList_ID, mCashRateSellOutList_ID, mSpotRateSellOutList_ID;
    private CheckBox checkBoxCashRate,checkBoxSpotRate,checkBoxBuyIn,checkBoxSellOut;
    public boolean isCashRateCheckBox,isSpotRateCheckBox,isBuyInCheckBox,isSellOutCheckBox;
    public boolean needUpdateList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG,"onCreate");
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_monitor_erlist);
        exchangeRateMonitorRecord = new ExchangeRateMonitorRecord(this,false);
        mRecyclerView = findViewById(R.id.recyclerview_exchangeratelist);
        checkBoxCashRate = findViewById(R.id.checkBox_CashRate);
        checkBoxSpotRate = findViewById(R.id.checkBox_SpotRate);
        checkBoxBuyIn = findViewById(R.id.checkBox_BuyIn);
        checkBoxSellOut = findViewById(R.id.checkBox_SellOut);
        Intent intent = getIntent();
        currentData = intent.getStringArrayExtra("currentData");
        LinearLayoutManager layoutManager = new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);
        mRecyclerView.setLayoutManager(layoutManager);  //can use 3 layoutManager LinearLayoutManager,StaggeredGridLayoutManager,GridLayoutManager
        mRecyclerView.setHasFixedSize(true);
        mExListAdapter = new MonitorERListAdapter(this);
        mRecyclerView.setAdapter(mExListAdapter);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new MyItemTouchHelperCallBack(this));
        itemTouchHelper.attachToRecyclerView(mRecyclerView);
        assignDataInfoToList();
        needUpdateList = false;
        setCheckBox();
    }

    @Override
    protected void onDestroy() {
        if(exchangeRateMonitorRecord != null){
            exchangeRateMonitorRecord.close();
        }
        super.onDestroy();
    }

    /**
     * This function make all monitor data in database assign to each list, as list name,
     * without "_ID" record all data, with "_ID" record the database data ID(row id),
     * we can easy to use this "_ID" list update data in database by ui
     */
    private void assignDataInfoToList(){
        ArrayList<ExchangeRateMonitorData> monitorDataList = exchangeRateMonitorRecord.querAllData();
        if(monitorDataList == null){
            Log.i(TAG,"no monitor data in database");
            return;
        }
        double currentExchangeRate;
        mCashRateBuyInList = new ArrayList();
        mSpotRateBuyInList = new ArrayList();
        mCashRateSellOutList = new ArrayList();
        mSpotRateSellOutList = new ArrayList();
        mCashRateBuyInList_ID = new ArrayList();
        mSpotRateBuyInList_ID = new ArrayList();
        mCashRateSellOutList_ID = new ArrayList();
        mSpotRateSellOutList_ID = new ArrayList();
        for(ExchangeRateMonitorData data : monitorDataList){
            Log.d(TAG,"Start >>>>>");
            Log.d(TAG,"getId = "+data.getId());
            Log.d(TAG,"getNowCurrency = "+data.getNowCurrency());
            Log.d(TAG,"getTargetCurrency = "+data.getTargetCurrency());
            Log.d(TAG,"getTypeOfExchangeRate = "+data.getTypeOfExchangeRate());
            Log.d(TAG,"getExpectedExchangeRate = "+data.getExpectedExchangeRate());
            Log.d(TAG,"END >>>>>>");
            if(data.getExpectedExchangeRate()!=-1){
                if(data.getTypeOfExchangeRate().equals(ExchangeRateMonitorData.CashRateName)){
                    if(data.getNowCurrency().equals(MonitorExRateActivity.TaiwanMoneyName)){
                        currentExchangeRate = findCurrentExchangeRate(data,2);
                        mCashRateBuyInList.add(assignDataToArray(data,currentExchangeRate));
                        mCashRateBuyInList_ID.add(data.getId());
                    }else if(data.getTargetCurrency().equals(MonitorExRateActivity.TaiwanMoneyName)){
                        currentExchangeRate = findCurrentExchangeRate(data,1);
                        mCashRateSellOutList.add(assignDataToArray(data,currentExchangeRate));
                        mCashRateSellOutList_ID.add(data.getId());
                    }

                }else if(data.getTypeOfExchangeRate().equals(ExchangeRateMonitorData.SpotRateName)){
                    if(data.getNowCurrency().equals(MonitorExRateActivity.TaiwanMoneyName)){
                        currentExchangeRate = findCurrentExchangeRate(data,4);
                        mSpotRateBuyInList.add(assignDataToArray(data,currentExchangeRate));
                        mSpotRateBuyInList_ID.add(data.getId());
                    }else if(data.getTargetCurrency().equals(MonitorExRateActivity.TaiwanMoneyName)){
                        currentExchangeRate = findCurrentExchangeRate(data,3);
                        mSpotRateSellOutList.add(assignDataToArray(data,currentExchangeRate));
                        mSpotRateSellOutList_ID.add(data.getId());
                    }
                }
            }
        }
    }

    /**
     * As function name, need to find the current exchangeRate, we need to parsing the value show to ui
     *
     * @param data The data(monitor data) in database.
     * @param offsetNum the number we can mapping to save data order
     *                  (1:sellOut with cashRate,2:buyIn with cashRate,3:sellOut with spotRate,4:buyIn with spotRate).
     * @return Current mapping exchangeRate.
     */
    private double findCurrentExchangeRate(ExchangeRateMonitorData data,int offsetNum){
        if (currentData == null) {
            Log.e(TAG, "get null current data");
            return -1;
        }

        for(int i=0;i<currentData.length;i+=5){
            if(offsetNum == 1 || offsetNum == 3){
                if(data.getNowCurrency().equals(currentData[i])){
                    if(offsetNum == 1){
                        return Double.parseDouble(currentData[i+1]);
                    }else{
                        return Double.parseDouble(currentData[i+3]);
                    }
                }
            }else if(offsetNum == 2 || offsetNum == 4){
                if(data.getTargetCurrency().equals(currentData[i])){
                    if(offsetNum == 2){
                        return Double.parseDouble(currentData[i+2]);
                    }else{
                        return Double.parseDouble(currentData[i+4]);
                    }
                }
            }
        }
        Log.d(TAG, "no match data in currentData");
        return -1;
    }
    private void setCheckBox(){
        checkBoxCashRate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean select) {
                if(select){
                    checkBoxSpotRate.setChecked(false);
                    if(needUpdateList){
                        assignDataInfoToList();
                    }
                    if(checkBoxBuyIn.isChecked()){
                        mExListAdapter.setExchangeRateDataList(mCashRateBuyInList);
                    }else if(checkBoxSellOut.isChecked()){
                        mExListAdapter.setExchangeRateDataList(mCashRateSellOutList);
                    }
                    isCashRateCheckBox = true;
                    isSpotRateCheckBox = false;
                }
            }
        });

        checkBoxSpotRate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean select) {
                if(select){
                    checkBoxCashRate.setChecked(false);
                    if(needUpdateList){
                        assignDataInfoToList();
                    }
                    if(checkBoxBuyIn.isChecked()){
                        mExListAdapter.setExchangeRateDataList(mSpotRateBuyInList);
                    }else if(checkBoxSellOut.isChecked()){
                        mExListAdapter.setExchangeRateDataList(mSpotRateSellOutList);
                    }
                    isCashRateCheckBox = false;
                    isSpotRateCheckBox = true;
                }
            }
        });

        checkBoxBuyIn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean select) {
                if(select){
                    checkBoxSellOut.setChecked(false);
                    if(needUpdateList){
                        assignDataInfoToList();
                    }
                    if(checkBoxCashRate.isChecked()){
                        mExListAdapter.setExchangeRateDataList(mCashRateBuyInList);
                    }else if(checkBoxSpotRate.isChecked()){
                        mExListAdapter.setExchangeRateDataList(mSpotRateBuyInList);
                    }
                    isBuyInCheckBox = true;
                    isSellOutCheckBox = false;
                }
            }
        });

        checkBoxSellOut.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean select) {
                if(select){
                    checkBoxBuyIn.setChecked(false);
                    if(needUpdateList){
                        assignDataInfoToList();
                    }
                    if(checkBoxCashRate.isChecked()){
                        mExListAdapter.setExchangeRateDataList(mCashRateSellOutList);
                    }else if(checkBoxSpotRate.isChecked()){
                        mExListAdapter.setExchangeRateDataList(mSpotRateSellOutList);
                    }
                    isBuyInCheckBox = false;
                    isSellOutCheckBox = true;
                }
            }
        });
        checkBoxCashRate.setChecked(false);
        checkBoxSpotRate.setChecked(true);
        checkBoxBuyIn.setChecked(true);
        checkBoxSellOut.setChecked(false);
    }
    /**
     * As function name, assign all data to array
     *
     * @param data The data(monitor data) in database.
     * @param currentExchangeRate Current mapping exchangeRate
     * @return Array with all info.
     */
    private String[] assignDataToArray(ExchangeRateMonitorData data, double currentExchangeRate){
        Log.d(TAG,"Byron check currentExchangeRate = "+currentExchangeRate);
        String info[] = new String[6];
        info[0] = data.getNowCurrency();
        info[1] = data.getTargetCurrency();
        info[2] = data.getTypeOfExchangeRate();
        info[3] = Integer.toString(data.getNotifyDone());
        info[4] = Double.toString(data.getExpectedExchangeRate());
        info[5] = Double.toString(currentExchangeRate);
        return info;
    }

    /**
     * As function name, delete monitor data when user needn't to monitor
     *
     * @param position The position which user select to remove in recyclerview.
     */
    public void deleteMonitorDataInList(int position){
        ExchangeRateMonitorData exchangeRateMonitorData = null;
        exchangeRateMonitorRecord = new ExchangeRateMonitorRecord(this,false);
        if(isCashRateCheckBox){
            if(isBuyInCheckBox){
                exchangeRateMonitorData = exchangeRateMonitorRecord.queryByID(mCashRateBuyInList_ID.get(position));
            }else if(isSellOutCheckBox){
                exchangeRateMonitorData = exchangeRateMonitorRecord.queryByID(mCashRateSellOutList_ID.get(position));
            }
        }else if(isSpotRateCheckBox){
            if(isBuyInCheckBox){
                exchangeRateMonitorData = exchangeRateMonitorRecord.queryByID(mSpotRateBuyInList_ID.get(position));
            }else if(isSellOutCheckBox){
                exchangeRateMonitorData = exchangeRateMonitorRecord.queryByID(mSpotRateSellOutList_ID.get(position));
            }
        }
        if(exchangeRateMonitorData == null){
            Log.e(TAG,"Can't match ID in database");
        }
        exchangeRateMonitorData.setExpectedExchangeRate(-1);
        boolean updateSuccess = exchangeRateMonitorRecord.updateByID(exchangeRateMonitorData.getId(),exchangeRateMonitorData);
        if(updateSuccess){
            needUpdateList = true;  //updateList when check box onclick or activity onCreate again
        }else{
            Log.e(TAG,"update database with ID ("+exchangeRateMonitorData.getId()+") failed");
        }

        return;
    }

    @Override
    public void onClick(String[] exchangeRateItem) {

    }
}
