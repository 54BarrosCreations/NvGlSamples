/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nvGlSamples.util;

import com.jogamp.opengl.GL4;
import com.jogamp.opengl.util.texture.TextureData;
import com.jogamp.opengl.util.texture.TextureIO;
import com.jogamp.opengl.util.texture.spi.DDSImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 *
 * @author elect
 */
public class NvImage {

    public static int uploadTextureFromDDSFile(GL4 gl4, String filePath) throws IOException {

        URL url = NvImage.class.getResource(filePath);
        File file = new File(url.getPath());

        DDSImage ddsImage = DDSImage.read(file);

        TextureData textureData = TextureIO.newTextureData(gl4.getGLProfile(), file, false, TextureIO.DDS);

        int texID = uploadTextureFromDDSData(gl4, ddsImage, textureData);

        return texID;
    }

    private static int uploadTextureFromDDSData(GL4 gl4, DDSImage ddsImage, TextureData textureData) {

        int[] texID = new int[1];

        gl4.glGenTextures(1, texID, 0);

//        if (ddsImage.isCubemap()) {
//            gl4.glBindTexture(GL4.GL_TEXTURE_CUBE_MAP, texID[0]);
//
//            for (int f = GL4.GL_TEXTURE_CUBE_MAP_POSITIVE_X;
//                    f <= GL4.GL_TEXTURE_CUBE_MAP_NEGATIVE_Z; f++) {
//
//                for (int l = 0; l < ddsImage.getNumMipMaps(); l++) {
//
//                    DDSImage.ImageInfo mipmap = ddsImage.getMipMap(l);
//
//                    int w = mipmap.getWidth();
//                    int h = mipmap.getHeight();
//
//                    if (ddsImage.isCompressed()) {
//
//                        gl4.glCompressedTexImage2D(f, l, ddsImage.getCompressionFormat(),
//                                w, h, 0, mipmap.getData().capacity(), mipmap.getData());
//                    } else {
//
//                        gl4.glTexImage2D(f, l, textureData.getInternalFormat(), w, h, 0,
//                                textureData.getPixelFormat(), textureData.getPixelType(),
//                                mipmap.getData());
//                    }
//                }
//            }
//        } else {
        gl4.glBindTexture(GL4.GL_TEXTURE_2D, texID[0]);

        if (ddsImage.getNumMipMaps() == 0) {

            if (ddsImage.isCompressed()) {

                gl4.glCompressedTexImage2D(GL4.GL_TEXTURE_2D, 0, ddsImage.getCompressionFormat(),
                        ddsImage.getWidth(), ddsImage.getHeight(), 0,
                        textureData.getBuffer().capacity(), textureData.getBuffer());
            } else {
//                    System.out.println("textureData.getInternalFormat() " + textureData.getInternalFormat());
//                    System.out.println("textureData.getPixelFormat() " + textureData.getPixelFormat());
//                    System.out.println("textureData.getPixelType() " + textureData.getPixelType());
//                    System.out.println("ddsImage.getCompressionFormat() " + ddsImage.getCompressionFormat());
//                    System.out.println("ddsImage.getPixelFormat() " + ddsImage.getPixelFormat());
//                    gl4.glTexImage2D(GL4.GL_TEXTURE_2D, 0, GL4.GL_RGB8, ddsImage.getWidth(), ddsImage.getHeight(), 0,
//                            textureData.getPixelFormat(), textureData.getPixelType(), textureData.getBuffer());
                gl4.glTexImage2D(GL4.GL_TEXTURE_2D, 0, GL4.GL_RGB8, ddsImage.getWidth(), ddsImage.getHeight(), 0,
                        GL4.GL_BGR, textureData.getPixelType(), textureData.getBuffer());
            }

        } else {

            for (int l = 0; l < ddsImage.getNumMipMaps(); l++) {

                DDSImage.ImageInfo mipmap = ddsImage.getMipMap(l);

                int w = mipmap.getWidth();
                int h = mipmap.getHeight();

                if (ddsImage.isCompressed()) {

                    gl4.glCompressedTexImage2D(GL4.GL_TEXTURE_2D, l,
                            ddsImage.getCompressionFormat(), w, h, 0,
                            mipmap.getData().capacity(), mipmap.getData());
                } else {
                    System.out.println("internalFormat " + textureData.getInternalFormat()
                            + " textureData.getPixelFormat() " + textureData.getPixelFormat()
                            + " textureData.getPixelType() " + textureData.getPixelType() + " l " + l);
                    gl4.glTexImage2D(GL4.GL_TEXTURE_2D, l, textureData.getInternalFormat(),
                            w, h, 0, textureData.getPixelFormat(), textureData.getPixelType(),
                            mipmap.getData());
                }
            }
        }
        gl4.glBindTexture(GL4.GL_TEXTURE_2D, 0);
//        }
        return texID[0];
    }
}
