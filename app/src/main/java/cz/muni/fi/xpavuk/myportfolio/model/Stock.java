package cz.muni.fi.xpavuk.myportfolio.model;

/**
 * author: Tomas Pavuk
 * date: 28.4.2018
 */

import android.content.Context;

import java.io.Serializable;
import cz.muni.fi.xpavuk.myportfolio.R;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Stock extends RealmObject implements Serializable {

    @PrimaryKey
    public String stockName;

    public boolean isValidStock;
    public boolean isCrypto;
    public double currentPrice;
    public double changeInPrice; // change from previous close price
    public boolean closed;
    public double intradayLowPrice;
    public double intradayHighPrice;
    public String lastUpdatedDate;
    public double openingPrice;

    public RealmList<HistoricalData> historicalData;

    public long timeStamp;
    public double ownedQuantity;
    public double totalSpentAmount;

    public String getIncreaseDecreaseText() {
        return changeInPrice > 0 ? "\u25B2" + changeInPrice : "\u25BC" + changeInPrice;

    }

    public String getupdatedAgoString(Context context) {
        int secDiff = (int) (System.currentTimeMillis() - timeStamp) / 1000;
        if (secDiff <= 60) {
            if (secDiff <= 5)
                return context.getString(R.string.updated_some_seconds_ago);
            else
                return context.getString(R.string.updated_seconds_ago, secDiff);
        } else {
            if (secDiff / 60 == 1)
                return context.getString(R.string.updated_single_minute_ago);
            else
                return context.getString(R.string.updated_minutes_ago, secDiff / 60);
        }
    }
}