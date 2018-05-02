package cz.muni.fi.xpavuk.myportfolio.fragments;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import cz.muni.fi.xpavuk.myportfolio.adapter.StockAdapter;


import com.bumptech.glide.Glide;

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
import static cz.muni.fi.xpavuk.myportfolio.api.ApiEnum.INTERVAL;

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
    @BindView(R.id.ticker)
    TextView mTicker;


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

        loadStock("MSFT", FUNCTION.TIME_SERIES_INTRADAY, INTERVAL.MIN_15.getValue());

        RealmResults<MetaData> users = mRealm.where(MetaData.class).findAll();
        mAdapter = new StockAdapter(getContext(), users);
        mList.setAdapter(mAdapter);
        mList.setLayoutManager(new LinearLayoutManager(getContext()));
        mList.setHasFixedSize(true);

        return view;
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

    //TODO FIX
    private void loadStock(@NonNull String ticker, FUNCTION function, String interval) {
        Call<ApiResponse> stockCall = mAlphaVantageApi.getService()
                .getIntraDayForSymbol(function.toString(), ticker, interval.toString());
        stockCall.enqueue(new Callback<ApiResponse>() {

            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                Stock stock = new Stock();
                stock.setStockName(response.body().metaData._2Symbol);
                if (stock == null) {
                    return;
                }

//                Glide.with(getContext())
//                        .load(user.avatarUrl)
//                        .into(mAvatar);
                mTicker.setText(stock.getStockName());
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }
}
