package com.example.chancharwei.dailyapp.data;

public class ExchangeRateMonitorData {
    private static final String TAG = ExchangeRateMonitorData.class.getName();
    public static final String CashRateName = "cashRate";
    public static final String SpotRateName = "spotRate";
    private long rowId = 0;
    private String nowCurrency;
    private String targetCurrency;
    private String typeOfExchangeRate;
    private double expectedExchangeRate;
    private int notifyDone;


    public void setId(long value){
        rowId = value;
    }
    public void setNowCurrency(String value){nowCurrency = value;}
    public void setTargetCurrency(String value){targetCurrency = value;}
    public void setTypeOfExchangeRate(String value){typeOfExchangeRate = value;}
    public void setExpectedExchangeRate(double value){expectedExchangeRate = value;}
    public void setNotifyDone(int value){notifyDone = value;}


    public long getId(){return rowId;}
    public String getNowCurrency(){return nowCurrency;}
    public String getTargetCurrency(){return targetCurrency;}
    public String getTypeOfExchangeRate(){return typeOfExchangeRate;}
    public double getExpectedExchangeRate(){return expectedExchangeRate;}
    public int getNotifyDone(){return notifyDone;}
}
