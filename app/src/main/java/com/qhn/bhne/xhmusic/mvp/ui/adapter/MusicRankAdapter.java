package com.qhn.bhne.xhmusic.mvp.ui.adapter;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.qhn.bhne.xhmusic.R;
import com.qhn.bhne.xhmusic.mvp.entity.MusicRank;
import com.qhn.bhne.xhmusic.mvp.ui.adapter.base.BaseRecyclerViewAdapter;
import com.qhn.bhne.xhmusic.utils.MyUtils;
import java.util.List;
import javax.inject.Inject;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by qhn
 * on 2016/11/9 0009.
 */

public class MusicRankAdapter extends BaseRecyclerViewAdapter<MusicRank,MusicRank> {



    @Inject
    Activity activity;
    @Inject
    public MusicRankAdapter() {
        super(null);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = getView(parent, R.layout.item_rank_music);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        ItemViewHolder itemViewHolder = (ItemViewHolder) holder;
        itemViewHolder.txtRankName.setText(mList.get(position).getRankname().replace("酷狗",""));
        List<MusicRank.SongInfoBean> songInfoBeanList=mList.get(position).getSonginfo();
        if (songInfoBeanList.size()!=0) {
            itemViewHolder.txtUpdateTime.setText(songInfoBeanList.get(0).getSongname()+"\n"+
                    songInfoBeanList.get(1).getSongname()+"\n"+
                    songInfoBeanList.get(2).getSongname());
        }

        MyUtils.loadImageFormNet(mList.get(position).getImgurl().replace("{size}","400"),
                itemViewHolder.imgRankPic,activity);


    }

    public class ItemViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.img_rank_pic)
        ImageView imgRankPic;
        @BindView(R.id.txt_rank_name)
        TextView txtRankName;
        @BindView(R.id.txt_update_time)
        TextView txtUpdateTime;
        public ItemViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    itemListener.onClick(getList().get(getAdapterPosition()),getAdapterPosition());
                }
            });
        }
    }
}
