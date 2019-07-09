package com.app.amber.chat.JobSchduler;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobCreator;

public class jobs_creator implements JobCreator {

    @Override
    @Nullable
    public Job create(@NonNull String tag) {
        switch (tag) {
            case "service_watcher":
                return new service_watcher();

            default:
                return null;
        }
    }
}
