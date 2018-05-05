package cz.muni.fi.xpavuk.myportfolio.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;

import butterknife.OnClick;
import cz.muni.fi.xpavuk.myportfolio.activities.AssetInterface;
import cz.muni.fi.xpavuk.myportfolio.adapter.StockAdapter;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import cz.muni.fi.xpavuk.myportfolio.R;
import cz.muni.fi.xpavuk.myportfolio.adapter.StockAdapter;
import cz.muni.fi.xpavuk.myportfolio.api.AlphaVantageApi;
import cz.muni.fi.xpavuk.myportfolio.api.ApiEnum;
import cz.muni.fi.xpavuk.myportfolio.model.ApiResponse;
import cz.muni.fi.xpavuk.myportfolio.model.MetaData;
import cz.muni.fi.xpavuk.myportfolio.model.Stock;
import io.realm.Realm;
import cz.muni.fi.xpavuk.myportfolio.model.StockData;

import io.realm.RealmResults;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static cz.muni.fi.xpavuk.myportfolio.api.ApiEnum.FUNCTION;
import static cz.muni.fi.xpavuk.myportfolio.api.ApiEnum.FUNCTION.TIME_SERIES_DAILY;
import static cz.muni.fi.xpavuk.myportfolio.utils.StockParser.getStockFromStockApiResponse;

/**
 * author: Tomas Pavuk
 * date: 30.4.2018
 */

public class StockListFragment extends Fragment implements AssetInterface{

    private static final String TAG = StockListFragment.class.getSimpleName();

    private AlphaVantageApi mAlphaVantageApi;
    private StockAdapter mAdapter;
    private Realm mRealm;

    private Unbinder mUnbinder;
    @BindView(android.R.id.list)
    RecyclerView mList;
//    @BindView(R.id.portfolio_value)
//    TextView mPortfolioValue;

    public static StockListFragment newInstance() {
        return new StockListFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAlphaVantageApi = new AlphaVantageApi();
        mRealm = Realm.getDefaultInstance();
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list, container, false);
        mUnbinder = ButterKnife.bind(this, view);

        RealmResults<Stock> ownedStocks = mRealm.where(Stock.class).findAll();
        mAdapter = new StockAdapter(getContext(), ownedStocks);
        mList.setAdapter(mAdapter);
        mList.setLayoutManager(new LinearLayoutManager(getContext()));
        mList.setHasFixedSize(true);
        return view;
    }

    private void setPortfolioValue()
    {

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

    private void addStockToList(@NonNull String ticker, FUNCTION function, String interval) {
        Call<ApiResponse> stockCall = mAlphaVantageApi.getService()
                .getIntraDayForSymbol(function.toString(), ticker, interval);
        stockCall.enqueue(new Callback<ApiResponse>() {

            @Override
            public void onResponse(@NonNull Call<ApiResponse> call, @NonNull Response<ApiResponse> response) {
                Stock stock = getStockFromStockApiResponse(response.body());
                stock.isCrypto = function == FUNCTION.DIGITAL_CURRENCY_DAILY;
                saveResult(Collections.singletonList(stock));
//                Stock stock = new Stock();
//                stock.stockName = response.body().metaData._2Symbol.toUpperCase();
//                saveResult(Arrays.asList(stock));
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse> call, @NonNull Throwable t) {
                t.printStackTrace();
            }
        });
    }

//    @OnClick(R.id.add_asset)
//    public void onAddAssetClicked() {
//        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
//        builder.setTitle("Ticker");
//
//        final EditText input = new EditText(getContext());
//        input.setInputType(InputType.TYPE_CLASS_TEXT);
//        builder.setView(input);
//
//        builder.setPositiveButton("OK", (dialog, which) -> {
//            String m_Text = input.getText().toString();
//            addStockToList(m_Text, TIME_SERIES_DAILY, null/*INTERVAL.MIN_15.getValue()*/);
//        });
//        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
//w
//        builder.show();
//    }

    //@OnClick(R.id.refresh)
    public void onRefresh()
    {
        RealmResults<Stock> ownedStocks = mRealm.where(Stock.class).findAll();
        for(Stock stock : ownedStocks)
        {
            addStockToList(stock.stockName, TIME_SERIES_DAILY, null);
        }
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

    //@OnClick(R.id.addAsset)
    public void action() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Ticker");

        final EditText input = new EditText(getContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("OK", (dialog, which) -> {
            String m_Text = input.getText().toString();
            addStockToList(m_Text, TIME_SERIES_DAILY, null/*INTERVAL.MIN_15.getValue()*/);
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }
}
