package tc.oc.pgm.utils;

import java.util.ArrayDeque;
import java.util.Queue;

public class RollingAverageFilter {
    /**
     *  A queue that can be used to smooth out numeric samples over time.
     */
    private final int windowSize;
    private final double temporalBias;
    private final Queue<Double> samples;

    public RollingAverageFilter(int windowSize, double temporalBias) {
        /**
         * Create a filter with the given fixed window size.
         * This will be the maximum number of samples that
         * the queue will retain. The temporalBias is the weight
         * ratio between successive samples. Values greater than
         * one will give recent samples more weight.
         */
        this.windowSize = windowSize;
        this.temporalBias = temporalBias;
        this.samples = new ArrayDeque<>(windowSize);
    }

    public double total() {
        /**
         * Return the filtered "total" of all samples in the window.
         * This is simply the average times the window size. In this
         * way, this method can return a consistently useful value
         * even if the filter is not always full.
         */
        return this.average() * this.windowSize;
    }

    public double average() {
        /**
         * Return the weighted average of all samples in the window,
         * accounting for the temporalBias parameter.
         */
        if(this.samples.isEmpty()) {
            return 0D;
        } else {
            double total = 0D, totalWeight = 0D, weight = 1D;
            for(Double n : this.samples) {
                weight *= this.temporalBias;
                totalWeight += weight;
                total += n * weight;
            }
            return total / totalWeight;
        }
    }

    public void sample(double n) {
        /**
         * Append a sample to the filter
         */
        if(this.samples.size() == this.windowSize) {
            this.samples.remove();
        }
        this.samples.add(n);
    }

    public void clear() {
        /**
         * Empty the filter of all samples
         */
        this.samples.clear();
    }

    public int size() {
        return this.samples.size();
    }
}
