package com.example.chancharwei.dailyapp.utilies;


import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;


public class ExchangeRateHTMLUtility {
    private static final String TAG = ExchangeRateHTMLUtility.class.getName();
    private static final int PARSE_NUM_DATA_TABLE = 5;
    private String[] currencyTitle, currencyInfo = null;

    public void parsingHTMLData(String url){
        Document exchangeRateDoc = null;
        try {
            exchangeRateDoc = Jsoup.connect(url).get();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //String doc = exchangeRateDoc.title();
        Elements tr_table = exchangeRateDoc.body().select("tbody").select("tr");

        currencyTitle = new String[PARSE_NUM_DATA_TABLE];
        currencyInfo = new String[PARSE_NUM_DATA_TABLE];
        for(Element element : tr_table){
            for(int i=0; i<PARSE_NUM_DATA_TABLE ;i++){
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
                Log.d(TAG,"Title = "+ currencyTitle[i]+", Content = "+ currencyInfo[i]);
            }
        }
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

}
