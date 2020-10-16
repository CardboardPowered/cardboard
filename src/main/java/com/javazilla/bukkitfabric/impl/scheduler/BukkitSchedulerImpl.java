package com.javazilla.bukkitfabric.impl.scheduler;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.logging.Level;
import org.apache.commons.lang.Validate;
import org.bukkit.plugin.IllegalPluginAccessException;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scheduler.BukkitWorker;

public class BukkitSchedulerImpl implements BukkitScheduler {

    /**
     * Counter for IDs. Order doesn't matter, only uniqueness.
     */
    private final AtomicInteger ids = new AtomicInteger(1);
    /**
     * Current head of linked-list. This reference is always stale, {@link BukkitTaskImpl#next} is the live reference.
     */
    private volatile BukkitTaskImpl head = new BukkitTaskImpl();
    /**
     * Tail of a linked-list. AtomicReference only matters when adding to queue
     */
    private final AtomicReference<BukkitTaskImpl> tail = new AtomicReference<BukkitTaskImpl>(head);
    /**
     * Main thread logic only
     */
    final PriorityQueue<BukkitTaskImpl> pending = new PriorityQueue<BukkitTaskImpl>(10,
            new Comparator<BukkitTaskImpl>() {
                @Override
                public int compare(final BukkitTaskImpl o1, final BukkitTaskImpl o2) {
                    int value = Long.compare(o1.getNextRun(), o2.getNextRun());
                    return value != 0 ? value : Integer.compare(o1.getTaskId(), o2.getTaskId()); // If the tasks should run on the same tick they should be run FIFO
                }
            });
    /**
     * Main thread logic only
     */
    private final List<BukkitTaskImpl> temp = new ArrayList<BukkitTaskImpl>();
    /**
     * These are tasks that are currently active. It's provided for 'viewing' the current state.
     */
    final ConcurrentHashMap<Integer, BukkitTaskImpl> runners = new ConcurrentHashMap<Integer, BukkitTaskImpl>();
    /**
     * The sync task that is currently running on the main thread.
     */
    private volatile BukkitTaskImpl currentTask = null;
    volatile int currentTick = -1;

    private final BukkitSchedulerImpl asyncScheduler;
    private final boolean isAsyncScheduler;
    public BukkitSchedulerImpl() {
        this(false);
    }

    public BukkitSchedulerImpl(boolean isAsync) {
        this.isAsyncScheduler = isAsync;
        if (isAsync) {
            this.asyncScheduler = this;
        } else {
            this.asyncScheduler = new PaperAsyncScheduler();
        }
    }

    @Override
    public int scheduleSyncDelayedTask(final Plugin plugin, final Runnable task) {
        return this.scheduleSyncDelayedTask(plugin, task, 0L);
    }

    @Override
    public BukkitTask runTask(Plugin plugin, Runnable runnable) {
        return runTaskLater(plugin, runnable, 0L);
    }

    @Override
    public void runTask(Plugin plugin, Consumer<BukkitTask> task) throws IllegalArgumentException {
        runTaskLater(plugin, task, 0L);
    }

    @Deprecated
    @Override
    public int scheduleAsyncDelayedTask(final Plugin plugin, final Runnable task) {
        return this.scheduleAsyncDelayedTask(plugin, task, 0L);
    }

    @Override
    public BukkitTask runTaskAsynchronously(Plugin plugin, Runnable runnable) {
        return runTaskLaterAsynchronously(plugin, runnable, 0L);
    }

    @Override
    public void runTaskAsynchronously(Plugin plugin, Consumer<BukkitTask> task) throws IllegalArgumentException {
        runTaskLaterAsynchronously(plugin, task, 0L);
    }

    @Override
    public int scheduleSyncDelayedTask(final Plugin plugin, final Runnable task, final long delay) {
        return this.scheduleSyncRepeatingTask(plugin, task, delay, BukkitTaskImpl.NO_REPEATING);
    }

    @Override
    public BukkitTask runTaskLater(Plugin plugin, Runnable runnable, long delay) {
        return runTaskTimer(plugin, runnable, delay, BukkitTaskImpl.NO_REPEATING);
    }

    @Override
    public void runTaskLater(Plugin plugin, Consumer<BukkitTask> task, long delay) throws IllegalArgumentException {
        runTaskTimer(plugin, task, delay, BukkitTaskImpl.NO_REPEATING);
    }

    @Deprecated
    @Override
    public int scheduleAsyncDelayedTask(final Plugin plugin, final Runnable task, final long delay) {
        return this.scheduleAsyncRepeatingTask(plugin, task, delay, BukkitTaskImpl.NO_REPEATING);
    }

    @Override
    public BukkitTask runTaskLaterAsynchronously(Plugin plugin, Runnable runnable, long delay) {
        return runTaskTimerAsynchronously(plugin, runnable, delay, BukkitTaskImpl.NO_REPEATING);
    }

    @Override
    public void runTaskLaterAsynchronously(Plugin plugin, Consumer<BukkitTask> task, long delay) throws IllegalArgumentException {
        runTaskTimerAsynchronously(plugin, task, delay, BukkitTaskImpl.NO_REPEATING);
    }

    @Override
    public void runTaskTimerAsynchronously(Plugin plugin, Consumer<BukkitTask> task, long delay, long period) throws IllegalArgumentException {
        runTaskTimerAsynchronously(plugin, (Object) task, delay, period);
    }

    @Override
    public int scheduleSyncRepeatingTask(final Plugin plugin, final Runnable runnable, long delay, long period) {
        return runTaskTimer(plugin, runnable, delay, period).getTaskId();
    }

    @Override
    public BukkitTask runTaskTimer(Plugin plugin, Runnable runnable, long delay, long period) {
        return runTaskTimer(plugin, (Object) runnable, delay, period);
    }

    @Override
    public void runTaskTimer(Plugin plugin, Consumer<BukkitTask> task, long delay, long period) throws IllegalArgumentException {
        runTaskTimer(plugin, (Object) task, delay, period);
    }

    public BukkitTask runTaskTimer(Plugin plugin, Object runnable, long delay, long period) {
        validate(plugin, runnable);
        if (delay < 0L) delay = 0;

        if (period == BukkitTaskImpl.ERROR) period = 1L;
        else if (period < BukkitTaskImpl.NO_REPEATING) period = BukkitTaskImpl.NO_REPEATING;

        return handle(new BukkitTaskImpl(plugin, runnable, nextId(), period), delay);
    }

    @Deprecated
    @Override
    public int scheduleAsyncRepeatingTask(final Plugin plugin, final Runnable runnable, long delay, long period) {
        return runTaskTimerAsynchronously(plugin, runnable, delay, period).getTaskId();
    }

    @Override
    public BukkitTask runTaskTimerAsynchronously(Plugin plugin, Runnable runnable, long delay, long period) {
        return runTaskTimerAsynchronously(plugin, (Object) runnable, delay, period);
    }

    public BukkitTask runTaskTimerAsynchronously(Plugin plugin, Object runnable, long delay, long period) {
        validate(plugin, runnable);
        if (delay < 0L) delay = 0;

        if (period == BukkitTaskImpl.ERROR) period = 1L;
        else if (period < BukkitTaskImpl.NO_REPEATING) period = BukkitTaskImpl.NO_REPEATING;

        return handle(new AsyncTaskImpl(runners, plugin, runnable, nextId(), period), delay);
    }

    @Override
    public <T> Future<T> callSyncMethod(final Plugin plugin, final Callable<T> task) {
        validate(plugin, task);
        final FutureTask<T> future = new FutureTask<T>(task, plugin, nextId());
        handle(future, 0L);
        return future;
    }

    @Override
    public void cancelTask(final int taskId) {
        if (taskId <= 0) return;

        if (!this.isAsyncScheduler)
            this.asyncScheduler.cancelTask(taskId);

        BukkitTaskImpl task = runners.get(taskId);
        if (task != null) task.cancel0();

        task = new BukkitTaskImpl(
                new Runnable() {
                    @Override
                    public void run() {
                        if (!check(BukkitSchedulerImpl.this.temp)) check(BukkitSchedulerImpl.this.pending);
                    }
                    private boolean check(final Iterable<BukkitTaskImpl> collection) {
                        final Iterator<BukkitTaskImpl> tasks = collection.iterator();
                        while (tasks.hasNext()) {
                            final BukkitTaskImpl task = tasks.next();
                            if (task.getTaskId() == taskId) {
                                task.cancel0();
                                tasks.remove();
                                if (task.isSync()) runners.remove(taskId);
                                return true;
                            }
                        }
                        return false;
                    }
                });
        handle(task, 0L);
        for (BukkitTaskImpl taskPending = head.getNext(); taskPending != null; taskPending = taskPending.getNext()) {
            if (taskPending == task) return;
            if (taskPending.getTaskId() == taskId) taskPending.cancel0();
        }
    }

    @Override
    public void cancelTasks(final Plugin plugin) {
        Validate.notNull(plugin, "Cannot cancel tasks of null plugin");

        if (!this.isAsyncScheduler)
            this.asyncScheduler.cancelTasks(plugin);

        final BukkitTaskImpl task = new BukkitTaskImpl(
                new Runnable() {
                    @Override
                    public void run() {
                        check(BukkitSchedulerImpl.this.pending);
                        check(BukkitSchedulerImpl.this.temp);
                    }
                    void check(final Iterable<BukkitTaskImpl> collection) {
                        final Iterator<BukkitTaskImpl> tasks = collection.iterator();
                        while (tasks.hasNext()) {
                            final BukkitTaskImpl task = tasks.next();
                            if (task.getOwner().equals(plugin)) {
                                task.cancel0();
                                tasks.remove();
                                if (task.isSync()) runners.remove(task.getTaskId());
                            }
                        }
                    }
                });
        handle(task, 0L);
        for (BukkitTaskImpl taskPending = head.getNext(); taskPending != null; taskPending = taskPending.getNext()) {
            if (taskPending == task) break;
            if (taskPending.getTaskId() != -1 && taskPending.getOwner().equals(plugin)) taskPending.cancel0();
        }
        for (BukkitTaskImpl runner : runners.values())
            if (runner.getOwner().equals(plugin)) runner.cancel0();
    }

    @Override
    public boolean isCurrentlyRunning(final int taskId) {
        if (!isAsyncScheduler)
            if (this.asyncScheduler.isCurrentlyRunning(taskId))
                return true;

        final BukkitTaskImpl task = runners.get(taskId);
        if (task == null) return false;
        if (task.isSync()) return (task == currentTask);

        final AsyncTaskImpl asyncTask = (AsyncTaskImpl) task;
        synchronized (asyncTask.getWorkers()) {
            return !asyncTask.getWorkers().isEmpty();
        }
    }

    @Override
    public boolean isQueued(final int taskId) {
        if (taskId <= 0) return false;

        if (!this.isAsyncScheduler && this.asyncScheduler.isQueued(taskId))
            return true;

        for (BukkitTaskImpl task = head.getNext(); task != null; task = task.getNext())
            if (task.getTaskId() == taskId)
                return task.getPeriod() >= BukkitTaskImpl.NO_REPEATING; // The task will run

        BukkitTaskImpl task = runners.get(taskId);
        return task != null && task.getPeriod() >= BukkitTaskImpl.NO_REPEATING;
    }

    @Override
    public List<BukkitWorker> getActiveWorkers() {
        if (!isAsyncScheduler)
            return this.asyncScheduler.getActiveWorkers();

        final ArrayList<BukkitWorker> workers = new ArrayList<BukkitWorker>();
        for (final BukkitTaskImpl taskObj : runners.values()) {
            // Iterator will be a best-effort (may fail to grab very new values) if called from an async thread
            if (taskObj.isSync()) continue;
            final AsyncTaskImpl task = (AsyncTaskImpl) taskObj;
            synchronized (task.getWorkers()) {
                // This will never have an issue with stale threads; it's state-safe
                workers.addAll(task.getWorkers());
            }
        }
        return workers;
    }

    @Override
    public List<BukkitTask> getPendingTasks() {
        final ArrayList<BukkitTaskImpl> truePending = new ArrayList<BukkitTaskImpl>();
        for (BukkitTaskImpl task = head.getNext(); task != null; task = task.getNext())
            if (task.getTaskId() != -1) truePending.add(task);

        final ArrayList<BukkitTask> pending = new ArrayList<BukkitTask>();
        for (BukkitTaskImpl task : runners.values())
            if (task.getPeriod() >= BukkitTaskImpl.NO_REPEATING) pending.add(task);

        for (final BukkitTaskImpl task : truePending)
            if (task.getPeriod() >= BukkitTaskImpl.NO_REPEATING && !pending.contains(task)) pending.add(task);

        if (!this.isAsyncScheduler)
            pending.addAll(this.asyncScheduler.getPendingTasks());
        return pending;
    }

    /**
     * This method is designed to never block or wait for locks; an immediate execution of all current tasks.
     */
    public void mainThreadHeartbeat(final int currentTick) {
        if (!this.isAsyncScheduler)
            this.asyncScheduler.mainThreadHeartbeat(currentTick);

        this.currentTick = currentTick;
        final List<BukkitTaskImpl> temp = this.temp;
        parsePending();
        while (isReady(currentTick)) {
            final BukkitTaskImpl task = pending.remove();
            if (task.getPeriod() < BukkitTaskImpl.NO_REPEATING) {
                if (task.isSync()) runners.remove(task.getTaskId(), task);
                parsePending();
                continue;
            }
            if (task.isSync()) {
                currentTask = task;
                try {
                    task.run();
                } catch (final Throwable throwable) {
                    task.getOwner().getLogger().log(Level.WARNING, "Task #" + task.getTaskId() + " for " + task.getOwner().getDescription().getFullName() + "generated an exception", throwable);
                } finally {currentTask = null; }
                parsePending();
            } else task.getOwner().getLogger().log(Level.SEVERE, "Unexpected Async Task in the Sync Scheduler. Report this to Paper");

            final long period = task.getPeriod(); // State consistency
            if (period > 0) {
                task.setNextRun(currentTick + period);
                temp.add(task);
            } else if (task.isSync()) runners.remove(task.getTaskId());
        }
        pending.addAll(temp);
        temp.clear();
    }

    private void addTask(final BukkitTaskImpl task) {
        final AtomicReference<BukkitTaskImpl> tail = this.tail;
        BukkitTaskImpl tailTask = tail.get();
        while (!tail.compareAndSet(tailTask, task))
            tailTask = tail.get();
        tailTask.setNext(task);
    }

    protected BukkitTaskImpl handle(final BukkitTaskImpl task, final long delay) {
        if (!this.isAsyncScheduler && !task.isSync()) {
            this.asyncScheduler.handle(task, delay);
            return task;
        }

        task.setNextRun(currentTick + delay);
        addTask(task);
        return task;
    }

    private static void validate(final Plugin plugin, final Object task) {
        Validate.notNull(plugin, "Plugin cannot be null");
        Validate.notNull(task, "Task cannot be null");
        Validate.isTrue(task instanceof Runnable || task instanceof Consumer || task instanceof Callable, "Task must be Runnable, Consumer, or Callable");
        if (!plugin.isEnabled()) throw new IllegalPluginAccessException("Plugin attempted to register task while disabled");
    }

    private int nextId() {
        return ids.incrementAndGet();
    }

    void parsePending() {
        BukkitTaskImpl head = this.head;
        BukkitTaskImpl task = head.getNext();
        BukkitTaskImpl lastTask = head;
        for (; task != null; task = (lastTask = task).getNext()) {
            if (task.getTaskId() == -1) {
                task.run();
            } else if (task.getPeriod() >= BukkitTaskImpl.NO_REPEATING) {
                pending.add(task);
                runners.put(task.getTaskId(), task);
            }
        }
        // We split this because of the way things are ordered for all of the async calls in BukkitSchedulerImpl (it prevents race-conditions)
        for (task = head; task != lastTask; task = head) {
            head = task.getNext();
            task.setNext(null);
        }
        this.head = lastTask;
    }

    private boolean isReady(final int currentTick) {
        return !pending.isEmpty() && pending.peek().getNextRun() <= currentTick;
    }

    @Override
    public String toString() {
        return "";
    }

    @Deprecated
    @Override
    public int scheduleSyncDelayedTask(Plugin plugin, BukkitRunnable task, long delay) {
        throw new UnsupportedOperationException("Use BukkitRunnable#runTaskLater(Plugin, long)");
    }

    @Deprecated
    @Override
    public int scheduleSyncDelayedTask(Plugin plugin, BukkitRunnable task) {
        throw new UnsupportedOperationException("Use BukkitRunnable#runTask(Plugin)");
    }

    @Deprecated
    @Override
    public int scheduleSyncRepeatingTask(Plugin plugin, BukkitRunnable task, long delay, long period) {
        throw new UnsupportedOperationException("Use BukkitRunnable#runTaskTimer(Plugin, long, long)");
    }

    @Deprecated
    @Override
    public BukkitTask runTask(Plugin plugin, BukkitRunnable task) throws IllegalArgumentException {
        throw new UnsupportedOperationException("Use BukkitRunnable#runTask(Plugin)");
    }

    @Deprecated
    @Override
    public BukkitTask runTaskAsynchronously(Plugin plugin, BukkitRunnable task) throws IllegalArgumentException {
        throw new UnsupportedOperationException("Use BukkitRunnable#runTaskAsynchronously(Plugin)");
    }

    @Deprecated
    @Override
    public BukkitTask runTaskLater(Plugin plugin, BukkitRunnable task, long delay) throws IllegalArgumentException {
        throw new UnsupportedOperationException("Use BukkitRunnable#runTaskLater(Plugin, long)");
    }

    @Deprecated
    @Override
    public BukkitTask runTaskLaterAsynchronously(Plugin plugin, BukkitRunnable task, long delay) throws IllegalArgumentException {
        throw new UnsupportedOperationException("Use BukkitRunnable#runTaskLaterAsynchronously(Plugin, long)");
    }

    @Deprecated
    @Override
    public BukkitTask runTaskTimer(Plugin plugin, BukkitRunnable task, long delay, long period) throws IllegalArgumentException {
        throw new UnsupportedOperationException("Use BukkitRunnable#runTaskTimer(Plugin, long, long)");
    }

    @Deprecated
    @Override
    public BukkitTask runTaskTimerAsynchronously(Plugin plugin, BukkitRunnable task, long delay, long period) throws IllegalArgumentException {
        throw new UnsupportedOperationException("Use BukkitRunnable#runTaskTimerAsynchronously(Plugin, long, long)");
    }

}