Dessert
=======

The name is a short form of **De**pendency A**ssert**. Hence Dessert is a library to check assertions for
dependencies. Typically it is used within unit-tests.

Goals
-----

- No additional dependencies but plain Java 6 or above
- Simple and intuitive API
- Assertions should be robust against refactorings (no strings for class or package names required)
- Easy and seamless integration with other testing or assertion frameworks
- Speed

Getting Started
---------------

After having included the test dependency `com.github.hajo70:dessert:0.3` from the
`https://jitpack.io` repository a test checking all dependencies of the dessert library can
be implemented like this:

    @Test
    public void checkDessertDependencies() throws IOException {
        SliceContext sc = new SliceContext();
        Slice dessert = sc.packageTreeOf("de.spricom.dessert")
                .without(sc.packageTreeOf("de.spricom.dessert.test"));
        Slice java = sc.packageTreeOf("java");
        SliceAssertions.assertThat(dessert).usesOnly(java);
    }

### Minimal Maven POM

A copy of the following Maven POM can be used to get started with dessert:

    <?xml version="1.0" encoding="UTF-8"?>
    <project xmlns="http://maven.apache.org/POM/4.0.0"
             xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
             xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
        <modelVersion>4.0.0</modelVersion>
    
        <groupId>com.company.samples</groupId>
        <artifactId>dessert-sample</artifactId>
        <version>1.0-SNAPSHOT</version>
    
        <dependencies>
            <dependency>
                <groupId>com.github.hajo70</groupId>
                <artifactId>dessert</artifactId>
                <version>0.3</version>
                <scope>test</scope>
            </dependency>
            <dependency>
                <groupId>junit</groupId>
                <artifactId>junit</artifactId>
                <version>4.12</version>
                <scope>test</scope>
            </dependency>
        </dependencies>
    
        <repositories>
            <repository>
                <id>jitpack.io</id>
                <url>https://jitpack.io</url>
            </repository>
        </repositories>
    </project>

### Minimal Gradle Buildfile

The corresponding gradle build file looks like this:

    apply plugin: 'java'
    
    repositories {
        jcenter()
        maven { url 'https://jitpack.io' }
    }
    
    dependencies {
        testCompile 'com.github.hajo70:dessert:0.3-SNAPSHOT'
        testCompile 'junit:junit:4.12'
    }

Background
----------

The goal of checking the dependencies of a class is to find any unintended dependency. Hence for each
class there is a set of classes for which dependencies are permitted and an other set of classes
for which dependencies are unwanted or disallowed.

Typically the same dependency rules that apply to one class apply to other somehow related classes.
Thus when specifying dependencies there is a group of related classes for which there may be dependencies
to some other group of classes.

A software product is constructed from different building blocks with defined interfaces within each other.
The classes constituting a building block normally belong to the same package structure, thus they are related.
For example in the dessert library the `de.spricom.dessert.classfile` packages analyze `.class` files, whereas
the `de.spricom.dessert.slicing` package tracks dependencies between related classes. Therefore the `slices`
block uses the `classfile` block, but not the other way round.

A library for dependency checking needs some concept to specify such building blocks. Thus it needs a way
to slice down the bunch of all classes into different parts. In dessert there is the `Slice` interface
to represent one such part or building block.

A `Slice` is an arbitrary set of classes. Therefore we need to
know what a class is: Physically a class is .class file located in some directory tree or a .jar file.
Within a directory tree a class is uniquely defined by its name and its position in the tree structure.
This can be expressed by the fully qualified class name (fqcn), 
i.e. `de.spricom.dessert.classfile.constpool.ConstantPool`. The same applies to a JAR file. But a class
with the same fqcn can appear in different directories or JAR files. Thus we need
besides the fqcn always it's container (directory or JAR file) to specify it
uniquely.

For the following a container is a directory or JAR file that could be added to the CLASSPATH to include
all classes within the container. A class is a concrete .class file uniquely defined by its fqcn name
and its container. Hence for the concepts below an interface or an inner class is a class because
it has its own .class file.

The classes belonging to a `Slice` are represented by `SliceEntry` objects. Each `SliceEntry` corresponds
with a class file inside a container. The same `SliceEntry` object may belong to different `Slice` objects.
The `SliceEntry` provides an API to access all direct dependencies of the corresponding class and other
information (classname, implemenation class, container file) that can be used for predicates.

The starting point for any dependency analysis with Dessert is the `SliceContext`. The `SliceContext` implements
a flyweight pattern for `SliceEntry` objects. Thus for two `SliceEntry` objects `se1` and `se2`
`se1.equals(se2)` is equivalent to `se1 == se2` if they come from the same `SliceContext`. Thus checking whether
some dependency belongs to a `SliceSet` is very fast. For performance reasons all dependency tests should use
the same `SliceContext`.

The `SliceContext` provides some methods (`packagesOf`, `packageTreeOf`) to create an initial `Slice`
whos slices contain all classes of a package or package-tree respectively.  

Two `Slice` objects can be combined with the `with` or `without` method to a bigger or smaller `Slice`
or the `slice` method can be used to create a smaller slices by specifying a `Predicate`.

Thus it is possible to check dependency rules for other related groups of classes that have nothing to
do with building blocks. Let's say there is a rule a presenter must not depend on a view implemetation.
This could be implemented like this:

    Slice presenters = uiSlice.slice(e -> e.getClassname().endsWith("Presenter"));
    Slice views = uiSlice.slice(e -> e.getClassname().endsWith("ViewImpl"));
    SliceAssertions.assertThat(persenters).doesNotUse(views);

If one prefers interfaces over names the following it would read like this:

    Slice presenters = uiSlice.slice(e -> Presenter.class.isAssignableFrom(e.getClass()));
    Slice views = uiSlice.slice(e -> ViewImpl.class.isAssignableFrom(e.getClass()));
    SliceAssertions.assertThat(persenters).doesNotUse(views);

For the actual dependency checking between such `Slice` objects the `SliceAssertions` class provides 
a fluent API.

For an example of using the API see
[DessertDependenciesTest.java](https://github.com/hajo70/dessert/blob/master/test/de/spricom/dessert/test/slicing/DessertDependenciesTest.java).

Cycle detection and general dependency rules
--------------------------------------------

All classes involved in a cycle are mutually dependent. Hence one cannot easily use or test a single class
without having working and properly initialized instances of the other classes. Dessert provides an easy way
to detect such cycles:

    @Test
    public void checkPackagesAreCycleFree() throws IOException {
        SliceSet subPackages = new SliceContext().subPackagesOf("de.spricom.dessert");
        SliceAssertions.dessert(subPackages).isCycleFree();
    }

One might want to enforce other general dependency rules. For example within dessert a deeper nested package
should not use classes of its parent package. Such a rule can be enforced like this:

    @Test
    public void checkNestedPackagesShouldNotUseOuterPackages() throws IOException {
        SliceSet subPackages = new SliceContext().subPackagesOf("de.spricom.dessert");
        for (Slice pckg : subPackages) {
            SliceAssertions.assertThat(pckg).doesNotUse(pckg.getParentPackage());
        }
    }

DuplicateClassFinder
====================

The DuplicateClassFinder is included in the dessert library. It checks if there are different implementations of
the same class on the class-path. You can use it form a Gradle file like that:

	apply plugin: 'java'
	
	repositories {
	    jcenter()
	    maven { url 'https://jitpack.io' }
	}
	
	configurations {
		dessert
	}
	
	dependencies {
		dessert 'com.github.hajo70:dessert:0.3'
		
		runtime 'org.apache.httpcomponents:httpclient:4.5.3'
		runtime 'org.keycloak:keycloak-osgi-thirdparty:1.1.1.Final'
	}
	
	task findDuplicates(type: JavaExec) {
	  classpath = files(configurations.dessert, configurations.runtime)
	  main = 'de.spricom.dessert.duplicates.DuplicateClassFinder'
	}