package com.likeits.simple.fragment.coupon;


import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.likeits.simple.R;
import com.likeits.simple.adapter.div_provider.member.CouponCenterListAdapter;
import com.likeits.simple.adapter.div_provider.member.CouponListAdapter;
import com.likeits.simple.base.BaseFragment;
import com.likeits.simple.network.model.BaseResponse;
import com.likeits.simple.network.model.member.CouponCenterModel;
import com.likeits.simple.network.model.member.CouponListModel;
import com.likeits.simple.network.util.RetrofitUtil;

import java.util.List;

import butterknife.BindView;
import rx.Subscriber;

/**
 * A simple {@link Fragment} subclass.
 */
public class CouponFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener, BaseQuickAdapter.RequestLoadMoreListener {


    private String id;
    private int pageNum = 1;
    private static final int PAGE_SIZE = 6;//为什么是6呢？
    private boolean isErr;
    private boolean mLoadMoreEndGone = false; //是否加载更多完毕
    private int mCurrentCounter = 0;
    int TOTAL_COUNTER = 0;

    @BindView(R.id.RecyclerView)
    RecyclerView mRecyclerView;
    @BindView(R.id.SwipeRefreshLayout)
    SwipeRefreshLayout mSwipeRefreshLayout;
    private List<CouponCenterModel.ListBean> data;
    private CouponCenterListAdapter mAdapter;

    @Override
    protected int setContentView() {
        return R.layout.fragment_coupon;
    }

//    @Override
//    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
//        super.onActivityCreated(savedInstanceState);
//        //获取Activity传来的数据
//
//    }

    @Override
    protected void lazyLoad() {
        Bundle bundle = getArguments();
        id = bundle.getString("id");
        initUI();

    }

    private void initUI() {
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeColors(Color.rgb(47, 223, 189));
        mRecyclerView.setLayoutManager(new WrapContentLinearLayoutManager(getActivity()));
        initData();
    }

    public class WrapContentLinearLayoutManager extends LinearLayoutManager {
        public WrapContentLinearLayoutManager(Context context) {
            super(context);
        }

        @Override
        public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
            try {
                super.onLayoutChildren(recycler, state);
            } catch (IndexOutOfBoundsException e) {
            }
        }
    }

    private void initData() {
      //  LoaddingShow();
        RetrofitUtil.getInstance().GetCouponCenterList(openid, id, String.valueOf(pageNum), new Subscriber<BaseResponse<CouponCenterModel>>() {
            @Override
            public void onCompleted() {
            }

            @Override
            public void onError(Throwable e) {
            }

            @Override
            public void onNext(BaseResponse<CouponCenterModel> baseResponse) {
               // LoaddingDismiss();
                if (baseResponse.getCode() == 200) {
                    data = baseResponse.getData().getList();
                    initAdapter();
                }
            }
        });
    }

    private void initAdapter() {
        mAdapter = new CouponCenterListAdapter(R.layout.layout_coupon_listview_items, data);
        mAdapter.setOnLoadMoreListener(this, mRecyclerView);
        mRecyclerView.setAdapter(mAdapter);
        mCurrentCounter = mAdapter.getData().size();
    }

    @Override
    public void onRefresh() {
        mAdapter.setEnableLoadMore(false);//禁止加载
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // mAdapter.setNewData(data);
                isErr = false;
                mCurrentCounter = PAGE_SIZE;
                pageNum = 1;//页数置为1 才能继续重新加载
                mSwipeRefreshLayout.setRefreshing(false);
                mAdapter.setEnableLoadMore(true);//启用加载
            }
        }, 2000);
    }

    @Override
    public void onLoadMoreRequested() {
        mSwipeRefreshLayout.setEnabled(false);
        //  TOTAL_COUNTER = Integer.valueOf(myfollowModel.getTotal());
        if (mAdapter.getData().size() < PAGE_SIZE) {
            mAdapter.loadMoreEnd(true);
        } else {
            if (mCurrentCounter >= TOTAL_COUNTER) {
                mAdapter.loadMoreEnd(mLoadMoreEndGone);
            } else {
                if (isErr) {
                    pageNum += 1;
                    //  initDate(pageNum, true);
                    //    mAdapter.addData(data);
                    mCurrentCounter = mAdapter.getData().size();
                    mAdapter.loadMoreComplete();
                } else {
                    isErr = true;
                    // Toast.makeText(getContext(), "错误", Toast.LENGTH_LONG).show();
                    mAdapter.loadMoreFail();
                }
            }
            mSwipeRefreshLayout.setEnabled(true);
        }
    }
}
