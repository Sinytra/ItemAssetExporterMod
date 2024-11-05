package org.sinytra.assetexport;

import org.sinytra.assetexport.dumper.Identifiable;

import java.util.concurrent.atomic.AtomicInteger;

public class ProgressTracker {
    public static long start;
    public static Identifiable<?> currentRender;
    public static Counter generated;
    public static Counter dumped;

    public record Counter(AtomicInteger done, int target) {
        public Counter(int target) {
            this(new AtomicInteger(), target);
        }

        public void increment() {
            done.incrementAndGet();
        }

        public float get() {
            return (float)done.get() / target;
        }
    }
}
