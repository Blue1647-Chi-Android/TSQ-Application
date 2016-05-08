package com.calculator.android.stockinfo.tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import com.calculator.android.stockinfo.classes.StockData;
import com.calculator.android.stockinfo.classes.StockInfo;
import com.calculator.android.stockinfo.events.StockRefreshData;
import com.squareup.otto.Bus;

import org.json.JSONArray;
import org.json.JSONObject;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Braxton on 5/7/2016.
 */
public class GetStockDataTask extends AsyncTask<Object, Void, StockRefreshData> {
    private final Bus mBus;
    private final Context mContext;

    private static final String TAG = GetStockDataTask.class.getSimpleName();
    private static final String TARGET_OPERATION = "webservice/v1/symbols/TSQ/quote";

    public GetStockDataTask(Bus eBus, Context context) {
        this.mBus = eBus;
        this.mContext = context;
    }

    @Override
    protected StockRefreshData doInBackground(Object... params) {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        StockRefreshData refreshData = new StockRefreshData();

        Log.e(TAG, "connecting to '" + StockInfo.TARGET_BASE_URI + "'");
        try{
            Request request = new Request.Builder()
                    .url(StockInfo.TARGET_BASE_URI + TARGET_OPERATION + "?format=" + StockInfo.URI_FORMAT)
                    .build();

            Response response = client.newCall(request).execute();
            refreshData.setResponseCode(response.networkResponse().code());

            if(response.isSuccessful()) {
                refreshData.setStatus(StockRefreshData.STATUS.OK);
                String jsonResponse = response.body().string();
                StockData data = new StockData();

                Log.e(TAG, "connection successful, response " + jsonResponse);
                JSONObject json = new JSONObject(jsonResponse);
                JSONObject list = json.getJSONObject("list");
                JSONArray resources = list.getJSONArray("resources");
                JSONObject resourcesContent = resources.getJSONObject(0);
                JSONObject resource = resourcesContent.getJSONObject("resource");
                JSONObject fields = resource.getJSONObject("fields");

                data.setChange(fields.getString("change"));
                data.setCompanyName(fields.getString("name"));
                data.setDayHigh(fields.getString("day_high"));
                data.setDayLow(fields.getString("day_low"));
                data.setIssuerName(fields.getString("issuer_name"));
                data.setPercentChange(fields.getString("chg_percent"));
                data.setPrice(fields.getString("price"));
                data.setTicker(fields.getString("symbol"));
                data.setTs(fields.getString("ts"));
                data.setType(fields.getString("type"));
                data.setVol(fields.getString("volume"));
                data.setYearHigh(fields.getString("year_high"));
                data.setYearLow(fields.getString("year_low"));

                refreshData.setRefreshData(data);
            }
            else {
                Log.e(TAG, "connection failed, code: " + refreshData.getResponseCode());
                refreshData.setStatus(StockRefreshData.STATUS.FAIL);
            }
        }
        catch(Exception e){
            e.printStackTrace();

            Log.e(TAG, "connection failed, code: " + refreshData.getResponseCode());
            refreshData.setStatus(StockRefreshData.STATUS.FAIL);
        }

        return refreshData;
    }

    @Override
    protected void onPostExecute(StockRefreshData data) {
        super.onPostExecute(data);

        mBus.post(data);
    }
}
