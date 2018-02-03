package de.spricom.dessert.classfile.attribute;

import de.spricom.dessert.classfile.constpool.ConstantPool;
import de.spricom.dessert.classfile.dependency.DependencyHolder;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Set;

public class InnerClass implements DependencyHolder {
    public static final int ACC_PUBLIC = 0x0001; // Marked or implicitly public in source.
    public static final int ACC_PRIVATE = 0x0002; // Marked private in source.
    public static final int ACC_PROTECTED = 0x0004; // Marked protected in source.
    public static final int ACC_STATIC = 0x0008; // Marked or implicitly static in source.
    public static final int ACC_FINAL = 0x0010; // Marked or implicitly final in source.
    public static final int ACC_INTERFACE = 0x0200; //  Was an interface in source.
    public static final int ACC_ABSTRACT = 0x0400; // Marked or implicitly abstract in source.
    public static final int ACC_SYNTHETIC = 0x1000; // Declared synthetic; not present in the source code.
    public static final int ACC_ANNOTATION = 0x2000; // Declared as an annotation type.
    public static final int ACC_ENUM = 0x4000; // Declared as an enum type.

    private final String innerClassName;
    private final String outerClassName;
    private final String simpleName;
    private final int accessFlags;

    public InnerClass(DataInputStream is, ConstantPool constantPool) throws IOException {
        innerClassName = constantPool.getConstantClassName(is.readUnsignedShort());
        outerClassName = constantPool.getConstantClassName(is.readUnsignedShort());
        int simpleNameIndex = is.readUnsignedShort();
        simpleName = simpleNameIndex == 0 ? null : constantPool.getUtf8String(simpleNameIndex);
        accessFlags = is.readUnsignedShort();
    }

    @Override
    public void addDependentClassNames(Set<String> classNames) {
    }

    public boolean isStatic() {
        return (accessFlags & ACC_STATIC) != 0;
    }

    public String getInnerClassName() {
        return innerClassName;
    }

    public String getOuterClassName() {
        return outerClassName;
    }

    public String getSimpleName() {
        return simpleName;
    }

    public int getAccessFlags() {
        return accessFlags;
    }
}
