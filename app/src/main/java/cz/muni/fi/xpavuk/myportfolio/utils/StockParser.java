package cz.muni.fi.xpavuk.myportfolio.utils;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import cz.muni.fi.xpavuk.myportfolio.model.HistoricalData;
import cz.muni.fi.xpavuk.myportfolio.model.Stock;
import cz.muni.fi.xpavuk.myportfolio.model.ApiResponse;
import io.realm.RealmList;

/**
 * author: Tomas Pavuk
 * date: 3.5.2018
 */

public class StockParser {

    public static Stock getStockFromStockApiResponse(ApiResponse apiResponse) {
        Stock stock = new Stock();
        if (apiResponse.errorMessage != null) {
            stock.isValidStock = false;
            return stock;
        }
        stock.isValidStock = true;

        String lastRefreshedString = apiResponse.metaData._3LastRefreshed.split("\\s+")[0];

        stock.stockName = apiResponse.metaData._2Symbol.toUpperCase();
        stock.currentPrice = (double)Math.round(apiResponse.timeSeries.get(lastRefreshedString).close*100) / 100;
        stock.closed = apiResponse.metaData._3LastRefreshed.split("\\s+").length <= 1;
        stock.openingPrice = (double)Math.round(apiResponse.timeSeries.get(lastRefreshedString).open*100) / 100;
        stock.changeInPrice = getChangeInPrice(lastRefreshedString, stock.currentPrice, apiResponse);
        stock.intradayLowPrice = (double)Math.round(apiResponse.timeSeries.get(lastRefreshedString).low*100) / 100;
        stock.intradayHighPrice = (double)Math.round(apiResponse.timeSeries.get(lastRefreshedString).high*100) / 100;
        stock.lastUpdatedDate = lastRefreshedString;
        stock.timeStamp = System.currentTimeMillis();

        RealmList<HistoricalData> stockDatePriceList = new RealmList<>();
        for (String key : apiResponse.timeSeries.keySet()) {
            HistoricalData data = new HistoricalData();
            data.key = key;
            data.value = apiResponse.timeSeries.get(key).close;
            stockDatePriceList.add(data);
        }
        stock.historicalData = stockDatePriceList;

        return stock;
    }

    private static double getChangeInPrice(String lastRefreshed, double currentPrice, ApiResponse apiResponse) {
        Date todayDate = convertStringToDate(lastRefreshed);
        todayDate.setTime(todayDate.getTime() - 2); // one day before
        if (!apiResponse.timeSeries.containsKey(convertDateToString(todayDate)))
            return 0d;
        return currentPrice - apiResponse.timeSeries.get(convertDateToString(todayDate)).close;
    }

    private static Date convertStringToDate(String dateString) {
        Date date = null;
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        try {
            date = df.parse(dateString);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return date;
    }

    private static String convertDateToString(Date date) {
        String dateString = null;
        SimpleDateFormat sdfr = new SimpleDateFormat("yyyy-MM-dd");
        try {
            dateString = sdfr.format(date);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return dateString;
    }
}
