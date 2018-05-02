package cz.muni.fi.xpavuk.myportfolio.adapter;
import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import cz.muni.fi.xpavuk.myportfolio.R;
import cz.muni.fi.xpavuk.myportfolio.model.MetaData;
import cz.muni.fi.xpavuk.myportfolio.model.StockData;
import io.realm.OrderedRealmCollection;
import io.realm.RealmRecyclerViewAdapter;

/**
 * author: Tomas Pavuk
 * date: 30.4.2018
 */

public class StockAdapter extends RealmRecyclerViewAdapter<MetaData, StockAdapter.ViewHolder>{

    private Context mContext;

    public StockAdapter(Context context, @Nullable OrderedRealmCollection<MetaData> data) {
        super(data, true);
        mContext = context;
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
        MetaData data = getItem(position);
//        Glide.with(mContext)
//                .load(user.avatarUrl)
//                .into(holder.mAvatar);
        // TODO: fixnut
        holder.mTicker.setText(data._2Symbol);
    }

    /**
     * Reusable ViewHolder objects.
     */
    class ViewHolder extends RecyclerView.ViewHolder {

//        @BindView(R.id.avatar)
//        ImageView mAvatar;
        @BindView(R.id.ticker)
        TextView mTicker;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}