/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gl4_kepler.bindlessApp;

import com.jogamp.opengl.test.junit.jogl.acore.TestGLDebug00NEWT;
import com.jogamp.opengl.test.junit.jogl.demos.es1.newt.TestGearsES1NEWT;
import com.jogamp.opengl.test.junit.jogl.demos.es1.newt.TestOlympicES1NEWT;
import nvAppBase.NvAppBase;

/**
 *
 * @author GBarbieri
 */
public class Main extends nvAppBase.ProgramEntry {

    public static void main(String[] args) {

        new Main(args);
    }

    public Main(String[] args) {
        super(args);
    }

    @Override
    public NvAppBase nvAppFactory(int width, int height) {
        return new BindlessApp(width, height);
    }
}
