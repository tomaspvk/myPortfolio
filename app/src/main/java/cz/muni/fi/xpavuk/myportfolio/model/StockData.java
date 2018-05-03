package cz.muni.fi.xpavuk.myportfolio.model;

/**
 * author: Tomas Pavuk
 * date: 29.4.2018
 */
import com.google.gson.annotations.SerializedName;

public class StockData {

    @SerializedName("1. open")
    public Double open;
    @SerializedName("2. high")
    public Double high;
    @SerializedName("3. low")
    public Double low;
    @SerializedName("4. close")
    public Double close;
    @SerializedName("5. volume")
    public Double volume;
}