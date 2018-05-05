package cz.muni.fi.xpavuk.myportfolio.model;

/**
 * author: Tomas Pavuk
 * date: 29.4.2018
 */
import com.google.gson.annotations.SerializedName;

public class StockData {

    @SerializedName(value="1. open", alternate="1a. open (USD)")
    public Double open;
    @SerializedName(value="2. high", alternate="2a. high (USD)")
    public Double high;
    @SerializedName(value="3. low", alternate="3a. low (USD)")
    public Double low;
    @SerializedName(value="4. close", alternate="4a. close (USD)")
    public Double close;
    @SerializedName("5. volume")
    public Double volume;
}