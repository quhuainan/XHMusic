package com.qhn.bhne.xhmusic.mvp.model;

import com.qhn.bhne.xhmusic.listener.RequestCallBack;

import rx.Subscription;

/**
 * Created by qhn
 * on 2016/11/8 0008.
 */

public interface ChannelListInteractor<T> {
    Subscription loadChannelList(RequestCallBack<T> listener);
}
