package cz.muni.fi.xpavuk.myportfolio.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import cz.muni.fi.xpavuk.myportfolio.model.HistoricalData;
import cz.muni.fi.xpavuk.myportfolio.model.Stock;
import cz.muni.fi.xpavuk.myportfolio.model.ApiStockResponse;
import io.realm.RealmList;

/**
 * author: Tomas Pavuk
 * date: 3.5.2018
 */

public class StockParser {

    public static Stock getStockFromStockApiResponse(ApiStockResponse apiStockResponse) {
        Stock stock = new Stock();
        if (apiStockResponse.errorMessage != null) {
            stock.isValidStock = false;
            return stock;
        }
        stock.isValidStock = true;

        String lastRefreshedString = apiStockResponse.metaData._3LastRefreshed.split("\\s+")[0];

        if (apiStockResponse.metaData._1Information.contains("Digital Currency"))
             stock.isCrypto = true;
        else
            stock.isCrypto = false;

        stock.stockName = apiStockResponse.metaData._2Symbol.toUpperCase();
        stock.currentPrice = (double)Math.round(apiStockResponse.timeSeries.get(lastRefreshedString).close*100) / 100;
        stock.closed = apiStockResponse.metaData._3LastRefreshed.split("\\s+").length <= 1;
        stock.openingPrice = (double)Math.round(apiStockResponse.timeSeries.get(lastRefreshedString).open*100) / 100;
        stock.changeInPrice = (double)Math.round((stock.currentPrice - apiStockResponse.timeSeries.get(lastRefreshedString).open)*100)/100;
        stock.intradayLowPrice = (double)Math.round(apiStockResponse.timeSeries.get(lastRefreshedString).low*100) / 100;
        stock.intradayHighPrice = (double)Math.round(apiStockResponse.timeSeries.get(lastRefreshedString).high*100) / 100;
        stock.lastUpdatedDate = lastRefreshedString;
        stock.timeStamp = System.currentTimeMillis();

        RealmList<HistoricalData> stockDatePriceList = new RealmList<>();
        for (String key : apiStockResponse.timeSeries.keySet()) {
            HistoricalData data = new HistoricalData();
            data.key = key;
            data.value = apiStockResponse.timeSeries.get(key).close;
            stockDatePriceList.add(data);
        }
        stock.historicalData = stockDatePriceList;

        return stock;
    }

    /*private static double getChangeInPrice(String lastRefreshed, double currentPrice, ApiStockResponse apiStockResponse) {
        Date todayDate = convertStringToDate(lastRefreshed);
        todayDate.setTime(todayDate.getTime() - 2); // one day before
        if (!apiStockResponse.timeSeries.containsKey(convertDateToString(todayDate)))
            return 0d;
        return currentPrice - apiStockResponse.timeSeries.get(convertDateToString(todayDate)).close;
    }*/

    public static Date convertStringToDate(String dateString) {
        Date date = null;
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        try {
            date = df.parse(dateString);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return date;
    }

    public static String convertDateToString(Date date) {
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
