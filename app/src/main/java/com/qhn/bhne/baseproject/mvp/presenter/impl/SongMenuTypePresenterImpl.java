package com.qhn.bhne.baseproject.mvp.presenter.impl;


import com.qhn.bhne.baseproject.mvp.entity.SongMenuType;
import com.qhn.bhne.baseproject.mvp.model.impl.SongMenuTypeModelImpl;
import com.qhn.bhne.baseproject.mvp.presenter.SongMenuTypePresenter;
import com.qhn.bhne.baseproject.mvp.presenter.base.BasePresenterImpl;
import com.qhn.bhne.baseproject.mvp.view.SongMenuTypeView;

import java.util.List;

import javax.inject.Inject;

/**
 * Created by Administrator on 2017/02/28
 */

public class SongMenuTypePresenterImpl extends BasePresenterImpl<SongMenuTypeView, List<SongMenuType>> implements SongMenuTypePresenter {

    @Inject
    SongMenuTypeModelImpl songMenuTypeModel;

    @Inject
    public SongMenuTypePresenterImpl(SongMenuTypeModelImpl songMenuTypeModel) {
        this.songMenuTypeModel = songMenuTypeModel;
    }


    @Override
    public void loadSongMenuType() {
        songMenuTypeModel.loadSongMenuType(this);
    }


    @Override
    public void success(List<SongMenuType> data) {
        mView.loadSuccess(data);
    }

    @Override
    public void create() {
        loadSongMenuType();
    }
}