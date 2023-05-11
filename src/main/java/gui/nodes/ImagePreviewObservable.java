package gui.nodes;

import be.interfaces.Observable;
import be.interfaces.Observer;

public class ImagePreviewObservable extends Observable<ImagePreview> {
    private ImagePreview imagePreview;

    public ImagePreviewObservable(ImagePreview imagePreview) {
        super();
        this.imagePreview = imagePreview;
    }

    public ImagePreview getImagePreview() {
        return imagePreview;
    }

    // Delegate methods to the ImagePreview instance
    public void addObserver(Observer<ImagePreview> o) {
        imagePreview.addObserver(o);
    }

    public void removeObserver(Observer<ImagePreview> o) {
        imagePreview.removeObserver(o);
    }

    protected void notifyObservers(ImagePreviewObservable observable, ImagePreview arg) {
        imagePreview.notifyObservers(observable, arg);
    }
}
