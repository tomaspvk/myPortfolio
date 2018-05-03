package cz.muni.fi.xpavuk.myportfolio.model;

/**
 * author: Tomas Pavuk
 * date: 29.4.2018
 */

import com.google.gson.annotations.SerializedName;
import java.util.HashMap;

public class ApiResponse  {

    @SerializedName("Meta Data")
    public MetaData metaData;

    @SerializedName("Time Series (Daily)") //(Time Series 15min)
    public HashMap<String, StockData> timeSeries;

    @SerializedName("Error Message")
    public String errorMessage;

}