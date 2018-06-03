package cz.muni.fi.xpavuk.myportfolio.fragments;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
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

import cz.muni.fi.xpavuk.myportfolio.activities.AssetInterface;
import cz.muni.fi.xpavuk.myportfolio.adapter.StockAdapter;

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
import io.realm.Sort;
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

public class StockListFragment extends Fragment implements AssetInterface, SwipeRefreshLayout.OnRefreshListener {

    public static final String TAG = StockListFragment.class.getSimpleName();

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
    @BindView(R.id.portfolio_change)
    TextView mPortfolioChange;

    public static StockListFragment newInstance() {
        return new StockListFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAlphaVantageApi = new AlphaVantageApi();
        mRealm = Realm.getDefaultInstance();
        setHasOptionsMenu(true);
        setRetainInstance(true);

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

        RealmResults<Stock> ownedStocks = mRealm
                .where(Stock.class)
                .and().equalTo("isValidStock", true)
                .findAll()
                .sort("ownedQuantity", Sort.DESCENDING);
        mAdapter = new StockAdapter(getContext(), ownedStocks, StockListFragment.this);
        mList.setAdapter(mAdapter);
        mList.setLayoutManager(new LinearLayoutManager(getContext()));
        mList.setHasFixedSize(true);

        swipeRefreshLayout.setOnRefreshListener(this);
        // Refresh if there are any stocks saved
        if (!ownedStocks.isEmpty()) {
            swipeRefreshLayout.post(this::run);
        }
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setPortfolioValue();
    }


    private void setPortfolioValue() {
        RealmResults<Stock> ownedStocks = mRealm.where(Stock.class).and().equalTo("isValidStock", true).findAll();
        double totalBalance = 0;
        double totalSpent = 0;
        for (Stock stock : ownedStocks) {
            totalSpent += stock.totalSpentAmount;
            totalBalance += stock.currentPrice * stock.ownedQuantity;
        }

        double totalSpentRounded = (double)Math.round(totalSpent*100) / 100;
        double totalBalanceRounded = (double)Math.round(totalBalance*100) / 100;
        String value = "$" + String.valueOf(totalBalanceRounded);
      
        if (mPortfolioValue != null) {
            mPortfolioValue.setText(value);
        }
        double change = totalBalanceRounded - totalSpentRounded;
        String changeText;
        int changeColor;
        if (change < 0) {
            changeText = "+$" + change;
            changeColor = Color.parseColor("#ffff4444");
        } else {
            changeText = "+$" + change;
            changeColor = Color.parseColor("#ff99cc00");
        }
        if (mPortfolioChange != null) {
            mPortfolioChange.setText(changeText);
            mPortfolioChange.setTextColor(changeColor);
        }
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

    private void addStockToList(@NonNull String ticker, FUNCTION function, String interval, double quantity, boolean isRefresh) {
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
                    Toast.makeText(getContext(), getString(R.string.invalidstock), Toast.LENGTH_SHORT).show();
                    return;
                }

                Stock stock = getStockFromStockApiResponse(response.body());
                if (!isRefresh) {
                    stock.totalSpentAmount = (double) Math.round(stock.currentPrice * quantity * 100) / 100;
                } else {
                    Stock savedField = mRealm.where(Stock.class).and().equalTo("stockName", stock.stockName).findFirst();
                    if (savedField != null) {
                        stock.totalSpentAmount = savedField.totalSpentAmount;
                    }
                }
                stock.isCrypto = function == FUNCTION.DIGITAL_CURRENCY_DAILY;
                stock.ownedQuantity = quantity;

                saveOrUpdateResult(stock);
                onItemsLoadComplete();
                if (swipeRefreshLayout != null) {
                    swipeRefreshLayout.setRefreshing(false);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiStockResponse> call, @NonNull Throwable t) {
                swipeRefreshLayout.setRefreshing(false);
                t.printStackTrace();
            }
        });
    }

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
            if (extras != null) {
                String ticker = extras.getString("ticker");
                String quantity_string = extras.getString("quantity");
                String type = extras.getString("type");

                if (ticker == null || quantity_string == null || type == null) {
                    Toast.makeText(getContext(), getString(R.string.emptystock), Toast.LENGTH_SHORT).show();
                    return;
                }
                double quantity = Double.parseDouble(quantity_string.replace(',', '.'));

                addStockToList(ticker, Enum.valueOf(ApiEnum.FUNCTION.class, type), null, quantity, false);
            }
        } else if (resultCode == 2) {
            Bundle extras = data.getExtras();
            if (extras != null) {
                Stock stockToDelete = (Stock)extras.getSerializable("stock");
                delete(stockToDelete);
            }
        } else if (resultCode == Activity.RESULT_CANCELED) {
            Toast.makeText(getContext(), getString(R.string.emptystock), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRefresh() {
        RealmResults<Stock> ownedStocks = mRealm.where(Stock.class).and().equalTo("isValidStock", true).findAll();
        if (ownedStocks.isEmpty()) {
            setPortfolioValue();
        }
        else for(Stock stock : ownedStocks)
        {
            if (stock.isCrypto)
                addStockToList(stock.stockName, DIGITAL_CURRENCY_DAILY, null, stock.ownedQuantity, true);
            else
                addStockToList(stock.stockName, TIME_SERIES_DAILY, null, stock.ownedQuantity, true);
        }
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (swipeRefreshLayout!=null) {
            swipeRefreshLayout.setRefreshing(false);
            swipeRefreshLayout.destroyDrawingCache();
            swipeRefreshLayout.clearAnimation();
        }
    }

    private void onItemsLoadComplete() {
        setPortfolioValue();
    }

    private void saveOrUpdateResult(final Stock stockToAdd) {
        Realm realm = null;
        try {
            realm = Realm.getDefaultInstance();
            updateStock(realm, stockToAdd);
            realm.executeTransaction(realm1 -> realm1.insertOrUpdate(stockToAdd));
        } finally {
            if (realm != null) {
                realm.close();
            }
        }
    }

    public void delete(final Stock stock){
        mRealm.executeTransaction(realm -> {
            RealmResults<Stock> result = realm.where(Stock.class).equalTo("stockName", stock.stockName).findAll();
            result.deleteAllFromRealm();
        });
        setPortfolioValue();
    }

    /**
     * Found whether stock to be added is already in my portfolio. If such stock is found increase quantity
     * @param realm
     * @param stockToAdd - stock to be added
     */
    private void updateStock(Realm realm, Stock stockToAdd) {
        Stock stockFromRealm = realm
                .where(Stock.class)
                .contains("stockName", stockToAdd.stockName)
                .findFirst();
        if (stockFromRealm != null) {
            Stock stockCopyFromRealm = realm.copyFromRealm(stockFromRealm);
            //increase quantity of stock in my portfolio by quantity of stock to be added
            stockToAdd.ownedQuantity += stockCopyFromRealm.ownedQuantity;
            stockToAdd.totalSpentAmount += stockCopyFromRealm.currentPrice;
        }
    }

    private void run() {
        swipeRefreshLayout.setRefreshing(true);
        onRefresh();
    }
}
