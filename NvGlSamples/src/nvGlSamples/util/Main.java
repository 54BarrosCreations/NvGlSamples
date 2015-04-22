/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nvGlSamples.util;

import nvGlSamples.bindlessApp.*;
import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 *
 * @author gbarbieri
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        final BindlessApp bindlessApp = new BindlessApp();

        final Frame frame = new Frame("Bindless Graphics");

        frame.add(bindlessApp.getNewtCanvasAWT());

        frame.setSize(bindlessApp.getGlWindow().getWidth(), bindlessApp.getGlWindow().getHeight());

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent windowEvent) {
                bindlessApp.getAnimator().stop();
                bindlessApp.getGlWindow().destroy();
                frame.dispose();
                System.exit(0);
            }
        });

        frame.setVisible(true);
    }
}
