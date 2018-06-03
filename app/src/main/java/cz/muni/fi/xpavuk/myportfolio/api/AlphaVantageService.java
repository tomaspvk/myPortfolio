package cz.muni.fi.xpavuk.myportfolio.api;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * author: Tomas Pavuk
 * date: 28.4.2018
 */

public interface AlphaVantageService {

    @GET("/query")
    Call<ApiStockResponse> getIntraDayForSymbol(@Query("function") String function,
                                                @Query("symbol") String symbol,
                                                @Query("interval") String interval);

    @GET("/query")
    Call<ApiStockResponse> getIntraDayForCryptoSymbol(@Query("function") String function,
                                                      @Query("symbol") String symbol,
                                                      @Query("interval") String interval,
                                                      @Query("market") String market);
}
