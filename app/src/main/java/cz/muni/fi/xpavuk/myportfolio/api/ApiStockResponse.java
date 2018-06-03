package cz.muni.fi.xpavuk.myportfolio.api;

/**
 * author: Tomas Pavuk
 * date: 29.4.2018
 */

import com.google.gson.annotations.SerializedName;
import java.util.HashMap;

import cz.muni.fi.xpavuk.myportfolio.model.MetaData;
import cz.muni.fi.xpavuk.myportfolio.model.StockData;

public class ApiStockResponse {

    @SerializedName("Meta Data")
    public MetaData metaData;

    @SerializedName(value="Time Series (Daily)", alternate="Time Series (Digital Currency Daily)") //(Time Series 15min)
    public HashMap<String, StockData> timeSeries;

    @SerializedName("Error Message")
    public String errorMessage;

    @SerializedName("Information")
    public String information;

}