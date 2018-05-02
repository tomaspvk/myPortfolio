package cz.muni.fi.xpavuk.myportfolio.api;

/**
 * author: Tomas Pavuk
 * date: 28.4.2018
 */

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import cz.muni.fi.xpavuk.myportfolio.BuildConfig;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class AlphaVantageApi {
    private final static String ALPHAVANTAGE_API_ENDPOINT = "https://www.alphavantage.co";
    private final static String ALPHAVANTAGE_API_KEY = "ESBB3WKEOEMIGFH7";

    private final AlphaVantageService mService;

    public AlphaVantageApi() {
        // Add interceptor so all retrofit queries have api key parameter included
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        Request original = chain.request();
                        HttpUrl httpUrl = original.url();

                        HttpUrl newHttpUrl = httpUrl
                                .newBuilder()
                                .addQueryParameter("apikey", ALPHAVANTAGE_API_KEY)
                                .build();

                        Request.Builder requestBuilder = original.newBuilder().url(newHttpUrl);
                        Request request = requestBuilder.build();
                        return chain.proceed(request);
                    }
                })
                .connectTimeout(30, TimeUnit.SECONDS) // connect timeout
                .readTimeout(30, TimeUnit.SECONDS); // socket timeout

        if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            builder.addInterceptor(interceptor);
        }

        final OkHttpClient client = builder.build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ALPHAVANTAGE_API_ENDPOINT)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        mService = retrofit.create(AlphaVantageService.class);
    }

    public AlphaVantageService getService() {
        return mService;
    }
}
