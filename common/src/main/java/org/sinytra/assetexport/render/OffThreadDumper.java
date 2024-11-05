package org.sinytra.assetexport.render;

import net.minecraft.util.thread.ReentrantBlockableEventLoop;

public class OffThreadDumper extends ReentrantBlockableEventLoop<Runnable> implements Runnable {
    private final Thread executorThread;
    public OffThreadDumper(Thread thread) {
        super(thread.getName());
        this.executorThread = thread;
    }

    @Override
    public void run() {
        managedBlock(() -> false);
    }

    @Override
    protected Runnable wrapRunnable(Runnable runnable) {
        return runnable;
    }

    @Override
    protected boolean shouldRun(Runnable runnable) {
        return true;
    }

    @Override
    protected Thread getRunningThread() {
        return executorThread;
    }
}
