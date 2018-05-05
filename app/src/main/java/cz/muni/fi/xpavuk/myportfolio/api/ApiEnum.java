package cz.muni.fi.xpavuk.myportfolio.api;

/**
 * author: Tomas Pavuk
 * date: 30.4.2018
 */

public class ApiEnum {
    public enum FUNCTION {
        TIME_SERIES_INTRADAY, TIME_SERIES_DAILY, DIGITAL_CURRENCY_INTRADAY, DIGITAL_CURRENCY_DAILY
    }

    public enum INTERVAL {
        MIN_1("1min"),
        MIN_15("15min");
        private String value;

        INTERVAL(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    public enum OUTPUT_SIZE {
        FULL("full"),
        COMPACT("compact");
        private String value;

        OUTPUT_SIZE(String value) {
            this.value = value;
        }
    }
}
