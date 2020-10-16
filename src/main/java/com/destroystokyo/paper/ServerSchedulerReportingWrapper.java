package com.destroystokyo.paper;

import com.google.common.base.Preconditions;
import com.javazilla.bukkitfabric.impl.scheduler.BukkitTaskImpl;

/**
 * Reporting wrapper to catch exceptions not natively
 */
public class ServerSchedulerReportingWrapper implements Runnable {

    private final BukkitTaskImpl internalTask;

    public ServerSchedulerReportingWrapper(BukkitTaskImpl internalTask) {
        this.internalTask = Preconditions.checkNotNull(internalTask, "internalTask");
    }

    @Override
    public void run() {
        try {
            internalTask.run();
        } catch (RuntimeException e) {
            e.printStackTrace();
            throw e;
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public BukkitTaskImpl getInternalTask() {
        return internalTask;
    }
}
