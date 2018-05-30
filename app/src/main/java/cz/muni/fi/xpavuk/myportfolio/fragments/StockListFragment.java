package cz.muni.fi.xpavuk.myportfolio.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import cz.muni.fi.xpavuk.myportfolio.activities.AssetInterface;
import cz.muni.fi.xpavuk.myportfolio.adapter.StockAdapter;

import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import cz.muni.fi.xpavuk.myportfolio.R;
import cz.muni.fi.xpavuk.myportfolio.api.AlphaVantageApi;
import cz.muni.fi.xpavuk.myportfolio.api.ApiEnum;
import cz.muni.fi.xpavuk.myportfolio.model.ApiStockResponse;
import cz.muni.fi.xpavuk.myportfolio.model.Stock;
import cz.muni.fi.xpavuk.myportfolio.utils.SimpleDividerItemDecoration;
import io.realm.Realm;

import io.realm.RealmResults;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static cz.muni.fi.xpavuk.myportfolio.api.ApiEnum.FUNCTION;
import static cz.muni.fi.xpavuk.myportfolio.api.ApiEnum.FUNCTION.DIGITAL_CURRENCY_DAILY;
import static cz.muni.fi.xpavuk.myportfolio.api.ApiEnum.FUNCTION.TIME_SERIES_DAILY;
import static cz.muni.fi.xpavuk.myportfolio.utils.StockParser.getStockFromStockApiResponse;

/**
 * author: Tomas Pavuk
 * date: 30.4.2018
 */

public class StockListFragment extends Fragment implements AssetInterface, SwipeRefreshLayout.OnRefreshListener{

    private static final String TAG = StockListFragment.class.getSimpleName();

    private AlphaVantageApi mAlphaVantageApi;
    private StockAdapter mAdapter;
    private Realm mRealm;

    private Unbinder mUnbinder;
    @BindView(android.R.id.list)
    RecyclerView mList;
    @BindView(R.id.portfolio_value)
    @Nullable
    TextView mPortfolioValue;
    @BindView(R.id.swipe_refresh_layout)
    SwipeRefreshLayout swipeRefreshLayout;

    public static StockListFragment newInstance() {
        return new StockListFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAlphaVantageApi = new AlphaVantageApi();
        mRealm = Realm.getDefaultInstance();
        setHasOptionsMenu(true);
        //setRetainInstance(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        mRealm = Realm.getDefaultInstance();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list, container, false);
        mUnbinder = ButterKnife.bind(this, view);

        RealmResults<Stock> ownedStocks = mRealm.where(Stock.class).and().equalTo("isValidStock", true).findAll();
        mAdapter = new StockAdapter(getContext(), ownedStocks, StockListFragment.this);
        mList.setAdapter(mAdapter);
        mList.setLayoutManager(new LinearLayoutManager(getContext()));
        mList.setHasFixedSize(true);

        swipeRefreshLayout.setOnRefreshListener(this);
        mList.addItemDecoration(new SimpleDividerItemDecoration(getContext()));
        //Refreshing
        //swipeRefreshLayout.post(this::run); //Uncomment this
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (mPortfolioValue == null){
            mPortfolioValue = view.findViewById(R.id.portfolio_value);
        }
        setPortfolioValue();
    }


    private void setPortfolioValue()
    {
        RealmResults<Stock> ownedStocks = mRealm.where(Stock.class).and().equalTo("isValidStock", true).findAll();
        double totalBalance = 0;
        double totalSpent = 0;
        for(Stock stock : ownedStocks)
        {
            totalSpent += stock.totalSpentAmount;
            totalBalance += stock.currentPrice * stock.ownedQuantity;
        }
        double totalSpentRounded = (double)Math.round(totalSpent*100) / 100;
        double totalBalanceRounded = (double)Math.round(totalBalance*100) / 100;
        String value = "$" + String.valueOf(totalBalanceRounded) + " (" + totalSpentRounded + ")";
        mPortfolioValue.setText(value);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mRealm.close();
    }

    private void addStockToList(@NonNull String ticker, FUNCTION function, String interval, int quantity, boolean isRefresh) {
        swipeRefreshLayout.setRefreshing(true);
        Call<ApiStockResponse> stockCall;
        if (function == FUNCTION.DIGITAL_CURRENCY_DAILY) {
            stockCall = mAlphaVantageApi.getService()
                    .getIntraDayForCryptoSymbol(function.toString(), ticker, interval, "USD");
        } else {
            stockCall = mAlphaVantageApi.getService()
                    .getIntraDayForSymbol(function.toString(), ticker, interval);
        }
        stockCall.enqueue(new Callback<ApiStockResponse>() {

            @Override
            public void onResponse(@NonNull Call<ApiStockResponse> call, @NonNull Response<ApiStockResponse> response) {
                if (response.body() == null || response.body().errorMessage != null) {
                    swipeRefreshLayout.setRefreshing(false);
                    String errorMessage = "Error has occured. Please check if your ticker is valid or try again.";
                    Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
                    return;
                }

                Stock stock = getStockFromStockApiResponse(response.body());
                if (!isRefresh) {
                    stock.totalSpentAmount = (double)Math.round(stock.currentPrice * quantity *100)/100;
                } else {
                    stock.totalSpentAmount = mRealm.where(Stock.class).and().equalTo("stockName", stock.stockName).findFirst().totalSpentAmount;
                }
                stock.isCrypto = function == FUNCTION.DIGITAL_CURRENCY_DAILY;
                stock.ownedQuantity = quantity;

                saveResult(Collections.singletonList(stock));
                final Handler handler = new Handler();
                onItemsLoadComplete();
            }

            @Override
            public void onFailure(@NonNull Call<ApiStockResponse> call, @NonNull Throwable t) {
                swipeRefreshLayout.setRefreshing(false);
                t.printStackTrace();
            }
        });
    }

    //@OnClick(R.id.add_asset)
    public void action() {
        AddAssetDialogFragment addAssetDialog = new AddAssetDialogFragment();
        addAssetDialog.setTargetFragment(this, 0);
        addAssetDialog.show(getFragmentManager(), "add_asset_dialog");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            Bundle extras = data.getExtras();
            String ticker = extras.getString("ticker");
            int quantity = Integer.parseInt(extras.getString("quantity"));
            String type = extras.getString("type");

            addStockToList(ticker, Enum.valueOf(ApiEnum.FUNCTION.class, type), null, quantity, false);
        } else if (resultCode == Activity.RESULT_CANCELED)
        {
            String errorMessage = "Ticker and Quantity should not be empty.";
            Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRefresh()
    {
        RealmResults<Stock> ownedStocks = mRealm.where(Stock.class).and().equalTo("isValidStock", true).findAll();
        for(Stock stock : ownedStocks)
        {
            if (stock.isCrypto)
                addStockToList(stock.stockName, DIGITAL_CURRENCY_DAILY, null, stock.ownedQuantity, true);
            else
                addStockToList(stock.stockName, TIME_SERIES_DAILY, null, stock.ownedQuantity, true);
        }
    }

    private void onItemsLoadComplete(){
        setPortfolioValue();
        swipeRefreshLayout.setRefreshing(false);
    }

    private void saveResult(final List<Stock> stocks) {
        Realm realm = null;
        try {
            realm = Realm.getDefaultInstance();
            realm.executeTransaction(realm1 -> realm1.insertOrUpdate(stocks));
        } finally {
            if(realm != null) {
                realm.close();
            }
        }
    }

    public boolean delete(final Stock stock){
        mRealm.executeTransaction(realm -> {
            RealmResults<Stock> result = realm.where(Stock.class).equalTo("stockName",stock.stockName).findAll();
            result.deleteAllFromRealm();
        });
        onRefresh(); //handle like event maybe?
        return true;
    }

    private void run() {
        swipeRefreshLayout.setRefreshing(true);
        onRefresh();
    }
}
