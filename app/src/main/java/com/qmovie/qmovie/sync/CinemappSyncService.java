package com.qmovie.qmovie.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class CinemappSyncService extends Service {
    private static final Object              sSyncAdapterLock     = new Object();
    private static       CinemappSyncAdapter sCinemappSyncAdapter = null;

    @Override
    public void onCreate()
    {
        synchronized (sSyncAdapterLock)
        {
            if (sCinemappSyncAdapter == null)
            {
                sCinemappSyncAdapter = new CinemappSyncAdapter(getApplicationContext(), true);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return sCinemappSyncAdapter.getSyncAdapterBinder();
    }
}