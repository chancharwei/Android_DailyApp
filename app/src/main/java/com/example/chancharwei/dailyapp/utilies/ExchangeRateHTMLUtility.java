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
    private String[] currencyTitle, currencyInfo = null;
    private ArrayList<String> originalDataList = new ArrayList<>();
    private ArrayList<Object> transformDoneDataList = new ArrayList<>();
    private static int numOfTypeCurrency = 0;
    private String[] dateTime = null;
    public void parsingHTMLData(String url){
        Document exchangeRateDoc = null;
        try {
            exchangeRateDoc = Jsoup.connect(url).get();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //String doc = exchangeRateDoc.title();
        if(exchangeRateDoc.body() == null){
            Log.e(TAG,"Byron exchangeRateDoc body is null");
        }else{
            Log.e(TAG,"Byron exchangeRateDoc body is not null");
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
        currencyTitle = new String[PARSE_NUM_DATA_EACH_CURRENCY];
        currencyInfo = new String[PARSE_NUM_DATA_EACH_CURRENCY];
        Log.d(TAG,"Byron check total size = "+PARSE_NUM_DATA_EACH_CURRENCY*tr_table.size());
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

    public ArrayList<Object> getTransformDoneDataList(){
        if(!transformDoneDataList.isEmpty()){
            return transformDoneDataList;
        }else{
            return null;
        }
    }

    public ArrayList<String> getOriginalDataList(){
        if(!originalDataList.isEmpty()){
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
