package cz.muni.fi.xpavuk.myportfolio.model;

/**
 * author: Tomas Pavuk
 * date: 29.4.2018
 */

import com.google.gson.annotations.SerializedName;

public class MetaData {

    @SerializedName("1. Information")
    public String _1Information;
    @SerializedName(value="2. Symbol", alternate="2. Digital Currency Code")
    public String _2Symbol;
    @SerializedName(value="3. Last Refreshed", alternate="6. Last Refreshed")
    public String _3LastRefreshed;
    @SerializedName("4. Interval")
    public String _4Interval;
    @SerializedName("5. Output Size")
    public String _5OutputSize;
    @SerializedName(value="6. Time Zone", alternate="7. Time Zone")
    public String _6TimeZone;
}
