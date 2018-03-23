package de.spricom.dessert.resolve;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

final class JarRoot extends ClassRoot {
    public JarRoot(File jarFile) throws IOException {
        super(jarFile);
    }

    @Override
    protected void scan(ClassCollector collector) throws IOException {
        Map<String, ClassPackage> packages = new HashMap<String, ClassPackage>();
        packages.put("", this);
        collector.addPackage(this);

        JarFile jarFile = new JarFile(getRootFile());
        Enumeration<JarEntry> entries = jarFile.entries();
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            if (!entry.isDirectory() && entry.getName().endsWith(".class")) {
                addClass(collector, packages, jarFile, entry);
            }
        }
        // JarFile must not be closed to be able to access the content of each JarEntry.
    }

    private void addClass(ClassCollector collector, Map<String, ClassPackage> packages,  JarFile jarFile, JarEntry entry) throws IOException {
        ClassPackage pckg = ensurePackage(collector, packages, packageName(entry));
        ClassEntry ce = new JarClassEntry(pckg, jarFile, entry);
        pckg.addClass(ce);
        collector.addClass(ce);
    }

    private String packageName(JarEntry entry) {
        return packageName(entry.getName(), '/').replace('/', '.');
    }

    private ClassPackage ensurePackage(ClassCollector collector, Map<String, ClassPackage> packages, String packageName) {
        ClassPackage pckg = packages.get(packageName);
        if (pckg != null) {
            return pckg;
        }
        ClassPackage parent = ensurePackage(collector, packages, parentPackageName(packageName));
        pckg = new ClassPackage(parent, packageName);
        collector.addPackage(pckg);
        packages.put(packageName, pckg);
        return pckg;
    }

    private String parentPackageName(String packageName) {
        return packageName(packageName, '.');
    }

    private String packageName(String name, char separator) {
        int index = name.lastIndexOf(separator);
        if (index == -1) {
            return "";
        }
        String packageName = name.substring(0, index);
        return packageName;
    }
}
