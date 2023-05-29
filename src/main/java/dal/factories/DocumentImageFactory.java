package dal.factories;

import gui.util.ImageByteConverter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.ConcurrentHashMap;

public class DocumentImageFactory {
    private ConcurrentHashMap<String, byte[]> imageCache = new ConcurrentHashMap<>();
    private static DocumentImageFactory instance;

    private DocumentImageFactory() {}

    public static DocumentImageFactory getInstance() {
        if (instance == null) {
            instance = new DocumentImageFactory();
        }
        return instance;
    }

    public byte[] create(String path) {
        byte[] image = imageCache.get(path);
        if (image == null) {
            try {
                image = ImageByteConverter.getBytesFromURL(path);
                imageCache.put(path, image);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return image;
    }
}
