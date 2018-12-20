package com.example.chancharwei.dailyapp.utilies;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class WeatherJsonUtility {
    private static final String TAG = WeatherJsonUtility.class.getName();


    private static final String WEATHER_BASE_URL = "https://opendata.cwb.gov.tw/api/v1/rest/datastore/";
    private static final String AUTHORITY_PARAM = "Authorization";
    private static final String FORMAT_PARAM = "format";
    private static final String LOCATION_PARAM = "locationName";
    private static final String authority = "CWB-B0E71EAD-D248-4EEE-9B11-19727C17FBDC";
    private static String WEATHER_LOCATION_URL = "location";


    private static String checkSuccess,checkCityLocation,checkAreaLocation;

    public String buildWeatherUrl(String areaLocationQuery) {
        if(areaLocationQuery.contains("台")){
            areaLocationQuery = areaLocationQuery.replace("台","臺");
        }
        //String = URLEncoder.encode("台北市","UTF-8"); also can use this method to enclde with UTF-8
        Uri builtUri = Uri.parse(WEATHER_LOCATION_URL).buildUpon()
                .appendQueryParameter(AUTHORITY_PARAM,authority)
                .appendQueryParameter(FORMAT_PARAM,"JSON")
                .appendQueryParameter(LOCATION_PARAM, areaLocationQuery)
                .appendQueryParameter("elementName", "MinT,MaxT,PoP,T,Wx") //PoP12h instead of PoP
                .build();

        checkAreaLocation = areaLocationQuery;
        String urlString = builtUri.toString().replace("%2C",","); //Transform for get char ","
        Log.i(TAG, "Byron check Built urlString " + urlString);

        return urlString;
    }

    public static String[] getWeatherStringsFromJson(Context context, String forecastJsonStr)
        throws JSONException {

        final String WEATHER_ELEMENT = "weatherElement"; //Array
        final String ELEMENTS_NAME = "elementName"; //T,Wx,MinT,MaxT
        final String ELEMENTS_DESCRIPTION = "description"; //平均溫度,天氣現象,最低溫度,最高溫度
        final String WEATHER_TIME = "time"; //Array
        final String ELEMTENTS_VALUE = "elementValue";
        final String VALUE = "value";
        boolean getFirstTimeValue;

        Log.d(TAG,"getWeatherStringsFromJson E");
        JSONObject forecastWeatherJson = new JSONObject(forecastJsonStr);

        checkSuccess = forecastWeatherJson.getString("success");

        if(checkSuccess.equals("true")){    //use equals instead of "=="
            Log.i(TAG,"Get Data Success");
        }else{
            Log.e(TAG,"Get Data failed");
            return null;
        }

        JSONObject FirstLayerObject= forecastWeatherJson.getJSONObject("records");
        JSONObject locationCityInfo = FirstLayerObject.getJSONArray("locations").getJSONObject(0); //get 1st object for locations array


        if(!checkCityLocation.equals(locationCityInfo.getString("locationsName"))){
            Log.e(TAG,"Get wrong data, correct city is "+locationCityInfo.getString("locationsName")+" input city is "+checkCityLocation);
            return null;
        }

        JSONObject locationAreaInfo =  locationCityInfo.getJSONArray("location").getJSONObject(0); //get 1st object for location array

        if(!checkAreaLocation.equals(locationAreaInfo.getString("locationName"))){
            Log.e(TAG,"Get wrong data, wrong data is "+locationAreaInfo.getString("locationName"));
            return null;
        }

        JSONArray elementArray = locationAreaInfo.getJSONArray(WEATHER_ELEMENT);//length is 4 T,Wx,MinT,MaxT
        //Log.d(TAG,"element count"+elementArray.length());
        String[] elementName = new String[elementArray.length()];

        JSONObject T_Object = elementArray.getJSONObject(0);
        JSONObject Wx_Object = elementArray.getJSONObject(1);
        JSONObject MinT_Object = elementArray.getJSONObject(2);
        JSONObject MaxT_Object = elementArray.getJSONObject(3);


        /** time zone count are independent in elements, so we can choose any elements arbitrary **/
        String[] startTime = new String[elementArray.getJSONObject(0).getJSONArray(WEATHER_TIME).length()];
        String[] endTime = new String[elementArray.getJSONObject(0).getJSONArray(WEATHER_TIME).length()];
        String[] totalData = new String[elementArray.getJSONObject(0).getJSONArray(WEATHER_TIME).length()];
        getFirstTimeValue = false;

        for(int element=0;element<elementArray.length();element++){
            elementName[element] = elementArray.getJSONObject(element).getString(ELEMENTS_NAME);
            //Log.d(TAG,"time count"+timeArray.length());
            JSONArray timeArray = elementArray.getJSONObject(element).getJSONArray(WEATHER_TIME);
            for(int time=0;time<timeArray.length();time++) {
                if(!getFirstTimeValue) {
                    startTime[time] = timeArray.getJSONObject(time).getString("startTime");
                    endTime[time] = timeArray.getJSONObject(time).getString("endTime");
                    totalData[time] = startTime[time]+"~"+endTime[time];
                }
                JSONArray valueArray = timeArray.getJSONObject(time).getJSONArray(ELEMTENTS_VALUE);
                //Log.d(TAG,"weather value count = "+valueArray.length());
                /**
                     {
                     "startTime": "2018-12-11 18:00:00",
                     "endTime": "2018-12-12 06:00:00",
                     "elementValue": [
                     {
                     "value": "18",
                     "measures": "攝氏度"
                     }
                     ]
                     },
                 **/
                if(elementName[element].equals("T")||elementName[element].equals("MinT")||elementName[element].equals("MaxT")||elementName[element].equals("PoP")){
                    totalData[time] = totalData[time] + "," + elementName[element] + "-" + valueArray.getJSONObject(0).getInt(VALUE);
                }else if(elementName[element].equals("Wx")){
                    totalData[time] = totalData[time] + "," + elementName[element] + "-" + valueArray.getJSONObject(0).getString(VALUE);
                }


                /**
                    "elementValue": [
                    {
                        "value": "18",
                        "measures": "攝氏度"
                    }
                 **/
            }
            if(getFirstTimeValue == false){
                getFirstTimeValue = true;
            }
        }
        for(int i=0; i<elementArray.getJSONObject(0).getJSONArray(WEATHER_TIME).length();i++) {
            Log.d(TAG, "Byron check totalData["+i+"] = "+totalData[i]);
        }
        return totalData;


    }

    public void selectWeatherDataFromLocation(String location){
        if(location.contains("台")){
            location = location.replace("台","臺");
            Log.d(TAG,"Byron check selectWeatherDataFromLocation location = "+location);
        }
        switch(location){
            case "宜蘭縣" :
                WEATHER_LOCATION_URL = WEATHER_BASE_URL+"F-D0047-003";
                break;
            case "桃園市" :
                WEATHER_LOCATION_URL = WEATHER_BASE_URL+"F-D0047-007";
                break;
            case "新竹縣" :
                WEATHER_LOCATION_URL = WEATHER_BASE_URL+"F-D0047-011";
                break;
            case "苗栗縣" :
                WEATHER_LOCATION_URL = WEATHER_BASE_URL+"F-D0047-015";
                break;
            case "彰化縣" :
                WEATHER_LOCATION_URL = WEATHER_BASE_URL+"F-D0047-019";
                break;
            case "南投縣" :
                WEATHER_LOCATION_URL = WEATHER_BASE_URL+"F-D0047-023";
                break;
            case "雲林縣" :
                WEATHER_LOCATION_URL = WEATHER_BASE_URL+"F-D0047-027";
                break;
            case "嘉義縣" :
                WEATHER_LOCATION_URL = WEATHER_BASE_URL+"F-D0047-031";
                break;
            case "屏東縣" :
                WEATHER_LOCATION_URL = WEATHER_BASE_URL+"F-D0047-035";
                break;
            case "臺東縣" :
                WEATHER_LOCATION_URL = WEATHER_BASE_URL+"F-D0047-039";
                break;
            case "花蓮縣" :
                WEATHER_LOCATION_URL = WEATHER_BASE_URL+"F-D0047-043";
                break;
            case "澎湖縣" :
                WEATHER_LOCATION_URL = WEATHER_BASE_URL+"F-D0047-047";
                break;
            case "基隆市" :
                WEATHER_LOCATION_URL = WEATHER_BASE_URL+"F-D0047-051";
                break;
            case "新竹市" :
                WEATHER_LOCATION_URL = WEATHER_BASE_URL+"F-D0047-055";
                break;
            case "嘉義市" :
                WEATHER_LOCATION_URL = WEATHER_BASE_URL+"F-D0047-059";
                break;
            case "臺北市" :
                WEATHER_LOCATION_URL = WEATHER_BASE_URL+"F-D0047-063";
                break;
            case "高雄市" :
                WEATHER_LOCATION_URL = WEATHER_BASE_URL+"F-D0047-067";
                break;
            case "新北市" :
            case "臺北縣" :
                location = "新北市";
                WEATHER_LOCATION_URL = WEATHER_BASE_URL+"F-D0047-071";
                break;
            case "臺中市" :
                WEATHER_LOCATION_URL = WEATHER_BASE_URL+"F-D0047-075";
                break;
            case "臺南市" :
                WEATHER_LOCATION_URL = WEATHER_BASE_URL+"F-D0047-079";
                break;
            case "連江縣" :
                WEATHER_LOCATION_URL = WEATHER_BASE_URL+"F-D0047-083";
                break;
            case "金門縣" :
                WEATHER_LOCATION_URL = WEATHER_BASE_URL+"F-D0047-087";
                break;
            default:
        }
        checkCityLocation = location;
        Log.d(TAG,"Byron check correct WEATHER_LOCATION_URL = "+WEATHER_LOCATION_URL);

    }

}
