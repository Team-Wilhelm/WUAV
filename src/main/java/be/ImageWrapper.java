package be;

import gui.nodes.ImagePreview;
import javafx.scene.image.Image;

import java.io.ByteArrayInputStream;

public class ImageWrapper {
    private String url, name, description;
    private byte[] imageBytes;
    private ImagePreview imagePreview;

    public ImageWrapper(String url, String name, byte[] imageBytes, String description) {
        this(url, name);
        this.imageBytes = imageBytes;
        this.description = description;
        this.imagePreview = new ImagePreview(this);
    }

    public ImageWrapper(String url, String name) {
        this.url = url;
        this.name = name;
    }

    public String getUrl() {
        return url;
    }
    public String getName() {
        return name;
    }
    public byte[] getImageBytes() {
        return imageBytes;
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
