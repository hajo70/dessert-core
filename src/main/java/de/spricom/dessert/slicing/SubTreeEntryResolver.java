package de.spricom.dessert.slicing;

import de.spricom.dessert.resolve.ClassContainer;
import de.spricom.dessert.resolve.ClassEntry;
import de.spricom.dessert.resolve.ClassPackage;
import de.spricom.dessert.resolve.ClassResolver;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SubTreeEntryResolver implements EntryResolver {
    private final SliceContext sc;
    private final ClassResolver resolver;
    private final String packageName;
    private Set<SliceEntry> sliceEntries;

    public SubTreeEntryResolver(SliceContext sc, ClassResolver resolver, String packageName) {
        this.sc = sc;
        this.resolver = resolver;
        this.packageName = packageName;
    }

    @Override
    public Set<SliceEntry> getSliceEntries() {
        if (sliceEntries == null) {
            sliceEntries = new HashSet<SliceEntry>();
            resolve();
        }
        return sliceEntries;
    }

    private void resolve() {
        ClassPackage cp = resolver.getPackage(packageName);
        if (cp == null) {
            throw new IllegalStateException("Cannot resolve " + packageName);
        }
        addRecursive(cp);
        while (cp.getNextAlternative() != null) {
            cp = cp.getNextAlternative();
            addRecursive(cp);
        }
    }

    private void addRecursive(ClassContainer cc) {
        add(cc);
        List<ClassPackage> subPackages = cc.getSubPackages();
        for (ClassPackage subp : subPackages) {
            addRecursive(subp);
        }
    }

    private void add(ClassContainer cc) {
        if (cc == null || cc.getClasses() == null || cc.getClasses().isEmpty()) {
            return;
        }
        List<ClassEntry> classes = cc.getClasses();
        for (ClassEntry cf : classes) {
            sliceEntries.add(new SliceEntry(sc, cf));
        }
    }
}