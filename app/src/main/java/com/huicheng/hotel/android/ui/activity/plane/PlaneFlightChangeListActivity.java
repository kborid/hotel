package com.huicheng.hotel.android.ui.activity.plane;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.bumptech.glide.Glide;
import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.common.SessionContext;
import com.huicheng.hotel.android.requestbuilder.bean.AirCompanyInfoBean;
import com.huicheng.hotel.android.requestbuilder.bean.PlaneFlightChangeInfoBean;
import com.huicheng.hotel.android.ui.base.BaseAppActivity;
import com.huicheng.hotel.android.ui.glide.CustomReqURLFormatModelImpl;
import com.prj.sdk.util.StringUtil;
import com.prj.sdk.util.Utils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

/**
 * @author kborid
 * @date 2018/4/11 0011.
 */

public class PlaneFlightChangeListActivity extends BaseAppActivity {
    private String mFlights;
    private List<PlaneFlightChangeInfoBean> changeList = null;

    @BindView(R.id.listView)
    ListView listView;

    @Override
    protected void setContentView() {
        setContentView(R.layout.act_plane_flightchangelist_layout);
    }

    @Override
    protected void dealIntent() {
        super.dealIntent();
        Bundle bundle = getIntent().getExtras();
        if (null != bundle) {
            mFlights = bundle.getString("flights");
        }
    }

    @Override
    protected void initParams() {
        super.initParams();
        findViewById(R.id.comm_title_rl).setBackgroundColor(getResources().getColor(R.color.white));
        tv_center_title.setText("改签航班列表");
        if (StringUtil.notEmpty(mFlights)) {
            changeList = JSON.parseArray(mFlights, PlaneFlightChangeInfoBean.class);
        }
        listView.setAdapter(new FlightChangeAdapter(this, changeList));
    }

    @Override
    protected void initListeners() {
        super.initListeners();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent data = new Intent();
                data.putExtra("flightInfo", changeList.get(position));
                setResult(RESULT_OK, data);
                finish();
            }
        });
    }

    private class FlightChangeAdapter extends BaseAdapter {
        private Context context;
        private List<PlaneFlightChangeInfoBean> mList = new ArrayList<>();

        FlightChangeAdapter(Context context, List<PlaneFlightChangeInfoBean> list) {
            this.context = context;
            this.mList = list;
        }

        @Override
        public int getCount() {
            return mList.size();
        }

        @Override
        public Object getItem(int position) {
            return mList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder = null;
            if (null == convertView) {
                convertView = LayoutInflater.from(context).inflate(R.layout.lv_plane_flight_item, null);
                viewHolder = new ViewHolder(convertView);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            PlaneFlightChangeInfoBean bean = mList.get(position);
            viewHolder.tv_off_time.setText(bean.startTime);
            viewHolder.tv_off_airport.setText(bean.startPlace);
            viewHolder.tv_off_terminal.setText(bean.dptTerminal);
            viewHolder.tv_on_time.setText(bean.endTime);
            viewHolder.tv_on_airport.setText(bean.endPlace);
            viewHolder.tv_on_terminal.setText(bean.arrTerminal);
            if (bean.stopFlightInfo.stopType != 1) {
                viewHolder.stopover_lay.setVisibility(View.VISIBLE);
                viewHolder.tv_stop_city.setText("经停" + bean.stopFlightInfo.stopCityInfoList);
            } else {
                viewHolder.stopover_lay.setVisibility(View.GONE);
            }
            viewHolder.tv_flight_during.setVisibility(View.INVISIBLE);
            viewHolder.tv_flight_price.setText(String.format(context.getString(R.string.rmbStr2), Integer.valueOf(bean.gqFee)));
            viewHolder.tv_flight_code.setText(bean.flightNo);
            viewHolder.tv_flight_name.setText(bean.flightType);
            if (StringUtil.notEmpty(bean.carrier)) {
                if (SessionContext.getAirCompanyMap().size() > 0
                        && SessionContext.getAirCompanyMap().containsKey(bean.carrier)) {
                    AirCompanyInfoBean companyInfoBean = SessionContext.getAirCompanyMap().get(bean.carrier);
                    viewHolder.tv_flight_carrier.setText(companyInfoBean.company);
                    viewHolder.iv_flight_icon.setVisibility(View.VISIBLE);
                    Glide.with(context)
                            .load(new CustomReqURLFormatModelImpl(companyInfoBean.logourl))
                            .fitCenter()
                            .override(Utils.dp2px(15), Utils.dp2px(15))
                            .into(viewHolder.iv_flight_icon);
                } else {
                    viewHolder.tv_flight_carrier.setText(bean.carrier);
                    viewHolder.iv_flight_icon.setVisibility(View.GONE);
                }
            } else {
                viewHolder.iv_flight_icon.setVisibility(View.GONE);
            }

            String correct = "";
            if (StringUtil.notEmpty(correct)) {
                viewHolder.tv_flight_percentInTime.setText(correct);
                viewHolder.zdl_lay.setVisibility(View.VISIBLE);
            } else {
                viewHolder.zdl_lay.setVisibility(View.GONE);
            }

            return convertView;
        }

        class ViewHolder {
            RelativeLayout rootView;
            TextView tv_off_time;
            TextView tv_off_airport;
            TextView tv_off_terminal;
            TextView tv_on_time;
            TextView tv_on_airport;
            TextView tv_on_terminal;
            LinearLayout stopover_lay;
            TextView tv_stop_city;
            TextView tv_flight_during;
            TextView tv_flight_price;
            ImageView iv_flight_icon;
            TextView tv_flight_carrier;
            TextView tv_flight_code;
            TextView tv_flight_name;
            LinearLayout zdl_lay;
            TextView tv_flight_percentInTime;
            TextView tv_tag_lowest;

            public ViewHolder(View itemView) {
                rootView = (RelativeLayout) itemView.findViewById(R.id.rootView);
                tv_off_time = (TextView) itemView.findViewById(R.id.tv_off_time);
                tv_off_airport = (TextView) itemView.findViewById(R.id.tv_off_airport);
                tv_off_terminal = (TextView) itemView.findViewById(R.id.tv_off_terminal);
                tv_on_time = (TextView) itemView.findViewById(R.id.tv_on_time);
                tv_on_airport = (TextView) itemView.findViewById(R.id.tv_on_airport);
                tv_on_terminal = (TextView) itemView.findViewById(R.id.tv_on_terminal);
                stopover_lay = (LinearLayout) itemView.findViewById(R.id.stopover_lay);
                tv_stop_city = (TextView) itemView.findViewById(R.id.tv_stop_city);
                tv_flight_during = (TextView) itemView.findViewById(R.id.tv_flight_during);
                tv_flight_price = (TextView) itemView.findViewById(R.id.tv_flight_price);
                iv_flight_icon = (ImageView) itemView.findViewById(R.id.iv_flight_icon);
                tv_flight_carrier = (TextView) itemView.findViewById(R.id.tv_flight_carrier);
                tv_flight_code = (TextView) itemView.findViewById(R.id.tv_flight_code);
                tv_flight_name = (TextView) itemView.findViewById(R.id.tv_flight_name);
                zdl_lay = (LinearLayout) itemView.findViewById(R.id.zdl_lay);
                tv_flight_percentInTime = (TextView) itemView.findViewById(R.id.tv_flight_percentInTime);
                tv_tag_lowest = (TextView) itemView.findViewById(R.id.tv_tag_lowest);
            }
        }
    }
}
