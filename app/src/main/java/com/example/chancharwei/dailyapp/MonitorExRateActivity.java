package com.example.chancharwei.dailyapp;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.chancharwei.dailyapp.data.ExchangeRateMonitorData;
import com.example.chancharwei.dailyapp.data.ExchangeRateMonitorRecord;
import com.example.chancharwei.dailyapp.utilies.NotificationUtily;

import java.util.ArrayList;

public class MonitorExRateActivity extends AppCompatActivity {
    private final String TAG = MonitorExRateActivity.class.getName();
    private final String TaiwanMoneyName = "新台幣 (TWI)";
    private Spinner nowCurrencySpinner,exchangeCurrencySpinner;
    private String selectedNowCurrency, selectedTargetCurrency;
    private final String spinnerFistItem = "Currency";
    private String[] currentData;
    private ImageView flagNowView,flagExchangeView;
    private CheckBox checkBoxCashRate,checkBoxSpotRate;
    private TextView refExchangeRate;
    private EditText inputExchangeRate;
    private String exchangeRateNow;
    ExchangeRateMonitorRecord exchangeRateMonitorRecord;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG,"onCreate");
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.activity_monitor_ex_rate);
        flagNowView = findViewById(R.id.nowCurrency);
        flagExchangeView = findViewById(R.id.exchangeCurrency);
        checkBoxCashRate = findViewById(R.id.checkBox_CashRate);
        checkBoxSpotRate = findViewById(R.id.checkBox_SpotRate);
        refExchangeRate = findViewById(R.id.textView_refExchangeRate);
        inputExchangeRate = findViewById(R.id.monitorRate);
        inputExchangeRate.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionID, KeyEvent keyEvent) {
                if(actionID == EditorInfo.IME_ACTION_DONE){
                    Log.d(TAG,"Byron check GO!!!");
                    InputMethodManager inputMethod = (InputMethodManager)textView.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    if(inputMethod.isActive()){
                        //inputMethod.hideSoftInputFromWindow(textView.getApplicationWindowToken(),0);
                        Log.d(TAG,"Byron check input text = "+inputExchangeRate.getText());
                    }
                    return false;
                }
                return false;
            }
        });
        inputExchangeRate.setEnabled(false);
        TextView  updateTime = findViewById(R.id.textView_updateTime);
        Intent intent = getIntent();
        String currency = intent.getStringExtra("currency");
        setCurrencySpinner(intent);
        if(currency != null){
            mappingCountryView(TaiwanMoneyName,flagNowView);
            nowCurrencySpinner.setSelection(((ArrayAdapter<String>)nowCurrencySpinner.getAdapter()).getPosition(TaiwanMoneyName));
            mappingCountryView(currency,flagExchangeView);
            exchangeCurrencySpinner.setSelection(((ArrayAdapter<String>)exchangeCurrencySpinner.getAdapter()).getPosition(currency));
        }
        updateTime.setText(intent.getStringExtra("updateTime"));
        currentData = intent.getStringArrayExtra("currentData");
        setCheckBox();
        //ExchangeRateMonitorRecord exchangeRateMonitorRecord = new ExchangeRateMonitorRecord(this,false);
        //exchangeRateMonitorRecord.deleteAllData();
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return super.dispatchKeyEvent(event);
    }

    private void setCheckBox(){
        checkBoxCashRate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean select) {
                if(select){
                    Log.d(TAG,"Byron check run checkBoxCashRate click");
                    checkBoxSpotRate.setChecked(false);
                    exchangeRateNow = mappingRate();
                }
            }
        });

        checkBoxSpotRate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean select) {
                if(select){
                    Log.d(TAG,"Byron check run checkBoxSpotRate click");
                    checkBoxCashRate.setChecked(false);
                    exchangeRateNow = mappingRate();
                }
            }
        });
        checkBoxCashRate.setChecked(false);
        checkBoxSpotRate.setChecked(true);
    }

    private String mappingRate() {
        String exchangeRate = null;
        if (currentData == null) {
            Log.e(TAG, "get null current data");
            inputExchangeRate.setEnabled(false);
            return null;
        }
        Log.d(TAG, "Byron check selectedNowCurrency = " + selectedNowCurrency + " selectedTargetCurrency = " + selectedTargetCurrency);
        if (selectedNowCurrency == null || selectedTargetCurrency == null) {
            refExchangeRate.setText("N/A");
            inputExchangeRate.setEnabled(false);
            return null;
        }else if(!selectedNowCurrency.equals(TaiwanMoneyName) && !selectedTargetCurrency.equals(TaiwanMoneyName)){
            refExchangeRate.setText("N/A");
            inputExchangeRate.setEnabled(false);
            return null;
        }else if( selectedNowCurrency.equals(TaiwanMoneyName) && selectedTargetCurrency.equals(TaiwanMoneyName)){
            refExchangeRate.setText("1.00");
            return null;
        }else if(selectedNowCurrency.equals(TaiwanMoneyName)){
            for(int i=0;i<currentData.length;i+=5){
                Log.d(TAG,"Byron check i = "+i);
                if(selectedTargetCurrency.equals(currentData[i])) {
                    if(checkBoxSpotRate.isChecked()){
                        refExchangeRate.setText(currentData[i+4]); //foreign currency spotRate buy in
                        exchangeRate = currentData[i+4];
                        Log.d(TAG,"Byron check value "+i);
                    }else{
                        refExchangeRate.setText(currentData[i+2]); //foreign currency cashRate buy in
                        exchangeRate = currentData[i+2];
                    }
                    break;
                }
            }
        }else if(selectedTargetCurrency.equals(TaiwanMoneyName)){
            for(int i=0;i<currentData.length;i+=5){
                Log.d(TAG,"Byron check i = "+i);
                if(selectedNowCurrency.equals(currentData[i])) {
                    if(checkBoxSpotRate.isChecked()) {
                        refExchangeRate.setText(currentData[i+3]); //foreign currency spotRate sell out
                        exchangeRate = currentData[i+3];
                    }else{
                        refExchangeRate.setText(currentData[i+1]); //foreign currency cashRate sell out
                        exchangeRate = currentData[i+1];
                    }
                    break;
                }
            }
        }

        if(exchangeRate != null && exchangeRate.equals("-")){
            inputExchangeRate.setEnabled(false);
            return null;
        }

        inputExchangeRate.setEnabled(true);
        return exchangeRate;
    }

    @Override
    protected void onStart() {
        Log.i(TAG,"onStart");
        super.onStart();
    }

    @Override
    protected void onResume() {
        Log.i(TAG,"onResume");
        super.onResume();
    }

    @Override
    protected void onStop() {
        Log.i(TAG,"onStop");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.i(TAG,"onDestroy");
        if(exchangeRateMonitorRecord != null){
            exchangeRateMonitorRecord.close();
        }
        super.onDestroy();
    }

    public void recordMonitorExchangeRate(View view){
        double expectedExchangeRate = 0;
        if(selectedNowCurrency == null || selectedTargetCurrency == null || inputExchangeRate.getText() == null){
            return;
        }
        try {
            expectedExchangeRate = Double.parseDouble(inputExchangeRate.getText().toString());
        }catch (NumberFormatException e){
            Log.e(TAG,"with wrong input expectedExchangeRate = "+inputExchangeRate.getText().toString());
            return;
        }
        exchangeRateMonitorRecord = new ExchangeRateMonitorRecord(this,false);
        ExchangeRateMonitorData newMonitorData = new ExchangeRateMonitorData();

        newMonitorData.setNowCurrency(selectedNowCurrency);
        newMonitorData.setTargetCurrency(selectedTargetCurrency);
        if(checkBoxSpotRate.isChecked()){
            newMonitorData.setTypeOfExchangeRate(newMonitorData.SpotRateName);
        }else{
            newMonitorData.setTypeOfExchangeRate(newMonitorData.CashRateName);
        }

        newMonitorData.setExpectedExchangeRate(expectedExchangeRate);
        newMonitorData.setNotifyDone(0);
        ExchangeRateMonitorData dataBaseMonitorData = exchangeRateMonitorRecord.queryToCheckExistData(newMonitorData);
        if(dataBaseMonitorData != null)
        Log.d(TAG,"Byron check dataBaseMonitorData expectedExchangeRate = "+dataBaseMonitorData.getExpectedExchangeRate()+" new = "+expectedExchangeRate);
        if(dataBaseMonitorData == null){
            exchangeRateMonitorRecord.insert(newMonitorData);
        }else if(dataBaseMonitorData.getExpectedExchangeRate() == expectedExchangeRate){
            Log.d(TAG,"Byron need not to change data");
            //do nothing
        }else{
            boolean updateSuccess = exchangeRateMonitorRecord.updateByID(dataBaseMonitorData.getId(),newMonitorData);
            Log.d(TAG,"Byron check updateSuccess = "+updateSuccess);
        }

    }

    private void setCurrencySpinner(Intent intent){
        final String[] currencyType = intent.getStringArrayExtra("currencyType");
        final String[] currencyTypeWithTWI = new String[currencyType.length+1];
        for(int i=0;i<currencyType.length;i++){
            currencyTypeWithTWI[i] = currencyType[i];
        }
        currencyTypeWithTWI[currencyType.length] = TaiwanMoneyName;
        nowCurrencySpinner = findViewById(R.id.dateSpinner_needChange);
        nowCurrencySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                Log.d(TAG,"Byron selected nowCurrencySpinner ->"+currencyTypeWithTWI[position]);
                selectedNowCurrency = currencyTypeWithTWI[position];
                if(selectedNowCurrency.equals(spinnerFistItem)){
                    selectedNowCurrency = null;
                    exchangeCurrencySpinner.setEnabled(true);
                    exchangeRateNow = mappingRate();
                    return;
                }else if(selectedNowCurrency.equals(TaiwanMoneyName)){
                    exchangeCurrencySpinner.setEnabled(true);
                }else if(!selectedNowCurrency.equals(TaiwanMoneyName)){
                    exchangeCurrencySpinner.setSelection(((ArrayAdapter<String>)exchangeCurrencySpinner.getAdapter()).getPosition(TaiwanMoneyName));
                    exchangeCurrencySpinner.setEnabled(false);
                }

                mappingCountryView(selectedNowCurrency,flagNowView);
                exchangeRateNow = mappingRate();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                Log.d(TAG,"Byron nothing select");
            }
        });
        exchangeCurrencySpinner = findViewById(R.id.dateSpinner_wantGetCurrency);
        exchangeCurrencySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                Log.d(TAG,"Byron selected exchangeCurrencySpinner ->"+currencyTypeWithTWI[position]);
                selectedTargetCurrency = currencyTypeWithTWI[position];
                if(selectedTargetCurrency.equals(spinnerFistItem)){
                    selectedTargetCurrency = null;
                    exchangeRateNow = mappingRate();
                    return;
                }
                mappingCountryView(selectedTargetCurrency,flagExchangeView);
                exchangeRateNow = mappingRate();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                Log.d(TAG,"Byron nothing select");
            }
        });
        ArrayAdapter<String> lunchList = new ArrayAdapter<>(MonitorExRateActivity.this,
                android.R.layout.simple_spinner_dropdown_item,
                currencyTypeWithTWI);
        nowCurrencySpinner.setAdapter(lunchList);
        exchangeCurrencySpinner.setAdapter(lunchList);
        exchangeCurrencySpinner.setEnabled(false);
    }

    private void mappingCountryView(String value,ImageView flagView){
        if(value.contains("USD")){
            flagView.setImageResource(R.drawable.flag_usd);
        }else if(value.contains("HKD")){
            flagView.setImageResource(R.drawable.flag_hkd);
        }else if(value.contains("GBP")){
            flagView.setImageResource(R.drawable.flag_gbp);
        }else if(value.contains("AUD")){
            flagView.setImageResource(R.drawable.flag_aud);
        }else if(value.contains("CAD")){
            flagView.setImageResource(R.drawable.flag_cad);
        }else if(value.contains("SGD")){
            flagView.setImageResource(R.drawable.flag_sgd);
        }else if(value.contains("CHF")){
            flagView.setImageResource(R.drawable.flag_chf);
        }else if(value.contains("JPY")){
            flagView.setImageResource(R.drawable.flag_jpy);
        }else if(value.contains("ZAR")){
            flagView.setImageResource(R.drawable.flag_zar);
        }else if(value.contains("SEK")){
            flagView.setImageResource(R.drawable.flag_sek);
        }else if(value.contains("NZD")){
            flagView.setImageResource(R.drawable.flag_nzd);
        }else if(value.contains("THB")){
            flagView.setImageResource(R.drawable.flag_thb);
        }else if(value.contains("PHP")){
            flagView.setImageResource(R.drawable.flag_php);
        }else if(value.contains("IDR")){
            flagView.setImageResource(R.drawable.flag_idr);
        }else if(value.contains("EUR")){
            flagView.setImageResource(R.drawable.flag_eur);
        }else if(value.contains("KRW")){
            flagView.setImageResource(R.drawable.flag_krw);
        }else if(value.contains("VND")){
            flagView.setImageResource(R.drawable.flag_vnd);
        }else if(value.contains("MYR")){
            flagView.setImageResource(R.drawable.flag_myr);
        }else if(value.contains("CNY")){
            flagView.setImageResource(R.drawable.flag_cny);
        }else if(value.contains("TWI")) {
            flagView.setImageResource(R.drawable.flag_twi);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemThatWasClickedId = item.getItemId();
        Intent featureIntent = new Intent();
        switch (itemThatWasClickedId) {
            case android.R.id.home:
                featureIntent.setClass(this, ExchangeRateActivity.class);
                startActivity(featureIntent);
                finish();
                break;
            default:
                return super.onOptionsItemSelected(item);

        }
        return true;

    }

    public void startMonitorListActivity(View view){
        Intent startIntent = new Intent();
        startIntent.setClass(this, MonitorERListActivity.class);
        startActivity(startIntent);
        finish();
    }

    /*public void testNotification(View view){
        NotificationUtily.remindExchangeRate(this);
    }*/



}
