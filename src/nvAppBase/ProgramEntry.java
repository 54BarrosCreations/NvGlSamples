/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nvAppBase;

import com.jogamp.newt.Display;
import com.jogamp.newt.NewtFactory;
import com.jogamp.newt.Screen;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.util.Animator;

/**
 *
 * @author GBarbieri
 */
public abstract class ProgramEntry {

    private int screenIdx = 0;
    private int defaultWidth = 1280;
    private int defaultHeight = 720;
    private boolean undecorated = false;
    private boolean alwaysOnTop = false;
    private boolean fullscreen = false;
    private boolean mouseVisible = true;
    private boolean mouseConfined = false;
    public static GLWindow glWindow;
    public static Animator animator;
    private NvAppBase app;

    public ProgramEntry (String[] args) {

        Display display = NewtFactory.createDisplay(null);
        Screen screen = NewtFactory.createScreen(display, screenIdx);
        GLProfile glProfile = GLProfile.get(GLProfile.GL4);
        GLCapabilities glCapabilities = new GLCapabilities(glProfile);
        glWindow = GLWindow.create(screen, glCapabilities);

        glWindow.setSize(defaultWidth, defaultHeight);
        glWindow.setPosition(50, 50);
        glWindow.setUndecorated(undecorated);
        glWindow.setAlwaysOnTop(alwaysOnTop);
        glWindow.setFullscreen(fullscreen);
        glWindow.setPointerVisible(mouseVisible);
        glWindow.confinePointer(mouseConfined);
        glWindow.setResizable(true);

        app = nvAppFactory(defaultWidth, defaultHeight);
        glWindow.addGLEventListener(app);
        glWindow.addKeyListener(app);

        glWindow.setTitle(app.getTitle());
        glWindow.setVisible(true);
        animator = new Animator(glWindow);
        animator.setRunAsFastAsPossible(true);
        animator.setUpdateFPSFrames(100, System.out);
        animator.start();
    }

    public abstract NvAppBase nvAppFactory(int width, int height);
}
