package com.example.hojiang.wallpaperchange.utils;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;

/**
 * Created by yingcai on 9/2/2015.
 */
public class HttpDownloader {
    private static final String TAG = HttpDownloader.class.getSimpleName();

    public enum DownloadResultCode {
        Succeeded,
        Cancelled,
        FailedUnknown,
        FailedHttpResponseFailure,
        FailedConnectionIOException,
        FailedFileIOException
    }

    public interface DownloadingProgressListener {
        void onProgressUpdate(int curr, int total);
    }

    public static class DownloadResult {
        public DownloadResultCode code;
        public int httpResponseCode;
        public byte[] content;

        public DownloadResult() {
            this.code = DownloadResultCode.FailedUnknown;
            this.httpResponseCode = -1;
            this.content = null;
        }

        public boolean isSucceeded() {
            return this.code.equals(DownloadResultCode.Succeeded);
        }
    }

    private URL urlToDownload;
    private boolean shouldCancelDownloading = false;

    public HttpDownloader(URL urlToDownload) {
        if (null == urlToDownload) {
            return;
        }

        this.urlToDownload = urlToDownload;
        this.shouldCancelDownloading = false;
    }

    public DownloadResult download() {
        return this.download(null);
    }

    public DownloadResultCode downloadToFile(String targetFilePath) {
        return this.downloadToFile(targetFilePath, null);
    }

    public DownloadResultCode downloadToFile(String targetFilePath, final DownloadingProgressListener progressListener) {
        DownloadResultCode resultCode = DownloadResultCode.FailedUnknown;

        DownloadResult result = this.download(new DownloadingProgressListener() {
            @Override
            public void onProgressUpdate(int curr, int total) {
                if (null != progressListener) {
                    progressListener.onProgressUpdate(curr, total + 1);
                }
            }
        });

        if (!result.isSucceeded()) {
            return result.code;
        }

        if (this.shouldCancelDownloading) {
            resultCode = DownloadResultCode.Cancelled;
            return resultCode;
        }

        File file = new File(targetFilePath);
        if (file.exists()) {
            file.delete();
        }

        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(targetFilePath, false);
            fileOutputStream.write(result.content);

            if (null != progressListener) {
                progressListener.onProgressUpdate(100, 100);
            }

            resultCode = DownloadResultCode.Succeeded;
        } catch (IOException ioe) {
            resultCode = DownloadResultCode.FailedFileIOException;
        } finally {
            try {
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
            } catch (IOException ignored) {

            }
        }

        return resultCode;
    }

    public void cancel() {
        this.shouldCancelDownloading = true;
    }

    private DownloadResult download(DownloadingProgressListener progressListener) {
        this.shouldCancelDownloading = false;

        DownloadResult result = new DownloadResult();

        HttpURLConnection connection = null;
        InputStream inputStream = null;
        ByteArrayOutputStream outputStream = null;

        try {
            connection = (HttpURLConnection) this.urlToDownload.openConnection();
            connection.connect();

            result.httpResponseCode = connection.getResponseCode();

            if (result.httpResponseCode == HttpURLConnection.HTTP_OK) {
                int contentLength = connection.getContentLength();

                inputStream = connection.getInputStream();
                outputStream = new ByteArrayOutputStream();

                int bytesRead;
                byte[] buffer = new byte[1024];
                int currDownloaded = 0;

                while ((bytesRead = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, bytesRead);
                    currDownloaded += bytesRead;

                    if (null != progressListener) {
                        progressListener.onProgressUpdate(currDownloaded, contentLength);
                    }

                    if (this.shouldCancelDownloading) {
                        result.code = DownloadResultCode.Cancelled;
                        break;
                    }
                }

                if (!result.code.equals(DownloadResultCode.Cancelled)) {
                    result.code = DownloadResultCode.Succeeded;
                    result.content = outputStream.toByteArray();
                }

            } else {
                result.code = DownloadResultCode.FailedHttpResponseFailure;
            }
        } catch (UnknownHostException uhe) {
            result.code = DownloadResultCode.FailedConnectionIOException;

            // Don't track this online, as this is known issue. Current best strategy is to let user know and retry.
        } catch (IOException ioe) {
            result.code = DownloadResultCode.FailedConnectionIOException;

            // Don't track this online, as this is known issue. Current best strategy is to let user know and retry.
        } catch (SecurityException se) {
            result.code = DownloadResultCode.FailedConnectionIOException;
        } catch (NullPointerException e) {
            result.code = DownloadResultCode.FailedConnectionIOException;

            // Don't track this online, as this is known issue. Current best strategy is to let user know and retry.
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }

                if (outputStream != null) {
                    outputStream.close();
                }

                if (connection != null) {
                    connection.disconnect();
                }
            } catch (Exception e) {

            }
        }

        return result;
    }
}
