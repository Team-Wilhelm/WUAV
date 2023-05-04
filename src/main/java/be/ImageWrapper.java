package be;

import javafx.scene.image.Image;

public class ImageWrapper {
    private String url;
    private String name;
    private Image image;

    public ImageWrapper(String url, String name, Image image) {
        this(url, name);
        this.image = image;
    }

    public ImageWrapper(String url, String name) {
        this.url = url;
        this.name = name;
        this.image = new Image(url);
    }

    public String getUrl() {
        return url;
    }
    public String getName() {
        return name;
    }
    public Image getImage() {
        return image;
    }
}
