/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nvAppBase;

import com.jogamp.newt.event.KeyListener;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;

/**
 *
 * @author GBarbieri
 */
public abstract class NvAppBase implements GLEventListener, KeyListener {

    private String appTitle;
    protected boolean hasInitializedGL = false;
    protected int width;
    protected int height;

    public NvAppBase(String appTitle) {
        this.appTitle = appTitle;
    }

    @Override
    public void init(GLAutoDrawable drawable) {

        GL4 gl4 = drawable.getGL().getGL4();

        if (!hasInitializedGL) {
            initRendering(gl4);
            hasInitializedGL = true;
        }

        drawable.setAutoSwapBufferMode(false);
    }

    public void initRendering(GL4 gl4) {
    }

    @Override
    public void display(GLAutoDrawable drawable) {

        GL4 gl4 = drawable.getGL().getGL4();

        draw(gl4);

        drawable.swapBuffers();
    }

    public void draw(GL4 gl4) {
    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {

        GL4 gl4 = drawable.getGL().getGL4();

        reshape(gl4, x, y, width, height);
    }

    public void reshape(GL4 gl4, int x, int y, int width, int height) {
    }

    @Override
    public void dispose(GLAutoDrawable drawable) {

        GL4 gl4 = drawable.getGL().getGL4();

        shutdownRendering(gl4);
        hasInitializedGL = false;
    }

    public void shutdownRendering(GL4 gl4) {
    }

    public String getTitle() {
        return appTitle;
    }

}
