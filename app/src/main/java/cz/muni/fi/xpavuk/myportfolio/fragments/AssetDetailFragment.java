package cz.muni.fi.xpavuk.myportfolio.fragments;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import cz.muni.fi.xpavuk.myportfolio.R;
import cz.muni.fi.xpavuk.myportfolio.databinding.FragmentAssetDetailBinding;

import cz.muni.fi.xpavuk.myportfolio.model.HistoricalData;
import cz.muni.fi.xpavuk.myportfolio.model.Stock;
import cz.muni.fi.xpavuk.myportfolio.utils.StockParser;
import io.realm.Realm;
import io.realm.RealmList;

import android.databinding.DataBindingUtil;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;


/**
 * author: Tomas Pavuk
 * date: 6.5.2018
 */

public class AssetDetailFragment extends Fragment {

    public static AssetDetailFragment newInstance() {
        return new AssetDetailFragment();
    }

    private Unbinder mUnbinder;

    Stock currentStock;

    @BindView(R.id.asset_detail_layout)
    LinearLayout linearLayout;

    FragmentAssetDetailBinding assetDetailBinding;

    @BindView(R.id.line_chart)
    LineChart lineChart;

    @BindView(R.id.textview_closed)
    TextView updatedAgoTextView;

    @BindView(R.id.delete_asset_from_details)
    Button deleteButton;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        assetDetailBinding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_asset_detail, container, false);
        mUnbinder = ButterKnife.bind(this, assetDetailBinding.getRoot());

        Drawable buttonBackground = deleteButton.getBackground();
        if(buttonBackground != null) {
            buttonBackground.setColorFilter(0xFFFFBB33, PorterDuff.Mode.MULTIPLY);
            deleteButton.setBackground(buttonBackground);
        }

        return assetDetailBinding.getRoot();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Stock s = (Stock)getArguments().getSerializable("selected_key");
        assetDetailBinding.setStock(s);
        currentStock = s;
        drawGraph(s);
        updatedAgoTextView.setText(currentStock.getupdatedAgoString(getContext()));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
    }
    @Override
    public void onStop() {
        super.onStop();
        ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.show();
        }
    }

    private void drawGraph(Stock stock) {
        if (stock.historicalData == null || stock.historicalData.size() == 0 || lineChart == null) {
            return;
        }
        lineChart.setVisibility(View.VISIBLE);

        int index = stock.historicalData.size() - 1;
        int maxIndex = index;
        Entry[] entries = new Entry[index + 1];

        HashMap<String, Double> historicalData = parseHistoricalDataToHashMap(stock.historicalData);
        HashMap<Integer, String> xAxisValueToTextMap = new HashMap<>();

        String key = stock.lastUpdatedDate;
        while (index >= 0) {
            if (historicalData.containsKey(key)) {
                entries[index] = new Entry(index, historicalData.get(key).floatValue());
                xAxisValueToTextMap.put(index, key);
                index--;
            }

            Date date = StockParser.convertStringToDate(key);
            date.setTime(date.getTime() - 2);
            key = StockParser.convertDateToString(date);

        }

        Description description = new Description();
        description.setText(getContext().getResources().getString(R.string.stock_history));
        lineChart.setDescription(description);
        LineDataSet lineDataSet = new LineDataSet(Arrays.asList(entries), getContext().getResources().getString(R.string.stock_price));

        lineChart.getAxisRight().setEnabled(false);
        lineChart.getXAxis().setValueFormatter((value, axis) -> xAxisValueToTextMap.get((int) value));

        lineDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        lineDataSet.setCubicIntensity(0.2f);
        lineDataSet.setDrawCircles(false);
        lineDataSet.setLineWidth(1.8f);
        lineDataSet.setCircleRadius(4f);
        lineDataSet.setCircleColor(Color.WHITE);
        lineDataSet.setHighLightColor(Color.rgb(244, 117, 117));
        lineDataSet.setColor(Color.WHITE);
        lineDataSet.setFillColor(Color.WHITE);
        lineDataSet.setFillAlpha(100);
        lineDataSet.setDrawHorizontalHighlightIndicator(false);
        lineDataSet.setFillFormatter((dataSet, dataProvider) -> -10);

        // create a data object with the datasets
        LineData data = new LineData(lineDataSet);
        data.setValueTextSize(9f);
        data.setDrawValues(false);

        // set data
        lineChart.setData(data);
        lineChart.setMaxVisibleValueCount(10);
        lineChart.setVisibleXRangeMaximum(10);
        lineChart.moveViewToX(maxIndex);
        lineChart.setScaleX(1);
        lineDataSet.setColor(ContextCompat.getColor(getContext(), R.color.holo_orange_dark));
        lineDataSet.setFillColor(ContextCompat.getColor(getContext(), R.color.holo_orange_dark));
    }

    private HashMap<String, Double> parseHistoricalDataToHashMap(RealmList<HistoricalData> historicalData) {
        HashMap<String, Double> dataMap = new HashMap<>();
        for (HistoricalData data : historicalData) {
            dataMap.put(data.key, data.value);
        }
        return dataMap;
    }

    @OnClick(R.id.delete_asset_from_details)
    void deleteStock() {
        AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
        alertDialog.setTitle(getString(R.string.delete));
        alertDialog.setMessage(getString(R.string.delete_dialog_message));
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.cancel_button),
                (dialog, which) -> dialog.dismiss());
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.delete),
                (dialog, which) -> {
                    Intent intent = new Intent();
                    Bundle extras = new Bundle();
                    extras.putSerializable("stock", currentStock);
                    intent.putExtras(extras);
                    dialog.dismiss();
                    getFragmentManager().popBackStack();
                    getFragmentManager().findFragmentByTag(StockListFragment.TAG).onActivityResult(getTargetRequestCode(), 2, intent);
                });

        alertDialog.show();
    }
}
