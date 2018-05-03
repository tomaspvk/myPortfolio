package cz.muni.fi.xpavuk.myportfolio;

import android.app.Application;
import android.content.Context;

import io.realm.Realm;

public class App
        extends Application {

    private static App sInstance;
    private static Context context;

    public static App getInstance() {
        return sInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;

        Realm.init(this);
        App.context = getApplicationContext();

    }

    public static Context getContext(){
        return context;
    }
}
