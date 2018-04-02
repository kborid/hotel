package com.prj.sdk.net.data;

import android.content.Intent;
import android.os.Handler;
import android.os.Looper;

import com.alibaba.fastjson.JSON;
import com.prj.sdk.app.AppContext;
import com.prj.sdk.constants.BroadCastConst;
import com.prj.sdk.net.cache.DiskLruCache;
import com.prj.sdk.net.cache.DiskLruCache.DiskCacheType;
import com.prj.sdk.net.cache.LruCache;
import com.prj.sdk.net.cache.LruCache.CacheType;
import com.prj.sdk.net.http.OKHttpHelper;
import com.prj.sdk.util.GUIDGenerator;
import com.prj.sdk.util.LogUtil;
import com.prj.sdk.util.NetworkUtil;
import com.prj.sdk.util.StringUtil;
import com.prj.sdk.util.Utils;

import java.io.File;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 数据异步加载
 *
 * @author Liao
 */
public class DataLoader {

    private final String TAG = getClass().getSimpleName();

    private static DataLoader mInstance = null;
    private final int mMaxTaskCount = 3;
    private volatile int mActiveTaskCount;
    private final List<Runnable> mRequests;
    private final Map<String, Runnable> mRunningRequests;
    private final Handler mDataHandler;
    private List<String> ids;

    public List<String> mCacheUrls = new ArrayList<String>();
    private final int DEFAULT_CACHE_SIZE = (int) Math.min(Runtime.getRuntime().maxMemory() / 4, 16 * 1024 * 1024);
    private final LruCache<String, byte[]> mMemCache;
    private final DiskLruCache<String, byte[]> mDiskCache;

    private DataLoader() {
        ids = Collections.synchronizedList(new LinkedList<String>());
        mRequests = Collections.synchronizedList(new LinkedList<Runnable>());
        mRunningRequests = Collections.synchronizedMap(new HashMap<String, Runnable>());
        mDataHandler = new Handler(Looper.getMainLooper());

        mMemCache = new LruCache<String, byte[]>(DEFAULT_CACHE_SIZE, CacheType.SIZE);
        mDiskCache = new DiskLruCache<String, byte[]>(new File(Utils.getFolderDir("dataCache")), 50 * 1024 * 1024, DiskCacheType.SIZE);

    }

    public static DataLoader getInstance() {
        if (mInstance == null) {
            synchronized (DataLoader.class) {
                if (mInstance == null) {
                    mInstance = new DataLoader();
                }
            }
        }
        return mInstance;
    }

    public String loadData(DataCallback callback, ResponseData request) {
        return loadData(callback, request, null);
    }

    public String loadData(DataCallback callback, ResponseData request, String id) {
        if (id == null || id.trim().equals("")) {
            id = GUIDGenerator.generate();
        }
        if (ids.contains(id)) {
            return id;
        }

        RequestTask mTask = new RequestTask(callback, request, id);
        ids.add(id);
        insertRequestQueue(mTask);
        return id;
    }

    public byte[] getCacheData(String mCacheUrl) {
        return mMemCache.get(mCacheUrl);
    }

    public void removeCacheData(String mCacheUrl) {
        if (mCacheUrl == null)
            return;
        mMemCache.remove(mCacheUrl);
        mDiskCache.remove(mCacheUrl);
    }

    private synchronized void flushRequests() {
        LogUtil.i(TAG, "flushRequests()");
        try {
            while (mActiveTaskCount < mMaxTaskCount && !mRequests.isEmpty()) {
                mActiveTaskCount++;
                Runnable request = mRequests.get(0);
                mRunningRequests.put(((RequestTask) request).id, request);
                mRequests.remove(0);
                Thread mThread = new Thread(request);
                mThread.setDaemon(true);
                mThread.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void insertRequestQueue(Runnable request) {
        LogUtil.i(TAG, "insertRequestQueue() id = " + ((RequestTask) request).id);
        mRequests.add(0, request);
        flushRequests();
    }

    /**
     * 和服务器交互线程
     */
    private class RequestTask implements Runnable {
        public String id;
        public OKHttpHelper mOKHttpHelper;
        public int count;            // 计数器
        private ResponseData request;
        private ResponseData response;
        private DataCallback callback;
        private Exception mException;

        public RequestTask(DataCallback callback, ResponseData request, String id) {
            this.callback = callback;
            this.request = request;
            this.id = id;
            mOKHttpHelper = new OKHttpHelper();
        }

        @Override
        public void run() {
            try {
                if (ids.contains(id)) {
                    preExecute();
                }
                if (ids.contains(id)) {
                    doExecute();
                }
            } catch (Exception e) {
                e.printStackTrace();
                mException = e;
            } finally {
                if (ids.contains(id)) {
                    postExecute();
                    clear(id);
                }
            }
        }

        private void preExecute() {
            mDataHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (callback != null) {
                        callback.preExecute(request);
                    }
                }
            });
        }

        private void doExecute() throws Exception {
            String url = request.path;
            if (NetworkUtil.isNetworkAvailable()) {
                LogUtil.i(TAG, "Request:" + url);
                LogUtil.i(TAG, "Request:" + request.data.toString());

                byte[] data = getDataFromNet();

                if (data != null && data.length > 0) {
                    String json = new String(data, "UTF-8");
                    LogUtil.i(TAG, "Response:" + url);
                    LogUtil.i(TAG, "Response:" + json);
                    response = JSON.parseObject(json, ResponseData.class);

                    String key = request.key;
                    if (StringUtil.isEmpty(key)) {
                        key = url;
                    }
                    if (mCacheUrls.contains(key)) {
                        mMemCache.put(key, data);
                        if (Utils.isSDCardEnable()) {
                            mDiskCache.put(key, data);
                        }
                    }
                }
            } else {
                throw new ConnectException("网络连接失败，请检查网络");
            }

        }

        private void postExecute() {
            mDataHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (callback != null) {
                        try {
                            if (response != null && response.head != null) {
                                ResponseData.Head head = JSON.parseObject(response.head.toString(), ResponseData.Head.class);
                                response.code = head.rtnCode;
                                response.data = head.rtnMsg;

                                if ("000000".equals(response.code)) {                                       //000000 请求成功
                                    callback.notifyMessage(request, response);
                                } else {                                                                    //900902
                                    if ("900902".equals(response.code) || "310001".equals(response.code)) { //310001 票据失效
                                        Intent intent = new Intent(BroadCastConst.UNLOGIN_ACTION);
                                        intent.putExtra("is_show_tip_dialog", true);
                                        AppContext.mAppContext.sendBroadcast(intent); //发送登录广播
                                        response.data = "登录超时,请重新登录";
                                        callback.notifyMessage(request, response);
                                    } else {                                                                //999999 业务处理异常
                                        callback.notifyError(request, response, mException);
                                    }
                                }
                                LogUtil.i(TAG, "notify response data:" + response.data + ", code:" + response.code);
                            } else {
                                LogUtil.e(TAG, "notify response = null || response.head = null");
                                callback.notifyError(request, response, mException);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            response.data = "data parse error";
                            callback.notifyError(request, response, e);
                            LogUtil.e(TAG, "notify response data:data parse error");
                        }
                    }
                }
            });
        }

        private byte[] getDataFromNet() {
            byte[] data = null;
            if (NetworkUtil.isNetworkAvailable()) {
                data = mOKHttpHelper.executeHttpRequest(request.path, request.type, request.header, request.data, request.isForm);
                if (data == null && request.retry > count) {
                    count++;
                    return getDataFromNet();
                }
            }
            return data;
        }
    }

    /**
     * 清除请求队列中的任务
     */
    public void clearRequests() {
        LogUtil.i(TAG, "clearRequests()");
        try {
            LogUtil.i(TAG, "ids.size = " + ids.size() + ", requests.size = " + mRequests.size());
            mRequests.clear();
            ids.clear();
            mActiveTaskCount = 0;
            mRunningRequests.clear();
            flushRequests();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 取消任务
     */
    public synchronized void clear(String id) {
        if (ids.contains(id)) {
            ids.remove(id);
            mActiveTaskCount--;

            RequestTask request = (RequestTask) mRunningRequests.get(id);
            // 中断http数据请求
            if (request != null) {
                mRunningRequests.remove(id);
            }
            flushRequests();
        }
    }
}