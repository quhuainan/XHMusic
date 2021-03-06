package com.qhn.bhne.xhmusic.mvp.ui.fragment;

import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.qhn.bhne.xhmusic.R;
import com.qhn.bhne.xhmusic.mvp.presenter.impl.MVListPresenterImpl;
import com.qhn.bhne.xhmusic.mvp.ui.adapter.MVListAdapter;
import com.qhn.bhne.xhmusic.mvp.view.MVListView;

import javax.inject.Inject;

import butterknife.BindView;

/**
 * Created by qhn
 * on 2016/11/8 0008.
 */

public class MVListFragment extends BaseFragment implements MVListView {
    @Inject
    MVListPresenterImpl mvListPresenter;
    @BindView(R.id.rec_mv_list)
    RecyclerView recMvList;
    @BindView(R.id.sp_choose_category)
    Spinner spChoose;
    private MVListAdapter adapter;

    @Override
    protected void initInjector() {
        getFragmentComponent().inject(this);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_mv_list;
    }

    @Override
    protected void initViews(View mFragmentView) {
        initPresenter();
        initSpinner();

    }

    private void initSpinner() {
        // 建立数据源
        String[] mItems = getResources().getStringArray(R.array.mv_category);
        // 建立Adapter并且绑定数据源
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, mItems);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spChoose.setAdapter(adapter);
        spChoose.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                mvListPresenter.chooseMVCategory(adapterView,view,i,l);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }



    private void initPresenter() {
        mPresenter = mvListPresenter;
        mvListPresenter.attachView(this);
        mvListPresenter.create();

    }

    @Override
    public void loadSuccess(final Object data) {
        super.loadSuccess(data);
        GridLayoutManager linearLayoutManager = new GridLayoutManager(getActivity(), 2);
        recMvList.setItemAnimator(new DefaultItemAnimator());
        recMvList.setLayoutManager(linearLayoutManager);
       // adapter = new MVListAdapter(getActivity(), (HashMap<String, List<MVList.MVBean>>) data);
        recMvList.setAdapter(adapter);
        adapter.notifyDataSetChanged();


    }



    @Override
    protected View getSuccessView() {
        return recMvList;
    }

    @Override
    public void changeMVCategory() {

    }
}
