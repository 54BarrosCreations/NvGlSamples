/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nvAppBase;

import com.jogamp.opengl.GL;
import static com.jogamp.opengl.GL.GL_INVALID_ENUM;
import static com.jogamp.opengl.GL.GL_INVALID_FRAMEBUFFER_OPERATION;
import static com.jogamp.opengl.GL.GL_INVALID_OPERATION;
import static com.jogamp.opengl.GL.GL_INVALID_VALUE;
import static com.jogamp.opengl.GL.GL_NO_ERROR;
import static com.jogamp.opengl.GL.GL_OUT_OF_MEMORY;
import static com.jogamp.opengl.GL.GL_RENDERER;
import static com.jogamp.opengl.GL.GL_VENDOR;
import static com.jogamp.opengl.GL.GL_VERSION;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLContext;

/**
 *
 * @author GBarbieri
 */
public abstract class NvSampleApp extends NvAppBase {

    protected NvInputTransformer transformer;
    private NvStopWatch frameTimer;
    private NvStopWatch drawTimer;
    private float frameDelta = 0.0f;
    private float totalTime;

    public NvSampleApp(String appTitle) {

        super(appTitle);

        transformer = new NvInputTransformer();

        ProgramEntry.glWindow.addMouseListener(transformer);
        
        frameTimer = new NvStopWatch();

        drawTimer = new NvStopWatch();
    }

    @Override
    public final void init(GLAutoDrawable drawable) {

        GL4 gl4 = drawable.getGL().getGL4();

        hasInitializedGL = false;

        totalTime = -1e6f;

        baseInitRendering(gl4);
        //TODO
//        mFrameTimer -> start();
//
//        mSumDrawTime = 0.0f;
//        mDrawTimeFrames = 0;
//        mDrawRate = 0.0f;
//        NvStopWatch * drawTime = createStopWatch();
        frameTimer.start();

        GLContext.getCurrent().setSwapInterval(0);

        drawable.setAutoSwapBufferMode(false);
    }

    private void baseInitRendering(GL4 gl4) {

        System.out.println("GL_RENDERER = " + gl4.glGetString(GL_RENDERER));
        System.out.println("GL_VERSION = " + gl4.glGetString(GL_VERSION));
        System.out.println("GL_VENDOR = " + gl4.glGetString(GL_VENDOR));

        /**
         * Break the extensions into lines without breaking extensions (since
         * unbroken line-wrap with extensions hurts search)
         */
        int[] count = {0};
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

        initRendering(gl4);
    }

    @Override
    public void initRendering(GL4 gl4) {
    }

    @Override
    public final void display(GLAutoDrawable drawable) {

        GL4 gl4 = drawable.getGL().getGL4();

        frameTimer.stop();

        frameDelta = frameTimer.getTime();
        // just an estimate
        totalTime += frameDelta;

        // TODO
        transformer.update(frameDelta);
        frameTimer.reset();

        frameTimer.start();

        drawTimer.start();

        baseDraw(gl4);
        
        checkError(gl4, "NvSampleApp.display()");
        
        drawable.swapBuffers();
    }

    private void baseDraw(GL4 gl4) {
        draw(gl4);
    }

    @Override
    public void draw(GL4 gl4) {
    }

    @Override
    public final void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {

        GL4 gl4 = drawable.getGL().getGL4();
        
        baseReshape(gl4, x, y, width, height);
    }

    private void baseReshape(GL4 gl4, int x, int y, int width, int height) {

        if ((width == this.width) && (height == this.height)) {
            return;
        }
        this.width = width;
        this.height = height;

        transformer.setScreenSize(width, height);
        
        reshape(gl4, x, y, width, height);
    }
    
    @Override
    public void reshape(GL4 gl4, int x, int y, int width, int height) {        
    }

    @Override
    public final void dispose(GLAutoDrawable drawable) {
        
        GL4 gl4 = drawable.getGL().getGL4();
        
        baseShutdownRendering(gl4);
        
        ProgramEntry.animator.stop();
        System.exit(0);
    }
    
    private void baseShutdownRendering(GL4 gl4) {
        shutdownRendering(gl4);
    }
    
    @Override
    public void shutdownRendering(GL4 gl4) {        
    }

    public float getFrameDeltaTime() {
        return frameDelta;
    }
    
    protected boolean checkError(GL gl, String title) {

        int error = gl.glGetError();
        if (error != GL_NO_ERROR) {
            String errorString;
            switch (error) {
                case GL_INVALID_ENUM:
                    errorString = "GL_INVALID_ENUM";
                    break;
                case GL_INVALID_VALUE:
                    errorString = "GL_INVALID_VALUE";
                    break;
                case GL_INVALID_OPERATION:
                    errorString = "GL_INVALID_OPERATION";
                    break;
                case GL_INVALID_FRAMEBUFFER_OPERATION:
                    errorString = "GL_INVALID_FRAMEBUFFER_OPERATION";
                    break;
                case GL_OUT_OF_MEMORY:
                    errorString = "GL_OUT_OF_MEMORY";
                    break;
                default:
                    errorString = "UNKNOWN";
                    break;
            }
            System.out.println("OpenGL Error(" + errorString + "): " + title);
            throw new Error();
        }
        return error == GL_NO_ERROR;
    }
}
