package org.jutils;

import java.awt.Image;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;

public class IconLoader {
    private final URL baseUrl;
    private final ClassLoader classLoader;
    private final String basePath;
    private final Map<String, ImageIcon> iconMap = new LinkedHashMap<String, ImageIcon>();

    public IconLoader(File file) throws MalformedURLException {
        this(file.toURI().toURL());
    }

    public IconLoader(Class<?> clazz, String path) {
        this.baseUrl = null;
        this.classLoader = clazz != null ? clazz.getClassLoader() : Thread.currentThread().getContextClassLoader();
        this.basePath = normalizeBasePath(path);
    }

    public IconLoader(URL url) {
        this.baseUrl = url;
        this.classLoader = null;
        this.basePath = null;
    }

    public ImageIcon getIcon(String name) {
        if (name == null) {
            return null;
        }

        ImageIcon cached = iconMap.get(name);
        if (cached != null) {
            return cached;
        }

        URL iconUrl = getIconUrl(name);
        if (iconUrl == null) {
            return null;
        }

        ImageIcon icon = new ImageIcon(iconUrl);
        iconMap.put(name, icon);
        return icon;
    }

    public List<ImageIcon> getIcons(String... names) {
        List<ImageIcon> icons = new ArrayList<ImageIcon>();
        for (String name : names) {
            icons.add(getIcon(name));
        }
        return icons;
    }

    public URL getIconUrl(String name) {
        if (name == null) {
            return null;
        }

        if (basePath != null) {
            String resourceName = basePath + "/" + stripLeadingSlash(name);
            URL url = null;
            if (classLoader != null) {
                url = classLoader.getResource(resourceName);
            }
            if (url == null) {
                ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
                if (contextClassLoader != null) {
                    url = contextClassLoader.getResource(resourceName);
                }
            }
            if (url == null) {
                url = IconLoader.class.getResource("/" + resourceName);
            }
            return url;
        }

        if (baseUrl != null) {
            try {
                return baseUrl.toURI().resolve(name).toURL();
            }
            catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }

        return null;
    }

    public Image getImage(String name) {
        ImageIcon icon = getIcon(name);
        return icon == null ? null : icon.getImage();
    }

    public List<Image> getImages(String... names) {
        List<Image> images = new ArrayList<Image>();
        for (String name : names) {
            images.add(getImage(name));
        }
        return images;
    }

    private static String normalizeBasePath(String path) {
        if (path == null) {
            return "";
        }
        String normalized = path.trim();
        if (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        if (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }

    private static String stripLeadingSlash(String path) {
        return path != null && path.startsWith("/") ? path.substring(1) : path;
    }
}
