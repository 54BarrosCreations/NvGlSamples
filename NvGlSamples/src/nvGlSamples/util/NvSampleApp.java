/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nvGlSamples.util;

import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLAutoDrawable;

/**
 *
 * @author elect
 */
public class NvSampleApp extends NvAppBase {

    protected NvInputTransformer transformer;
    private NvStopWatch frameTimer;
    private NvStopWatch drawTimer;
    private float totalTime;
    private float drawTime;
    private int frames;
    private int last1kFrame;
    private long timeSinceLast1k;
//    private long frameTime;
    protected float frameDelta;
    private long[] frameGpuTime;
    private long totalGpuTime;
    private int[] queryId;

    public NvSampleApp() {

        super();

        transformer = new NvInputTransformer();

        frameTimer = new NvStopWatch();
        frameTimer.start();
        drawTimer = new NvStopWatch();

//        totalTime = (long) -1e6;
        frameGpuTime = new long[1];
        queryId = new int[1];
    }

    @Override
    public void init(GLAutoDrawable glad) {
        System.out.println("init");
        GL4 gl4 = glad.getGL().getGL4();

        System.out.println("GL_RENDERER = " + gl4.glGetString(GL4.GL_RENDERER));
        System.out.println("GL_VERSION = " + gl4.glGetString(GL4.GL_VERSION));
        System.out.println("GL_VENDOR = " + gl4.glGetString(GL4.GL_VENDOR));

        /**
         * Break the extensions into lines without breaking extensions (since
         * unbroken line-wrap with extensions hurts search)
         */
        int[] count = new int[1];
        gl4.glGetIntegerv(GL4.GL_NUM_EXTENSIONS, count, 0);
        System.out.println("GL_EXTENSIONS = ");
        int lineMaxLen = 80;
        String currExt, currLine = gl4.glGetStringi(GL4.GL_EXTENSIONS, 0);
        for (int i = 1; i < count[0]; i++) {

            currExt = gl4.glGetStringi(GL4.GL_EXTENSIONS, i);

            if ((currLine + " " + currExt).length() < lineMaxLen) {

                currLine = currLine + " " + currExt;

            } else {

                System.out.println("" + currLine);

                currLine = currExt;
            }
        }
        System.out.println("" + currLine);

        glad.setAutoSwapBufferMode(false);
//        System.out.println("gl4.getSwapInterval() " + gl4.getSwapInterval());

        initRendering(gl4);

//        Thread t = glWindow.setExclusiveContextThread(null);
//        glWindow.setExclusiveContextThread(t);
//        animator.setExclusiveContext(t);
        animator.setRunAsFastAsPossible(true);
        animator.setUpdateFPSFrames(100, System.out);
        animator.start();

        timeSinceLast1k = System.currentTimeMillis();

        gl4.glGenQueries(1, queryId, 0);
    }

    protected void initRendering(GL4 gl4) {

    }

    @Override
    public void display(GLAutoDrawable glad) {

        frameTimer.stop();

        frameDelta = frameTimer.getTime();

        // just an estimate
//        System.out.println("totalTime " + totalTime + " frameDelta " + frameDelta);
        totalTime += frameDelta;
//        if (frames % 1000 == 0) {
//            long now = System.currentTimeMillis();
//            System.out.println("totalTime " + totalTime + " ms, average time per frame " + totalTime / (frames + 1)
//                    + " average time per frame in the last 1k frames " + (float) (now - timeSinceLast1k) / (1000));
//            timeSinceLast1k = now;
//        }

        transformer.update(frameDelta);

        frameTimer.reset();

        GL4 gl4 = glad.getGL().getGL4();

        gl4.glBeginQuery(GL4.GL_TIME_ELAPSED, queryId[0]);
        {
            frameTimer.start();
//        drawTimer.start();

            draw(gl4);

            checkGlError(gl4);

            glad.swapBuffers();
        }
        gl4.glEndQuery(GL4.GL_TIME_ELAPSED);
        gl4.glGetQueryObjecti64v(queryId[0], GL4.GL_QUERY_RESULT, frameGpuTime, 0);
        totalGpuTime += frameGpuTime[0];

//        drawTimer.stop();
//        drawTime += drawTimer.getTime();
        if (frames % 1000 == 0) {
//            long now = System.currentTimeMillis();
//            System.out.println("average time per draw " + drawTime / (frames + 1));
            System.out.println("last frameGpuTime " + frameGpuTime[0] + " totalGpuTime " + totalGpuTime
                    + " frames " + (frames + 1) + " average frameGpuTime " + (totalGpuTime / (frames + 1)));
        }
//        drawTimer.reset();
        frames++;
    }

    public void draw(GL4 gl4) {

    }

    protected void checkGlError(GL4 gl4) {
        int error = gl4.glGetError();
        if (error != GL4.GL_NO_ERROR) {
            System.out.println("error " + error);
        }
    }
}
