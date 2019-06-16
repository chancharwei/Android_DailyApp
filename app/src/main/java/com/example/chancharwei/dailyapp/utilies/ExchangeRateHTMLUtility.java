package com.example.chancharwei.dailyapp.utilies;


import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;


public class ExchangeRateHTMLUtility {
    private static final String TAG = ExchangeRateHTMLUtility.class.getName();
    public static final int PARSE_NUM_DATA_EACH_CURRENCY = 5;
    private static final int NUM_OF_CURRENCY_TYPE = 19;
    private String[] currencyTitle, currencyInfo = null;
    private ArrayList<String> originalDataList = new ArrayList<>();
    private ArrayList<Object> transformDoneDataList = new ArrayList<>();
    private static int numOfTypeCurrency = 0;
    private String[] dateTime = null;
    public int parsingHTMLData(String url){
        int ERROR_NULL_EXCHANGERATE_INFO = -1;
        int ERROR_WRONG_NUM_CURRENCY = -2;
        Document exchangeRateDoc = null;
        transformDoneDataList.clear(); //initial dataList
        originalDataList.clear(); //initial dataList
        try {
            exchangeRateDoc = Jsoup.connect(url).get();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(exchangeRateDoc == null){
            Log.d(TAG,"exchangeRateDoc is null");
            return ERROR_NULL_EXCHANGERATE_INFO;
        }
        Elements elements = exchangeRateDoc.body().select("p");
        for(Element element : elements){
            if(element.attr("class").toString().equals("text-info")){
                Log.d(TAG,"Byron check 0 = "+element.select("span").get(1).text());
                if(element.select("span").get(1).text() != null){
                    dateTime = element.select("span").get(1).text().split(" ");
                }
            }
        }
        Elements tr_table = exchangeRateDoc.body().select("tbody").select("tr");
        numOfTypeCurrency = tr_table.size();
        if(numOfTypeCurrency != NUM_OF_CURRENCY_TYPE){
            Log.w(TAG,"get number of type currency = "+numOfTypeCurrency);
            return ERROR_WRONG_NUM_CURRENCY;
        }
        currencyTitle = new String[PARSE_NUM_DATA_EACH_CURRENCY];
        currencyInfo = new String[PARSE_NUM_DATA_EACH_CURRENCY];
        for(Element element : tr_table){
            for(int i=0; i<PARSE_NUM_DATA_EACH_CURRENCY ;i++){
                Element data = element.select("td").get(i);
                currencyTitle[i] = data.attr("data-table");
                if(data.select("div").size() != 0){
                    for(Element currency : data.select("div")){
                        if(currency.attr("class").equals("visible-phone print_hide")){
                            currencyInfo[i] = currency.text();
                        }
                    }
                }else{
                    currencyInfo[i] = element.select("td").get(i).text();
                }
                originalDataList.add(currencyInfo[i]);
                Log.d(TAG,"Title = "+ currencyTitle[i]+", Content = "+ currencyInfo[i]);
            }
        }
        transformDoneDataList = transformData(originalDataList);
        return 0;
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
                try{
                    afterTransformList.add(Double.parseDouble(list));
                }catch (NumberFormatException exception){
                    Log.d(TAG,"parsing Unexpected string ("+list+")");
                    afterTransformList.add(-1.0);
                }
                //Log.d(TAG,"Byron others list = "+list);
            }
        }
        return afterTransformList;
    }

    public ArrayList<Object> getTransformDoneDataList(){
        if(!transformDoneDataList.isEmpty()){
            Log.w(TAG,"currency data list is null");
            return transformDoneDataList;
        }else{
            return null;
        }
    }

    public ArrayList<String> getOriginalDataList(){
        if(!originalDataList.isEmpty()){
            Log.w(TAG,"currency data list is null");
            return originalDataList;
        }else{
            return null;
        }
    }

    public int getNumOfTypeCurrency(){
        return numOfTypeCurrency;
    }

    public String[] getCurrencyTitle(){
        if(currencyTitle != null){
            return currencyTitle;
        }else{
            return null;
        }
    }

    public String[] getCurrencyInfo(){
        if(currencyInfo != null){
            return currencyInfo;
        }else{
            return null;
        }
    }

    public String[] getDateTime() {
        //dateTime[0] = 2019/01/22 (date)
        //dateTime[1] = 16:00 (time)
        return dateTime;
    }

}
