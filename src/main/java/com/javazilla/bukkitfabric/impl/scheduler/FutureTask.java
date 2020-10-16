package com.javazilla.bukkitfabric.impl.scheduler;

import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.bukkit.plugin.Plugin;

public class FutureTask<T> extends BukkitTaskImpl implements Future<T> {

    private final Callable<T> callable;
    private T value;
    private Exception exception = null;

    public FutureTask(final Callable<T> callable, final Plugin plugin, final int id) {
        super(plugin, null, id, BukkitTaskImpl.NO_REPEATING);
        this.callable = callable;
    }

    @Override
    public synchronized boolean cancel(final boolean mayInterruptIfRunning) {
        if (getPeriod() != BukkitTaskImpl.NO_REPEATING) return false;
        setPeriod(BukkitTaskImpl.CANCEL);
        return true;
    }

    @Override
    public boolean isDone() {
        final long period = this.getPeriod();
        return period != BukkitTaskImpl.NO_REPEATING && period != BukkitTaskImpl.PROCESS_FOR_FUTURE;
    }

    @Override
    public T get() throws CancellationException, InterruptedException, ExecutionException {
        try {
            return get(0, TimeUnit.MILLISECONDS);
        } catch (final TimeoutException e) {
            throw new Error(e);
        }
    }

    @Override
    public synchronized T get(long timeout, final TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        timeout = unit.toMillis(timeout);
        long period = this.getPeriod();
        long timestamp = timeout > 0 ? System.currentTimeMillis() : 0L;
        while (true) {
            if (period == BukkitTaskImpl.NO_REPEATING || period == BukkitTaskImpl.PROCESS_FOR_FUTURE) {
                this.wait(timeout);
                period = this.getPeriod();
                if (period == BukkitTaskImpl.NO_REPEATING || period == BukkitTaskImpl.PROCESS_FOR_FUTURE) {
                    if (timeout == 0L)
                        continue;
                    timeout += timestamp - (timestamp = System.currentTimeMillis());
                    if (timeout > 0)
                        continue;
                    throw new TimeoutException();
                }
            }
            if (period == BukkitTaskImpl.CANCEL) throw new CancellationException();
            if (period == BukkitTaskImpl.DONE_FOR_FUTURE) {
                if (exception == null) return value;
                throw new ExecutionException(exception);
            }
            throw new IllegalStateException("Expected " + BukkitTaskImpl.NO_REPEATING + " to " + BukkitTaskImpl.DONE_FOR_FUTURE + ", got " + period);
        }
    }

    @Override
    public void run() {
        synchronized (this) {
            if (getPeriod() == BukkitTaskImpl.CANCEL) return;
            setPeriod(BukkitTaskImpl.PROCESS_FOR_FUTURE);
        }
        try {
            value = callable.call();
        } catch (final Exception e) {
            exception = e;
        } finally {
            synchronized (this) {
                setPeriod(BukkitTaskImpl.DONE_FOR_FUTURE);
                this.notifyAll();
            }
        }
    }

    @Override
    synchronized boolean cancel0() {
        if (getPeriod() != BukkitTaskImpl.NO_REPEATING) return false;
        setPeriod(BukkitTaskImpl.CANCEL);
        notifyAll();
        return true;
    }

}