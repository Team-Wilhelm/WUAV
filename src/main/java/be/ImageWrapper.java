package be;

import gui.nodes.ImagePreview;

/**
 * This class is used to wrap an image stored as a byte array with its name, url and description.
 */
public class ImageWrapper {
    private String url, name, description;
    private byte[] imageBytes;
    private ImagePreview imagePreview;

    /**
     * This constructor is used to create an ImageWrapper object that contains the url, name, imageBytes and description of the image.
     * Should be used to create an ImageWrapper object that will be used to display an image.
     * Automatically creates an ImagePreview object.
     * @param url The image's url in blob service.
     * @param name File name.
     * @param imageBytes The image's bytes.
     * @param description The image's description.
     */
    public ImageWrapper(String url, String name, byte[] imageBytes, String description) {
        this(url, name);
        this.imageBytes = imageBytes;
        this.description = description;
        this.imagePreview = new ImagePreview(this);
    }

    /**
     * This constructor is used to create an ImageWrapper object that only contains the url and name of the image.
     * Should not be used to create an ImageWrapper object that will be used to display an image.
     * @param url The image's url in blob service.
     * @param name File name.
     */
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
