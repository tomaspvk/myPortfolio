package cz.muni.fi.xpavuk.myportfolio.adapter;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnLongClick;
import cz.muni.fi.xpavuk.myportfolio.R;
import cz.muni.fi.xpavuk.myportfolio.activities.MainActivity;
import cz.muni.fi.xpavuk.myportfolio.fragments.AssetDetailFragment;
import cz.muni.fi.xpavuk.myportfolio.fragments.StockListFragment;
import cz.muni.fi.xpavuk.myportfolio.model.Stock;
import io.realm.OrderedRealmCollection;
import io.realm.RealmRecyclerViewAdapter;

/**
 * author: Tomas Pavuk
 * date: 30.4.2018
 */

public class StockAdapter extends RealmRecyclerViewAdapter<Stock, StockAdapter.ViewHolder>{

    private Context mContext;
    private StockListFragment fragment;

    public StockAdapter(Context context, @Nullable OrderedRealmCollection<Stock> data, StockListFragment fragment) {
        super(data, true);
        mContext = context;
        this.fragment = fragment;
    }

    /**
     * Creates new ViewHolder instances and inflates them with XML layout.
     */
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_asset, parent, false));
    }

    /**
     * Gets inflated ViewHolder instance and fills views with data.
     */
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Stock stock = getItem(position);
        if (stock == null) {
            return;
        }
        holder.mTicker.setText(stock.stockName);
        holder.mCurrentPrice.setText(String.valueOf(stock.currentPrice));
        holder.mChange.setText(stock.getIncreaseDecreaseText());
        int changeColor = stock.changeInPrice > 0 ? Color.parseColor("#ff99cc00") : Color.parseColor("#ffff4444");
        holder.mChange.setTextColor(changeColor);
        holder.mQuantity.setText(String.valueOf(stock.ownedQuantity));

        holder.mInfo.setOnClickListener(view -> fragmentJump(stock));
    }

    private void fragmentJump(Stock mStockSelected) {
        Fragment detailFragment = new AssetDetailFragment();
        Bundle mBundle = new Bundle();
        mBundle.putSerializable("selected_key", mStockSelected);
        detailFragment.setArguments(mBundle);
        switchContent(fragment.getId(), detailFragment);
    }

    private void switchContent(int id, Fragment fragment) {
        if (mContext == null)
            return;
        if (mContext instanceof MainActivity) {
            MainActivity mainActivity = (MainActivity) mContext;
            mainActivity.switchContent(id, fragment);
        }

    }


    /**
     * Reusable ViewHolder objects.
     */
    class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.ticker)
        TextView mTicker;
        @BindView(R.id.current_price)
        TextView mCurrentPrice;
        @BindView(R.id.change)
        TextView mChange;
        @BindView(R.id.quantity)
        TextView mQuantity;
        @BindView(R.id.show_info)
        ImageView mInfo;

        private ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @OnLongClick
        boolean onLongClick(View view){
            PopupMenu popup = new PopupMenu(view.getContext(), view);
            popup.getMenuInflater().inflate(R.menu.popup_menu, popup.getMenu());
            popup.setOnMenuItemClickListener(menuItem -> {
                switch(menuItem.getItemId()){
                    case(R.id.menu_asset_delete):
                        fragment.delete(getItem(getAdapterPosition()));
                        notifyDataSetChanged();
                        return true;
                    default: return false;
                }
            });
            popup.show();
            return true;
        }
    }
}