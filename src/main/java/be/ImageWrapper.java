package be;

import javafx.scene.image.Image;

public class ImageWrapper {
    private String url;
    private String name;
    private Image image;

    public ImageWrapper(String utl, String name, Image image) {
        this(utl, name);
        this.image = image;
    }

    public ImageWrapper(String utl, String name) {
        this.url = utl;
        this.name = name;
        this.image = new Image(utl);
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
