/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nvGlSamples.util;

import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.util.Animator;

/**
 *
 * @author elect
 */
public class NvAppBase implements GLEventListener {

    protected GLWindow glWindow;
    protected Animator animator;

    public NvAppBase() {
        initGL();
    }

    protected final void initGL() {

        GLProfile glProfile = GLProfile.getDefault();

        GLCapabilities glCapabilities = new GLCapabilities(glProfile);

        glWindow = GLWindow.create(glCapabilities);
        glWindow.setTitle("NvAppBase");
        glWindow.setSize(1280, 720);
        glWindow.setUndecorated(false);
        glWindow.setPointerVisible(true);
        glWindow.setVisible(true);
        
        glWindow.addGLEventListener(this);
        
        animator = new Animator();
        animator.add(glWindow);
        
//        Thread t = glWindow.setExclusiveContextThread(null);
//        glWindow.setExclusiveContextThread(t);
//        animator.setExclusiveContext(t);
//        animator.setModeBits(false, AnimatorBase.DEFAULT_FRAMES_PER_INTERVAL);
        animator.setExclusiveContext(true);
        animator.setRunAsFastAsPossible(true);
        animator.setUpdateFPSFrames(500, System.out);
        animator.start();
    }

    @Override
    public void init(GLAutoDrawable glad) {
        
    }

    @Override
    public void dispose(GLAutoDrawable glad) {
//        animator.stop();
//        glWindow.destroy();
    }

    @Override
    public void display(GLAutoDrawable glad) {

    }

    @Override
    public void reshape(GLAutoDrawable glad, int i, int i1, int i2, int i3) {

    }
}
