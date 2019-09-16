package io.bidmachine;

import android.support.annotation.VisibleForTesting;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

class AdRequestExecutor extends ThreadPoolExecutor {

    private static volatile AdRequestExecutor instance;

    public static AdRequestExecutor get() {
        if (instance == null) {
            synchronized (AdRequestExecutor.class) {
                if (instance == null) {
                    instance = new AdRequestExecutor(
                            Runtime.getRuntime().availableProcessors() * 2);
                }
            }
        }
        return instance;
    }

    private List<Runnable> pendingCommands;
    private boolean isEnabled = false;

    @VisibleForTesting
    AdRequestExecutor(int nThreads) {
        super(nThreads, nThreads,
              0L, TimeUnit.MILLISECONDS,
              new LinkedBlockingQueue<Runnable>());
    }

    @Override
    public void execute(Runnable command) {
        if (isEnabled) {
            super.execute(command);
        } else {
            if (pendingCommands == null) {
                pendingCommands = new ArrayList<>();
            }
            pendingCommands.add(command);
        }
    }

    void enable() {
        isEnabled = true;
        if (pendingCommands != null) {
            for (Runnable command : pendingCommands) {
                execute(command);
            }
            pendingCommands.clear();
        }
    }
}