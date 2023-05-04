package gui.util;

import javafx.scene.image.Image;

import java.util.HashMap;

public class IconFactory {
private HashMap<Icon, Image> iconCache = new HashMap<>();
    private HashMap<Icon, String> iconPathCache = new HashMap<>();
    public enum Icon {CALENDAR, NAMETAG}
    private static IconFactory instance;

    private IconFactory() {
        iconPathCache.put(Icon.CALENDAR, "/img/material-symbols_calendar-month-outline-rounded.png");
        iconPathCache.put(Icon.NAMETAG, "/img/mdi_clipboard-account-outline.png");
    }

    public static IconFactory getInstance() {
        if (instance == null) {
            instance = new IconFactory();
        }
        return instance;
    }

    public Image create(Icon icon) {
        Image image = iconCache.get(icon);
        if (image == null) {
            image = new Image(iconPathCache.get(icon), 50, 50, true, true);
            iconCache.put(icon, image);
        }
        return image;
    }
}
