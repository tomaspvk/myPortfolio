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
import android.widget.EditText;
import android.widget.TextView;

import butterknife.OnClick;
import cz.muni.fi.xpavuk.myportfolio.adapter.StockAdapter;

import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import cz.muni.fi.xpavuk.myportfolio.R;
import cz.muni.fi.xpavuk.myportfolio.api.AlphaVantageApi;
import cz.muni.fi.xpavuk.myportfolio.model.ApiStockResponse;
import cz.muni.fi.xpavuk.myportfolio.model.Stock;
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

public class StockListFragment extends Fragment {

    private static final String TAG = StockListFragment.class.getSimpleName();

    private AlphaVantageApi mAlphaVantageApi;
    private StockAdapter mAdapter;
    private Realm mRealm;

    private Unbinder mUnbinder;
    @BindView(android.R.id.list)
    RecyclerView mList;
    @BindView(R.id.portfolio_value)
    TextView mPortfolioValue;

    public static StockListFragment newInstance() {
        return new StockListFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAlphaVantageApi = new AlphaVantageApi();
        mRealm = Realm.getDefaultInstance();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list, container, false);
        mUnbinder = ButterKnife.bind(this, view);

        RealmResults<Stock> ownedStocks = mRealm.where(Stock.class).and().equalTo("isValidStock", true).findAll();
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
        Call<ApiStockResponse> stockCall;
        if (function == FUNCTION.DIGITAL_CURRENCY_DAILY) {
            stockCall = mAlphaVantageApi.getService()
                    .getIntraDayForCryptoSymbol(function.toString(), ticker, interval, "EUR");
        } else {
            stockCall = mAlphaVantageApi.getService()
                    .getIntraDayForSymbol(function.toString(), ticker, interval);
        }
        stockCall.enqueue(new Callback<ApiStockResponse>() {

            @Override
            public void onResponse(@NonNull Call<ApiStockResponse> call, @NonNull Response<ApiStockResponse> response) {
                Stock stock = getStockFromStockApiResponse(response.body());
                stock.isCrypto = function == FUNCTION.DIGITAL_CURRENCY_DAILY;
                saveResult(Collections.singletonList(stock));
//                Stock stock = new Stock();
//                stock.stockName = response.body().metaData._2Symbol.toUpperCase();
//                saveResult(Arrays.asList(stock));
            }

            @Override
            public void onFailure(@NonNull Call<ApiStockResponse> call, @NonNull Throwable t) {
                t.printStackTrace();
            }
        });
    }

    FUNCTION selectedAssetType = FUNCTION.TIME_SERIES_DAILY;

    @OnClick(R.id.add_asset)
    public void onAddAssetClicked() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Ticker");

        final EditText input = new EditText(getContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        final CharSequence[] items = {" Stock "," Crypto "};
        //FUNCTION selectedAssetType = FUNCTION.TIME_SERIES_DAILY;
        builder.setSingleChoiceItems(items, 0, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int item) {

                switch(item)
                {
                    case 0:
                        selectedAssetType = TIME_SERIES_DAILY;
                        break;
                    case 1:

                        selectedAssetType = DIGITAL_CURRENCY_DAILY;
                        break;
                }
                //alertDialog1.dismiss();
            }
        });

        builder.setPositiveButton("OK", (dialog, which) -> {
            String m_Text = input.getText().toString();
            addStockToList(m_Text, selectedAssetType, null/*INTERVAL.MIN_15.getValue()*/);
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    @OnClick(R.id.refresh)
    public void onRefreshClicked()
    {
        RealmResults<Stock> ownedStocks = mRealm.where(Stock.class).and().equalTo("isValidStock", true).findAll();
        for(Stock stock : ownedStocks)
        {
            if (stock.isCrypto)
                addStockToList(stock.stockName, DIGITAL_CURRENCY_DAILY, null);
            else
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

}
