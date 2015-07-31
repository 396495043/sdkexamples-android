package com.skytech.chatim.proxy;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.util.Log;

import com.easemob.chatuidemo.R;
import com.skytech.chatim.sky.util.DataUtil;

public class DownloadUtil {
    private static String TAG = DownloadUtil.class.getSimpleName();
    private static long downloadId;

    public static void downloadWebex(final Activity activity) {

        AlertDialog.Builder builder = new Builder(activity);
        builder.setMessage(R.string.needInstallWebex);
        builder.setPositiveButton(android.R.string.ok, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                showDowloadMessage(activity);
                if (downloadId != 0) {
                    return;
                }
                downloadApk(activity);
            }

        });
        builder.setNegativeButton(android.R.string.cancel, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    private static void showDowloadMessage(final Activity activity) {
        AlertDialog.Builder builder = new Builder(activity);
        builder.setMessage(R.string.downloadWebexMes);
        builder.setPositiveButton(android.R.string.ok, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                downloadApk(activity);
            }

        });
        builder.create().show();

    }

    private static void downloadApk(final Activity activity) {
        DownloadManager downloadManager = (DownloadManager) activity
                .getSystemService(activity.DOWNLOAD_SERVICE);
        String apkUrl = "http://sky-techcloud.com/apk/webex.apk";
        DownloadManager.Request request = new DownloadManager.Request(
                Uri.parse(apkUrl));
        DataUtil.setDir();
        request.setDestinationInExternalPublicDir(DataUtil.DataDir, "webex.apk");
        request.setTitle( activity.getString(R.string.downloadWebex));
        // request.setDescription("MeiLiShuo desc");
        // request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        // request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI);
        // request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN);
        // request.setMimeType("application/cn.trinea.download.file");
        downloadId = downloadManager.enqueue(request);
        Log.e(TAG, " downloadId " + downloadId);
        CompleteReceiver completeReceiver = new CompleteReceiver();
        /** register download success broadcast **/
        activity.registerReceiver(completeReceiver, new IntentFilter(
                DownloadManager.ACTION_DOWNLOAD_COMPLETE));

    }

    static class CompleteReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (downloadId == 0) {
                Log.e(TAG, " onReceive " + downloadId);
                return;
            }
            // get complete download id
            long completeDownloadId = intent.getLongExtra(
                    DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            if (completeDownloadId == downloadId) {
                String serviceString = Context.DOWNLOAD_SERVICE;
                DownloadManager dManager = (DownloadManager) context
                        .getSystemService(serviceString);
                Intent install = new Intent(Intent.ACTION_VIEW);
                Uri downloadFileUri = dManager
                        .getUriForDownloadedFile(downloadId);
                install.setDataAndType(downloadFileUri,
                        "application/vnd.android.package-archive");
                install.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(install);
            }
        }
    };
}
