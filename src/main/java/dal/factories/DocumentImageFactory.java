package dal.factories;

import javafx.scene.image.Image;

import java.util.concurrent.ConcurrentHashMap;

public class DocumentImageFactory {
    private ConcurrentHashMap<String, Image> imageCache = new ConcurrentHashMap<>();
    private static DocumentImageFactory instance;

    private DocumentImageFactory() {}

    public static DocumentImageFactory getInstance() {
        if (instance == null) {
            instance = new DocumentImageFactory();
        }
        return instance;
    }

    public Image create(String path) {
        Image image = imageCache.get(path);
        if (image == null) {
            image = new Image(path);
            imageCache.put(path, image);
        }
        return image;
    }
}
