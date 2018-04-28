package cz.muni.fi.xpavuk.myportfolio.model;

import com.google.gson.annotations.SerializedName;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * author: Tomas Pavuk
 * date: 28.4.2018
 */

public class StockIntraday extends RealmObject{
//    @SerializedName("avatar_url")
//    public String avatarUrl;

    @PrimaryKey
    public String name;

//    public String login;
}
