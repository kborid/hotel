package com.huicheng.hotel.android.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.common.HotelOrderManager;
import com.huicheng.hotel.android.net.bean.FansHotelInfoBean;
import com.huicheng.hotel.android.ui.activity.HotelCalendarChooseActivity;
import com.huicheng.hotel.android.ui.base.BaseFragment;
import com.huicheng.hotel.android.ui.custom.RoundedAllImageView;

public class CardFragment extends BaseFragment {

    private FansHotelInfoBean bean = null;

    private RoundedAllImageView iv_background;
    private TextView tv_name, tv_order;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        bean = (FansHotelInfoBean) getArguments().getSerializable("bean");
        View view = inflater.inflate(R.layout.vp_fanshotel_item, container, false);
        initViews(view);
        initParams();
        initListeners();
        return view;
    }

    @Override
    protected void initViews(View view) {
        super.initViews(view);
        iv_background = (RoundedAllImageView) view.findViewById(R.id.iv_background);
        tv_name = (TextView) view.findViewById(R.id.tv_name);
        tv_order = (TextView) view.findViewById(R.id.tv_order);
    }

    @Override
    protected void initParams() {
        super.initParams();
        tv_name.setText(bean.name);
        loadImage(iv_background, R.drawable.def_fans, bean.featurePicPath, 750, 1050);
    }

    @Override
    protected void initListeners() {
        super.initListeners();
        tv_order.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), HotelCalendarChooseActivity.class);
                HotelOrderManager.getInstance().reset();
                HotelOrderManager.getInstance().setIsVipHotel(true);
                HotelOrderManager.getInstance().setVipHotelId(bean.hotelId);
                HotelOrderManager.getInstance().setCityStr(bean.provinceName, bean.cityName);
                startActivity(intent);
            }
        });
    }

    public static Fragment newInstance(int index, FansHotelInfoBean bean) {
        Fragment fragment = new CardFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("index", index);
        bundle.putSerializable("bean", bean);
        fragment.setArguments(bundle);
        return fragment;
    }
}