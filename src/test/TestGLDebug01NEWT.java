package test;

import java.io.IOException;

import com.jogamp.opengl.GL2ES2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLContext;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.GLRunnable;

import com.jogamp.newt.opengl.GLWindow;

public class TestGLDebug01NEWT {

    static String dbgTstMsg0 = "Hello World";
    static int dbgTstId0 = 42;

    public static void main(final String args[]) throws IOException, InterruptedException {
        TestGLDebug01NEWT t = new TestGLDebug01NEWT();

        t.test01GL2GL3DebugDisabled();
    }

    public void test01GL2GL3DebugDisabled() throws InterruptedException {
        final GLProfile glp = getGLProfile(GLProfile.GL2GL3);
        if (null == glp) {
            return;
        }
        testX1GLDebugEnableDisable(glp, false, null, -1);
    }

    public void test02GL2GL3DebugEnabled() throws InterruptedException {
        final GLProfile glp = getGLProfile(GLProfile.GL2GL3);
        if (null == glp) {
            return;
        }
        testX1GLDebugEnableDisable(glp, true, dbgTstMsg0, dbgTstId0);
    }

    public void test11GLES2DebugDisabled() throws InterruptedException {
        final GLProfile glp = getGLProfile(GLProfile.GLES2);
        if (null == glp) {
            return;
        }
        testX1GLDebugEnableDisable(glp, false, null, -1);
    }

    public void test12GLES2DebugEnabled() throws InterruptedException {
        final GLProfile glp = getGLProfile(GLProfile.GLES2);
        if (null == glp) {
            return;
        }
        testX1GLDebugEnableDisable(glp, true, dbgTstMsg0, dbgTstId0);
    }

    void testX1GLDebugEnableDisable(final GLProfile glp, final boolean enable, 
            final String dbgTstMsg, final int dbgTstId) throws InterruptedException {
        
        final GLWindow window = createWindow(glp, enable);
        final GLContext ctx = window.getContext();
        final MyGLDebugListener myGLDebugListener = new MyGLDebugListener(dbgTstMsg, dbgTstId);
        if (enable) {
            ctx.addGLDebugListener(myGLDebugListener);
        }
        final String glDebugExt = ctx.getGLDebugMessageExtension();
        System.err.println("glDebug extension: " + glDebugExt);
        System.err.println("glDebug enabled: " + ctx.isGLDebugMessageEnabled());
        System.err.println("glDebug sync: " + ctx.isGLDebugSynchronous());
        System.err.println("context version: " + ctx.getGLVersion());

        boolean assertEquals = ((null == glDebugExt) ? false : enable) == ctx.isGLDebugMessageEnabled();
        if (!assertEquals) {
            throw new Error();
        }

        if (ctx.isGLDebugMessageEnabled() && null != dbgTstMsg && 0 <= dbgTstId) {
            window.invoke(true, new GLRunnable() {
                public boolean run(final GLAutoDrawable drawable) {
                    drawable.getContext().glDebugMessageInsert(GL2ES2.GL_DEBUG_SOURCE_APPLICATION,
                            GL2ES2.GL_DEBUG_TYPE_OTHER,
                            dbgTstId,
                            GL2ES2.GL_DEBUG_SEVERITY_MEDIUM, dbgTstMsg);
                    return true;
                }
            });
            if (!myGLDebugListener.received()) {
                throw new Error();
            }
        }

        destroyWindow(window);
    }

    public void test03GL2GL3DebugError() throws InterruptedException {
        final GLProfile glp = getGLProfile(GLProfile.GL2GL3);
        if (null == glp) {
            return;
        }
        testX3GLDebugError(glp);
    }

    public void test13GLES2DebugError() throws InterruptedException {
        final GLProfile glp = getGLProfile(GLProfile.GLES2);
        if (null == glp) {
            return;
        }
        testX3GLDebugError(glp);
    }

    void testX3GLDebugError(final GLProfile glp) throws InterruptedException {
        final GLWindow window = createWindow(glp, true);

        final MyGLDebugListener myGLDebugListener = new MyGLDebugListener(
                GL2ES2.GL_DEBUG_SOURCE_API,
                GL2ES2.GL_DEBUG_TYPE_ERROR,
                GL2ES2.GL_DEBUG_SEVERITY_HIGH);
        window.getContext().addGLDebugListener(myGLDebugListener);

        window.invoke(true, new GLRunnable() {
            public boolean run(final GLAutoDrawable drawable) {
                drawable.getGL().glBindFramebuffer(-1, -1); // ERROR !
                return true;
            }
        });

        if (window.getContext().isGLDebugMessageEnabled()) {
            if (!myGLDebugListener.received()) {
                throw new Error();
            }
        }

        destroyWindow(window);
    }

    static GLProfile getGLProfile(final String profile) {
        if (!GLProfile.isAvailable(profile)) {
            System.err.println("Profile " + profile + " n/a");
            return null;
        }
        return GLProfile.get(profile);
    }

    GLWindow createWindow(final GLProfile glp, final boolean debugGL) {
        final GLCapabilities caps = new GLCapabilities(glp);
        //
        // Create native windowing resources .. X11/Win/OSX
        //
        final GLWindow window = GLWindow.create(caps);
        if (window == null) {
            throw new Error();
        }
        window.setContextCreationFlags(debugGL ? GLContext.CTX_OPTION_DEBUG : 0);
        window.setSize(128, 128);
        window.setVisible(true);

        if (window.getContext() == null) {
            throw new Error();
        }
        if (!window.getContext().isCreated()) {
            throw new Error();
        }

        return window;
    }

    void destroyWindow(final GLWindow window) {
        window.destroy();
    }

}
