package com.huicheng.hotel.android.ui.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.route.BusRouteResult;
import com.amap.api.services.route.DrivePath;
import com.amap.api.services.route.DriveRouteResult;
import com.amap.api.services.route.RideRouteResult;
import com.amap.api.services.route.RouteSearch;
import com.amap.api.services.route.WalkPath;
import com.amap.api.services.route.WalkRouteResult;
import com.google.gson.Gson;
import com.huicheng.hotel.android.R;
import com.huicheng.hotel.android.common.HotelCommDef;
import com.huicheng.hotel.android.common.HotelOrderManager;
import com.huicheng.hotel.android.common.SessionContext;
import com.huicheng.hotel.android.control.AMapLocationControl;
import com.huicheng.hotel.android.net.bean.HotelDetailInfoBean;
import com.huicheng.hotel.android.net.bean.HotelMapInfoBean;
import com.huicheng.hotel.android.ui.adapter.PinInfoWindowAdapter;
import com.huicheng.hotel.android.ui.base.BaseActivity;
import com.huicheng.hotel.android.ui.dialog.CustomDialog;
import com.huicheng.hotel.android.ui.mapoverlay.DrivingRouteOverlay;
import com.huicheng.hotel.android.ui.mapoverlay.WalkRouteOverlay;
import com.prj.sdk.util.LogUtil;
import com.prj.sdk.util.StringUtil;
import com.prj.sdk.widget.CustomToast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author kborid
 * @date 2016/10/31 0031
 * @modify 2017/03/15
 */
public class HotelMapActivity extends BaseActivity
        implements AMap.OnMarkerClickListener, RouteSearch.OnRouteSearchListener,
        LocationSource, AMapLocationListener {

    private static final String TAG = "HotelMapActivity";

    private ImageView iv_zoom_out;
    private ImageView iv_zoom_in;
    private ImageView iv_loc;
    private ImageView iv_reroute;
    private ImageView iv_navi;
    private EditText et_search;
    private ImageView iv_search;
    private TextView tv_driver;
    private TextView tv_foot;

    private MapView mapview = null;
    private AMap amap = null;
    private Marker currentMarker;
    private OnLocationChangedListener mListener;
    private AMapLocation aMapLocation;
    private RouteSearch routeSearch;
    private DrivingRouteOverlay drivingRouteOverlay = null;
    private DriveRouteResult driveRouteResult = null;
    private WalkRouteOverlay walkRouteOverlay = null;
    private WalkRouteResult walkRouteResult = null;
    private ArrayList<MarkerOptions> markerOptionses = new ArrayList<>();
    private List<Marker> markers = new ArrayList<>();
    private boolean isToMyLoc = false;

    private HotelDetailInfoBean hotelDetailInfoBean = null;
    private List<HotelMapInfoBean> hotelList = new ArrayList<>();
    private String hotelCityStr;
    private String key = null;

    private int mPinId, mPricePinId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_hotelmap_layout);
        TypedArray ta = obtainStyledAttributes(R.styleable.MyTheme);
        mPinId = ta.getResourceId(R.styleable.MyTheme_mapPinImage, R.drawable.iv_pin);
        mPricePinId = ta.getResourceId(R.styleable.MyTheme_mapPricePinImage, R.drawable.iv_pin_price);
        ta.recycle();

        initViews();
        initParams();
        initListeners();
        //在activity执行onCreate时执行mapview.onCreate(savedInstanceState)，实现地图生命周期管理
        mapview.onCreate(savedInstanceState);
        refreshMapOverLayout(false);
        if (hotelList.size() > 0 || hotelDetailInfoBean != null) {
            isToMyLoc = false;
            showSearchResultToMap();
        } else {
            isToMyLoc = true;
        }
        LogUtil.i(TAG, "isToMyLoc = " + isToMyLoc);
    }

    @Override
    public void initViews() {
        super.initViews();
        iv_zoom_out = (ImageView) findViewById(R.id.iv_zoom_out);
        iv_zoom_in = (ImageView) findViewById(R.id.iv_zoom_in);
        iv_loc = (ImageView) findViewById(R.id.iv_loc);
        iv_reroute = (ImageView) findViewById(R.id.iv_reroute);
        iv_navi = (ImageView) findViewById(R.id.iv_navi);
        et_search = (EditText) findViewById(R.id.et_search);
        iv_search = (ImageView) findViewById(R.id.iv_search);
        tv_driver = (TextView) findViewById(R.id.tv_driver);
        tv_foot = (TextView) findViewById(R.id.tv_foot);
        mapview = (MapView) findViewById(R.id.mapview);
    }

    @Override
    public void dealIntent() {
        super.dealIntent();
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            if (bundle.getSerializable("bean") != null) {
                hotelDetailInfoBean = (HotelDetailInfoBean) bundle.getSerializable("bean");
            }
            if (bundle.getString("hotelCityStr") != null) {
                hotelCityStr = bundle.getString("hotelCityStr");
            }
            switch (bundle.getInt("index")) {
                case 0:
                    hotelList.addAll(SessionContext.getAllDayList());
                    key = HotelCommDef.ALLDAY;
                    break;
                case 1:
                    hotelList.addAll(SessionContext.getClockList());
                    key = HotelCommDef.CLOCK;
                    break;
                case 2:
                    hotelList.addAll(SessionContext.getYgrList());
                    key = HotelCommDef.YEGUIREN;
                    break;
                case 3:
                    hotelList.addAll(SessionContext.getHhyList());
                    key = HotelCommDef.HOUHUIYAO;
                    break;
                default:
                    hotelList.addAll(SessionContext.getAllDayList());
                    key = HotelCommDef.ALLDAY;
                    break;
            }
        }
    }

    @Override
    public void initParams() {
        super.initParams();
        if (StringUtil.isEmpty(hotelCityStr)) {
            hotelCityStr = HotelOrderManager.getInstance().getCityStr();
        }
        tv_center_title.setText(hotelCityStr + "(" + HotelOrderManager.getInstance().getDateStr() + ")");
        tv_center_title.getPaint().setFakeBoldText(true);

        if (amap == null) {
            amap = mapview.getMap();
            setUpMap();
        }
        routeSearch = new RouteSearch(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    private Bitmap getMyPinBitmap(String price) {
        View view = LayoutInflater.from(this).inflate(R.layout.map_item_pin, null);
        view.setBackgroundResource(mPricePinId);
        TextView tv_price = (TextView) view.findViewById(R.id.tv_price);
        tv_price.setText(price);
        view.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        view.buildDrawingCache();
        return view.getDrawingCache();
    }

    private void showSearchResultToMap() {
        LogUtil.i(TAG, "showSearchResultToMap()");
        markerOptionses.clear();
        markers.clear();

        if (hotelDetailInfoBean != null) {
            double lon = Double.parseDouble(hotelDetailInfoBean.lon);
            double lat = Double.parseDouble(hotelDetailInfoBean.lat);
            LatLng latLng = new LatLng(lat, lon);
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(latLng).title(hotelDetailInfoBean.name).draggable(true);
            Map<String, String> param = new HashMap<>();
            param.put("name", hotelDetailInfoBean.name);
            param.put("icon", hotelDetailInfoBean.picPath.get(0));
            param.put("address", hotelDetailInfoBean.address);
            param.put("hotelId", String.valueOf(hotelDetailInfoBean.id));
            markerOptions.snippet(new Gson().toJson(param));
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(getResources(), mPinId)));
            markerOptionses.add(markerOptions);
        } else {
            LogUtil.i(TAG, "current marker = " + currentMarker);
            if (currentMarker != null) {
                amap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(currentMarker.getPosition().latitude, currentMarker.getPosition().longitude), 12f));
            }
            for (int i = 0; i < hotelList.size(); i++) {
                HotelMapInfoBean bean = hotelList.get(i);
                if (StringUtil.notEmpty(bean.coordinate)) {
                    String[] latlngStr = bean.coordinate.split("\\|");
                    LatLng latLng = new LatLng(Double.parseDouble(latlngStr[0]), Double.parseDouble(latlngStr[1]));
                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(latLng).title(bean.hotelName).draggable(true);
                    Map<String, String> param = new HashMap<>();
                    param.put("name", bean.hotelName);
                    param.put("icon", bean.hotelIcon);
                    param.put("address", bean.hotelAddress);
                    param.put("hotelId", String.valueOf(bean.hotelId));
                    markerOptions.snippet(new Gson().toJson(param));
                    markerOptions.icon(BitmapDescriptorFactory.fromBitmap(getMyPinBitmap(String.valueOf(bean.price))));
                    markerOptionses.add(markerOptions);
                }
            }
        }

        markers = amap.addMarkers(markerOptionses, true);
    }

    private void setUpMap() {
        amap.getUiSettings().setScaleControlsEnabled(true);
        amap.getUiSettings().setZoomControlsEnabled(false);
        amap.getUiSettings().setTiltGesturesEnabled(false);// 设置地图是否可以倾斜
        amap.setTrafficEnabled(false);// 设置地图是否显示traffic情报

        final PinInfoWindowAdapter infoWindowAdapter = new PinInfoWindowAdapter(this);
        infoWindowAdapter.setNaviClickListener(new PinInfoWindowAdapter.NaviClickListener() {
            @Override
            public void calculateRoute(Marker marker) {
                if (marker.isInfoWindowShown()) {
                    marker.hideInfoWindow();
                }
                if (null != aMapLocation) {
                    calculateRouteByDriver(new LatLonPoint(aMapLocation.getLatitude(), aMapLocation.getLongitude()), new LatLonPoint(marker.getPosition().latitude, marker.getPosition().longitude));
                }
            }

            @Override
            public void changeScreen(int hotelId) {
                Intent intent = new Intent(HotelMapActivity.this, RoomListActivity.class);
                intent.putExtra("hotelId", hotelId);
                intent.putExtra("key", key);
                startActivity(intent);
            }
        });
        amap.setInfoWindowAdapter(infoWindowAdapter);
        MyLocationStyle myLocationStyle = new MyLocationStyle();
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_FOLLOW_NO_CENTER);
        myLocationStyle.myLocationIcon(BitmapDescriptorFactory.fromResource(R.drawable.iv_gps));
        amap.setMyLocationStyle(myLocationStyle);
    }

    @Override
    public void initListeners() {
        super.initListeners();
        iv_zoom_out.setOnClickListener(this);
        iv_zoom_in.setOnClickListener(this);
        iv_loc.setOnClickListener(this);
        iv_search.setOnClickListener(this);
        tv_driver.setOnClickListener(this);
        tv_foot.setOnClickListener(this);
        iv_reroute.setOnClickListener(this);
        iv_navi.setOnClickListener(this);
        routeSearch.setRouteSearchListener(this);

        amap.setOnMarkerClickListener(this);
        amap.setLocationSource(this);// 设置定位监听
        amap.setMyLocationEnabled(true);
        amap.setOnMapClickListener(new AMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if (currentMarker.isInfoWindowShown()) {
                    currentMarker.hideInfoWindow();
                }
            }
        });
    }


    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.iv_zoom_in:
                if (amap.getCameraPosition().zoom < amap.getMaxZoomLevel()) {
                    if (!iv_zoom_out.isEnabled()) {
                        iv_zoom_out.setEnabled(true);
                    }
                    amap.animateCamera(CameraUpdateFactory.zoomIn());
                } else {
                    iv_zoom_in.setEnabled(false);
                }
                break;
            case R.id.iv_zoom_out:
                if (amap.getCameraPosition().zoom > amap.getMinZoomLevel()) {
                    if (!iv_zoom_in.isEnabled()) {
                        iv_zoom_in.setEnabled(true);
                    }
                    amap.animateCamera(CameraUpdateFactory.zoomOut());
                } else {
                    iv_zoom_out.setEnabled(false);
                }
                break;
            case R.id.iv_loc:
                if (aMapLocation != null) {
                    amap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(aMapLocation.getLatitude(), aMapLocation.getLongitude()), amap.getCameraPosition().zoom));
                } else {
                    isToMyLoc = true;
                    AMapLocationControl.getInstance().startLocationAlways(this, this);
                }
                break;
            case R.id.iv_search:
                String keywork = et_search.getText().toString().trim();
                break;

            case R.id.tv_driver: {
                if (tv_driver.isSelected()) {
                    return;
                }
                calculateRouteByDriver(new LatLonPoint(aMapLocation.getLatitude(), aMapLocation.getLongitude()), new LatLonPoint(currentMarker.getPosition().latitude, currentMarker.getPosition().longitude));
                break;
            }
            case R.id.tv_foot: {
                if (tv_foot.isSelected()) {
                    return;
                }
                calculateRouteByWalk(new LatLonPoint(aMapLocation.getLatitude(), aMapLocation.getLongitude()), new LatLonPoint(currentMarker.getPosition().latitude, currentMarker.getPosition().longitude));
                break;
            }
            case R.id.iv_reroute:
                if (tv_driver.isSelected()) {
                    calculateRouteByDriver(new LatLonPoint(aMapLocation.getLatitude(), aMapLocation.getLongitude()), new LatLonPoint(currentMarker.getPosition().latitude, currentMarker.getPosition().longitude));
                } else {
                    calculateRouteByWalk(new LatLonPoint(aMapLocation.getLatitude(), aMapLocation.getLongitude()), new LatLonPoint(currentMarker.getPosition().latitude, currentMarker.getPosition().longitude));
                }
                break;
            case R.id.iv_navi:
                if (currentMarker != null) {
                    try {
                        CustomDialog dialog = new CustomDialog(this);
                        dialog.setMessage("是否启动本手机的自带导航");
                        dialog.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Uri mUri = Uri.parse("geo:" + currentMarker.getPosition().latitude + "," + currentMarker.getPosition().longitude + "?q=" + currentMarker.getTitle());
                                Intent intent = new Intent(Intent.ACTION_VIEW, mUri);
                                startActivity(intent);
                            }
                        });
                        dialog.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        dialog.show();
                    } catch (Exception e) {
                        e.printStackTrace();
                        CustomToast.show("您的手机中未安装任何地图导航工具", Toast.LENGTH_SHORT);
                    }
                }
                break;
            default:
                break;
        }
    }

    private void calculateRouteByDriver(LatLonPoint start, LatLonPoint end) {
        LogUtil.i(TAG, "calculateRouteByDriver()");
        final RouteSearch.FromAndTo fromAndTo = new RouteSearch.FromAndTo(start, end);
        RouteSearch.DriveRouteQuery query = new RouteSearch.DriveRouteQuery(fromAndTo, RouteSearch.DrivingDefault, null, null, "");
        routeSearch.calculateDriveRouteAsyn(query);
        if (!isProgressShowing()) {
            showProgressDialog(this);
        }
    }

    private void calculateRouteByWalk(LatLonPoint start, LatLonPoint end) {
        LogUtil.i(TAG, "calculateRouteByWalk()");
        final RouteSearch.FromAndTo fromAndTo = new RouteSearch.FromAndTo(start, end);
        RouteSearch.WalkRouteQuery query = new RouteSearch.WalkRouteQuery(fromAndTo, RouteSearch.WalkDefault);
        routeSearch.calculateWalkRouteAsyn(query);
        if (!isProgressShowing()) {
            showProgressDialog(this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapview.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapview.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapview.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != markerOptionses) {
            markerOptionses.clear();
        }
        if (null != markers) {
            markers.clear();
        }
        if (null != amap) {
            amap.clear();
        }
        mapview.onDestroy();
        AMapLocationControl.getInstance().stopLocation();
    }

    private void refreshMapOverLayout(boolean hasRoute) {
        if (hasRoute) {
            iv_loc.setVisibility(View.GONE);
            iv_reroute.setVisibility(View.VISIBLE);
            iv_navi.setVisibility(View.VISIBLE);

            tv_driver.setVisibility(View.VISIBLE);
            tv_foot.setVisibility(View.VISIBLE);
        } else {
            iv_loc.setVisibility(View.VISIBLE);
            iv_reroute.setVisibility(View.GONE);
            iv_navi.setVisibility(View.GONE);

            tv_driver.setVisibility(View.GONE);
            tv_foot.setVisibility(View.GONE);
        }
    }

    private void clearRoute() {
        if (walkRouteOverlay != null) {
            walkRouteOverlay.removeFromMap(); //清理之前规划的路线

        }
        if (drivingRouteOverlay != null) {
            drivingRouteOverlay.removeFromMap(); //清理之前规划的路线
        }
        walkRouteResult = null;
        driveRouteResult = null;
        amap.clear();
        refreshMapOverLayout(false);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        if (driveRouteResult == null && walkRouteResult == null) {
            currentMarker = marker;
            amap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentMarker.getPosition(), amap.getCameraPosition().zoom));
            return false;
        } else {
            return true;
        }
    }


    @Override
    public void onBusRouteSearched(BusRouteResult busRouteResult, int i) {
    }

    @Override
    public void onDriveRouteSearched(DriveRouteResult result, int rCode) {
        removeProgressDialog();
        LogUtil.i(TAG, "onDriveRouteSearched()");
        if (rCode == 1000) {
            if (result != null && result.getPaths() != null && result.getPaths().size() > 0) {
                clearRoute();
                driveRouteResult = result;
                DrivePath drivePath = driveRouteResult.getPaths().get(0);
                amap.clear();// 清理地图上的所有覆盖物
                drivingRouteOverlay = new DrivingRouteOverlay(
                        this, amap, drivePath, driveRouteResult.getStartPos(),
                        driveRouteResult.getTargetPos(), null);
                drivingRouteOverlay.removeFromMap();
                drivingRouteOverlay.addToMap();
                drivingRouteOverlay.zoomToSpan();

                refreshMapOverLayout(true);
                tv_driver.setSelected(true);
                tv_foot.setSelected(false);
            } else {
                CustomToast.show("算路失败", CustomToast.LENGTH_SHORT);
            }
        } else {
            LogUtil.i(TAG, "算路失败(" + rCode + ")");
            CustomToast.show("算路失败", CustomToast.LENGTH_SHORT);
        }
    }

    @Override
    public void onWalkRouteSearched(WalkRouteResult result, int rCode) {
        removeProgressDialog();
        LogUtil.i(TAG, "onWalkRouteSearched()");
        if (rCode == 1000) {
            if (result != null && result.getPaths() != null && result.getPaths().size() > 0) {
                walkRouteResult = result;
                WalkPath walkPath = walkRouteResult.getPaths().get(0);
                amap.clear();// 清理地图上的所有覆盖物
                WalkRouteOverlay walkRouteOverlay = new WalkRouteOverlay(this,
                        amap, walkPath, walkRouteResult.getStartPos(),
                        walkRouteResult.getTargetPos());
                walkRouteOverlay.removeFromMap();
                walkRouteOverlay.addToMap();
                walkRouteOverlay.zoomToSpan();
                refreshMapOverLayout(true);
                tv_driver.setSelected(false);
                tv_foot.setSelected(true);
            } else {
                CustomToast.show("算路失败", CustomToast.LENGTH_SHORT);
            }
        } else {
            LogUtil.i(TAG, "算路失败(" + rCode + ")");
            CustomToast.show("算路失败", CustomToast.LENGTH_SHORT);
        }
    }

    @Override
    public void onRideRouteSearched(RideRouteResult rideRouteResult, int i) {
    }

    @Override
    public void activate(OnLocationChangedListener listener) {
        mListener = listener;
        AMapLocationControl.getInstance().startLocationAlways(this, this);
    }

    @Override
    public void deactivate() {
        mListener = null;
        AMapLocationControl.getInstance().stopLocation();
    }

    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        LogUtil.i(TAG, "onLocationChanged()");
        if (null != aMapLocation) {
            if (aMapLocation.getErrorCode() == 0) {
                if (mListener != null) {
                    mListener.onLocationChanged(aMapLocation);// 显示系统小蓝点
                }
                this.aMapLocation = aMapLocation;
                if (isToMyLoc) {
                    isToMyLoc = false;
                    amap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(aMapLocation.getLatitude(), aMapLocation.getLongitude()), amap.getCameraPosition().zoom));
                }
            } else {
                LogUtil.e(TAG, "定位失败," + aMapLocation.getErrorCode() + ": " + aMapLocation.getErrorInfo());
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (KeyEvent.KEYCODE_BACK == keyCode) {
            if (driveRouteResult != null || walkRouteResult != null) {
                clearRoute();
                showSearchResultToMap();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }
}
