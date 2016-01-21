//----------------------------------------------------------------------------------
// File:        NvAppBase/MainWin32.cpp
// SDK Version: v2.11 
// Email:       gameworks@nvidia.com
// Site:        http://developer.nvidia.com/
//
// Copyright (c) 2014-2015, NVIDIA CORPORATION. All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions
// are met:
//  * Redistributions of source code must retain the above copyright
//    notice, this list of conditions and the following disclaimer.
//  * Redistributions in binary form must reproduce the above copyright
//    notice, this list of conditions and the following disclaimer in the
//    documentation and/or other materials provided with the distribution.
//  * Neither the name of NVIDIA CORPORATION nor the names of its
//    contributors may be used to endorse or promote products derived
//    from this software without specific prior written permission.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS ``AS IS'' AND ANY
// EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
// IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
// PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
// CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
// EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
// PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
// PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
// OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
// (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
// OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
//
//----------------------------------------------------------------------------------
package nvAppBase;

/**
 *
 * @author elect
 */
public class NvStopWatch {

    private long startTime;
    private long diffTime;
    private boolean running;

    // Constructor, default
    public NvStopWatch() {
        running = false;
        diffTime = 0;
    }

    /**
     * Start time measurement.
     */
    public void start() {
        /**
         * IMPORTANT. Avoid System.currentTimeMillis() as on Ubuntu can shift
         * and getting drifted returning a negative difference.
         */
//        startTime = System.currentTimeMillis();
        startTime = System.nanoTime();
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

    /**
     * In ns.
     * @return 
     */
    public float getTime() {
//        float ms = running ? getDiffTime() : diffTime;
//        if ((ms) < 0) {
//            System.out.println("ms " + ms + " running " + running + " diffTime " + diffTime);
//        }
//System.out.println(""+(running ? getDiffTime() : diffTime));
//System.out.println(""+(long) ((double)(running ? getDiffTime() : diffTime) / 1_000_000_000));
        return (float)(running ? getDiffTime() : diffTime) / 1_000_000_000;
    }

    /**
     * Get difference between start time and current time.
     *
     * @return
     */
    private long getDiffTime() {
//        long now = System.currentTimeMillis();
        long now = System.nanoTime();
        return now - startTime;
//        long diff = now - startTime;
//        if (diff < 0) {
//            System.out.println("diff " + diff + " now " + now + " startTime " + startTime);
//            diff = (long) BindlessApp.minimumFrameDeltaTime;
//        }
//        return diff;
    }
}
