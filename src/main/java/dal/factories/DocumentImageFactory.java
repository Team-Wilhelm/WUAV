package dal.factories;

import java.io.*;
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
                image = getBytesFromURL(path);
                imageCache.put(path, image);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return image;
    }

    private byte[] getBytesFromURL(String urlString) throws IOException {
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
}
