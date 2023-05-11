package be;

import gui.nodes.ImagePreview;
import javafx.scene.image.Image;

public class ImageWrapper {
    private String url;
    private String name;
    private Image image;
    private ImagePreview imagePreview;

    public ImageWrapper(String url, String name, Image image) {
        this(url, name);
        this.image = image;
    }

    public ImageWrapper(String url, String name) {
        this.url = url;
        this.name = name;
        this.image = new Image(url);
        this.imagePreview = new ImagePreview(this);
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
    public ImagePreview getImagePreview() {
        return imagePreview;
    }
}
