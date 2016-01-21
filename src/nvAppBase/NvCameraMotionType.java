/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nvAppBase;

/**
 *
 * @author GBarbieri
 */
public enum NvCameraMotionType {
    ORBITAL, ///< Camera orbits the world origin
    FIRST_PERSON, ///< Camera moves as in a 3D, first-person shooter
    PAN_ZOOM, ///< Camera pans and zooms in 2D
    DUAL_ORBITAL  ///< Two independent orbital transforms
}
