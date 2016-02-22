/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test;

import java.io.IOException;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2ES2;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLContext;
import com.jogamp.opengl.GLDrawable;
import com.jogamp.opengl.GLDrawableFactory;
import com.jogamp.opengl.GLProfile;

import com.jogamp.newt.Display;
import com.jogamp.newt.NewtFactory;
import com.jogamp.newt.Screen;
import com.jogamp.newt.Window;

public class TestGLDebug00NEWT {

    static String dbgTstMsg0 = "Hello World";
    static int dbgTstId0 = 42;

    public static void main(final String args[]) throws IOException, InterruptedException {

        TestGLDebug00NEWT t = new TestGLDebug00NEWT();
//        t.test02GL2GL3DebugEnabled();
//        t.test03GL2GL3DebugError();
        t.test04GL2GL3DebugInsert();
    }

    public void test01GL2GL3DebugDisabled() throws InterruptedException {
        final GLProfile glp = getGLProfile(GLProfile.GL2GL3);
        if (null == glp) {
            return;
        }
        testX1GLDebugEnableDisable(glp, false);
    }

    public void test02GL2GL3DebugEnabled() throws InterruptedException {
        final GLProfile glp = getGLProfile(GLProfile.GL2GL3);
        if (null == glp) {
            return;
        }
        testX1GLDebugEnableDisable(glp, true);
    }

    public void test11GLES2DebugDisabled() throws InterruptedException {
        final GLProfile glp = getGLProfile(GLProfile.GLES2);
        if (null == glp) {
            return;
        }
        testX1GLDebugEnableDisable(glp, false);
    }

    public void test12GLES2DebugEnabled() throws InterruptedException {
        final GLProfile glp = getGLProfile(GLProfile.GLES2);
        if (null == glp) {
            return;
        }
        testX1GLDebugEnableDisable(glp, true);
    }

    static GLProfile getGLProfile(final String profile) {
        if (!GLProfile.isAvailable(profile)) {
            System.err.println("Profile " + profile + " n/a");
            return null;
        }
        return GLProfile.get(profile);
    }

    void testX1GLDebugEnableDisable(final GLProfile glp, final boolean enable) throws InterruptedException {
        final WindowContext winctx = createWindow(glp, enable);
        final String glDebugExt = winctx.context.getGLDebugMessageExtension();
        System.err.println("glDebug extension: " + glDebugExt);
        System.err.println("glDebug enabled: " + winctx.context.isGLDebugMessageEnabled());
        System.err.println("glDebug sync: " + winctx.context.isGLDebugSynchronous());
        System.err.println("context version: " + winctx.context.getGLVersion());

        boolean equals = ((null == glDebugExt) ? false : enable) == winctx.context.isGLDebugMessageEnabled();
        if (!equals) {
            throw new Error();
        }

        destroyWindow(winctx);
    }

    WindowContext createWindow(final GLProfile glp, final boolean debugGL) {
        final GLCapabilities caps = new GLCapabilities(glp);
        //
        // Create native windowing resources .. X11/Win/OSX
        //
        final Display display = NewtFactory.createDisplay(null); // local display
        if (display == null) {
            throw new Error();
        }

        final Screen screen = NewtFactory.createScreen(display, 0); // screen 0
        if (screen == null) {
            throw new Error();
        }

        final Window window = NewtFactory.createWindow(screen, caps);
        if (window == null) {
            throw new Error();
        }
        window.setSize(128, 128);
        window.setVisible(true);

        final GLDrawableFactory factory = GLDrawableFactory.getFactory(glp);
        final GLDrawable drawable = factory.createGLDrawable(window);
        if (drawable == null) {
            throw new Error();
        }

        drawable.setRealized(true);

        final GLContext context = drawable.createContext(null);
        if (context == null) {
            throw new Error();
        }

        context.enableGLDebugMessage(debugGL);

        final int res = context.makeCurrent();
        if (!(GLContext.CONTEXT_CURRENT_NEW == res || GLContext.CONTEXT_CURRENT == res)) {
            throw new Error();
        }

        return new WindowContext(window, context);
    }

    void destroyWindow(final WindowContext winctx) {
        final GLDrawable drawable = winctx.context.getGLDrawable();

        if (winctx.context == null) {
            throw new Error();
        }
        winctx.context.destroy();

        if (drawable == null) {
            throw new Error();
        }
        drawable.setRealized(false);

        if (winctx.window == null) {
            throw new Error();
        }
        winctx.window.destroy();
    }

    public void test03GL2GL3DebugError() throws InterruptedException {
        final GLProfile glp = getGLProfile(GLProfile.GL2GL3);
        if (null == glp) {
            return;
        }
        testX2GLDebugError(glp);
    }

    public void test13GLES2DebugError() throws InterruptedException {
        final GLProfile glp = getGLProfile(GLProfile.GLES2);
        if (null == glp) {
            return;
        }
        testX2GLDebugError(glp);
    }

    void testX2GLDebugError(final GLProfile glp) throws InterruptedException {
        final WindowContext winctx = createWindow(glp, true);

        final MyGLDebugListener myGLDebugListener = new MyGLDebugListener(
                GL2ES2.GL_DEBUG_SOURCE_API,
                GL2ES2.GL_DEBUG_TYPE_ERROR,
                GL2ES2.GL_DEBUG_SEVERITY_HIGH);
        winctx.context.addGLDebugListener(myGLDebugListener);

        final GL gl = winctx.context.getGL();

        gl.glBindFramebuffer(-1, -1); // ERROR !

        if (winctx.context.isGLDebugMessageEnabled()) {
            if (!myGLDebugListener.received()) {
                throw new Error();
            }
        }

        destroyWindow(winctx);
    }

    public void test04GL2GL3DebugInsert() throws InterruptedException {
        final GLProfile glp = getGLProfile(GLProfile.GL2GL3);
        if (null == glp) {
            return;
        }
        testX3GLDebugInsert(glp);
    }

    public void test14GLES2DebugInsert() throws InterruptedException {
        final GLProfile glp = getGLProfile(GLProfile.GLES2);
        if (null == glp) {
            return;
        }
        testX3GLDebugInsert(glp);
    }

    void testX3GLDebugInsert(final GLProfile glp) throws InterruptedException {
        final WindowContext winctx = createWindow(glp, true);
        final MyGLDebugListener myGLDebugListener = new MyGLDebugListener(dbgTstMsg0, dbgTstId0);
        winctx.context.addGLDebugListener(myGLDebugListener);

        final String glDebugExt = winctx.context.getGLDebugMessageExtension();
        boolean equals = (null != glDebugExt) == winctx.context.isGLDebugMessageEnabled();
        if (!equals) {
            throw new Error();
        }

        if (winctx.context.isGLDebugMessageEnabled()) {
            winctx.context.glDebugMessageInsert(GL2ES2.GL_DEBUG_SOURCE_APPLICATION,
                    GL2ES2.GL_DEBUG_TYPE_OTHER,
                    dbgTstId0,
                    GL2ES2.GL_DEBUG_SEVERITY_MEDIUM, dbgTstMsg0);
            if (!myGLDebugListener.received()) {
                throw new Error();
            }
        }

        destroyWindow(winctx);
    }

    public static class WindowContext {

        public final Window window;
        public final GLContext context;

        public WindowContext(final Window w, final GLContext c) {
            window = w;
            context = c;
        }
    }

}
