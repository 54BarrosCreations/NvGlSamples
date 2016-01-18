/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gl4_kepler.bindlessApp;

import com.jogamp.nativewindow.util.Dimension;
import com.jogamp.newt.Display;
import com.jogamp.newt.NewtFactory;
import com.jogamp.newt.Screen;
import com.jogamp.newt.opengl.GLWindow;
import static com.jogamp.opengl.GL.GL_EXTENSIONS;
import static com.jogamp.opengl.GL.GL_TRUE;
import static com.jogamp.opengl.GL2ES2.GL_FRAGMENT_SHADER;
import static com.jogamp.opengl.GL2ES2.GL_PROGRAM_SEPARABLE;
import static com.jogamp.opengl.GL2ES2.GL_VERTEX_SHADER;
import static com.jogamp.opengl.GL2ES3.GL_NUM_EXTENSIONS;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.util.Animator;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;

/**
 *
 * @author elect
 */
public class BindlessApp implements GLEventListener {

    private static int screenIdx = 0;
    private static Dimension windowSize = new Dimension(1024, 768);
    private static boolean undecorated = false;
    private static boolean alwaysOnTop = false;
    private static boolean fullscreen = false;
    private static boolean mouseVisible = true;
    private static boolean mouseConfined = false;
    public static GLWindow glWindow;
    public static Animator animator;
    private static String title = "Bindless Graphics Sample";

    public static void main(String[] args) {

        Display display = NewtFactory.createDisplay(null);
        Screen screen = NewtFactory.createScreen(display, screenIdx);
        GLProfile glProfile = GLProfile.get(GLProfile.GL4);
        GLCapabilities glCapabilities = new GLCapabilities(glProfile);
        glWindow = GLWindow.create(screen, glCapabilities);

        glWindow.setSize(windowSize.getWidth(), windowSize.getHeight());
        glWindow.setPosition(50, 50);
        glWindow.setUndecorated(undecorated);
        glWindow.setAlwaysOnTop(alwaysOnTop);
        glWindow.setFullscreen(fullscreen);
        glWindow.setPointerVisible(mouseVisible);
        glWindow.confinePointer(mouseConfined);
        glWindow.setTitle(title);
        glWindow.setVisible(true);

        BindlessApp bindlessApp = new BindlessApp();
        glWindow.addGLEventListener(bindlessApp);
//        glWindow.addKeyListener(bindlessApp);

        animator = new Animator(glWindow);
        animator.start();
    }

    private NvGLSLProgram shader;
    
    public BindlessApp() {

    }

    @Override
    public void init(GLAutoDrawable drawable) {
        System.out.println("init");

        GL4 gl4 = drawable.getGL().getGL4();

        // Check extensions; exit on failure
        if (!requireExtension(gl4, "GL_NV_vertex_buffer_unified_memory")) {
            return;
        }
        if (!requireExtension(gl4, "GL_NV_shader_buffer_load")) {
            return;
        }
        if (!requireExtension(gl4, "GL_EXT_direct_state_access")) {
            return;
        }
        if (!requireExtension(gl4, "GL_NV_bindless_texture")) {
            return;
        }
        NvGLSLProgram.createFromFiles(gl4, "src/gl4_kepler/bindlessApp/shaders", "bindless");
        
        
    }

    @Override
    public void display(GLAutoDrawable drawable) {

    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {

    }

    @Override
    public void dispose(GLAutoDrawable drawable) {
        System.out.println("dispose");

        GL4 gl4 = drawable.getGL().getGL4();

        System.exit(0);
    }

    

    private boolean requireExtension(GL4 gl4, String ext) {
        return requireExtension(gl4, ext, true);
    }

    private boolean requireExtension(GL4 gl4, String ext, boolean exitOnFailure) {
        boolean found = false;
        int[] extensionCount = {0};
        gl4.glGetIntegerv(GL_NUM_EXTENSIONS, extensionCount, 0);
        for (int i = 0; i < extensionCount[0]; i++) {
            if (gl4.glGetStringi(GL_EXTENSIONS, i).equals(ext)) {
                return true;
            }
        }
        if (!found) {
            if (exitOnFailure) {
                System.err.println("The current system does not appear to support the extension "
                        + ext + ", which is required by the sample. This is likely because "
                        + "the system's GPU or driver does not support the extension. "
                        + "Please see the sample's source code for details");
                quit();
            }
        }
        return found;
    }

    private void quit() {
        BindlessApp.animator.stop();
        BindlessApp.glWindow.destroy();
    }

}
