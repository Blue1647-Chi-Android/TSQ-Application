package com.calculator.android.stockinfo.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.calculator.android.stockinfo.R;
import com.calculator.android.stockinfo.classes.NetworkHelper;
import com.calculator.android.stockinfo.classes.NumberFormatHelper;
import com.calculator.android.stockinfo.classes.StockData;
import com.calculator.android.stockinfo.events.StockRefreshData;
import com.calculator.android.stockinfo.tasks.GetStockDataTask;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

public class MainActivity extends Activity implements View.OnClickListener{

    private Bus eventBus;
    private static final String TAG = MainActivity.class.getSimpleName();

    /*
    * Stock UI content
    */
    ProgressBar progressBar;
    TextView refreshBtn;
    RelativeLayout noConnectionContainer;
    RelativeLayout stockInfoContainer;

    TextView ticker, priceVal, change, companyName, open,
            mktCap, high, low, yrHigh, yrLow, vol, perCh;

    private GetStockDataTask mStockUpdateTask;
    private boolean refreshPending = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        eventBus = new Bus();
        initViews();
    }

    private void startRefresh() {
        enableProgressBar();
        hideNoInternetIcon();

        /*
        * If isNetworkAvailable() parse json data
        * and display elements to user
        *
        * else if !isNetworkAvailable() handle network
        * errors to be displayed to the user
        * */
        if (NetworkHelper.isNetworkAvailable(this)) {
            mStockUpdateTask = new GetStockDataTask(eventBus, this);
            mStockUpdateTask.execute();
        }
        else {
            refreshPending = false;

            hideStockLayout();
            showNoInternetIcon();
            disableProgressBar();
        }
    }

    @Subscribe
    public void onRefresh(final StockRefreshData data) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                disableProgressBar();
                refreshPending = false;

                if(data.getStatus() == StockRefreshData.STATUS.OK) {
                    showStockLayout();
                    updateViews(data.getRefreshData());
                }
                else {
                    hideStockLayout();
                    showNoInternetIcon();
                    showErrorDialog(data.getResponseCode());
                    Log.i(TAG, "Something went wrong while trying to refresh.");
                }
            }
        });
    }

    private void showErrorDialog(int responseCode) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage("Http error:  failure to connect to website. Response code: " + responseCode)
                .setTitle(R.string.http_err_title)
                .setNegativeButton("Close", null);

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void updateViews(StockData data) {
        MainActivity.this.ticker.setText(data.getTicker());
        MainActivity.this.priceVal.setText(NumberFormatHelper.format(data.getPrice()));
        MainActivity.this.change.setText(NumberFormatHelper.format(data.getChange()));
        MainActivity.this.companyName.setText(NumberFormatHelper.format(data.getIssuerName()));
        MainActivity.this.open.setText(NumberFormatHelper.format(data.getPrice()));
        MainActivity.this.mktCap.setText(NumberFormatHelper.formatInteger(
                (long)(Float.parseFloat(data.getPrice()) * Float.parseFloat(data.getVol()))));
        MainActivity.this.high.setText(NumberFormatHelper.format(data.getDayHigh()));
        MainActivity.this.low.setText(NumberFormatHelper.format(data.getDayLow()));
        MainActivity.this.yrHigh.setText(NumberFormatHelper.format(data.getYearHigh()));
        MainActivity.this.yrLow.setText(NumberFormatHelper.format(data.getYearLow()));
        MainActivity.this.vol.setText(NumberFormatHelper.format(data.getVol()));
        MainActivity.this.perCh.setText(NumberFormatHelper.format(data.getPercentChange()));
    }

    void enableProgressBar(){
        progressBar.setVisibility(View.VISIBLE);
    }

    void disableProgressBar() {
        progressBar.setVisibility(View.INVISIBLE);
    }

    void showStockLayout(){
        stockInfoContainer.setVisibility(View.VISIBLE);
    }

    void hideStockLayout() {
        stockInfoContainer.setVisibility(View.GONE);
    }

    void showNoInternetIcon(){
        noConnectionContainer.setVisibility(View.VISIBLE);
    }

    void hideNoInternetIcon(){
        noConnectionContainer.setVisibility(View.GONE);
    }

    private void initViews() {
        progressBar = (ProgressBar) findViewById(R.id.connection_progress_bar);
        refreshBtn = (TextView) findViewById(R.id.stock_refresh_btn);
        noConnectionContainer = (RelativeLayout) findViewById(R.id.no_internet_connection_view);
        stockInfoContainer = (RelativeLayout)findViewById(R.id.stock_info_container);

        ticker = (TextView) findViewById(R.id.topBarCompanyName);
        priceVal = (TextView) findViewById(R.id.topThemeBarSecurityCost);
        change = (TextView) findViewById(R.id.topThemeBarPerChange);
        companyName = (TextView) findViewById(R.id.company_name);
        open = (TextView) findViewById(R.id.stock_value_1);
        mktCap = (TextView) findViewById(R.id.stock_value_2);
        high = (TextView) findViewById(R.id.stock_value_3);
        low = (TextView) findViewById(R.id.stock_value_4);
        yrHigh = (TextView) findViewById(R.id.stock_value_5);
        yrLow = (TextView) findViewById(R.id.stock_value_6);
        vol = (TextView) findViewById(R.id.stock_value_7);
        perCh = (TextView) findViewById(R.id.stock_value_8);

        refreshBtn.setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        eventBus.register(this);
        if(!refreshPending) {
            refreshPending = true;
            startRefresh();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        eventBus.unregister(this);
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.stock_refresh_btn) {
            if(!refreshPending) {
                refreshPending = true;
                startRefresh();
            }
        }
    }
}
