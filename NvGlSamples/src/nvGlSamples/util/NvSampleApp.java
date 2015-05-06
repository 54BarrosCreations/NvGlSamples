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
    private long totalTime;
    private long frameTime;
    protected float frameDelta;

    public NvSampleApp() {

        super();

        transformer = new NvInputTransformer();

        frameTimer = new NvStopWatch();
        frameTimer.start();
        drawTimer = new NvStopWatch();

        totalTime = (long) -1e6;
    }

    @Override
    public void init(GLAutoDrawable glad) {

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
    }

    protected void initRendering(GL4 gl4) {

    }

    @Override
    public void display(GLAutoDrawable glad) {

        frameTimer.stop();

        frameDelta = frameTimer.getTime();

        // just an estimate
        totalTime += frameDelta;

        transformer.update(frameDelta);

        frameTimer.reset();

        GL4 gl4 = glad.getGL().getGL4();

        frameTimer.start();
//        drawTimer.start();

        draw(gl4);

        checkGlError(gl4);

        glad.swapBuffers();

//        drawTimer.stop();
//        drawTimer.reset();
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
