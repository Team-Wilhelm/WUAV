package gui.util;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * Convert an image to a byte array and vice versa.
 * Convert a URL to a byte array.
 */
public class ImageByteConverter {
    public ImageByteConverter() {}

    public static Image getImageFromBytes(byte[] bytes) {
        return new Image(new ByteArrayInputStream(bytes));
    }

    public static byte[] getBytesFromImage(Image image) throws IOException {
        BufferedImage bImage = SwingFXUtils.fromFXImage(image, null);
        ByteArrayOutputStream s = new ByteArrayOutputStream();
        ImageIO.write(bImage, "png", s);
        byte[] res  = s.toByteArray();
        s.close(); //especially if you are using a different output stream.
        return res;
    }

    public static byte[] getBytesFromURL(String urlString) throws IOException {
        URL url = new URL(urlString);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (InputStream inputStream = url.openStream()) {
            int n = 0;
            byte [] buffer = new byte[4096];
            while (-1 != (n = inputStream.read(buffer))) {
                output.write(buffer, 0, n);
            }
        }
        return output.toByteArray();
    }

    public static ImageView getImageViewFromBytes(byte[] bytes) {
        return new ImageView(getImageFromBytes(bytes));
    }
}
