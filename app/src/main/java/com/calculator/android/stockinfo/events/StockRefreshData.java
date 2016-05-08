package com.calculator.android.stockinfo.events;

import com.calculator.android.stockinfo.classes.StockData;

/**
 * Created by Braxton on 5/7/2016.
 */
public class StockRefreshData {
    private Enum mStatus;
    private int responseCode;
    private StockData refreshData;

    public static enum STATUS {
        OK, FAIL
    };

    public StockData getRefreshData() {
        return refreshData;
    }

    public void setRefreshData(StockData refreshData) {
        this.refreshData = refreshData;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    public void setStatus(Enum status){
        this.mStatus = status;
    }

    public Enum getStatus() {
        return mStatus;
    }
}
