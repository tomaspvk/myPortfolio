package cz.muni.fi.xpavuk.myportfolio.api;

import cz.muni.fi.xpavuk.myportfolio.model.StockIntraday;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * author: Tomas Pavuk
 * date: 28.4.2018
 */

public interface AlphaVantageService {

    @GET("/query")
    Call<StockIntraday> getIntraDayForSymbol(@Query("function") String function,
                                             @Query("symbol") String symbol,
                                             @Query("interval") String interval);

    // https://developer.github.com/v3/activity/watching/
//    @GET("repos/{username}/{reponame}/subscribers")
//    Call<List<User>> getWatcherList(@Path("username") String username, @Path("reponame") String reponame);

}
