package org.bukkit.plugin.java;

import com.google.common.base.Preconditions;
import com.mohistmc.banner.bukkit.nms.ClassLoaderContext;
import com.mohistmc.banner.bukkit.nms.model.ClassMapping;
import com.mohistmc.banner.bukkit.nms.utils.RemapUtils;
import com.mohistmc.bukkit.pluginfix.PluginFixManager;
import com.mohistmc.dynamicenum.MohistDynamEnum;
import net.md_5.specialsource.repo.RuntimeRepo;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.SimplePluginManager;
import org.cardboardpowered.util.MyPluginFixManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.security.CodeSigner;
import java.security.CodeSource;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.logging.Level;

/**
 * A ClassLoader for plugins, to allow shared classes across multiple plugins
 */
public class PluginClassLoader extends URLClassLoader {
    private final JavaPluginLoader loader;
    private final Map<String, Class<?>> classes = new ConcurrentHashMap<String, Class<?>>();
    private final PluginDescriptionFile description;
    private final File dataFolder;
    private final File file;
    private final JarFile jar;
    private final Manifest manifest;
    private final URL url;
    private final ClassLoader libraryLoader;
    final JavaPlugin plugin;
    private JavaPlugin pluginInit;
    private IllegalStateException pluginState;
    private final Set<String> seenIllegalAccess = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private final Set<Package> packageCache = Collections.newSetFromMap(new ConcurrentHashMap<>());

    static {
        ClassLoader.registerAsParallelCapable();
    }

    PluginClassLoader(@NotNull final JavaPluginLoader loader, @Nullable final ClassLoader parent, @NotNull final PluginDescriptionFile description, @NotNull final File dataFolder, @NotNull final File file, @Nullable ClassLoader libraryLoader) throws IOException, InvalidPluginException, MalformedURLException {
        super(new URL[] {file.toURI().toURL()}, parent);
        Preconditions.checkArgument(loader != null, "Loader cannot be null");

        this.loader = loader;
        this.description = description;
        this.dataFolder = dataFolder;
        this.file = file;
        this.jar = new JarFile(file);
        this.manifest = jar.getManifest();
        this.url = file.toURI().toURL();
        this.libraryLoader = libraryLoader;

        try {
            Class<?> jarClass;
            try {
                jarClass = Class.forName(description.getMain(), true, this);
            } catch (ClassNotFoundException ex) {
                throw new InvalidPluginException("Cannot find main class `" + description.getMain() + "'", ex);
            }

            Class<? extends JavaPlugin> pluginClass;
            try {
                pluginClass = jarClass.asSubclass(JavaPlugin.class);
            } catch (ClassCastException ex) {
                throw new InvalidPluginException("main class `" + description.getMain() + "' does not extend JavaPlugin", ex);
            }

            plugin = pluginClass.newInstance();
        } catch (IllegalAccessException ex) {
            throw new InvalidPluginException("No public constructor", ex);
        } catch (InstantiationException ex) {
            throw new InvalidPluginException("Abnormal plugin type", ex);
        }
    }

    // private String mapCraftBukkit(String name) {
    //     if (name.startsWith("org.bukkit.craftbukkit.")) {
    //         int nameStart = name.indexOf('.', 23);
    //         if (nameStart != -1) {
    //             return "org.bukkit.craftbukkit" + name.substring(nameStart);
    //         }
    //     } else if(name.startsWith("org/bukkit/craftbukkit")) {
    //         int nameStart = name.indexOf('/', 23);
    //         if (nameStart != -1) {
    //             return "org/bukkit/craftbukkit" + name.substring(nameStart);
    //         }
    //     }
    //
    //     return name;
    // }

    @Override
    public URL getResource(String name) {
        return findResource(name);
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        return findResources(name);
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        return loadClass0(name, resolve, true, true);
    }

    Class<?> loadClass0(@NotNull String name, boolean resolve, boolean checkGlobal, boolean checkLibraries) throws ClassNotFoundException {
        // name = mapCraftBukkit(name);

        try {
            Class<?> result = super.loadClass(name, resolve);

            // SPIGOT-6749: Library classes will appear in the above, but we don't want to return them to other plugins
            if (checkGlobal || result.getClassLoader() == this) {
                return result;
            }
        } catch (ClassNotFoundException ex) {
        }

        if (checkLibraries && libraryLoader != null) {
            try {
                return libraryLoader.loadClass(name);
            } catch (ClassNotFoundException ex) {
            }
        }

        if (checkGlobal) {
            // This ignores the libraries of other plugins, unless they are transitive dependencies.
            Class<?> result = loader.getClassByName(name, resolve, description);

            if (result != null) {
                // If the class was loaded from a library instead of a PluginClassLoader, we can assume that its associated plugin is a transitive dependency and can therefore skip this check.
                if (result.getClassLoader() instanceof PluginClassLoader) {
                    PluginDescriptionFile provider = ((PluginClassLoader) result.getClassLoader()).description;

                    if (provider != description
                            && !seenIllegalAccess.contains(provider.getName())
                            && !((SimplePluginManager) loader.server.getPluginManager()).isTransitiveDepend(description, provider)) {

                        seenIllegalAccess.add(provider.getName());
                        if (plugin != null) {
                            plugin.getLogger().log(Level.WARNING, "Loaded class {0} from {1} which is not a depend or softdepend of this plugin.", new Object[]{name, provider.getFullName()});
                        } else {
                            // In case the bad access occurs on construction
                            loader.server.getLogger().log(Level.WARNING, "[{0}] Loaded class {1} from {2} which is not a depend or softdepend of this plugin.", new Object[]{description.getName(), name, provider.getFullName()});
                        }
                    }
                }

                return result;
            }
        }

        throw new ClassNotFoundException(name);
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        // name = mapCraftBukkit(name);

        ClassLoaderContext.put(this);
        Class<?> result;
        try {
            if (RemapUtils.needRemap(name.replace('/','.'))) {
                ClassMapping remappedClassMapping = RemapUtils.jarMapping.byNMSName.get(name);
                if(remappedClassMapping == null){
                    throw new ClassNotFoundException(name.replace('/','.'));
                }
                String remappedClass = remappedClassMapping.getMcpName();
                return Class.forName(remappedClass);
            }
            if (name.startsWith("org.bukkit.")) {
                throw new ClassNotFoundException(name);
            }
            result = classes.get(name);
            synchronized (name.intern()) {
                if (result == null) {
                    result = remappedFindClass(name);

                    if (result != null) {
                        loader.setClass(name, result);
                    }

                    if (result == null) {
                        try {
                            result = CraftServer.server.getClass().getClassLoader().loadClass(name);
                        } catch (Throwable throwable) {
                            throw new ClassNotFoundException(name, throwable);
                        }
                    }

                    loader.setClass(name, result);
                    classes.put(name, result);
                }
            }
        } finally {
            ClassLoaderContext.pop();
        }
        return result;
    }
    
    private static File debug_folder = new File("C:\\Users\\isaia\\");

    private Class<?> remappedFindClass(String name) {
        Class<?> result = null;

        try {
            // Load the resource to the name
            String path = name.replace('.', '/').concat(".class");
            URL url = this.findResource(path);
            if (url != null) {
                InputStream stream = url.openStream();
                if (stream != null) {
                    byte[] bytecode = RemapUtils.jarRemapper.remapClassFile(stream, RuntimeRepo.getInstance());
                    
                    //if (path.contains("/worldedit/bukkit/adapter/impl/")) {
                    //	System.out.println("Debug: Processing class: " + path);
                    //}
                    
                    bytecode = loader.server.getUnsafe().processClass(description, path, bytecode);
                    bytecode = RemapUtils.remapFindClass(bytecode);

                    bytecode = modifyByteCode(name, bytecode); // Mohist: add entry point for asm or mixin

                    bytecode = MyPluginFixManager.injectPluginFix(name, bytecode); // Mohist - Inject plugin fix

                    JarURLConnection jarURLConnection = (JarURLConnection) url.openConnection();
                    URL jarURL = jarURLConnection.getJarFileURL();

                    final Manifest manifest = jarURLConnection.getManifest();
                    fixPackage(manifest, url, name);

                    CodeSource codeSource = new CodeSource(jarURL, new CodeSigner[0]);
                    result = this.defineClass(name, bytecode, 0, bytecode.length, codeSource);
                    if (result != null) {
                        // Resolve it - sets the class loader of the class
                        this.resolveClass(result);
                    }
                    
                    if (debug_folder.isDirectory() && path.contains("worldedit")) {
	                    File out = new File("C:\\Users\\isaia\\Documents\\fo\\" + name.replace('.', File.separatorChar) + ".class");
	                    try {
	                    	Files.createDirectories(out.toPath().getParent());
	    					Files.write(out.toPath(), bytecode);
	    				} catch (IOException e) {
	    					// TODO Auto-generated catch block
	    					e.printStackTrace();
	    				}
                    }
					
                }
            }
        } catch (Exception t) {
            t.printStackTrace();
        }

        return result;
    }

    // Mohist start: add entry point for asm or mixin
    private byte[] modifyByteCode(String className, byte[] bytes) {
        return bytes;
    }
    //Mohist end

    private void fixPackage(Manifest manifest, URL url, String name) {
        int dot = name.lastIndexOf('.');
        if (dot != -1) {
            String pkgName = name.substring(0, dot);
            Package pkg = getPackage(pkgName);
            if (pkg == null) {
                try {
                    if (manifest != null) {
                        pkg = definePackage(pkgName, manifest, url);
                    } else {
                        pkg = definePackage(pkgName, null, null, null, null, null, null, null);
                    }
                } catch (IllegalArgumentException ignored) {
                }
            }
            if (pkg != null && manifest != null) {
                if (!packageCache.contains(pkg)) {
                    Attributes attributes = manifest.getMainAttributes();
                    if (attributes != null) {
                        try {
                            try {
                                Object versionInfo = MohistDynamEnum.getField(pkg, Package.class.getDeclaredField("versionInfo"));
                                if (versionInfo != null) {
                                    Class<?> Package$VersionInfo = Class.forName("java.lang.Package$VersionInfo");
                                    MohistDynamEnum.setField(versionInfo, attributes.getValue(Attributes.Name.IMPLEMENTATION_TITLE), Package$VersionInfo.getDeclaredField("implTitle"));
                                    MohistDynamEnum.setField(versionInfo, attributes.getValue(Attributes.Name.IMPLEMENTATION_VERSION), Package$VersionInfo.getDeclaredField("implVersion"));
                                    MohistDynamEnum.setField(versionInfo, attributes.getValue(Attributes.Name.IMPLEMENTATION_VENDOR), Package$VersionInfo.getDeclaredField("implVendor"));
                                    MohistDynamEnum.setField(versionInfo, attributes.getValue(Attributes.Name.SPECIFICATION_TITLE), Package$VersionInfo.getDeclaredField("specTitle"));
                                    MohistDynamEnum.setField(versionInfo, attributes.getValue(Attributes.Name.SPECIFICATION_VERSION), Package$VersionInfo.getDeclaredField("specVersion"));
                                    MohistDynamEnum.setField(versionInfo, attributes.getValue(Attributes.Name.SPECIFICATION_VENDOR), Package$VersionInfo.getDeclaredField("specVendor"));
                                }
                            } catch (Exception ignored) {
                            }
                        } finally {
                            packageCache.add(pkg);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void close() throws IOException {
        try {
            super.close();
        } finally {
            jar.close();
        }
    }

    @NotNull
    Collection<Class<?>> getClasses() {
        return classes.values();
    }

    synchronized void initialize(@NotNull JavaPlugin javaPlugin) {
        Preconditions.checkArgument(javaPlugin != null, "Initializing plugin cannot be null");
        Preconditions.checkArgument(javaPlugin.getClass()
                .getClassLoader() == this, "Cannot initialize plugin outside of this class loader");
        if (this.plugin != null || this.pluginInit != null) {
            throw new IllegalArgumentException("Plugin already initialized!", pluginState);
        }

        pluginState = new IllegalStateException("Initial initialization");
        this.pluginInit = javaPlugin;

        javaPlugin.init(loader, loader.server, description, dataFolder, file, this);
    }
}



/*package org.bukkit.plugin.java;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.security.CodeSigner;
import java.security.CodeSource;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.logging.Level;

import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.SimplePluginManager;

import com.google.common.io.ByteStreams;
import com.google.common.base.Preconditions;
import com.mohistmc.remapper.McVersion;
import com.mohistmc.remapper.v2.ClassLoaderRemapper;
import com.mohistmc.remapper.v2.GlobalClassRepo;
import com.mohistmc.remapper.v2.MohistRemapper;
import com.mohistmc.remapper.v2.RemappingClassLoader;
import com.mohistmc.bukkit.pluginfix.PluginFixManager;

import java.net.*;
import java.util.Collection;
import java.util.concurrent.Callable;

import io.izzel.tools.product.Product;
import io.izzel.tools.product.Product2;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
*/
/**
 * A ClassLoader for plugins, to allow shared classes across multiple plugins
 *
 * A ClassLoader for plugins, to allow shared classes across multiple plugins
 *
 * public class PluginClassLoader extends URLClassLoader implements RemappingClassLoader {
 *
 * private final JavaPluginLoader loader;
 * private final Map<String, Class<?>> classes = new ConcurrentHashMap<String, Class<?>>();
 * private final PluginDescriptionFile description;
 * private final File dataFolder;
 * private final File file;
 * public final JarFile jar;
 * private final Manifest manifest;
 * private final URL url;
 * final JavaPlugin plugin;
 * private JavaPlugin pluginInit;
 * private IllegalStateException pluginState;
 * private final Set<String> seenIllegalAccess = Collections.newSetFromMap(new ConcurrentHashMap<>());
 *
 * static {
 * ClassLoader.registerAsParallelCapable();
 * }
 *
 * private ClassLoaderRemapper remapper;
 *
 * PluginClassLoader(final JavaPluginLoader loader, final ClassLoader parent, final PluginDescriptionFile description, final File dataFolder, final File file) throws IOException, InvalidPluginException, MalformedURLException {
 * super(new URL[] {file.toURI().toURL()}, parent);;
 * //Validate.notNull(loader, "Loader cannot be null");
 *
 * this.loader = loader;
 * this.description = description;
 * this.dataFolder = dataFolder;
 * this.file = file;
 * this.jar = new JarFile(file);
 * this.manifest = jar.getManifest();
 * this.url = file.toURI().toURL();
 *
 * try {
 * Class<?> jarClass;
 * try {
 * jarClass = Class.forName(description.getMain(), true, this);
 * } catch (ClassNotFoundException ex) {
 * throw new InvalidPluginException("Cannot find main class `" + description.getMain() + "'", ex);
 * }
 *
 * Class<? extends JavaPlugin> pluginClass;
 * try {
 * pluginClass = jarClass.asSubclass(JavaPlugin.class);
 * } catch (ClassCastException ex) {
 * throw new InvalidPluginException("main class `" + description.getMain() + "' does not extend JavaPlugin", ex);
 * }
 *
 * plugin = pluginClass.newInstance();
 * } catch (IllegalAccessException ex) {
 * throw new InvalidPluginException("No public constructor", ex);
 * } catch (InstantiationException ex) {
 * throw new InvalidPluginException("Abnormal plugin type", ex);
 * }
 * }
 * @Override public Class<?> loadClass(String name) throws ClassNotFoundException {
 * return super.loadClass(name);
 * }
 * @Override public URL getResource(String name) {
 * return findResource(name);
 * }
 * @Override public Enumeration<URL> getResources(String name) throws IOException {
 * return findResources(name);
 * }
 * @Override protected Class<?> findClass(String name) throws ClassNotFoundException {
 * return findClass_(name, true);
 * }
 *
 * public Product2<byte[], CodeSource> remapClassold(String className, Callable<byte[]> byteSource, URLConnection connection) throws ClassNotFoundException {
 * try {
 * byte[] bytes = byteSource.call();
 * URL url;
 * CodeSigner[] signers;
 * if (connection instanceof JarURLConnection) {
 * url = ((JarURLConnection) connection).getJarFileURL();
 * signers = ((JarURLConnection) connection).getJarEntry().getCodeSigners();
 * } else {
 * url = connection.getURL();
 * signers = null;
 * }
 * return Product.of(bytes, new CodeSource(url, signers));
 * } catch (Exception e) {
 * e.printStackTrace();
 * throw new ClassNotFoundException(className, e);
 * }
 * }
 *
 * Class<?> findClass_(String name, boolean checkGlobal) throws ClassNotFoundException {
 *
 * Class<?> result = classes.get(name);
 *
 * if (result == null) {
 *
 * if (checkGlobal) {
 * result = loader.getClassByName(name);
 *
 * if (result != null) {
 * PluginDescriptionFile provider = ((PluginClassLoader) result.getClassLoader()).description;
 *
 * if (provider != description && !seenIllegalAccess.contains(provider.getName())
 * && !((SimplePluginManager) loader.server.getPluginManager()).isTransitiveDepend(description, provider)) {
 *
 * seenIllegalAccess.add(provider.getName());
 * if (plugin != null) {
 * plugin.getLogger().log(Level.WARNING, "Loaded class " + name + " from " + provider.getFullName() + " which is not a depend, softdepend or loadbefore of this plugin.");
 * } else {
 * // In case the bad access occurs on construction
 * loader.server.getLogger().log(Level.WARNING, "[{0}] Loaded class {1} from {2} which is not a depend, softdepend or loadbefore of this plugin.", new Object[]{description.getName(), name, provider.getFullName()});
 * }
 * }
 * }
 * }
 *
 * String path = name.replace('.', '/').concat(".class");
 * URL url = this.findResource(path);
 * JarEntry entry = jar.getJarEntry(path);
 *
 * if (url != null && entry != null && result == null) {
 * // System.out.println("REMAP NEED! Debug;");
 * URLConnection connection;
 * Callable<byte[]> byteSource;
 *
 * /*byte[] classBytes0;
 *
 * try (InputStream is = jar.getInputStream(entry)) {
 * classBytes0 = ByteStreams.toByteArray(is);
 * } catch (IOException ex) {
 * throw new ClassNotFoundException(name, ex);
 * }
 *
 * classBytes0 = loader.server.getUnsafe().processClass(description, path, classBytes0);
 * classBytes0 = PluginFixManager.injectPluginFix(name, classBytes0); // Mohist - Inject plugin fix
 *
 * byteSource = () -> {
 * return classBytes0;
 * };//
 *
 * try {
 * connection = url.openConnection();
 * connection.connect();
 * byteSource = () -> {
 * try (InputStream is = connection.getInputStream()) {
 * byte[] classBytes = ByteStreams.toByteArray(is);
 * classBytes = loader.server.getUnsafe().processClass(description, path, classBytes);
 * // classBytes = PluginFixManager.injectPluginFix(name, classBytes); // Mohist - Inject plugin fix
 *
 * return classBytes;
 * }
 * };
 * } catch (IOException e) {
 * throw new ClassNotFoundException(name, e);
 * }
 *
 * // Product2<byte[], CodeSource> classBytes = remapClass(name, byteSource, connection);
 * Product2<byte[], CodeSource> classBytes = this.getRemapper().remapClass(name, byteSource, connection);
 *
 *
 * int dot = name.lastIndexOf('.');
 * if (dot != -1) {
 * String pkgName = name.substring(0, dot);
 * if (getPackage(pkgName) == null) {
 * try {
 * if (manifest != null) {
 * definePackage(pkgName, manifest, this.url);
 * } else {
 * definePackage(pkgName, null, null, null, null, null, null, null);
 * }
 * } catch (IllegalArgumentException ex) {
 * if (getPackage(pkgName) == null) {
 * throw new IllegalStateException("Cannot find package " + pkgName);
 * }
 * }
 * }
 * }
 * //this.defineClass
 * CodeSource source = new CodeSource(url, entry.getCodeSigners());
 * result = defineClass(name, classBytes._1, 0, classBytes._1.length, source);
 *
 * File out = new File("C:\\Users\\isaia\\Documents\\fo\\" + name.replace('.', File.separatorChar) + ".class");
 * try {
 * Files.createDirectories(out.toPath().getParent());
 * Files.write(out.toPath(), classBytes._1);
 * } catch (IOException e) {
 * // TODO Auto-generated catch block
 * e.printStackTrace();
 * }
 * }
 *
 * if (result == null) {
 * result = super.findClass(name);
 * }
 *
 * if (result != null) {
 * loader.setClass(name, result);
 * classes.put(name, result);
 * }
 * }
 * return result;
 * }
 * @SuppressWarnings("deprecation") Class<?> findClass123(String name, boolean checkGlobal) throws ClassNotFoundException {
 * Class<?> result = classes.get(name);
 *
 * if (result == null) {
 * if (checkGlobal) {
 * result = loader.getClassByName(name);
 *
 * if (result != null) {
 * PluginDescriptionFile provider = ((PluginClassLoader) result.getClassLoader()).description;
 *
 * if (provider != description && !seenIllegalAccess.contains(provider.getName())
 * && !((SimplePluginManager) loader.server.getPluginManager()).isTransitiveDepend(description, provider)) {
 *
 * seenIllegalAccess.add(provider.getName());
 * if (plugin != null) {
 * plugin.getLogger().log(Level.WARNING, "Loaded class " + name + " from " + provider.getFullName() + " which is not a depend, softdepend or loadbefore of this plugin.");
 * } else {
 * // In case the bad access occurs on construction
 * loader.server.getLogger().log(Level.WARNING, "[{0}] Loaded class {1} from {2} which is not a depend, softdepend or loadbefore of this plugin.", new Object[]{description.getName(), name, provider.getFullName()});
 * }
 * }
 * }
 * }
 *
 * if (result == null) {
 * String path = name.replace('.', '/').concat(".class");
 * JarEntry entry = jar.getJarEntry(path);
 *
 * if (entry != null) {
 * byte[] classBytes;
 *
 * try (InputStream is = jar.getInputStream(entry)) {
 * classBytes = ByteStreams.toByteArray(is);
 * } catch (IOException ex) {
 * throw new ClassNotFoundException(name, ex);
 * }
 *
 * classBytes = loader.server.getUnsafe().processClass(description, path, classBytes);
 *
 * int dot = name.lastIndexOf('.');
 * if (dot != -1) {
 * String pkgName = name.substring(0, dot);
 * if (getPackage(pkgName) == null) {
 * try {
 * if (manifest != null)
 * definePackage(pkgName, manifest, url);
 * else definePackage(pkgName, null, null, null, null, null, null, null);
 * } catch (IllegalArgumentException ex) {
 * if (getPackage(pkgName) == null)
 * throw new IllegalStateException("Cannot find package " + pkgName);
 * }
 * }
 * }
 *
 *
 * // Product2<byte[], CodeSource> classBytes_ = this.getRemapper().remapClass(name, byteSource, connection);
 *
 * CodeSource source = new CodeSource(url, entry.getCodeSigners());
 * result = defineClass(name, classBytes, 0, classBytes.length, source);
 * }
 *
 * try {
 * if (result == null)
 * result = super.findClass(name);
 * } catch (ClassNotFoundException | NoClassDefFoundError e) {return null;}
 *
 * if (result != null)
 * loader.setClass(name, result);
 *
 * if (result != null)
 * classes.put(name, result);
 * }
 * }
 *
 * return result;
 * }
 * @Override public void close() throws IOException {
 * try {
 * super.close();
 * } finally {
 * jar.close();
 * }
 * }
 *
 * Set<String> getClasses() {
 * return classes.keySet();
 * }
 *
 * synchronized void initialize(JavaPlugin javaPlugin) {
 * //Validate.notNull(javaPlugin, "Initializing plugin cannot be null");
 * // Validate.isTrue(javaPlugin.getClass().getClassLoader() == this, "Cannot initialize plugin outside of this class loader");
 * if (this.plugin != null || this.pluginInit != null)
 * throw new IllegalArgumentException("Plugin already initialized", pluginState);
 *
 * pluginState = new IllegalStateException("Initial initialization");
 * this.pluginInit = javaPlugin;
 *
 * javaPlugin.init(loader, loader.server, description, dataFolder, file, this);
 * }
 * @Override public ClassLoaderRemapper getRemapper() {
 * if (remapper == null) {
 * MohistRemapper.init(McVersion.v1_19_4);
 * remapper = MohistRemapper.createClassLoaderRemapper(this);
 * }
 * return remapper;
 * }
 *
 * }
 *
 * A ClassLoader for plugins, to allow shared classes across multiple plugins
 *
 * public class PluginClassLoader extends URLClassLoader implements RemappingClassLoader {
 *
 * private final JavaPluginLoader loader;
 * private final Map<String, Class<?>> classes = new ConcurrentHashMap<String, Class<?>>();
 * private final PluginDescriptionFile description;
 * private final File dataFolder;
 * private final File file;
 * public final JarFile jar;
 * private final Manifest manifest;
 * private final URL url;
 * final JavaPlugin plugin;
 * private JavaPlugin pluginInit;
 * private IllegalStateException pluginState;
 * private final Set<String> seenIllegalAccess = Collections.newSetFromMap(new ConcurrentHashMap<>());
 *
 * static {
 * ClassLoader.registerAsParallelCapable();
 * }
 *
 * private ClassLoaderRemapper remapper;
 *
 * PluginClassLoader(final JavaPluginLoader loader, final ClassLoader parent, final PluginDescriptionFile description, final File dataFolder, final File file) throws IOException, InvalidPluginException, MalformedURLException {
 * super(new URL[] {file.toURI().toURL()}, parent);;
 * //Validate.notNull(loader, "Loader cannot be null");
 *
 * this.loader = loader;
 * this.description = description;
 * this.dataFolder = dataFolder;
 * this.file = file;
 * this.jar = new JarFile(file);
 * this.manifest = jar.getManifest();
 * this.url = file.toURI().toURL();
 *
 * try {
 * Class<?> jarClass;
 * try {
 * jarClass = Class.forName(description.getMain(), true, this);
 * } catch (ClassNotFoundException ex) {
 * throw new InvalidPluginException("Cannot find main class `" + description.getMain() + "'", ex);
 * }
 *
 * Class<? extends JavaPlugin> pluginClass;
 * try {
 * pluginClass = jarClass.asSubclass(JavaPlugin.class);
 * } catch (ClassCastException ex) {
 * throw new InvalidPluginException("main class `" + description.getMain() + "' does not extend JavaPlugin", ex);
 * }
 *
 * plugin = pluginClass.newInstance();
 * } catch (IllegalAccessException ex) {
 * throw new InvalidPluginException("No public constructor", ex);
 * } catch (InstantiationException ex) {
 * throw new InvalidPluginException("Abnormal plugin type", ex);
 * }
 * }
 * @Override public Class<?> loadClass(String name) throws ClassNotFoundException {
 * return super.loadClass(name);
 * }
 * @Override public URL getResource(String name) {
 * return findResource(name);
 * }
 * @Override public Enumeration<URL> getResources(String name) throws IOException {
 * return findResources(name);
 * }
 * @Override protected Class<?> findClass(String name) throws ClassNotFoundException {
 * return findClass_(name, true);
 * }
 *
 * public Product2<byte[], CodeSource> remapClassold(String className, Callable<byte[]> byteSource, URLConnection connection) throws ClassNotFoundException {
 * try {
 * byte[] bytes = byteSource.call();
 * URL url;
 * CodeSigner[] signers;
 * if (connection instanceof JarURLConnection) {
 * url = ((JarURLConnection) connection).getJarFileURL();
 * signers = ((JarURLConnection) connection).getJarEntry().getCodeSigners();
 * } else {
 * url = connection.getURL();
 * signers = null;
 * }
 * return Product.of(bytes, new CodeSource(url, signers));
 * } catch (Exception e) {
 * e.printStackTrace();
 * throw new ClassNotFoundException(className, e);
 * }
 * }
 *
 * Class<?> findClass_(String name, boolean checkGlobal) throws ClassNotFoundException {
 *
 * Class<?> result = classes.get(name);
 *
 * if (result == null) {
 *
 * if (checkGlobal) {
 * result = loader.getClassByName(name);
 *
 * if (result != null) {
 * PluginDescriptionFile provider = ((PluginClassLoader) result.getClassLoader()).description;
 *
 * if (provider != description && !seenIllegalAccess.contains(provider.getName())
 * && !((SimplePluginManager) loader.server.getPluginManager()).isTransitiveDepend(description, provider)) {
 *
 * seenIllegalAccess.add(provider.getName());
 * if (plugin != null) {
 * plugin.getLogger().log(Level.WARNING, "Loaded class " + name + " from " + provider.getFullName() + " which is not a depend, softdepend or loadbefore of this plugin.");
 * } else {
 * // In case the bad access occurs on construction
 * loader.server.getLogger().log(Level.WARNING, "[{0}] Loaded class {1} from {2} which is not a depend, softdepend or loadbefore of this plugin.", new Object[]{description.getName(), name, provider.getFullName()});
 * }
 * }
 * }
 * }
 *
 * String path = name.replace('.', '/').concat(".class");
 * URL url = this.findResource(path);
 * JarEntry entry = jar.getJarEntry(path);
 *
 * if (url != null && entry != null && result == null) {
 * // System.out.println("REMAP NEED! Debug;");
 * URLConnection connection;
 * Callable<byte[]> byteSource;
 *
 * /*byte[] classBytes0;
 *
 * try (InputStream is = jar.getInputStream(entry)) {
 * classBytes0 = ByteStreams.toByteArray(is);
 * } catch (IOException ex) {
 * throw new ClassNotFoundException(name, ex);
 * }
 *
 * classBytes0 = loader.server.getUnsafe().processClass(description, path, classBytes0);
 * classBytes0 = PluginFixManager.injectPluginFix(name, classBytes0); // Mohist - Inject plugin fix
 *
 * byteSource = () -> {
 * return classBytes0;
 * };//
 *
 * try {
 * connection = url.openConnection();
 * connection.connect();
 * byteSource = () -> {
 * try (InputStream is = connection.getInputStream()) {
 * byte[] classBytes = ByteStreams.toByteArray(is);
 * classBytes = loader.server.getUnsafe().processClass(description, path, classBytes);
 * // classBytes = PluginFixManager.injectPluginFix(name, classBytes); // Mohist - Inject plugin fix
 *
 * return classBytes;
 * }
 * };
 * } catch (IOException e) {
 * throw new ClassNotFoundException(name, e);
 * }
 *
 * // Product2<byte[], CodeSource> classBytes = remapClass(name, byteSource, connection);
 * Product2<byte[], CodeSource> classBytes = this.getRemapper().remapClass(name, byteSource, connection);
 *
 *
 * int dot = name.lastIndexOf('.');
 * if (dot != -1) {
 * String pkgName = name.substring(0, dot);
 * if (getPackage(pkgName) == null) {
 * try {
 * if (manifest != null) {
 * definePackage(pkgName, manifest, this.url);
 * } else {
 * definePackage(pkgName, null, null, null, null, null, null, null);
 * }
 * } catch (IllegalArgumentException ex) {
 * if (getPackage(pkgName) == null) {
 * throw new IllegalStateException("Cannot find package " + pkgName);
 * }
 * }
 * }
 * }
 * //this.defineClass
 * CodeSource source = new CodeSource(url, entry.getCodeSigners());
 * result = defineClass(name, classBytes._1, 0, classBytes._1.length, source);
 *
 * File out = new File("C:\\Users\\isaia\\Documents\\fo\\" + name.replace('.', File.separatorChar) + ".class");
 * try {
 * Files.createDirectories(out.toPath().getParent());
 * Files.write(out.toPath(), classBytes._1);
 * } catch (IOException e) {
 * // TODO Auto-generated catch block
 * e.printStackTrace();
 * }
 * }
 *
 * if (result == null) {
 * result = super.findClass(name);
 * }
 *
 * if (result != null) {
 * loader.setClass(name, result);
 * classes.put(name, result);
 * }
 * }
 * return result;
 * }
 * @SuppressWarnings("deprecation") Class<?> findClass123(String name, boolean checkGlobal) throws ClassNotFoundException {
 * Class<?> result = classes.get(name);
 *
 * if (result == null) {
 * if (checkGlobal) {
 * result = loader.getClassByName(name);
 *
 * if (result != null) {
 * PluginDescriptionFile provider = ((PluginClassLoader) result.getClassLoader()).description;
 *
 * if (provider != description && !seenIllegalAccess.contains(provider.getName())
 * && !((SimplePluginManager) loader.server.getPluginManager()).isTransitiveDepend(description, provider)) {
 *
 * seenIllegalAccess.add(provider.getName());
 * if (plugin != null) {
 * plugin.getLogger().log(Level.WARNING, "Loaded class " + name + " from " + provider.getFullName() + " which is not a depend, softdepend or loadbefore of this plugin.");
 * } else {
 * // In case the bad access occurs on construction
 * loader.server.getLogger().log(Level.WARNING, "[{0}] Loaded class {1} from {2} which is not a depend, softdepend or loadbefore of this plugin.", new Object[]{description.getName(), name, provider.getFullName()});
 * }
 * }
 * }
 * }
 *
 * if (result == null) {
 * String path = name.replace('.', '/').concat(".class");
 * JarEntry entry = jar.getJarEntry(path);
 *
 * if (entry != null) {
 * byte[] classBytes;
 *
 * try (InputStream is = jar.getInputStream(entry)) {
 * classBytes = ByteStreams.toByteArray(is);
 * } catch (IOException ex) {
 * throw new ClassNotFoundException(name, ex);
 * }
 *
 * classBytes = loader.server.getUnsafe().processClass(description, path, classBytes);
 *
 * int dot = name.lastIndexOf('.');
 * if (dot != -1) {
 * String pkgName = name.substring(0, dot);
 * if (getPackage(pkgName) == null) {
 * try {
 * if (manifest != null)
 * definePackage(pkgName, manifest, url);
 * else definePackage(pkgName, null, null, null, null, null, null, null);
 * } catch (IllegalArgumentException ex) {
 * if (getPackage(pkgName) == null)
 * throw new IllegalStateException("Cannot find package " + pkgName);
 * }
 * }
 * }
 *
 *
 * // Product2<byte[], CodeSource> classBytes_ = this.getRemapper().remapClass(name, byteSource, connection);
 *
 * CodeSource source = new CodeSource(url, entry.getCodeSigners());
 * result = defineClass(name, classBytes, 0, classBytes.length, source);
 * }
 *
 * try {
 * if (result == null)
 * result = super.findClass(name);
 * } catch (ClassNotFoundException | NoClassDefFoundError e) {return null;}
 *
 * if (result != null)
 * loader.setClass(name, result);
 *
 * if (result != null)
 * classes.put(name, result);
 * }
 * }
 *
 * return result;
 * }
 * @Override public void close() throws IOException {
 * try {
 * super.close();
 * } finally {
 * jar.close();
 * }
 * }
 *
 * Set<String> getClasses() {
 * return classes.keySet();
 * }
 *
 * synchronized void initialize(JavaPlugin javaPlugin) {
 * //Validate.notNull(javaPlugin, "Initializing plugin cannot be null");
 * // Validate.isTrue(javaPlugin.getClass().getClassLoader() == this, "Cannot initialize plugin outside of this class loader");
 * if (this.plugin != null || this.pluginInit != null)
 * throw new IllegalArgumentException("Plugin already initialized", pluginState);
 *
 * pluginState = new IllegalStateException("Initial initialization");
 * this.pluginInit = javaPlugin;
 *
 * javaPlugin.init(loader, loader.server, description, dataFolder, file, this);
 * }
 * @Override public ClassLoaderRemapper getRemapper() {
 * if (remapper == null) {
 * MohistRemapper.init(McVersion.v1_19_4);
 * remapper = MohistRemapper.createClassLoaderRemapper(this);
 * }
 * return remapper;
 * }
 *
 * }
 *
 * A ClassLoader for plugins, to allow shared classes across multiple plugins
 *
 * public class PluginClassLoader extends URLClassLoader implements RemappingClassLoader {
 *
 * private final JavaPluginLoader loader;
 * private final Map<String, Class<?>> classes = new ConcurrentHashMap<String, Class<?>>();
 * private final PluginDescriptionFile description;
 * private final File dataFolder;
 * private final File file;
 * public final JarFile jar;
 * private final Manifest manifest;
 * private final URL url;
 * final JavaPlugin plugin;
 * private JavaPlugin pluginInit;
 * private IllegalStateException pluginState;
 * private final Set<String> seenIllegalAccess = Collections.newSetFromMap(new ConcurrentHashMap<>());
 *
 * static {
 * ClassLoader.registerAsParallelCapable();
 * }
 *
 * private ClassLoaderRemapper remapper;
 *
 * PluginClassLoader(final JavaPluginLoader loader, final ClassLoader parent, final PluginDescriptionFile description, final File dataFolder, final File file) throws IOException, InvalidPluginException, MalformedURLException {
 * super(new URL[] {file.toURI().toURL()}, parent);;
 * //Validate.notNull(loader, "Loader cannot be null");
 *
 * this.loader = loader;
 * this.description = description;
 * this.dataFolder = dataFolder;
 * this.file = file;
 * this.jar = new JarFile(file);
 * this.manifest = jar.getManifest();
 * this.url = file.toURI().toURL();
 *
 * try {
 * Class<?> jarClass;
 * try {
 * jarClass = Class.forName(description.getMain(), true, this);
 * } catch (ClassNotFoundException ex) {
 * throw new InvalidPluginException("Cannot find main class `" + description.getMain() + "'", ex);
 * }
 *
 * Class<? extends JavaPlugin> pluginClass;
 * try {
 * pluginClass = jarClass.asSubclass(JavaPlugin.class);
 * } catch (ClassCastException ex) {
 * throw new InvalidPluginException("main class `" + description.getMain() + "' does not extend JavaPlugin", ex);
 * }
 *
 * plugin = pluginClass.newInstance();
 * } catch (IllegalAccessException ex) {
 * throw new InvalidPluginException("No public constructor", ex);
 * } catch (InstantiationException ex) {
 * throw new InvalidPluginException("Abnormal plugin type", ex);
 * }
 * }
 * @Override public Class<?> loadClass(String name) throws ClassNotFoundException {
 * return super.loadClass(name);
 * }
 * @Override public URL getResource(String name) {
 * return findResource(name);
 * }
 * @Override public Enumeration<URL> getResources(String name) throws IOException {
 * return findResources(name);
 * }
 * @Override protected Class<?> findClass(String name) throws ClassNotFoundException {
 * return findClass_(name, true);
 * }
 *
 * public Product2<byte[], CodeSource> remapClassold(String className, Callable<byte[]> byteSource, URLConnection connection) throws ClassNotFoundException {
 * try {
 * byte[] bytes = byteSource.call();
 * URL url;
 * CodeSigner[] signers;
 * if (connection instanceof JarURLConnection) {
 * url = ((JarURLConnection) connection).getJarFileURL();
 * signers = ((JarURLConnection) connection).getJarEntry().getCodeSigners();
 * } else {
 * url = connection.getURL();
 * signers = null;
 * }
 * return Product.of(bytes, new CodeSource(url, signers));
 * } catch (Exception e) {
 * e.printStackTrace();
 * throw new ClassNotFoundException(className, e);
 * }
 * }
 *
 * Class<?> findClass_(String name, boolean checkGlobal) throws ClassNotFoundException {
 *
 * Class<?> result = classes.get(name);
 *
 * if (result == null) {
 *
 * if (checkGlobal) {
 * result = loader.getClassByName(name);
 *
 * if (result != null) {
 * PluginDescriptionFile provider = ((PluginClassLoader) result.getClassLoader()).description;
 *
 * if (provider != description && !seenIllegalAccess.contains(provider.getName())
 * && !((SimplePluginManager) loader.server.getPluginManager()).isTransitiveDepend(description, provider)) {
 *
 * seenIllegalAccess.add(provider.getName());
 * if (plugin != null) {
 * plugin.getLogger().log(Level.WARNING, "Loaded class " + name + " from " + provider.getFullName() + " which is not a depend, softdepend or loadbefore of this plugin.");
 * } else {
 * // In case the bad access occurs on construction
 * loader.server.getLogger().log(Level.WARNING, "[{0}] Loaded class {1} from {2} which is not a depend, softdepend or loadbefore of this plugin.", new Object[]{description.getName(), name, provider.getFullName()});
 * }
 * }
 * }
 * }
 *
 * String path = name.replace('.', '/').concat(".class");
 * URL url = this.findResource(path);
 * JarEntry entry = jar.getJarEntry(path);
 *
 * if (url != null && entry != null && result == null) {
 * // System.out.println("REMAP NEED! Debug;");
 * URLConnection connection;
 * Callable<byte[]> byteSource;
 *
 * /*byte[] classBytes0;
 *
 * try (InputStream is = jar.getInputStream(entry)) {
 * classBytes0 = ByteStreams.toByteArray(is);
 * } catch (IOException ex) {
 * throw new ClassNotFoundException(name, ex);
 * }
 *
 * classBytes0 = loader.server.getUnsafe().processClass(description, path, classBytes0);
 * classBytes0 = PluginFixManager.injectPluginFix(name, classBytes0); // Mohist - Inject plugin fix
 *
 * byteSource = () -> {
 * return classBytes0;
 * };//
 *
 * try {
 * connection = url.openConnection();
 * connection.connect();
 * byteSource = () -> {
 * try (InputStream is = connection.getInputStream()) {
 * byte[] classBytes = ByteStreams.toByteArray(is);
 * classBytes = loader.server.getUnsafe().processClass(description, path, classBytes);
 * // classBytes = PluginFixManager.injectPluginFix(name, classBytes); // Mohist - Inject plugin fix
 *
 * return classBytes;
 * }
 * };
 * } catch (IOException e) {
 * throw new ClassNotFoundException(name, e);
 * }
 *
 * // Product2<byte[], CodeSource> classBytes = remapClass(name, byteSource, connection);
 * Product2<byte[], CodeSource> classBytes = this.getRemapper().remapClass(name, byteSource, connection);
 *
 *
 * int dot = name.lastIndexOf('.');
 * if (dot != -1) {
 * String pkgName = name.substring(0, dot);
 * if (getPackage(pkgName) == null) {
 * try {
 * if (manifest != null) {
 * definePackage(pkgName, manifest, this.url);
 * } else {
 * definePackage(pkgName, null, null, null, null, null, null, null);
 * }
 * } catch (IllegalArgumentException ex) {
 * if (getPackage(pkgName) == null) {
 * throw new IllegalStateException("Cannot find package " + pkgName);
 * }
 * }
 * }
 * }
 * //this.defineClass
 * CodeSource source = new CodeSource(url, entry.getCodeSigners());
 * result = defineClass(name, classBytes._1, 0, classBytes._1.length, source);
 *
 * File out = new File("C:\\Users\\isaia\\Documents\\fo\\" + name.replace('.', File.separatorChar) + ".class");
 * try {
 * Files.createDirectories(out.toPath().getParent());
 * Files.write(out.toPath(), classBytes._1);
 * } catch (IOException e) {
 * // TODO Auto-generated catch block
 * e.printStackTrace();
 * }
 * }
 *
 * if (result == null) {
 * result = super.findClass(name);
 * }
 *
 * if (result != null) {
 * loader.setClass(name, result);
 * classes.put(name, result);
 * }
 * }
 * return result;
 * }
 * @SuppressWarnings("deprecation") Class<?> findClass123(String name, boolean checkGlobal) throws ClassNotFoundException {
 * Class<?> result = classes.get(name);
 *
 * if (result == null) {
 * if (checkGlobal) {
 * result = loader.getClassByName(name);
 *
 * if (result != null) {
 * PluginDescriptionFile provider = ((PluginClassLoader) result.getClassLoader()).description;
 *
 * if (provider != description && !seenIllegalAccess.contains(provider.getName())
 * && !((SimplePluginManager) loader.server.getPluginManager()).isTransitiveDepend(description, provider)) {
 *
 * seenIllegalAccess.add(provider.getName());
 * if (plugin != null) {
 * plugin.getLogger().log(Level.WARNING, "Loaded class " + name + " from " + provider.getFullName() + " which is not a depend, softdepend or loadbefore of this plugin.");
 * } else {
 * // In case the bad access occurs on construction
 * loader.server.getLogger().log(Level.WARNING, "[{0}] Loaded class {1} from {2} which is not a depend, softdepend or loadbefore of this plugin.", new Object[]{description.getName(), name, provider.getFullName()});
 * }
 * }
 * }
 * }
 *
 * if (result == null) {
 * String path = name.replace('.', '/').concat(".class");
 * JarEntry entry = jar.getJarEntry(path);
 *
 * if (entry != null) {
 * byte[] classBytes;
 *
 * try (InputStream is = jar.getInputStream(entry)) {
 * classBytes = ByteStreams.toByteArray(is);
 * } catch (IOException ex) {
 * throw new ClassNotFoundException(name, ex);
 * }
 *
 * classBytes = loader.server.getUnsafe().processClass(description, path, classBytes);
 *
 * int dot = name.lastIndexOf('.');
 * if (dot != -1) {
 * String pkgName = name.substring(0, dot);
 * if (getPackage(pkgName) == null) {
 * try {
 * if (manifest != null)
 * definePackage(pkgName, manifest, url);
 * else definePackage(pkgName, null, null, null, null, null, null, null);
 * } catch (IllegalArgumentException ex) {
 * if (getPackage(pkgName) == null)
 * throw new IllegalStateException("Cannot find package " + pkgName);
 * }
 * }
 * }
 *
 *
 * // Product2<byte[], CodeSource> classBytes_ = this.getRemapper().remapClass(name, byteSource, connection);
 *
 * CodeSource source = new CodeSource(url, entry.getCodeSigners());
 * result = defineClass(name, classBytes, 0, classBytes.length, source);
 * }
 *
 * try {
 * if (result == null)
 * result = super.findClass(name);
 * } catch (ClassNotFoundException | NoClassDefFoundError e) {return null;}
 *
 * if (result != null)
 * loader.setClass(name, result);
 *
 * if (result != null)
 * classes.put(name, result);
 * }
 * }
 *
 * return result;
 * }
 * @Override public void close() throws IOException {
 * try {
 * super.close();
 * } finally {
 * jar.close();
 * }
 * }
 *
 * Set<String> getClasses() {
 * return classes.keySet();
 * }
 *
 * synchronized void initialize(JavaPlugin javaPlugin) {
 * //Validate.notNull(javaPlugin, "Initializing plugin cannot be null");
 * // Validate.isTrue(javaPlugin.getClass().getClassLoader() == this, "Cannot initialize plugin outside of this class loader");
 * if (this.plugin != null || this.pluginInit != null)
 * throw new IllegalArgumentException("Plugin already initialized", pluginState);
 *
 * pluginState = new IllegalStateException("Initial initialization");
 * this.pluginInit = javaPlugin;
 *
 * javaPlugin.init(loader, loader.server, description, dataFolder, file, this);
 * }
 * @Override public ClassLoaderRemapper getRemapper() {
 * if (remapper == null) {
 * MohistRemapper.init(McVersion.v1_19_4);
 * remapper = MohistRemapper.createClassLoaderRemapper(this);
 * }
 * return remapper;
 * }
 *
 * }
 *
 * A ClassLoader for plugins, to allow shared classes across multiple plugins
 *
 * public class PluginClassLoader extends URLClassLoader implements RemappingClassLoader {
 *
 * private final JavaPluginLoader loader;
 * private final Map<String, Class<?>> classes = new ConcurrentHashMap<String, Class<?>>();
 * private final PluginDescriptionFile description;
 * private final File dataFolder;
 * private final File file;
 * public final JarFile jar;
 * private final Manifest manifest;
 * private final URL url;
 * final JavaPlugin plugin;
 * private JavaPlugin pluginInit;
 * private IllegalStateException pluginState;
 * private final Set<String> seenIllegalAccess = Collections.newSetFromMap(new ConcurrentHashMap<>());
 *
 * static {
 * ClassLoader.registerAsParallelCapable();
 * }
 *
 * private ClassLoaderRemapper remapper;
 *
 * PluginClassLoader(final JavaPluginLoader loader, final ClassLoader parent, final PluginDescriptionFile description, final File dataFolder, final File file) throws IOException, InvalidPluginException, MalformedURLException {
 * super(new URL[] {file.toURI().toURL()}, parent);;
 * //Validate.notNull(loader, "Loader cannot be null");
 *
 * this.loader = loader;
 * this.description = description;
 * this.dataFolder = dataFolder;
 * this.file = file;
 * this.jar = new JarFile(file);
 * this.manifest = jar.getManifest();
 * this.url = file.toURI().toURL();
 *
 * try {
 * Class<?> jarClass;
 * try {
 * jarClass = Class.forName(description.getMain(), true, this);
 * } catch (ClassNotFoundException ex) {
 * throw new InvalidPluginException("Cannot find main class `" + description.getMain() + "'", ex);
 * }
 *
 * Class<? extends JavaPlugin> pluginClass;
 * try {
 * pluginClass = jarClass.asSubclass(JavaPlugin.class);
 * } catch (ClassCastException ex) {
 * throw new InvalidPluginException("main class `" + description.getMain() + "' does not extend JavaPlugin", ex);
 * }
 *
 * plugin = pluginClass.newInstance();
 * } catch (IllegalAccessException ex) {
 * throw new InvalidPluginException("No public constructor", ex);
 * } catch (InstantiationException ex) {
 * throw new InvalidPluginException("Abnormal plugin type", ex);
 * }
 * }
 * @Override public Class<?> loadClass(String name) throws ClassNotFoundException {
 * return super.loadClass(name);
 * }
 * @Override public URL getResource(String name) {
 * return findResource(name);
 * }
 * @Override public Enumeration<URL> getResources(String name) throws IOException {
 * return findResources(name);
 * }
 * @Override protected Class<?> findClass(String name) throws ClassNotFoundException {
 * return findClass_(name, true);
 * }
 *
 * public Product2<byte[], CodeSource> remapClassold(String className, Callable<byte[]> byteSource, URLConnection connection) throws ClassNotFoundException {
 * try {
 * byte[] bytes = byteSource.call();
 * URL url;
 * CodeSigner[] signers;
 * if (connection instanceof JarURLConnection) {
 * url = ((JarURLConnection) connection).getJarFileURL();
 * signers = ((JarURLConnection) connection).getJarEntry().getCodeSigners();
 * } else {
 * url = connection.getURL();
 * signers = null;
 * }
 * return Product.of(bytes, new CodeSource(url, signers));
 * } catch (Exception e) {
 * e.printStackTrace();
 * throw new ClassNotFoundException(className, e);
 * }
 * }
 *
 * Class<?> findClass_(String name, boolean checkGlobal) throws ClassNotFoundException {
 *
 * Class<?> result = classes.get(name);
 *
 * if (result == null) {
 *
 * if (checkGlobal) {
 * result = loader.getClassByName(name);
 *
 * if (result != null) {
 * PluginDescriptionFile provider = ((PluginClassLoader) result.getClassLoader()).description;
 *
 * if (provider != description && !seenIllegalAccess.contains(provider.getName())
 * && !((SimplePluginManager) loader.server.getPluginManager()).isTransitiveDepend(description, provider)) {
 *
 * seenIllegalAccess.add(provider.getName());
 * if (plugin != null) {
 * plugin.getLogger().log(Level.WARNING, "Loaded class " + name + " from " + provider.getFullName() + " which is not a depend, softdepend or loadbefore of this plugin.");
 * } else {
 * // In case the bad access occurs on construction
 * loader.server.getLogger().log(Level.WARNING, "[{0}] Loaded class {1} from {2} which is not a depend, softdepend or loadbefore of this plugin.", new Object[]{description.getName(), name, provider.getFullName()});
 * }
 * }
 * }
 * }
 *
 * String path = name.replace('.', '/').concat(".class");
 * URL url = this.findResource(path);
 * JarEntry entry = jar.getJarEntry(path);
 *
 * if (url != null && entry != null && result == null) {
 * // System.out.println("REMAP NEED! Debug;");
 * URLConnection connection;
 * Callable<byte[]> byteSource;
 *
 * /*byte[] classBytes0;
 *
 * try (InputStream is = jar.getInputStream(entry)) {
 * classBytes0 = ByteStreams.toByteArray(is);
 * } catch (IOException ex) {
 * throw new ClassNotFoundException(name, ex);
 * }
 *
 * classBytes0 = loader.server.getUnsafe().processClass(description, path, classBytes0);
 * classBytes0 = PluginFixManager.injectPluginFix(name, classBytes0); // Mohist - Inject plugin fix
 *
 * byteSource = () -> {
 * return classBytes0;
 * };//
 *
 * try {
 * connection = url.openConnection();
 * connection.connect();
 * byteSource = () -> {
 * try (InputStream is = connection.getInputStream()) {
 * byte[] classBytes = ByteStreams.toByteArray(is);
 * classBytes = loader.server.getUnsafe().processClass(description, path, classBytes);
 * // classBytes = PluginFixManager.injectPluginFix(name, classBytes); // Mohist - Inject plugin fix
 *
 * return classBytes;
 * }
 * };
 * } catch (IOException e) {
 * throw new ClassNotFoundException(name, e);
 * }
 *
 * // Product2<byte[], CodeSource> classBytes = remapClass(name, byteSource, connection);
 * Product2<byte[], CodeSource> classBytes = this.getRemapper().remapClass(name, byteSource, connection);
 *
 *
 * int dot = name.lastIndexOf('.');
 * if (dot != -1) {
 * String pkgName = name.substring(0, dot);
 * if (getPackage(pkgName) == null) {
 * try {
 * if (manifest != null) {
 * definePackage(pkgName, manifest, this.url);
 * } else {
 * definePackage(pkgName, null, null, null, null, null, null, null);
 * }
 * } catch (IllegalArgumentException ex) {
 * if (getPackage(pkgName) == null) {
 * throw new IllegalStateException("Cannot find package " + pkgName);
 * }
 * }
 * }
 * }
 * //this.defineClass
 * CodeSource source = new CodeSource(url, entry.getCodeSigners());
 * result = defineClass(name, classBytes._1, 0, classBytes._1.length, source);
 *
 * File out = new File("C:\\Users\\isaia\\Documents\\fo\\" + name.replace('.', File.separatorChar) + ".class");
 * try {
 * Files.createDirectories(out.toPath().getParent());
 * Files.write(out.toPath(), classBytes._1);
 * } catch (IOException e) {
 * // TODO Auto-generated catch block
 * e.printStackTrace();
 * }
 * }
 *
 * if (result == null) {
 * result = super.findClass(name);
 * }
 *
 * if (result != null) {
 * loader.setClass(name, result);
 * classes.put(name, result);
 * }
 * }
 * return result;
 * }
 * @SuppressWarnings("deprecation") Class<?> findClass123(String name, boolean checkGlobal) throws ClassNotFoundException {
 * Class<?> result = classes.get(name);
 *
 * if (result == null) {
 * if (checkGlobal) {
 * result = loader.getClassByName(name);
 *
 * if (result != null) {
 * PluginDescriptionFile provider = ((PluginClassLoader) result.getClassLoader()).description;
 *
 * if (provider != description && !seenIllegalAccess.contains(provider.getName())
 * && !((SimplePluginManager) loader.server.getPluginManager()).isTransitiveDepend(description, provider)) {
 *
 * seenIllegalAccess.add(provider.getName());
 * if (plugin != null) {
 * plugin.getLogger().log(Level.WARNING, "Loaded class " + name + " from " + provider.getFullName() + " which is not a depend, softdepend or loadbefore of this plugin.");
 * } else {
 * // In case the bad access occurs on construction
 * loader.server.getLogger().log(Level.WARNING, "[{0}] Loaded class {1} from {2} which is not a depend, softdepend or loadbefore of this plugin.", new Object[]{description.getName(), name, provider.getFullName()});
 * }
 * }
 * }
 * }
 *
 * if (result == null) {
 * String path = name.replace('.', '/').concat(".class");
 * JarEntry entry = jar.getJarEntry(path);
 *
 * if (entry != null) {
 * byte[] classBytes;
 *
 * try (InputStream is = jar.getInputStream(entry)) {
 * classBytes = ByteStreams.toByteArray(is);
 * } catch (IOException ex) {
 * throw new ClassNotFoundException(name, ex);
 * }
 *
 * classBytes = loader.server.getUnsafe().processClass(description, path, classBytes);
 *
 * int dot = name.lastIndexOf('.');
 * if (dot != -1) {
 * String pkgName = name.substring(0, dot);
 * if (getPackage(pkgName) == null) {
 * try {
 * if (manifest != null)
 * definePackage(pkgName, manifest, url);
 * else definePackage(pkgName, null, null, null, null, null, null, null);
 * } catch (IllegalArgumentException ex) {
 * if (getPackage(pkgName) == null)
 * throw new IllegalStateException("Cannot find package " + pkgName);
 * }
 * }
 * }
 *
 *
 * // Product2<byte[], CodeSource> classBytes_ = this.getRemapper().remapClass(name, byteSource, connection);
 *
 * CodeSource source = new CodeSource(url, entry.getCodeSigners());
 * result = defineClass(name, classBytes, 0, classBytes.length, source);
 * }
 *
 * try {
 * if (result == null)
 * result = super.findClass(name);
 * } catch (ClassNotFoundException | NoClassDefFoundError e) {return null;}
 *
 * if (result != null)
 * loader.setClass(name, result);
 *
 * if (result != null)
 * classes.put(name, result);
 * }
 * }
 *
 * return result;
 * }
 * @Override public void close() throws IOException {
 * try {
 * super.close();
 * } finally {
 * jar.close();
 * }
 * }
 *
 * Set<String> getClasses() {
 * return classes.keySet();
 * }
 *
 * synchronized void initialize(JavaPlugin javaPlugin) {
 * //Validate.notNull(javaPlugin, "Initializing plugin cannot be null");
 * // Validate.isTrue(javaPlugin.getClass().getClassLoader() == this, "Cannot initialize plugin outside of this class loader");
 * if (this.plugin != null || this.pluginInit != null)
 * throw new IllegalArgumentException("Plugin already initialized", pluginState);
 *
 * pluginState = new IllegalStateException("Initial initialization");
 * this.pluginInit = javaPlugin;
 *
 * javaPlugin.init(loader, loader.server, description, dataFolder, file, this);
 * }
 * @Override public ClassLoaderRemapper getRemapper() {
 * if (remapper == null) {
 * MohistRemapper.init(McVersion.v1_19_4);
 * remapper = MohistRemapper.createClassLoaderRemapper(this);
 * }
 * return remapper;
 * }
 *
 * }
 */
/*
public final class PluginClassLoader extends URLClassLoader implements RemappingClassLoader {
    public JavaPlugin getPlugin() { return plugin; } // Spigot
    private final JavaPluginLoader loader;
    final Map<String, Class<?>> classes = new ConcurrentHashMap<String, Class<?>>();
    private final PluginDescriptionFile description;
    private final File dataFolder;
    private final File file;
    private final JarFile jar;
    private final Manifest manifest;
    private final URL url;
    // private final ClassLoader libraryLoader;
    final JavaPlugin plugin;
    private JavaPlugin pluginInit;
    private IllegalStateException pluginState;
    private final Set<String> seenIllegalAccess = Collections.newSetFromMap(new ConcurrentHashMap<>());

    static {
        ClassLoader.registerAsParallelCapable();
    }

    private ClassLoaderRemapper remapper;

    PluginClassLoader(@NotNull final JavaPluginLoader loader, @Nullable final ClassLoader parent, @NotNull final PluginDescriptionFile description, @NotNull final File dataFolder, @NotNull final File file) throws IOException, InvalidPluginException, MalformedURLException {
        super(new URL[] {file.toURI().toURL()}, parent);
        Preconditions.checkArgument(loader != null, "Loader cannot be null");

        this.loader = loader;
        this.description = description;
        this.dataFolder = dataFolder;
        this.file = file;
        this.jar = new JarFile(file);
        this.manifest = jar.getManifest();
        this.url = file.toURI().toURL();
        // this.libraryLoader = libraryLoader;

        try {
            Class<?> jarClass;
            try {
                jarClass = Class.forName(description.getMain(), true, this);
            } catch (ClassNotFoundException ex) {
                throw new InvalidPluginException("Cannot find main class `" + description.getMain() + "'", ex);
            }

            Class<? extends JavaPlugin> pluginClass;
            try {
                pluginClass = jarClass.asSubclass(JavaPlugin.class);
            } catch (ClassCastException ex) {
                throw new InvalidPluginException("main class `" + description.getMain() + "' does not extend JavaPlugin", ex);
            }

            plugin = pluginClass.newInstance();
        } catch (IllegalAccessException ex) {
            throw new InvalidPluginException("No public constructor", ex);
        } catch (InstantiationException ex) {
            throw new InvalidPluginException("Abnormal plugin type", ex);
        }
    }

    @Override
    public URL getResource(String name) {
        return findResource(name);
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        return findResources(name);
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        return loadClass0(name, resolve, true, true);
    }

    Class<?> loadClass0(@NotNull String name, boolean resolve, boolean checkGlobal, boolean checkLibraries) throws ClassNotFoundException {
        try {
            Class<?> result = super.loadClass(name, resolve);

            // SPIGOT-6749: Library classes will appear in the above, but we don't want to return them to other plugins
            //if (checkGlobal || result.getClassLoader() == this) {
                return result;
            //}
        } catch (ClassNotFoundException ex) {
        }

        if (checkGlobal) {
            // This ignores the libraries of other plugins, unless they are transitive dependencies.
            Class<?> result = loader.getClassByName(name, resolve, description);

            if (result != null) {
                // If the class was loaded from a library instead of a PluginClassLoader, we can assume that its associated plugin is a transitive dependency and can therefore skip this check.
                if (result.getClassLoader() instanceof PluginClassLoader) {
                    PluginDescriptionFile provider = ((PluginClassLoader) result.getClassLoader()).description;

                    if (provider != description
                            && !seenIllegalAccess.contains(provider.getName())
                            && !((SimplePluginManager) loader.server.getPluginManager()).isTransitiveDepend(description, provider)) {

                        seenIllegalAccess.add(provider.getName());
                        if (plugin != null) {
                            plugin.getLogger().log(Level.WARNING, "Loaded class {0} from {1} which is not a depend or softdepend of this plugin.", new Object[]{name, provider.getFullName()});
                        } else {
                            // In case the bad access occurs on construction
                            loader.server.getLogger().log(Level.WARNING, "[{0}] Loaded class {1} from {2} which is not a depend or softdepend of this plugin.", new Object[]{description.getName(), name, provider.getFullName()});
                        }
                    }
                }

                return result;
            }
        }

        //return super.loadClass(name);
        throw new ClassNotFoundException(name);
    }

    @Override
    public Class<?> findClass(String name) throws ClassNotFoundException {
        return findClass(name, true);
    }

    Class<?> findClass(String name, boolean checkGlobal) throws ClassNotFoundException {

        Class<?> result = classes.get(name);

        if (result == null) {
            String path = name.replace('.', '/').concat(".class");
            URL url = this.findResource(path);
            JarEntry entry = jar.getJarEntry(path);

            if (url != null && entry != null) {

                URLConnection connection;
                Callable<byte[]> byteSource;
                try {
                    connection = url.openConnection();
                    connection.connect();
                    byteSource = () -> {
                        try (InputStream is = connection.getInputStream()) {
                            byte[] classBytes = ByteStreams.toByteArray(is);
                            classBytes = loader.server.getUnsafe().processClass(description, path, classBytes);
                            classBytes = PluginFixManager.injectPluginFix(name, classBytes); // Mohist - Inject plugin fix

                            return classBytes;
                        }
                    };
                } catch (IOException e) {
                    throw new ClassNotFoundException(name, e);
                }

                Product2<byte[], CodeSource> classBytes = this.getRemapper().remapClass(name, byteSource, connection);

                int dot = name.lastIndexOf('.');
                if (dot != -1) {
                    String pkgName = name.substring(0, dot);
                    if (getPackage(pkgName) == null) {
                        try {
                            if (manifest != null) {
                                definePackage(pkgName, manifest, this.url);
                            } else {
                                definePackage(pkgName, null, null, null, null, null, null, null);
                            }
                        } catch (IllegalArgumentException ex) {
                            if (getPackage(pkgName) == null) {
                                throw new IllegalStateException("Cannot find package " + pkgName);
                            }
                        }
                    }
                }

                result = defineClass(name, classBytes._1, 0, classBytes._1.length, classBytes._2);
            }

            if (result == null) {
                result = super.findClass(name);
            }

            loader.setClass(name, result);
            classes.put(name, result);
        }
        return result;
    }

    @Override
    public void close() throws IOException {
        try {
            super.close();
        } finally {
            jar.close();
        }
    }

    @NotNull
    Collection<Class<?>> getClasses() {
        return classes.values();
    }

    synchronized void initialize(@NotNull JavaPlugin javaPlugin) {
        Preconditions.checkArgument(javaPlugin != null, "Initializing plugin cannot be null");
        Preconditions.checkArgument(javaPlugin.getClass().getClassLoader() == this, "Cannot initialize plugin outside of this class loader");
        if (this.plugin != null || this.pluginInit != null) {
            throw new IllegalArgumentException("Plugin already initialized!", pluginState);
        }

        pluginState = new IllegalStateException("Initial initialization");
        this.pluginInit = javaPlugin;

        javaPlugin.init(loader, loader.server, description, dataFolder, file, this);
    }

    @Override
    public ClassLoaderRemapper getRemapper() {
        if (remapper == null) {
            MohistRemapper.init(McVersion.v1_19_4);
            remapper = MohistRemapper.createClassLoaderRemapper(this);
        }
        return remapper;
    }


}
*/

/**
 * A ClassLoader for plugins, to allow shared classes across multiple plugins
 *
 public class PluginClassLoader extends URLClassLoader implements RemappingClassLoader {

 private final JavaPluginLoader loader;
 private final Map<String, Class<?>> classes = new ConcurrentHashMap<String, Class<?>>();
 private final PluginDescriptionFile description;
 private final File dataFolder;
 private final File file;
 public final JarFile jar;
 private final Manifest manifest;
 private final URL url;
 final JavaPlugin plugin;
 private JavaPlugin pluginInit;
 private IllegalStateException pluginState;
 private final Set<String> seenIllegalAccess = Collections.newSetFromMap(new ConcurrentHashMap<>());

 static {
 ClassLoader.registerAsParallelCapable();
 }

 private ClassLoaderRemapper remapper;

 PluginClassLoader(final JavaPluginLoader loader, final ClassLoader parent, final PluginDescriptionFile description, final File dataFolder, final File file) throws IOException, InvalidPluginException, MalformedURLException {
 super(new URL[] {file.toURI().toURL()}, parent);;
 //Validate.notNull(loader, "Loader cannot be null");

 this.loader = loader;
 this.description = description;
 this.dataFolder = dataFolder;
 this.file = file;
 this.jar = new JarFile(file);
 this.manifest = jar.getManifest();
 this.url = file.toURI().toURL();

 try {
 Class<?> jarClass;
 try {
 jarClass = Class.forName(description.getMain(), true, this);
 } catch (ClassNotFoundException ex) {
 throw new InvalidPluginException("Cannot find main class `" + description.getMain() + "'", ex);
 }

 Class<? extends JavaPlugin> pluginClass;
 try {
 pluginClass = jarClass.asSubclass(JavaPlugin.class);
 } catch (ClassCastException ex) {
 throw new InvalidPluginException("main class `" + description.getMain() + "' does not extend JavaPlugin", ex);
 }

 plugin = pluginClass.newInstance();
 } catch (IllegalAccessException ex) {
 throw new InvalidPluginException("No public constructor", ex);
 } catch (InstantiationException ex) {
 throw new InvalidPluginException("Abnormal plugin type", ex);
 }
 }

 @Override public Class<?> loadClass(String name) throws ClassNotFoundException {
 return super.loadClass(name);
 }

 @Override public URL getResource(String name) {
 return findResource(name);
 }

 @Override public Enumeration<URL> getResources(String name) throws IOException {
 return findResources(name);
 }

 @Override protected Class<?> findClass(String name) throws ClassNotFoundException {
 return findClass_(name, true);
 }

 public Product2<byte[], CodeSource> remapClassold(String className, Callable<byte[]> byteSource, URLConnection connection) throws ClassNotFoundException {
 try {
 byte[] bytes = byteSource.call();
 URL url;
 CodeSigner[] signers;
 if (connection instanceof JarURLConnection) {
 url = ((JarURLConnection) connection).getJarFileURL();
 signers = ((JarURLConnection) connection).getJarEntry().getCodeSigners();
 } else {
 url = connection.getURL();
 signers = null;
 }
 return Product.of(bytes, new CodeSource(url, signers));
 } catch (Exception e) {
 e.printStackTrace();
 throw new ClassNotFoundException(className, e);
 }
 }

 Class<?> findClass_(String name, boolean checkGlobal) throws ClassNotFoundException {

 Class<?> result = classes.get(name);

 if (result == null) {

 if (checkGlobal) {
 result = loader.getClassByName(name);

 if (result != null) {
 PluginDescriptionFile provider = ((PluginClassLoader) result.getClassLoader()).description;

 if (provider != description && !seenIllegalAccess.contains(provider.getName())
 && !((SimplePluginManager) loader.server.getPluginManager()).isTransitiveDepend(description, provider)) {

 seenIllegalAccess.add(provider.getName());
 if (plugin != null) {
 plugin.getLogger().log(Level.WARNING, "Loaded class " + name + " from " + provider.getFullName() + " which is not a depend, softdepend or loadbefore of this plugin.");
 } else {
 // In case the bad access occurs on construction
 loader.server.getLogger().log(Level.WARNING, "[{0}] Loaded class {1} from {2} which is not a depend, softdepend or loadbefore of this plugin.", new Object[]{description.getName(), name, provider.getFullName()});
 }
 }
 }
 }

 String path = name.replace('.', '/').concat(".class");
 URL url = this.findResource(path);
 JarEntry entry = jar.getJarEntry(path);

 if (url != null && entry != null && result == null) {
 // System.out.println("REMAP NEED! Debug;");
 URLConnection connection;
 Callable<byte[]> byteSource;

 /*byte[] classBytes0;

 try (InputStream is = jar.getInputStream(entry)) {
 classBytes0 = ByteStreams.toByteArray(is);
 } catch (IOException ex) {
 throw new ClassNotFoundException(name, ex);
 }

 classBytes0 = loader.server.getUnsafe().processClass(description, path, classBytes0);
 classBytes0 = PluginFixManager.injectPluginFix(name, classBytes0); // Mohist - Inject plugin fix

 byteSource = () -> {
 return classBytes0;
 };//

 try {
 connection = url.openConnection();
 connection.connect();
 byteSource = () -> {
 try (InputStream is = connection.getInputStream()) {
 byte[] classBytes = ByteStreams.toByteArray(is);
 classBytes = loader.server.getUnsafe().processClass(description, path, classBytes);
 // classBytes = PluginFixManager.injectPluginFix(name, classBytes); // Mohist - Inject plugin fix

 return classBytes;
 }
 };
 } catch (IOException e) {
 throw new ClassNotFoundException(name, e);
 }

 // Product2<byte[], CodeSource> classBytes = remapClass(name, byteSource, connection);
 Product2<byte[], CodeSource> classBytes = this.getRemapper().remapClass(name, byteSource, connection);


 int dot = name.lastIndexOf('.');
 if (dot != -1) {
 String pkgName = name.substring(0, dot);
 if (getPackage(pkgName) == null) {
 try {
 if (manifest != null) {
 definePackage(pkgName, manifest, this.url);
 } else {
 definePackage(pkgName, null, null, null, null, null, null, null);
 }
 } catch (IllegalArgumentException ex) {
 if (getPackage(pkgName) == null) {
 throw new IllegalStateException("Cannot find package " + pkgName);
 }
 }
 }
 }
 //this.defineClass
 CodeSource source = new CodeSource(url, entry.getCodeSigners());
 result = defineClass(name, classBytes._1, 0, classBytes._1.length, source);

 File out = new File("C:\\Users\\isaia\\Documents\\fo\\" + name.replace('.', File.separatorChar) + ".class");
 try {
 Files.createDirectories(out.toPath().getParent());
 Files.write(out.toPath(), classBytes._1);
 } catch (IOException e) {
 // TODO Auto-generated catch block
 e.printStackTrace();
 }
 }

 if (result == null) {
 result = super.findClass(name);
 }

 if (result != null) {
 loader.setClass(name, result);
 classes.put(name, result);
 }
 }
 return result;
 }

 @SuppressWarnings("deprecation") Class<?> findClass123(String name, boolean checkGlobal) throws ClassNotFoundException {
 Class<?> result = classes.get(name);

 if (result == null) {
 if (checkGlobal) {
 result = loader.getClassByName(name);

 if (result != null) {
 PluginDescriptionFile provider = ((PluginClassLoader) result.getClassLoader()).description;

 if (provider != description && !seenIllegalAccess.contains(provider.getName())
 && !((SimplePluginManager) loader.server.getPluginManager()).isTransitiveDepend(description, provider)) {

 seenIllegalAccess.add(provider.getName());
 if (plugin != null) {
 plugin.getLogger().log(Level.WARNING, "Loaded class " + name + " from " + provider.getFullName() + " which is not a depend, softdepend or loadbefore of this plugin.");
 } else {
 // In case the bad access occurs on construction
 loader.server.getLogger().log(Level.WARNING, "[{0}] Loaded class {1} from {2} which is not a depend, softdepend or loadbefore of this plugin.", new Object[]{description.getName(), name, provider.getFullName()});
 }
 }
 }
 }

 if (result == null) {
 String path = name.replace('.', '/').concat(".class");
 JarEntry entry = jar.getJarEntry(path);

 if (entry != null) {
 byte[] classBytes;

 try (InputStream is = jar.getInputStream(entry)) {
 classBytes = ByteStreams.toByteArray(is);
 } catch (IOException ex) {
 throw new ClassNotFoundException(name, ex);
 }

 classBytes = loader.server.getUnsafe().processClass(description, path, classBytes);

 int dot = name.lastIndexOf('.');
 if (dot != -1) {
 String pkgName = name.substring(0, dot);
 if (getPackage(pkgName) == null) {
 try {
 if (manifest != null)
 definePackage(pkgName, manifest, url);
 else definePackage(pkgName, null, null, null, null, null, null, null);
 } catch (IllegalArgumentException ex) {
 if (getPackage(pkgName) == null)
 throw new IllegalStateException("Cannot find package " + pkgName);
 }
 }
 }


 // Product2<byte[], CodeSource> classBytes_ = this.getRemapper().remapClass(name, byteSource, connection);

 CodeSource source = new CodeSource(url, entry.getCodeSigners());
 result = defineClass(name, classBytes, 0, classBytes.length, source);
 }

 try {
 if (result == null)
 result = super.findClass(name);
 } catch (ClassNotFoundException | NoClassDefFoundError e) {return null;}

 if (result != null)
 loader.setClass(name, result);

 if (result != null)
 classes.put(name, result);
 }
 }

 return result;
 }

 @Override public void close() throws IOException {
 try {
 super.close();
 } finally {
 jar.close();
 }
 }

 Set<String> getClasses() {
 return classes.keySet();
 }

 synchronized void initialize(JavaPlugin javaPlugin) {
 //Validate.notNull(javaPlugin, "Initializing plugin cannot be null");
 // Validate.isTrue(javaPlugin.getClass().getClassLoader() == this, "Cannot initialize plugin outside of this class loader");
 if (this.plugin != null || this.pluginInit != null)
 throw new IllegalArgumentException("Plugin already initialized", pluginState);

 pluginState = new IllegalStateException("Initial initialization");
 this.pluginInit = javaPlugin;

 javaPlugin.init(loader, loader.server, description, dataFolder, file, this);
 }

 @Override public ClassLoaderRemapper getRemapper() {
 if (remapper == null) {
 MohistRemapper.init(McVersion.v1_19_4);
 remapper = MohistRemapper.createClassLoaderRemapper(this);
 }
 return remapper;
 }

 }*/
