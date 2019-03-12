package com.example.chancharwei.dailyapp;

import android.app.DatePickerDialog;
import android.content.Intent;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
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

public class ExchangeRateActivity extends AppCompatActivity {
    final static String TAG = ExchangeRateActivity.class.getName();
    private static final String TAIWAN_BANK_EXCHANGERATE = "https://rate.bot.com.tw/xrt?Lang=zh-TW";
    private ExchangeRatePerDayRecord exchangeRatePerDayRecord = null;
    private ExchangeRateHTMLUtility exchangeRateHTMLUtility;
    private SharedPreferences preference;
    private Spinner currencySpinner;
    private Handler updateUIHandler;
    private String[] currencyType;
    private String selectedDate = null,selectedCurrency = null;
    private ServiceHandleWork serviceHandleWork;
    private final int WORK_GETDATA_TO_DATABASE = 1,WORK_SEARCHDATA_FROM_DATABASE = 2; //WORK CONTENT IN NEW THREAD
    private final int UPDATE_UI_CURRENT_EXCHANGERATE= 1,UPDATE_UI_SPINNER_CURRENCY = 2,UPDATE_UI_SEARCH_EXCHANGERATE = 3;
    HorizontalScrollView horizontalScrollView_database;
    HorizontalScrollView horizontalScrollView_current;
    String dateTime;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG,"onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exchange_rate);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ImageView calendar_view = (ImageView) findViewById(R.id.calendar_view);
        calendar_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                calendarFeature();
            }
        });
        ImageView search_view = (ImageView) findViewById(R.id.search_view);
        search_view.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                //reset table first +++
                TableLayout tableLayout = findViewById(R.id.search_exchangeRate_table);
                TableRow tableRow = findViewById(R.id.row_item);
                tableLayout.removeAllViews();
                tableLayout.addView(tableRow,new TableLayout.LayoutParams(TableLayout.LayoutParams.FILL_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));
                //reset table first ---
                searchDataFromDataBase();
                currencySpinner.setSelection(0);
            }
        });
        horizontalScrollView_database = (HorizontalScrollView) findViewById(R.id.horizontalScrollView_databse);
        horizontalScrollView_current = (HorizontalScrollView) findViewById(R.id.horizontalScrollView_current);
        horizontalScrollView_database.setVisibility(View.INVISIBLE);
        horizontalScrollView_current.setVisibility(View.INVISIBLE);
        preference = getSharedPreferences("exchangeRate", MODE_PRIVATE);
        currencySpinner = findViewById(R.id.dateSpinner);
        currencySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                Log.d(TAG,"Byron selected ->"+currencyType[position]);
                selectedCurrency = currencyType[position];
                if(selectedCurrency.equals("select currency")){
                    selectedCurrency = null;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                Log.d(TAG,"Byron nothing select");
            }
        });
        searchExchangeRate();
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
        TableLayout tableLayout = (TableLayout)findViewById(R.id.current_exchangeRate_table);
        for(int i=0;i<data.length/ExchangeRateHTMLUtility.PARSE_NUM_DATA_EACH_CURRENCY;i++){
            TableRow tableRow = new TableRow(this);
            tableRow.setLayoutParams(new TableLayout.LayoutParams(TableRow.LayoutParams.FILL_PARENT,TableRow.LayoutParams.MATCH_PARENT));
            TextView textView_date = new TextView(this);
            textView_date.setText(dateTime);
            textView_date.setBackground(getResources().getDrawable(R.drawable.border_style2));
            tableRow.addView(textView_date);
            for(int j=i*ExchangeRateHTMLUtility.PARSE_NUM_DATA_EACH_CURRENCY;j<i*ExchangeRateHTMLUtility.PARSE_NUM_DATA_EACH_CURRENCY+ExchangeRateHTMLUtility.PARSE_NUM_DATA_EACH_CURRENCY;j++){
                TextView textView = new TextView(this);
                textView.setText(data[j]);
                textView.setBackground(getResources().getDrawable(R.drawable.border_style2));
                tableRow.addView(textView);
            }
            final int selectCurrency =i*ExchangeRateHTMLUtility.PARSE_NUM_DATA_EACH_CURRENCY;
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
    }
    public void searchExchangeRate(){
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
                        break;
                    default:
                        //do nothing
                }

                super.handleMessage(msg);
            }
        };

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
                        exchangeRateHTMLUtility = new ExchangeRateHTMLUtility();
                        exchangeRateHTMLUtility.parsingHTMLData(TAIWAN_BANK_EXCHANGERATE);

                        if(exchangeRateHTMLUtility.getCurrencyTitle()!=null && exchangeRateHTMLUtility.getCurrencyInfo() != null){
                            //save to database

                            showAndRecordExchangeRate(exchangeRateHTMLUtility.getDataList());
                        }
                    }else {
                        Log.e(TAG, "Network Status Wrong");
                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                break;
            case WORK_SEARCHDATA_FROM_DATABASE:
                //exchangeRatePerDayRecord = new ExchangeRatePerDayRecord(this,true);
                Log.d(TAG,"Byron Searchdate = "+selectedDate+" currency = "+selectedCurrency);

                if(selectedDate == null && selectedCurrency != null){
                    ArrayList<ExchangeRateTableData> dataArrayList = exchangeRatePerDayRecord.queryDateOrCurrency(selectedDate,selectedCurrency);
                    if(dataArrayList == null){
                        return;
                    }
                    for(ExchangeRateTableData exchangeRateTableData : dataArrayList){
                        updateUIfromDataBase(exchangeRateTableData);
                    }
                    selectedCurrency = null;
                }else if(selectedDate != null && selectedCurrency == null){
                    ArrayList<ExchangeRateTableData> dataArrayList = exchangeRatePerDayRecord.queryDateOrCurrency(selectedDate,selectedCurrency);
                    if(dataArrayList == null){
                        return;
                    }
                    for(ExchangeRateTableData exchangeRateTableData : dataArrayList){
                        updateUIfromDataBase(exchangeRateTableData);
                    }
                    selectedDate = null;
                }else{
                    ExchangeRateTableData exchangeRateTableData = exchangeRatePerDayRecord.queryByDateAndCurrency(selectedDate,selectedCurrency);
                    updateUIfromDataBase(exchangeRateTableData);
                    selectedCurrency = null;
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
    private void showAndRecordExchangeRate(ArrayList<String> dataList){
        exchangeRatePerDayRecord = new ExchangeRatePerDayRecord(this,false);

        ArrayList<Object> afterTransformList = transformData(dataList);
        boolean setNewDataForNewDay;
        if(exchangeRateHTMLUtility.getDateTime() != null){
            dateTime = exchangeRateHTMLUtility.getDateTime()[0];
        }
        updateUIInfo(UPDATE_UI_CURRENT_EXCHANGERATE,dataList);
        Log.d(TAG,"lastDateTime = "+getSharedPreferences("exchangeRate", MODE_PRIVATE).getString("recordDateTime","yyyy/MM/dd")+" dateTime = "+dateTime);
        if(getSharedPreferences("exchangeRate", MODE_PRIVATE).getString("recordDateTime","yyyy/MM/dd").equals(dateTime)){
            setNewDataForNewDay = false;
        }else{
            setNewDataForNewDay = true;
            preference.edit().putString("recordDateTime",dateTime).commit();
        }
        ArrayList<String> currencyTypeList = new ArrayList<>();
        for(int currency=0;currency<exchangeRateHTMLUtility.getNumOfTypeCurrency();currency++){
            ExchangeRateTableData data = new ExchangeRateTableData();
            data.setDateTime(dateTime);

            for(int j=currency*ExchangeRateHTMLUtility.PARSE_NUM_DATA_EACH_CURRENCY;
                j<currency*ExchangeRateHTMLUtility.PARSE_NUM_DATA_EACH_CURRENCY+ExchangeRateHTMLUtility.PARSE_NUM_DATA_EACH_CURRENCY;
                j++){
                switch(j%ExchangeRateHTMLUtility.PARSE_NUM_DATA_EACH_CURRENCY){
                    case 0: //currency type
                        data.setCurrency((String)afterTransformList.get(j));
                        currencyTypeList.add((String)afterTransformList.get(j));
                        break;
                    case 1: //CashRateBuy
                        data.setCashRateBuyMax((Double)afterTransformList.get(j));
                        data.setCashRateBuyMin((Double)afterTransformList.get(j));
                        break;
                    case 2: //CashRateSell
                        data.setCashRateSellMax((Double)afterTransformList.get(j));
                        data.setCashRateSellMin((Double)afterTransformList.get(j));
                        break;
                    case 3: //SpotRateBuy
                        data.setSpotRateBuyMax((Double)afterTransformList.get(j));
                        data.setSpotRateBuyMin((Double)afterTransformList.get(j));
                        break;
                    case 4: //SpotRateSell
                        data.setSpotRateSellMax((Double)afterTransformList.get(j));
                        data.setSpotRateSellMin((Double)afterTransformList.get(j));
                        break;
                }
            }


            if(setNewDataForNewDay){
                data = exchangeRatePerDayRecord.insert(data);
                preference.edit().putLong("perDateDataBase_"+currency,data.getId()).commit();
                Log.d(TAG,"get("+currency+") id = "+data.getId());
            }else{
                boolean value = updateNewData(currency,data);
                Log.d(TAG,"Byron check update success ? = "+value);
                exchangeRatePerDayRecord.queryByDateAndCurrency(data.getDateTime(),data.getCurrency());
                //exchangeRatePerDayRecord.update(data);
            }

        }
        //exchangeRatePerDayRecord.queryAllData();
        if(currencyTypeList.size()!=0){
            currencyTypeList.add(0,"select currency");
            updateUIInfo(UPDATE_UI_SPINNER_CURRENCY,currencyTypeList);
        }

    }

    private void updateUIInfo(int updateID,Object settingObject){
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

    private boolean updateNewData(int currency,ExchangeRateTableData newData){
        boolean needUpdate = false;
        Log.d(TAG,"check currency = "+currency+" newData = "+getSharedPreferences("exchangeRate", MODE_PRIVATE).getLong("perDateDataBase_"+currency, (long) -1));
        long getSaveId = getSharedPreferences("exchangeRate", MODE_PRIVATE).getLong("perDateDataBase_"+currency, (long) -1);
        if(getSaveId == -1){
            Log.d(TAG,"Didn't save mapping id");
            return false;
        }

        ExchangeRateTableData queryDataFromDataBase = exchangeRatePerDayRecord.queryByID(getSaveId);
        if(queryDataFromDataBase != null){
            Log.d(TAG,queryDataFromDataBase.getCashRateBuyMax()+","+queryDataFromDataBase.getCurrency()+","+queryDataFromDataBase.getCashRateBuyMax());

            if(queryDataFromDataBase.getCashRateBuyMax() < newData.getCashRateBuyMax()){
                queryDataFromDataBase.setCashRateBuyMax(newData.getCashRateBuyMax());
                needUpdate = true;
            }

            if(queryDataFromDataBase.getCashRateBuyMin() > newData.getCashRateBuyMin()){
                queryDataFromDataBase.setCashRateBuyMin(newData.getCashRateBuyMin());
                needUpdate = true;
            }

            if(queryDataFromDataBase.getCashRateSellMax() < newData.getCashRateSellMax()){
                queryDataFromDataBase.setCashRateSellMax(newData.getCashRateSellMax());
                needUpdate = true;
            }

            if(queryDataFromDataBase.getCashRateSellMin() > newData.getCashRateSellMin()){
                queryDataFromDataBase.setCashRateSellMin(newData.getCashRateSellMin());
                needUpdate = true;
            }

            if(queryDataFromDataBase.getSpotRateBuyMax() < newData.getSpotRateBuyMax()){
                queryDataFromDataBase.setSpotRateBuyMax(newData.getSpotRateBuyMax());
                needUpdate = true;
            }

            if(queryDataFromDataBase.getSpotRateBuyMin() > newData.getSpotRateBuyMin()){
                queryDataFromDataBase.setSpotRateBuyMin(newData.getSpotRateBuyMin());
                needUpdate = true;
            }

            if(queryDataFromDataBase.getSpotRateSellMax() < newData.getSpotRateSellMax()){
                queryDataFromDataBase.setSpotRateSellMax(newData.getSpotRateSellMax());
                needUpdate = true;
            }

            if(queryDataFromDataBase.getSpotRateSellMin() > newData.getSpotRateSellMin()){
                queryDataFromDataBase.setSpotRateSellMin(newData.getSpotRateSellMin());
                needUpdate = true;
            }

            if(needUpdate){
                return exchangeRatePerDayRecord.update(queryDataFromDataBase);
            }else{
                Log.e(TAG,"no need to update DataBase id("+getSaveId+") currency ("+queryDataFromDataBase.getCurrency()+")");
                return false;
            }
        }
        return false;
    }

    private ArrayList<Object> transformData(ArrayList<String> dataList){
        ArrayList<Object> afterTransformList = new ArrayList<>();
        for(String list : dataList){
            //Log.d(TAG,"Byron dataList index = "+dataList.indexOf(list)+" list = "+list);
            if(dataList.indexOf(list) % 5 == 0){
                //type of currency
                afterTransformList.add(list);
                //Log.d(TAG,"Byron currecny list = "+list);
            }else if(dataList.indexOf(list) == dataList.indexOf("-")){
                // -
                afterTransformList.add(-1.0);
                //Log.d(TAG,"Byron - list = "+list);
            }else{
                afterTransformList.add(Double.parseDouble(list));
                //Log.d(TAG,"Byron others list = "+list);
            }
        }
        return afterTransformList;
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
            Thread.sleep(1000);
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