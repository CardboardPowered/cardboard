package com.javazilla.bukkitfabric;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.CodeSource;
import java.util.Enumeration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.bukkit.plugin.java.JavaPluginLoader;

import com.google.common.io.ByteStreams;
import com.javazilla.bukkitfabric.nms.ReflectionRemapper;

public class LibraryClassLoader extends URLClassLoader {

    private final Map<String, Class<?>> classes = new ConcurrentHashMap<String, Class<?>>();
    public final JarFile jar;
    private final Manifest manifest;
    private final URL url;

    public LibraryClassLoader(URL[] urls, File file) throws IOException {
        super(urls);
        this.jar = new JarFile(file);
        this.manifest = jar.getManifest();
        this.url = file.toURI().toURL();
        test();
    }

    public void test() {
        Enumeration<JarEntry> o = jar.entries();
        while (o.hasMoreElements()) {
            JarEntry entry = o.nextElement();
            String path = entry.getName();
            if (!path.endsWith(".class")) continue;

            Class<?> result = null;
            if (result == null) {
                String name = path.replace('/', '.').replace(".class", "");

                if (entry != null) {
                    byte[] classBytes = null;
                    try (InputStream is = jar.getInputStream(entry)) {
                        classBytes = ByteStreams.toByteArray(is);
                    } catch (IOException ex) {/*ERROR*/}

                    int dot = name.lastIndexOf('.');
                    if (dot != -1) {
                        String pkgName = name.substring(0, dot);
                        if (getPackage(pkgName) == null) {
                            try {
                                if (manifest != null) definePackage(pkgName, manifest, url);
                                else definePackage(pkgName, null, null, null, null, null, null, null);
                            } catch (IllegalArgumentException ex) {
                                if (getPackage(pkgName) == null) throw new IllegalStateException("Cannot find package " + pkgName);
                            }
                        }
                    }

                    CodeSource source = new CodeSource(url, entry.getCodeSigners());
                    try {
                        result = defineClass(name, classBytes, 0, classBytes.length, source);
                    } catch (LinkageError ignore) {
                        //System.err.println(ignore.getMessage()); // Should only be in dev
                    }
                }

                try {
                    if (result == null) result = super.findClass(name);
                } catch (ClassNotFoundException | LinkageError e) {}

                if (result != null) setClass(name, result);

                if (result != null)
                classes.put(name, result);
            }
        }
    }


    public void setClass(String name, Class<?> result) {
        JavaPluginLoader jpl = ReflectionRemapper.getFirstJPL();
        try {
            Method m = jpl.getClass().getDeclaredMethod("setClass", String.class, Class.class);
            m.setAccessible(true);
            m.invoke(jpl, name, result);
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

}