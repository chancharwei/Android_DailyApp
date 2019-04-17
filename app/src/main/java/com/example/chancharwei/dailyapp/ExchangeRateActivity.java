package com.example.chancharwei.dailyapp;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.example.chancharwei.dailyapp.data.ExchangeRatePerDayRecord;
import com.example.chancharwei.dailyapp.data.ExchangeRateTableData;
import com.example.chancharwei.dailyapp.utilies.ExchangeRateHTMLUtility;
import com.example.chancharwei.dailyapp.utilies.NetworkUtility;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static java.lang.Thread.*;

public class ExchangeRateActivity extends AppCompatActivity {
    private final static String TAG = ExchangeRateActivity.class.getName();
    private static final String TAIWAN_BANK_EXCHANGERATE = "https://rate.bot.com.tw/xrt?Lang=zh-TW";
    final static String exchangeRateSharedPreference = "exchangeRate";
    private SharedPreferences exchangeRatePreference;
    private Spinner currencySpinner;
    private final String spinnerFistItem = "Currency";
    private Handler updateUIHandler;
    private String[] currencyType;
    private String selectedDate = null,selectedCurrency = null;
    ExchangeRatePerDayRecord exchangeRatePerDayRecord = null;
    private ServiceHandleWork serviceHandleWork;
    private final int WORK_GETDATA_TO_DATABASE = 1,WORK_SEARCHDATA_FROM_DATABASE = 2; //WORK CONTENT IN NEW THREAD
    private final int UPDATE_UI_CURRENT_EXCHANGERATE= 1,UPDATE_UI_SPINNER_CURRENCY = 2,UPDATE_UI_SEARCH_EXCHANGERATE = 3;
    private final int numDataEachCurrency = ExchangeRateHTMLUtility.PARSE_NUM_DATA_EACH_CURRENCY;
    HorizontalScrollView horizontalScrollView_database;
    HorizontalScrollView horizontalScrollView_current;
    private ImageView mImageView_Calendar, mImageView_Search, mImageView_Coin,mImageView_Background;
    ProgressBar progressBar_Loading;
    private String[] recordCurrentData;
    private String dateTime,clockTime;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG,"onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exchange_rate);
        getSupportActionBar().setTitle(R.string.exchangeRate_activity);
        //getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        progressBar_Loading = findViewById(R.id.progressBar_Loading);
        progressBar_Loading.setVisibility(View.VISIBLE);
        mImageView_Calendar = findViewById(R.id.calendar_view);
        mImageView_Background = findViewById(R.id.imageView_background);
        mImageView_Background.setVisibility(View.INVISIBLE);
        mImageView_Calendar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                calendarFeature();
            }
        });
        mImageView_Calendar.setVisibility(View.INVISIBLE);
        mImageView_Search = findViewById(R.id.search_view);
        mImageView_Search.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                //reset table first +++
                TableLayout tableLayout = findViewById(R.id.search_exchangeRate_table);
                TableRow tableRow = findViewById(R.id.row_item);
                if(selectedDate == null && selectedCurrency == null){
                    return;
                }
                tableLayout.removeAllViews();
                tableLayout.addView(tableRow,new TableLayout.LayoutParams(TableLayout.LayoutParams.FILL_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));
                //reset table first ---
                searchDataFromDataBase();
            }
        });
        mImageView_Search.setVisibility(View.INVISIBLE);
        mImageView_Coin = (ImageView) findViewById(R.id.coin_view);
        mImageView_Coin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startMonitorExRate();
            }
        });
        mImageView_Coin.setVisibility(View.INVISIBLE);
        horizontalScrollView_database = (HorizontalScrollView) findViewById(R.id.horizontalScrollView_databse);
        horizontalScrollView_current = (HorizontalScrollView) findViewById(R.id.horizontalScrollView_current);
        horizontalScrollView_database.setVisibility(View.INVISIBLE);
        horizontalScrollView_current.setVisibility(View.INVISIBLE);
        exchangeRatePreference = getSharedPreferences(exchangeRateSharedPreference, MODE_PRIVATE);
        currencySpinner = findViewById(R.id.dateSpinner_needChange);
        currencySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                Log.d(TAG,"Byron selected ->"+currencyType[position]);
                selectedCurrency = currencyType[position];
                if(selectedCurrency.equals(spinnerFistItem)){
                    selectedCurrency = null;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                Log.d(TAG,"Byron nothing select");
            }
        });
        currencySpinner.setVisibility(View.INVISIBLE);
        prepareServiceWork();
        if(needUpdateData()){
            searchExchangeRate();
        }else{
            updateUIwithSaveData();
        }
        triggerAlarmManager();
    }

    //prepare for let job do in other thread +++
    private void prepareServiceWork(){
        serviceHandleWork = new ServiceHandleWork();

        updateUIHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                switch (msg.getData().getInt("updateID")){
                    case UPDATE_UI_CURRENT_EXCHANGERATE:
                        String[] current_data = msg.getData().getStringArrayList("currentExchangeRate").toArray(new String[msg.getData().getStringArrayList("currentExchangeRate").size()]);
                        showCurrentDataInTable(current_data);
                        break;
                    case UPDATE_UI_SPINNER_CURRENCY:
                        currencyType = msg.getData().getStringArrayList("currencyType").toArray(new String[msg.getData().getStringArrayList("currencyType").size()]);
                        ArrayAdapter<String> lunchList = new ArrayAdapter<>(ExchangeRateActivity.this,
                                android.R.layout.simple_spinner_dropdown_item,
                                currencyType);
                        currencySpinner.setAdapter(lunchList);
                        break;
                    case UPDATE_UI_SEARCH_EXCHANGERATE:
                        String[] search_data = msg.getData().getStringArrayList("searchExchangeRate").toArray(new String[msg.getData().getStringArrayList("searchExchangeRate").size()]);
                        showSearchDataInTable(search_data);
                        //currencySpinner.setSelection(0);
                        break;
                    default:
                        //do nothing
                }

                super.handleMessage(msg);
            }
        };
    }
    //prepare for let job do in other thread ---

    private boolean needUpdateData(){
        boolean needUpdateData;
        long lastStartTime = exchangeRatePreference.getLong("lastStartActivityTime",0);
        long nowTime = SystemClock.elapsedRealtime();
        if(nowTime - lastStartTime < 1000*60*5){
            needUpdateData = false;
        }else{
            needUpdateData = true;
        }
        exchangeRatePreference.edit().putLong("lastStartActivityTime",nowTime).commit();
        return needUpdateData;
    }

    private void updateUIwithSaveData(){
        ArrayList<String> exchangeRateDataList = new ArrayList<>();

        for(int i = 0; i< exchangeRatePreference.getInt("exchangeRateData_numData",0); i++){
            exchangeRateDataList.add(exchangeRatePreference.getString("exchangeRateData_"+i,"null"));
            //Log.d(TAG,"Byron check get index = "+data);
        }
        dateTime = exchangeRatePreference.getString("recordDateTime","yyyy/MM/dd");
        updateUI(UPDATE_UI_CURRENT_EXCHANGERATE,exchangeRateDataList);

        ArrayList<String> spinnerDataList = new ArrayList<>();
        for(int i = 0; i< exchangeRatePreference.getInt("spinnerData_numData",0); i++){
            spinnerDataList.add(exchangeRatePreference.getString("spinnerData_"+i,"null"));
        }

        updateUI(UPDATE_UI_SPINNER_CURRENCY,spinnerDataList);
    }
    public void triggerAlarmManager(){
        Intent intent = new Intent();
        intent.setClass(this,MonitorReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this,0,intent,0);
        AlarmManager alarm = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        alarm.setAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, 60000/*SystemClock.elapsedRealtime()+AlarmManager.INTERVAL_FIFTEEN_MINUTES*/,pendingIntent);
    }

    private void calendarFeature(){
        final Calendar calendar = Calendar.getInstance();
        int initYear,initMonth,initDay;
        initYear = calendar.get(Calendar.YEAR);
        initMonth = calendar.get(Calendar.MONTH);
        initDay = calendar.get(Calendar.DAY_OF_MONTH);
        new DatePickerDialog(ExchangeRateActivity.this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                month = month+1;
                String yearString = Integer.toString(year);
                String monthString = Integer.toString(month);
                String dayString = Integer.toString(day);
                if(month < 10){
                    monthString = "0".concat(monthString); //2->02
                }
                if(day < 10){
                    dayString = "0".concat(dayString); //2->02
                }
                selectedDate = yearString+"/"+monthString+"/"+dayString;
                Log.d(TAG,"Byron check select date = "+selectedDate);
                //searchDataFromDataBase();
            }

        },initYear,initMonth,initDay).show();
    }

    private void searchDataFromDataBase(){
        Log.d(TAG,"Byron date null? = "+selectedDate+" currency null? = "+selectedCurrency);
        if(selectedDate == null && selectedCurrency == null) {
            return;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                Work work = new Work() {
                    @Override
                    public void doWork() {
                        workSelect(WORK_SEARCHDATA_FROM_DATABASE);
                    }
                };
                serviceHandleWork.arrangeWork(work);
            }
        }).start();
    }
    private void showSearchDataInTable(String[] data){
        TableLayout tableLayout = (TableLayout)findViewById(R.id.search_exchangeRate_table);
        TableRow tableRow = new TableRow(this);
        tableRow.setLayoutParams(new TableLayout.LayoutParams(TableRow.LayoutParams.FILL_PARENT,TableRow.LayoutParams.MATCH_PARENT));
        for(int i=0;i<data.length;i++){
            TextView textView = new TextView(this);
            textView.setText(data[i]);
            textView.setBackground(getResources().getDrawable(R.drawable.border_style2));
            if(i == 1){ //click currency
                textView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        view.setBackground(getResources().getDrawable(R.drawable.border_style3));
                        //view.setBackground(getResources().getDrawable(R.drawable.border_style2));
                    }
                });
            }
            tableRow.addView(textView);
        }
        tableLayout.addView(tableRow,new TableLayout.LayoutParams(TableLayout.LayoutParams.FILL_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));
        horizontalScrollView_current.setVisibility(View.INVISIBLE);
        horizontalScrollView_database.setVisibility(View.VISIBLE);
    }

    private void showCurrentDataInTable(final String[] data){
        recordCurrentData = data;
        TableLayout tableLayout = (TableLayout)findViewById(R.id.current_exchangeRate_table);
        for(int i=0;i<data.length/numDataEachCurrency;i++){
            TableRow tableRow = new TableRow(this);
            tableRow.setLayoutParams(new TableLayout.LayoutParams(TableRow.LayoutParams.FILL_PARENT,TableRow.LayoutParams.MATCH_PARENT));
            TextView textView_date = new TextView(this);

            textView_date.setText(dateTime);
            textView_date.setBackground(getResources().getDrawable(R.drawable.border_style2));
            tableRow.addView(textView_date);
            for(int j=i*numDataEachCurrency;j<i*numDataEachCurrency+numDataEachCurrency;j++){
                TextView textView = new TextView(this);
                textView.setText(data[j]);
                textView.setBackground(getResources().getDrawable(R.drawable.border_style2));
                tableRow.addView(textView);
            }
            final int selectCurrency =i*numDataEachCurrency;
            tableRow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    view.setBackgroundColor(Color.GREEN);
                    Log.d(TAG,"Byron onclick row "+data[selectCurrency]);
                }
            });
            tableLayout.addView(tableRow,new TableLayout.LayoutParams(TableLayout.LayoutParams.FILL_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));

        }
        horizontalScrollView_database.setVisibility(View.INVISIBLE);
        horizontalScrollView_current.setVisibility(View.VISIBLE);
        currencySpinner.setVisibility(View.VISIBLE);
        mImageView_Calendar.setVisibility(View.VISIBLE);
        mImageView_Search.setVisibility(View.VISIBLE);
        mImageView_Coin.setVisibility(View.VISIBLE);
        mImageView_Background.setVisibility(View.VISIBLE);
        progressBar_Loading.setVisibility(View.INVISIBLE);
    }
    private void searchExchangeRate(){

        new Thread(new Runnable() {
            @Override
            public void run() {
                Work work = new Work() {
                    @Override
                    public void doWork() {
                        workSelect(WORK_GETDATA_TO_DATABASE);
                    }
                };
                serviceHandleWork.arrangeWork(work);
            }
        }).start();

    }

    @Override
    protected void onPause() {
        Log.d(TAG,"onPause");
        super.onPause();
    }

    @Override
    protected void onStop() {
        Log.d(TAG,"onStop");
        if(exchangeRatePerDayRecord != null){
            exchangeRatePerDayRecord.close();
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG,"onDestroy");
        serviceHandleWork.cleanAllWork();
        super.onDestroy();
    }
    private void workSelect(int workID){
        switch (workID){
            case WORK_GETDATA_TO_DATABASE:
                try {
                    URL exchangeRateURL = new URL(TAIWAN_BANK_EXCHANGERATE);
                    if (NetworkUtility.HttpCheckStatusWithURL(exchangeRateURL)) {
                        ExchangeRateHTMLUtility exchangeRateHTMLUtility = new ExchangeRateHTMLUtility();
                        exchangeRateHTMLUtility.parsingHTMLData(TAIWAN_BANK_EXCHANGERATE);

                        if(exchangeRateHTMLUtility.getCurrencyTitle()!=null && exchangeRateHTMLUtility.getCurrencyInfo() != null){
                            showAndRecordExchangeRate(exchangeRateHTMLUtility);
                        }
                    }else {
                        Log.e(TAG, "Network Status Wrong");
                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                break;
            case WORK_SEARCHDATA_FROM_DATABASE:
                exchangeRatePerDayRecord = new ExchangeRatePerDayRecord(this,true);
                Log.d(TAG,"Byron Searchdate = "+selectedDate+" currency = "+selectedCurrency);

                if(selectedDate == null && selectedCurrency != null){
                    ArrayList<ExchangeRateTableData> dataArrayList = exchangeRatePerDayRecord.queryDateOrCurrency(selectedDate,selectedCurrency);
                    if(dataArrayList == null){
                        return;
                    }
                    for(ExchangeRateTableData exchangeRateTableData : dataArrayList){
                        updateUIfromDataBase(exchangeRateTableData);
                    }
                    //selectedCurrency = null;
                }else if(selectedDate != null && selectedCurrency == null){
                    ArrayList<ExchangeRateTableData> dataArrayList = exchangeRatePerDayRecord.queryDateOrCurrency(selectedDate,selectedCurrency);
                    if(dataArrayList == null){
                        return;
                    }
                    for(ExchangeRateTableData exchangeRateTableData : dataArrayList){
                        updateUIfromDataBase(exchangeRateTableData);
                    }
                    selectedDate = null;
                }else if(selectedDate != null && selectedCurrency != null){
                    ExchangeRateTableData exchangeRateTableData = exchangeRatePerDayRecord.queryByDateAndCurrency(selectedDate,selectedCurrency);
                    updateUIfromDataBase(exchangeRateTableData);
                    //selectedCurrency = null;
                    selectedDate = null;
                }
                break;
            default:
                //do nothing
                break;
        }
    }
    /*
     */
    private void updateUIfromDataBase(ExchangeRateTableData exchangeRateTableData){
        if(exchangeRateTableData != null){
            Log.d(TAG,"Byron exchangeRateTableData = "+exchangeRateTableData.getSpotRateSellMin());
            ArrayList<String> arrayList = new ArrayList<>();
            arrayList.add(exchangeRateTableData.getDateTime());
            arrayList.add(exchangeRateTableData.getCurrency());
            arrayList.add(exchangeRateTableData.getCashRateBuyMax()==-1.0?"--":Double.toString(exchangeRateTableData.getCashRateBuyMax()));
            arrayList.add(exchangeRateTableData.getCashRateBuyMin()==-1.0?"--":Double.toString(exchangeRateTableData.getCashRateBuyMin()));
            arrayList.add(exchangeRateTableData.getCashRateSellMax()==-1.0?"--":Double.toString(exchangeRateTableData.getCashRateSellMax()));
            arrayList.add(exchangeRateTableData.getCashRateSellMin()==-1.0?"--":Double.toString(exchangeRateTableData.getCashRateSellMin()));
            arrayList.add(exchangeRateTableData.getSpotRateBuyMax()==-1.0?"--":Double.toString(exchangeRateTableData.getSpotRateBuyMax()));
            arrayList.add(exchangeRateTableData.getSpotRateBuyMin()==-1.0?"--":Double.toString(exchangeRateTableData.getSpotRateBuyMin()));
            arrayList.add(exchangeRateTableData.getSpotRateSellMax()==-1.0?"--":Double.toString(exchangeRateTableData.getSpotRateSellMax()));
            arrayList.add(exchangeRateTableData.getSpotRateSellMin()==-1.0?"--":Double.toString(exchangeRateTableData.getSpotRateSellMin()));
            updateUIInfo(UPDATE_UI_SEARCH_EXCHANGERATE,arrayList);
        }else{
            Log.d(TAG,"DataBase with null row data");
        }

    }
    private void showAndRecordExchangeRate(ExchangeRateHTMLUtility exchangeRateHTMLUtility){
        exchangeRatePerDayRecord = new ExchangeRatePerDayRecord(this,false);
        ArrayList<Object> dataList = exchangeRateHTMLUtility.getTransformDoneDataList();
        long updateRowID = -1,updateStartRowID = -1,updateEndRowID = -1;
        boolean setNewDataForNewDay;
        if(exchangeRateHTMLUtility.getDateTime() != null){
            dateTime = exchangeRateHTMLUtility.getDateTime()[0];
            clockTime = exchangeRateHTMLUtility.getDateTime()[1];
        }

        if(exchangeRateHTMLUtility.getOriginalDataList()!=null){
            updateUIInfo(UPDATE_UI_CURRENT_EXCHANGERATE,exchangeRateHTMLUtility.getOriginalDataList());
            saveData(UPDATE_UI_CURRENT_EXCHANGERATE,exchangeRateHTMLUtility.getOriginalDataList());
        }

        Log.d(TAG,"lastDateTime = "+ exchangeRatePreference.getString("recordDateTime","yyyy/MM/dd")+" dateTime = "+dateTime);
        if(exchangeRatePreference.getString("recordDateTime","yyyy/MM/dd").equals(dateTime)){
            setNewDataForNewDay = false;
            if(exchangeRatePreference.getString("recordClockTime","hh:mm").equals(clockTime)){
                ArrayList<String> spinnerDataList = new ArrayList<>();
                for(int i = 0; i< exchangeRatePreference.getInt("spinnerData_numData",0); i++){
                    spinnerDataList.add(exchangeRatePreference.getString("spinnerData_"+i,"null"));
                }
                updateUIInfo(UPDATE_UI_SPINNER_CURRENCY,spinnerDataList);
                Log.i(TAG,"same data with time ("+dateTime+","+clockTime+")");
                return;
            }else{
                if(exchangeRateHTMLUtility.getDateTime() != null){
                    exchangeRatePreference.edit().putString("recordClockTime",clockTime).commit();
                }
            }
        }else{
            setNewDataForNewDay = true;
            exchangeRatePreference.edit().putString("recordDateTime",dateTime).commit();
        }

        if(!setNewDataForNewDay){
            updateStartRowID = exchangeRatePreference.getLong("singleDayStartRowID", (long) -1);
            updateEndRowID = exchangeRatePreference.getLong("singleDayEndRowID", (long) -1);
            updateRowID = updateStartRowID;
        }


        ArrayList<String> currencyTypeList = new ArrayList<>();
        for(int currency=0;currency<exchangeRateHTMLUtility.getNumOfTypeCurrency();currency++){
            ExchangeRateTableData data = new ExchangeRateTableData();
            data.setDateTime(dateTime);

            for(int j=currency*numDataEachCurrency;j<currency*numDataEachCurrency+numDataEachCurrency;j++){
                if(j%numDataEachCurrency == 0){
                    currencyTypeList.add((String)dataList.get(j));
                }
                data.assignMappingData(j,j%numDataEachCurrency,dataList);
            }


            if(setNewDataForNewDay){
                long rowID = exchangeRatePerDayRecord.insert(data);
                if(currency == 0){
                    exchangeRatePreference.edit().putLong("singleDayStartRowID",rowID).commit();
                }else if(currency == exchangeRateHTMLUtility.getNumOfTypeCurrency()-1){
                    exchangeRatePreference.edit().putLong("singleDayEndRowID",rowID).commit();
                }
                exchangeRatePreference.edit().putLong("perDateDataBase_"+currency,rowID).commit();
                Log.d(TAG,"get("+currency+") id = "+rowID);
            }else{
                Log.d(TAG,"Byron check updateRowID = "+updateRowID+", updateEndRowID = "+updateEndRowID);
                if(updateRowID<=updateEndRowID) {
                    ExchangeRateTableData originalData = exchangeRatePerDayRecord.queryByID(updateRowID);
                    boolean needUpdate = data.updateWithNewData(originalData);
                    if(needUpdate){
                        Log.d(TAG,"Byron needUpdate updateRowID = "+updateRowID);
                        boolean updateDataSuccess = exchangeRatePerDayRecord.update(data);
                        if(!updateDataSuccess){
                            Log.w(TAG,"update this row failed, rowID("+originalData.getId()+")");
                        }
                    }else{
                        Log.d(TAG,"no need to update DataBase id("+updateRowID+") currency ("+originalData.getCurrency()+")");
                    }
                    updateRowID++;
                }
            }

        }
        if(currencyTypeList.size()!=0){
            currencyTypeList.add(0,spinnerFistItem);
            updateUIInfo(UPDATE_UI_SPINNER_CURRENCY,currencyTypeList);
            saveData(UPDATE_UI_SPINNER_CURRENCY,currencyTypeList);
        }
    }

    private void saveData(int updateID,ArrayList<String> dataList){
        int numData = dataList.size();
        switch (updateID){
            case UPDATE_UI_CURRENT_EXCHANGERATE:
                exchangeRatePreference.edit().putInt("exchangeRateData_numData",numData).commit();
                for(int i=0;i<dataList.size();i++){
                    exchangeRatePreference.edit().putString("exchangeRateData_"+i,dataList.get(i)).commit();
                }
                break;
            case UPDATE_UI_SPINNER_CURRENCY:
                exchangeRatePreference.edit().putInt("spinnerData_numData",numData).commit();
                for(int i=0;i<dataList.size();i++){
                    exchangeRatePreference.edit().putString("spinnerData_"+i,dataList.get(i)).commit();
                }
                break;

        }
    }

    private void updateUI(int updateID,ArrayList<String> dataList){
        switch (updateID) {
            case UPDATE_UI_CURRENT_EXCHANGERATE:
                String[] current_data = dataList.toArray(new String[dataList.size()]);
                showCurrentDataInTable(current_data);
                break;
            case UPDATE_UI_SPINNER_CURRENCY:
                currencyType = dataList.toArray(new String[dataList.size()]);
                ArrayAdapter<String> lunchList = new ArrayAdapter<>(ExchangeRateActivity.this,
                        android.R.layout.simple_spinner_dropdown_item,
                        currencyType);
                currencySpinner.setAdapter(lunchList);
                break;
            case UPDATE_UI_SEARCH_EXCHANGERATE:
                //do nothing
                break;
            default:
                //do nothing
        }
    }
    private void updateUIInfo(int updateID,Object settingObject){
        if(settingObject == null){
            return;
        }
        Bundle newBundle = new Bundle();
        newBundle.putInt("updateID",updateID);
        Message msg = new Message();
        switch (updateID){
            case UPDATE_UI_SPINNER_CURRENCY:
                newBundle.putStringArrayList("currencyType",(ArrayList<String>) settingObject);
                break;
            case UPDATE_UI_SEARCH_EXCHANGERATE:
                newBundle.putStringArrayList("searchExchangeRate", (ArrayList<String>) settingObject);
                break;
            case UPDATE_UI_CURRENT_EXCHANGERATE:
                newBundle.putStringArrayList("currentExchangeRate",(ArrayList<String>) settingObject);
                break;
        }
        msg.setData(newBundle);
        updateUIHandler.sendMessage(msg);
    }

    private void startMonitorExRate(){
        Intent startIntent = new Intent();
        if(selectedCurrency != null){
            startIntent.putExtra("currency",selectedCurrency);
        }

        if(currencyType.length!=0){
            startIntent.putExtra("currencyType",currencyType);
        }

        if(recordCurrentData.length!=0){
            startIntent.putExtra("currentData",recordCurrentData);
        }
        dateTime = exchangeRatePreference.getString("recordDateTime","yyyy/mm/dd");
        clockTime = exchangeRatePreference.getString("recordClockTime","hh:mm");
        if(!dateTime.equals("yyyy/mm/dd") && !clockTime.equals("hh:mm")){
            startIntent.putExtra("updateTime",dateTime.concat(","+clockTime));
        }
        startIntent.setClass(this, MonitorExRateActivity.class);
        startActivity(startIntent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.exchangerate,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemThatWasClickedId = item.getItemId();
        Intent featureIntent = new Intent();
        switch (itemThatWasClickedId) {
            case android.R.id.home:
                featureIntent.setClass(this, MainActivity.class);
                startActivity(featureIntent);
                finish();
                break;
            case R.id.weather:
                featureIntent.setClass(this,WeatherActivity.class);
                startActivity(featureIntent);
                finish();
                break;
            default:
                return super.onOptionsItemSelected(item);

        }
        return true;

    }
}



interface Work{
    void doWork();
}

class WorkerThread extends Thread{
    private static final String TAG = WorkerThread.class.getName();
    private Work work;
    private boolean isContinue = true;
    public boolean isIdle(){
        return work == null;
    }
    public void setWork(Work work){
        synchronized (this){
            if(isIdle()){
                this.work = work;
                notify();
            }
        }
    }
    public void run(){
        while (isContinue){
            synchronized (this){
                try{
                    wait();
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
                work.doWork();
                Log.d(TAG,"Byron work done");
                work = null;
            }
        }
    }
    public void terminate(){
        isContinue = false;
        setWork(new Work() {
            @Override
            public void doWork() {

            }
        });
    }
}

class WorkerThreadPool{
    private List<WorkerThread> workerThreads;
    //TODO
    private final int defaultThreadCount = 5;
    WorkerThreadPool(){
        workerThreads = new ArrayList<>();
    }

    synchronized void service(Work work){
        boolean idleThreadNotFound = true;
        for(WorkerThread workerThread : workerThreads){
            if(workerThread.isIdle()){
                workerThread.setWork(work);
                idleThreadNotFound = false;
                break;
            }
        }

        if(idleThreadNotFound){
            WorkerThread workerThread = createWorkThread();
            workerThread.setWork(work);
        }
    }

    private WorkerThread createWorkThread(){
        WorkerThread workerThread = new WorkerThread();
        workerThread.start();
        workerThreads.add(workerThread);
        try {
            sleep(1000);
        }catch (InterruptedException e){
            e.printStackTrace();
        }
        return workerThread;
    }

    synchronized void cleanIdle(){
        for(WorkerThread workerThread : workerThreads){
            if(workerThread.isIdle()){
                workerThreads.remove(workerThread);
                workerThread.terminate();
            }
        }
    }
}

class ServiceHandleWork{
    private WorkerThreadPool workerThreadPool = new WorkerThreadPool();
    public void arrangeWork(Work work){
        workerThreadPool.service(work);
    }
    public void cleanAllWork(){
        workerThreadPool.cleanIdle();
    }
}