/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nvGlSamples.util;

/**
 *
 * @author elect
 */
public class NvStopWatch {

    private long startTime;
    private long diffTime;
    private boolean running;

    public NvStopWatch() {
        running = false;
    }

    /**
     * Start time measurement.
     */
    public void start() {
        startTime = System.currentTimeMillis();
        running = true;
    }

    /**
     * Stop time measurement.
     */
    public void stop() {
        diffTime = getDiffTime();
        running = false;
    }

    /**
     * Reset time counters to zero.
     */
    public void reset() {
        diffTime = 0;
        if (running) {
            start();
        }
    }

    public long getTime() {
        return running ? getDiffTime() : diffTime;
    }

    /**
     * Get difference between start time and current time.
     *
     * @return
     */
    public long getDiffTime() {
        return System.currentTimeMillis() - startTime;
    }
}
