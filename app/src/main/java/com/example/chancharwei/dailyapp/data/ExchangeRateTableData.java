package com.example.chancharwei.dailyapp.data;

public class ExchangeRateTableData {

    private long rowId = 0;
    private String dateTime = "yyyy/MM/dd";
    private String currency = "TWD";
    private double cashRateBuyMax = 0.0;
    private double cashRateBuyMin = 0.0;
    private double cashRateSellMax = 0.0;
    private double cashRateSellMin = 0.0;
    private double spotRateBuyMax = 0.0;
    private double spotRateBuyMin = 0.0;
    private double spotRateSellMax = 0.0;
    private double spotRateSellMin = 0.0;

    public long getId(){
        return rowId;
    }

    public String getDateTime(){
        return dateTime;
    }

    public String getCurrency(){
        return currency;
    }
    public double getCashRateBuyMax(){
        return cashRateBuyMax;
    }
    public double getCashRateBuyMin(){
        return cashRateBuyMin;
    }
    public double getCashRateSellMax(){
        return cashRateSellMax;
    }
    public double getCashRateSellMin(){
        return cashRateSellMin;
    }
    public double getSpotRateBuyMax(){
        return spotRateBuyMax;
    }
    public double getSpotRateBuyMin(){
        return spotRateBuyMin;
    }
    public double getSpotRateSellMax(){
        return spotRateSellMax;
    }
    public double getSpotRateSellMin(){
        return spotRateSellMin;
    }

    public void setId(long value){
        rowId = value;
    }
    public void setDateTime(String value){
        dateTime = value;
    }
    public void setCurrency(String value){
        currency = value;
    }
    public void setCashRateBuyMax(double value){
        cashRateBuyMax = value;
    }
    public void setCashRateBuyMin(double value){
        cashRateBuyMin = value;
    }
    public void setCashRateSellMax(double value){
        cashRateSellMax = value;
    }
    public void setCashRateSellMin(double value){
        cashRateSellMin = value;
    }
    public void setSpotRateBuyMax(double value){
        spotRateBuyMax = value;
    }
    public void setSpotRateBuyMin(double value){
        spotRateBuyMin = value;
    }
    public void setSpotRateSellMax(double value){
        spotRateSellMax = value;
    }
    public void setSpotRateSellMin(double value){
        spotRateSellMin = value;
    }


}
