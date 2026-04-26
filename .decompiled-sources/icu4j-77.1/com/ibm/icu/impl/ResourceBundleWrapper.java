/*
 * Decompiled with CFR 0.152.
 */
package com.ibm.icu.impl;

import com.ibm.icu.impl.CacheBase;
import com.ibm.icu.impl.ClassLoaderUtil;
import com.ibm.icu.impl.ICUDebug;
import com.ibm.icu.impl.SoftCache;
import com.ibm.icu.util.ULocale;
import com.ibm.icu.util.UResourceBundle;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

public final class ResourceBundleWrapper
extends UResourceBundle {
    private ResourceBundle bundle = null;
    private String localeID = null;
    private String baseName = null;
    private List<String> keys = null;
    private static CacheBase<String, ResourceBundleWrapper, Loader> BUNDLE_CACHE = new SoftCache<String, ResourceBundleWrapper, Loader>(){

        @Override
        protected ResourceBundleWrapper createInstance(String unusedKey, Loader loader) {
            return loader.load();
        }
    };
    private static final boolean DEBUG = ICUDebug.enabled("resourceBundleWrapper");

    private ResourceBundleWrapper(ResourceBundle bundle) {
        this.bundle = bundle;
    }

    @Override
    protected Object handleGetObject(String aKey) {
        Object obj = null;
        for (ResourceBundleWrapper current = this; current != null; current = (ResourceBundleWrapper)current.getParent()) {
            try {
                obj = current.bundle.getObject(aKey);
                break;
            }
            catch (MissingResourceException ex) {
                continue;
            }
        }
        if (obj == null) {
            throw new MissingResourceException("Can't find resource for bundle " + this.baseName + ", key " + aKey, this.getClass().getName(), aKey);
        }
        return obj;
    }

    @Override
    public Enumeration<String> getKeys() {
        return Collections.enumeration(this.keys);
    }

    private void initKeysVector() {
        this.keys = new ArrayList<String>();
        for (ResourceBundleWrapper current = this; current != null; current = (ResourceBundleWrapper)current.getParent()) {
            Enumeration<String> e = current.bundle.getKeys();
            while (e.hasMoreElements()) {
                String elem = e.nextElement();
                if (this.keys.contains(elem)) continue;
                this.keys.add(elem);
            }
        }
    }

    @Override
    protected String getLocaleID() {
        return this.localeID;
    }

    @Override
    protected String getBaseName() {
        return this.bundle.getClass().getName().replace('.', '/');
    }

    @Override
    public ULocale getULocale() {
        return new ULocale(this.localeID);
    }

    @Override
    public UResourceBundle getParent() {
        return (UResourceBundle)this.parent;
    }

    public static ResourceBundleWrapper getBundleInstance(String baseName, String localeID, ClassLoader root, boolean disableFallback) {
        ResourceBundleWrapper b;
        if (root == null) {
            root = ClassLoaderUtil.getClassLoader();
        }
        if ((b = disableFallback ? ResourceBundleWrapper.instantiateBundle(baseName, localeID, null, root, disableFallback) : ResourceBundleWrapper.instantiateBundle(baseName, localeID, ULocale.getDefault().getBaseName(), root, disableFallback)) == null) {
            String separator = "_";
            if (baseName.indexOf(47) >= 0) {
                separator = "/";
            }
            throw new MissingResourceException("Could not find the bundle " + baseName + separator + localeID, "", "");
        }
        return b;
    }

    private static boolean localeIDStartsWithLangSubtag(String localeID, String lang) {
        return localeID.startsWith(lang) && (localeID.length() == lang.length() || localeID.charAt(lang.length()) == '_');
    }

    private static ResourceBundleWrapper instantiateBundle(final String baseName, final String localeID, final String defaultID, final ClassLoader root, final boolean disableFallback) {
        final String name = localeID.isEmpty() ? baseName : baseName + '_' + localeID;
        String cacheKey = disableFallback ? name : name + '#' + defaultID;
        return BUNDLE_CACHE.getInstance(cacheKey, new Loader(){

            /*
             * WARNING - Removed try catching itself - possible behaviour change.
             */
            @Override
            public ResourceBundleWrapper load() {
                ResourceBundleWrapper b;
                block33: {
                    boolean parentIsRoot;
                    boolean loadFromProperties;
                    ResourceBundleWrapper parent;
                    block31: {
                        parent = null;
                        int i = localeID.lastIndexOf(95);
                        loadFromProperties = false;
                        parentIsRoot = false;
                        if (i != -1) {
                            String locName = localeID.substring(0, i);
                            parent = ResourceBundleWrapper.instantiateBundle(baseName, locName, defaultID, root, disableFallback);
                        } else if (!localeID.isEmpty()) {
                            parent = ResourceBundleWrapper.instantiateBundle(baseName, "", defaultID, root, disableFallback);
                            parentIsRoot = true;
                        }
                        b = null;
                        try {
                            Class<ResourceBundle> cls = root.loadClass(name).asSubclass(ResourceBundle.class);
                            ResourceBundle bx = cls.newInstance();
                            b = new ResourceBundleWrapper(bx);
                            if (parent != null) {
                                b.setParent(parent);
                            }
                            b.baseName = baseName;
                            b.localeID = localeID;
                        }
                        catch (ClassNotFoundException e) {
                            loadFromProperties = true;
                        }
                        catch (NoClassDefFoundError e) {
                            loadFromProperties = true;
                        }
                        catch (Exception e) {
                            if (DEBUG) {
                                System.out.println("failure");
                            }
                            if (!DEBUG) break block31;
                            System.out.println(e);
                        }
                    }
                    if (loadFromProperties) {
                        try {
                            final String resName = name.replace('.', '/') + ".properties";
                            InputStream stream = AccessController.doPrivileged(new PrivilegedAction<InputStream>(){

                                @Override
                                public InputStream run() {
                                    return root.getResourceAsStream(resName);
                                }
                            });
                            if (stream != null) {
                                stream = new BufferedInputStream(stream);
                                try {
                                    b = new ResourceBundleWrapper(new PropertyResourceBundle(stream));
                                    if (parent != null) {
                                        b.setParent(parent);
                                    }
                                    b.baseName = baseName;
                                    b.localeID = localeID;
                                }
                                catch (Exception exception) {
                                }
                                finally {
                                    try {
                                        stream.close();
                                    }
                                    catch (Exception exception) {}
                                }
                            }
                            if (!(b != null || disableFallback || localeID.isEmpty() || localeID.indexOf(95) >= 0 || ResourceBundleWrapper.localeIDStartsWithLangSubtag(defaultID, localeID))) {
                                b = ResourceBundleWrapper.instantiateBundle(baseName, defaultID, defaultID, root, disableFallback);
                            }
                            if (!(b != null || parentIsRoot && disableFallback)) {
                                b = parent;
                            }
                        }
                        catch (Exception e) {
                            if (DEBUG) {
                                System.out.println("failure");
                            }
                            if (!DEBUG) break block33;
                            System.out.println(e);
                        }
                    }
                }
                if (b != null) {
                    b.initKeysVector();
                } else if (DEBUG) {
                    System.out.println("Returning null for " + baseName + "_" + localeID);
                }
                return b;
            }
        });
    }

    private static abstract class Loader {
        private Loader() {
        }

        abstract ResourceBundleWrapper load();
    }
}

