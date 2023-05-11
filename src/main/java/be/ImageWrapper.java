package be;

import gui.nodes.ImagePreview;
import javafx.scene.image.Image;

public class ImageWrapper {
    private String url, name, description;
    private Image image;
    private ImagePreview imagePreview;

    public ImageWrapper(String url, String name, Image image, String description) {
        this(url, name);
        this.image = image;
        this.description = description;
    }

    public ImageWrapper(String url, String name) {
        this.url = url;
        this.name = name;
        this.image = new Image(url);
        this.imagePreview = new ImagePreview(this);
    }

    //TODO add description to constructor

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
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
}
