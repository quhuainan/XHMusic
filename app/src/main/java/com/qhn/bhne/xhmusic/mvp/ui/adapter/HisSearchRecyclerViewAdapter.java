package com.qhn.bhne.xhmusic.mvp.ui.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.qhn.bhne.xhmusic.R;
import com.qhn.bhne.xhmusic.mvp.entity.db.HistorySearch;
import com.qhn.bhne.xhmusic.mvp.ui.adapter.base.BaseRecyclerViewAdapter;
import com.qhn.bhne.xhmusic.mvp.ui.fragment.SearchRecommendFragment;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by qhn
 * on 2017/2/24 0024.
 */

public class HisSearchRecyclerViewAdapter extends BaseRecyclerViewAdapter<HistorySearch,HistorySearch> {
    private SearchRecommendFragment searchRecommendFragment;

    private SearchRecommendFragment getSearchRecommendFragment() {
        return searchRecommendFragment;
    }

    public void setSearchRecommendFragment(SearchRecommendFragment searchRecommendFragment) {
        this.searchRecommendFragment = searchRecommendFragment;
    }

    @Inject
    public HisSearchRecyclerViewAdapter() {
        super(null);

    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = getView(parent, R.layout.item_history_search);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        super.onBindViewHolder(holder, position);
        final HistorySearch historySearch = getList().get(position);
        ItemViewHolder itemViewHolder = (ItemViewHolder) holder;
        itemViewHolder.txtSongName.setText(historySearch.getName());

        itemViewHolder.txtSongName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getSearchRecommendFragment().getPresenter().doSearch(historySearch.getName());
            }
        });
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.txt_song_name)
        TextView txtSongName;
        @BindView(R.id.img_delete_record)
        ImageView imgDelete;

        public ItemViewHolder(final View view) {
            super(view);
            ButterKnife.bind(this, view);

            imgDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getSearchRecommendFragment().getPresenter().deleteHistoryRecord((mList.get(getAdapterPosition())));
                    mList.remove(getAdapterPosition());
                    notifyDataSetChanged();
                }
            });

        }
    }
}
