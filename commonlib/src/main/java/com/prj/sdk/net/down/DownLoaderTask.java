package com.prj.sdk.net.down;

import android.os.AsyncTask;

import com.prj.sdk.util.LogUtil;
import com.prj.sdk.util.NetworkUtil;
import com.prj.sdk.util.Utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * AsyncTask异步下载任务
 */
public class DownLoaderTask extends AsyncTask<Void, Integer, Long> {

    private final String TAG = getClass().getSimpleName();

    public static final int DOWNLOAD_CANCEL = 0;
    public static final int DOWNLOAD_SUCCESS = 1;
    public static final int DOWNLOAD_FAILED = 2;

    private URL mUrl;
    private File mFile;
    private int mProgress = 0;
    private int mContentLength = 0;
    private boolean mReplaceAll;
    private DownCallback mDownCallback;
    private int mDownStatus;                    // 下载状态： 0取消,1成功，2失败
    private String fileName;
    private final int TIMEOUT = 10000;            // 设置连接下载超时

    public DownLoaderTask(String url, String fileName, boolean replaceAll, DownCallback mDownCallback) {
        super();
        this.mReplaceAll = replaceAll;
        this.mDownCallback = mDownCallback;
        this.fileName = fileName;
        try {
            mUrl = new URL(url);
            if (fileName == null) {
                fileName = new File(mUrl.getFile()).getName();
            }
            mFile = new File(Utils.getFolderDir("cache"), fileName);
            LogUtil.d(TAG, "out=" + Utils.getFolderDir("cache") + ", name=" + fileName + ",mUrl.getFile()=" + mUrl.getFile());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (null != mDownCallback) {
            mDownCallback.beginDownload(mUrl.getFile(), mFile.toString(), fileName, mDownStatus);
        }
    }

    @Override
    protected Long doInBackground(Void... params) {
        return download();
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        if (null != mDownCallback) {
            mDownCallback.downloading(mDownStatus, values[0], mContentLength);
        }
    }

    @Override
    protected void onPostExecute(Long result) {
        super.onPostExecute(result);
        if (isCancelled()) {
            return;
        }
        if (mDownCallback != null) {
            mDownCallback.finishDownload(mDownStatus);
        }
    }

    private long download() {
        URLConnection connection = null;
        int bytesCopied = 0;
        try {
            if (NetworkUtil.isNetworkAvailable()) {
                connection = mUrl.openConnection();
                connection.setConnectTimeout(TIMEOUT);
                connection.setReadTimeout(TIMEOUT);
                mContentLength = connection.getContentLength();
                if (!mReplaceAll && mFile.exists() && mContentLength == mFile.length()) {
                    LogUtil.d(TAG, "file '" + mFile.getName() + "' already exits!!");
                    return 0L;
                }
                ProgressReportingOutputStream mOutputStream = new ProgressReportingOutputStream(mFile);
                publishProgress(0);// 进度条每次更新,执行中创建新线程处理onProgressUpdate()
                bytesCopied = copy(connection.getInputStream(), mOutputStream);
                if (bytesCopied != mContentLength && mContentLength != -1) {
                    LogUtil.e(TAG, "Download incomplete bytesCopied=" + bytesCopied + ", mContentLength" + mContentLength);
                }
                mOutputStream.close();
                if (bytesCopied == mContentLength && mContentLength != -1) {
                    mDownStatus = DOWNLOAD_SUCCESS;
                } else {
                    mDownStatus = DOWNLOAD_FAILED;
                }
            } else {
                mDownStatus = DOWNLOAD_FAILED;
                throw new ConnectException("网络连接失败，请检查网络");
            }
        } catch (IOException e) {
            mDownStatus = DOWNLOAD_FAILED;
            e.printStackTrace();
        }
        return bytesCopied;
    }

    private int copy(InputStream input, OutputStream output) {
        byte[] buffer = new byte[1024 * 8];
        BufferedInputStream in = new BufferedInputStream(input, 1024 * 8);
        BufferedOutputStream out = new BufferedOutputStream(output, 1024 * 8);
        int count = 0, n = 0;
        try {
            while ((n = in.read(buffer, 0, 1024 * 8)) != -1) {
                out.write(buffer, 0, n);
                count += n;
            }
            out.flush();
        } catch (IOException e) {
            mDownStatus = DOWNLOAD_FAILED;
            e.printStackTrace();
        } finally {
            try {
                out.close();
                in.close();
            } catch (IOException e) {
                mDownStatus = DOWNLOAD_FAILED;
                e.printStackTrace();
            }
        }
        return count;
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        mDownStatus = DOWNLOAD_CANCEL;
        if (mDownCallback != null) {
            mDownCallback.finishDownload(mDownStatus);
        }
    }

    private final class ProgressReportingOutputStream extends FileOutputStream {
        ProgressReportingOutputStream(File file) throws FileNotFoundException {
            super(file);
        }

        @Override
        public void write(byte[] buffer, int byteOffset, int byteCount) throws IOException {
            super.write(buffer, byteOffset, byteCount);
            mProgress += byteCount;
            publishProgress(mProgress);
        }

    }

}