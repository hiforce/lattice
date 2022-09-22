package org.hiforce.lattice.runtime.utils;


import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.reflections.Reflections;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;

/**
 * @author Rocky Yu
 * @since 2022/9/16
 */
@Slf4j
public class ClassPathScanHandler {

    /**
     * class file extension name.
     */
    private static final String CLASS_EXTENSION_NAME = ".class";
    /**
     * 是否排除内部类 true->是 false->否.
     */
    @Getter
    @Setter
    private boolean excludeInner = true;
    /**
     * 过滤规则适用情况 true—>搜索符合规则的 false->排除符合规则的.
     */
    @Getter
    @Setter
    private boolean checkInOrEx = true;
    /**
     * 过滤规则列表 如果是null或者空，即全部符合不过滤.
     */
    @Getter
    @Setter
    private List<String> classFilters = null;
    /**
     * the reflections.
     */
    @Getter
    @Setter
    private Reflections reflections = null;

    private static final Map<ClassLoader, Map<String, Set<Class<?>>>> cachedClassloaderClassSetMap = new HashMap<ClassLoader, Map<String, Set<Class<?>>>>();


    /**
     * 无参构造器，默认是排除内部类、并搜索符合规则.
     */
    public ClassPathScanHandler() {
    }

    public synchronized Set<Class<?>> getPackageAllClasses(final String basePackage, final boolean recursive) {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Set<Class<?>> cachedClassSet = null;
        String cacheKey = basePackage + "," + recursive;

        Map<String, Set<Class<?>>> cachedClassSetMap = cachedClassloaderClassSetMap.get(classLoader);
        if (cachedClassSetMap == null) {
            cachedClassSetMap = new HashMap<>();
        }
        cachedClassSet = cachedClassSetMap.get(cacheKey);

        if (cachedClassSet == null) {
            cachedClassSet = innerGetPackageAllClasses(basePackage, recursive);
            if (cachedClassSet == null) {
                cachedClassSet = new HashSet<Class<?>>();
            }
            cachedClassSetMap.put(cacheKey, cachedClassSet);
            cachedClassloaderClassSetMap.put(classLoader, cachedClassSetMap);
        }
        return cachedClassSet;
    }

    /**
     * scan the package.
     *
     * @param basePackage the basic class package's string.
     * @param recursive   whether to search recursive.
     * @return Set create the found classes.
     */
    @SuppressWarnings("unchecked")
    public synchronized Set<Class<?>> innerGetPackageAllClasses(String basePackage, boolean recursive) {
        if (StringUtils.isEmpty(basePackage))
            return new HashSet<Class<?>>();
        Set<Class<?>> classes = new LinkedHashSet<Class<?>>();
        String packageName = basePackage;
        if (packageName.endsWith(".")) {
            packageName = packageName.substring(0, packageName.lastIndexOf('.'));
        }
        String package2Path = packageName.replace('.', '/');

        Enumeration<URL> dirs;
        try {
            dirs = Thread.currentThread().getContextClassLoader().getResources(package2Path);
            while (dirs.hasMoreElements()) {
                URL url = dirs.nextElement();
                String protocol = url.getProtocol();
                if ("file".equals(protocol)) {
                    String filePath = URLDecoder.decode(url.getFile(), "UTF-8");
                    doScanPackageClassesByFile(classes, packageName, filePath, recursive);
                } else if ("jar".equals(protocol)) {
                    doScanPackageClassesByJar(packageName, url, recursive, classes);
                }
            }
        } catch (IOException e) {
            log.error("IOException error:", e);
        }

        TreeSet<Class<?>> sortedClasses = new TreeSet<Class<?>>(new ClassNameComparator());
        sortedClasses.addAll(classes);
        return sortedClasses;
    }

    /**
     * 以jar的方式扫描包下的所有Class文件<br>.
     *
     * @param basePackage eg：michael.utils.
     * @param url         the url.
     * @param recursive   whether to search recursive.
     * @param classes     set create the found classes.
     */
    private void doScanPackageClassesByJar(String basePackage, URL url, final boolean recursive, Set<Class<?>> classes) {
        String package2Path = basePackage.replace('.', '/');
        JarFile jar;
        try {
            jar = ((JarURLConnection) url.openConnection()).getJarFile();
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String name = entry.getName();
                if (!name.startsWith(package2Path) || entry.isDirectory()) {
                    continue;
                }
                // 判断是否递归搜索子包
                if (!recursive && name.lastIndexOf('/') != package2Path.length()) {
                    continue;
                }
                // 判断是否过滤 inner class
                if (this.excludeInner && name.indexOf('$') != -1) {
                    log.debug("exclude inner class with name:" + name);
                    continue;
                }
                String classSimpleName = name.substring(name.lastIndexOf('/') + 1);
                // 判定是否符合过滤条件
                if (this.filterClassName(classSimpleName)) {
                    String className = name.replace('/', '.');
                    className = className.substring(0, className.length() - 6);
                    try {
                        classes.add(Thread.currentThread().getContextClassLoader().loadClass(className));
                    } catch (ClassNotFoundException e) {
                        log.error("LoadClass Exception:URL is ===>" + url.getPath() + " , Class ===> " + className, e);
                    } catch (NoClassDefFoundError error) {
                        log.error("LoadClass error:URL is ===>" + url.getPath() + " , Class ===> " + className, error);
                    }
                }
            }
        } catch (IOException e) {
            log.error("IOException error:URL is ===>" + url.getPath(), e);
        } catch (Throwable e) {
            log.error("ScanPackageClassesByJar error:URL is ===>" + url.getPath(), e);
        }
    }

    /**
     * 以文件的方式扫描包下的所有Class文件.
     *
     * @param packageName the package name for scanning.
     * @param packagePath the package path for scanning.
     * @param recursive   whether to search recursive.
     * @param classes     set create the found classes.
     */
    private void doScanPackageClassesByFile(
            Set<Class<?>> classes, String packageName, String packagePath, final boolean recursive) {
        File dir = new File(packagePath);
        if (!dir.exists() || !dir.isDirectory()) {
            return;
        }
        File[] files = dir.listFiles(new FileFilter() {

            @Override
            public boolean accept(File pathname) {
                return filterClassFileByCustomization(pathname, recursive);
            }
        });
        if (null == files || files.length == 0) {
            return;
        }
        for (File file : files) {
            if (file.isDirectory()) {
                doScanPackageClassesByFile(classes, packageName + "." + file.getName(), file.getAbsolutePath(), recursive);
            } else {
                String className = file.getName().substring(0,
                        file.getName().length() - CLASS_EXTENSION_NAME.length());
                try {
                    classes.add(Thread.currentThread().getContextClassLoader().loadClass(packageName + '.' + className));
                } catch (ClassNotFoundException e) {
                    log.error("LoadClass exception: ===>" + className, e);
                } catch (NoClassDefFoundError error) {
                    log.error("LoadClass error: ===>" + className, error);
                }
            }
        }
    }

    /**
     * filter the class file from the customized rules.
     *
     * @param file      the class file to be filtered.
     * @param recursive whether search recursive.
     * @return true: match,  false: not match.
     */
    private boolean filterClassFileByCustomization(@Nonnull File file, boolean recursive) {
        if (file.isDirectory()) {
            return recursive;
        }
        String filename = file.getName();
        if (excludeInner && filename.indexOf('$') != -1) {
            log.debug("exclude inner class with name:" + filename);
            return false;
        }
        return filterClassName(filename);
    }

    /**
     * 根据过滤规则判断类名.
     *
     * @param className the class name.
     * @return whether to be filtered.
     */
    private boolean filterClassName(String className) {
        if (!className.endsWith(CLASS_EXTENSION_NAME)) {
            return false;
        }
        if (null == this.classFilters || this.classFilters.isEmpty()) {
            return true;
        }
        String tmpName = className.substring(0, className.length() - 6);
        boolean flag = false;
        for (String str : classFilters) {
            flag = matchInnerClassname(tmpName, str);
            if (flag) break;
        }
        return (checkInOrEx && flag) || (!checkInOrEx && !flag);
    }

    /**
     * check the className whether match the inner class's rule.
     *
     * @param className    the inner class name.
     * @param filterString the filter string.
     * @return true or false.
     */
    private boolean matchInnerClassname(String className, String filterString) {
        String reg = "^" + filterString.replace("*", ".*") + "$";
        Pattern p = Pattern.compile(reg);
        return p.matcher(className).find();
    }

    public static void clearCache() {
        cachedClassloaderClassSetMap.clear();
    }
}
