/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nvGlSamples.util;

import nvGlSamples.bindlessApp.BindlessApp;

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
//        if (diffTime < 0) {
//            System.out.println("diffTime " + diffTime);
//        }
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

    public float getTime() {
        float ms = running ? getDiffTime() : diffTime;
//        if ((ms) < 0) {
//            System.out.println("ms " + ms + " running " + running + " diffTime " + diffTime);
//        }
        return ms / 1000;
    }

    /**
     * Get difference between start time and current time.
     *
     * @return
     */
    private long getDiffTime() {
        long now = System.currentTimeMillis();
        long diff = now - startTime;
        if (diff < 0) {
            System.out.println("diff " + diff + " now " + now + " startTime " + startTime);
            diff = (long) BindlessApp.minimumFrameDeltaTime;
        }
        return diff;
    }
}
