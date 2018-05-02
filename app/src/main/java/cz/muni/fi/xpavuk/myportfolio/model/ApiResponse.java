package cz.muni.fi.xpavuk.myportfolio.model;

/**
 * author: Tomas Pavuk
 * date: 29.4.2018
 */

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.HashMap;

import io.realm.RealmObject;

public class ApiResponse extends RealmObject {

    @SerializedName("Meta Data")
    //@Expose
    public MetaData metaData;
//    @SerializedName("Time Series (1min)")
//    //@Expose
//    public HashMap<String, StockData> timeSeries15min;
//    //@Expose
//    @SerializedName("Error Message")
//    String errorMessage;

//    public MetaData getMetaData() {
//        return metaData;
//    }
//
//    public void setMetaData(MetaData metaData) {
//        this.metaData = metaData;
//    }
//
//    public HashMap<String, StockData> getTimeSeries15min() {
//        return timeSeries15min;
//    }
//
//    public void setTimeSeries15min(HashMap<String, StockData> timeSeries15min) {
//        this.timeSeries15min = timeSeries15min;
//    }
//
//    public String getErrorMessage() {
//        return errorMessage;
//    }
//
//    public void setErrorMessage(String errorMessage) {
//        this.errorMessage = errorMessage;
//    }
}