package com.app.amber.chat.JobSchduler;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;

import com.app.amber.chat.service.socket_events_listener;
import com.evernote.android.job.Job;
import com.evernote.android.job.JobManager;
import com.evernote.android.job.JobRequest;

import java.util.List;
import java.util.concurrent.TimeUnit;


public class service_watcher extends Job {

    public static final String TAG = "service_watcher";

    public static int count=0;
    @Override
    @NonNull
    protected Result onRunJob(Params params) {
        // run your job here


        System.out.println("runing11122 service_watcher");
        JobManager mJobManager;
        mJobManager = JobManager.instance();


        /*if(isMyServiceRunning(socket_events_listener.class,getContext().getApplicationContext()) && count>=1){
            getContext().stopService(new Intent(getContext().getApplicationContext(),socket_events_listener.class));
        }*/
        // startService(new Intent(getApplicationContext(),socket_events_listener.class));

        if(!isMyServiceRunning(socket_events_listener.class,getContext())){
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    getContext().startForegroundService(new Intent(getContext(),socket_events_listener.class));
                }else{
                    getContext().startService(new Intent(getContext(),socket_events_listener.class));
                }
            }else{
            System.out.println("service is running");
        }


        mJobManager.cancelAllForTag(service_watcher.TAG);

        /*if(count<=1){
            int restartService = new JobRequest.Builder(service_watcher.TAG)
                    .setExecutionWindow(30_000L, 40_000L)
                    .build()
                    .schedule();

        }else{
            int restartService = new JobRequest.Builder(service_watcher.TAG)
                    .setPeriodic(TimeUnit.MINUTES.toMillis(15), TimeUnit.MINUTES.toMillis(7))
                    .build()
                    .schedule();
        }*/
        int restartService = new JobRequest.Builder(service_watcher.TAG)
                .setPeriodic(TimeUnit.MINUTES.toMillis(15), TimeUnit.MINUTES.toMillis(7))
                .build()
                .schedule();


        return Result.SUCCESS;
    }



    @Override
    protected void onReschedule(int newJobId) {
        System.out.println("reschedule job");

    }


    private boolean isMyServiceRunning(Class<?> serviceClass,Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public static boolean isAppRunning(final Context context, final String packageName) {
        final ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        final List<ActivityManager.RunningAppProcessInfo> procInfos = activityManager.getRunningAppProcesses();
        if (procInfos != null)
        {
            for (final ActivityManager.RunningAppProcessInfo processInfo : procInfos) {
                if (processInfo.processName.equals(packageName)) {
                    return true;
                }
            }
        }
        return false;
    }
}
