package core.information;

import org.jetbrains.annotations.NotNull;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;
import org.slf4j.LoggerFactory;
import util.Utils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * The type Package information contains Information about the static dependencies of a java package.
 */
@NodeEntity
public class PackageInformation implements Comparable<PackageInformation> {

    @Id
    @GeneratedValue
    private Long id;
    private String packageName;
    @Relationship(type = "IS_CLASS_OF", direction = Relationship.INCOMING)
    private Set<ClassInformation> classInformations;

    @Deprecated
    private boolean isInternalPackage;

    /**
     * Instantiates a new Package information.
     *
     * @param packageName the package name
     */
    public PackageInformation(String packageName) {
        this.packageName = packageName;
        this.classInformations = new TreeSet<>();
    }

    /**
     * Gets package name.
     *
     * @return the package name
     */
    public String getPackageName() {
        return packageName;
    }

    /**
     * Gets referenced packages by extracting them from it's {@link ClassInformation}.
     *
     * @return the referenced packages
     */
    public Set<PackageInformation> getPackageDependencies() {
        Set<PackageInformation> packageDependencies = new TreeSet<>();
        classInformations.forEach(classInformation -> packageDependencies.addAll(classInformation.getPackageDependencies()));
        return packageDependencies;
    }

    /**
     * Gets referenced classes by extracting them from it's {@link ClassInformation}.
     *
     * @return the referenced classes
     */
    public Set<ClassInformation> getClassDependencies() {
        Set<ClassInformation> classDependencies = new TreeSet<>();
        classInformations.forEach(classInformation -> classDependencies.addAll(classInformation.getClassDependencies()));
        return classDependencies;
    }

    /**
     * Gets referenced method by extracting them from it's {@link ClassInformation}.
     *
     * @return the referenced methods
     */
    public Set<MethodInformation> getMethodDependencies() {
        Set<MethodInformation> methodDependencies = new TreeSet<>();
        classInformations.forEach(classInformation -> methodDependencies.addAll(classInformation.getMethodDependencies()));
        return methodDependencies;
    }

    /**
     * @return if internal package or not.
     * @see ClassInformation#isInternal()
     */
    @Deprecated
    public boolean isInternalPackage() {
        return classInformations.stream().allMatch(ClassInformation::isInternal);
    }

    /**
     * Set if internal package or not.
     *
     * @param internalPackage true if package is interal
     */
    @Deprecated
    public void setInternalPackage(boolean internalPackage) {
        LoggerFactory.getLogger(getClass()).warn("setInternalPackage called, but not in use anymore!", new Exception());
        isInternalPackage = internalPackage;
    }

    /**
     * Gets class information about all the java classes that were contained in the described package.
     *
     * @return all class information.
     */
    public Set<ClassInformation> getClassInformations() {
        return classInformations;
    }

    /**
     * Add a class information. Used to add {@link ClassInformation} about java classes that were originally part of the described package.
     *
     * @param classInformation the class information
     */
    public void addClassInformation(ClassInformation classInformation) {
        if (isInternalPackage) classInformation.setInternal(true);
        this.classInformations.add(classInformation);
    }

    @Override
    public int compareTo(@NotNull PackageInformation packageInformation) {
        return Comparator.comparing(PackageInformation::getPackageName)
                .thenComparing(PackageInformation::isInternalPackage)
                .thenComparing(PackageInformation::getClassInformations, Utils.setComparator())
                .compare(this, packageInformation);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof PackageInformation && ((PackageInformation) obj).compareTo(this) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(packageName, classInformations);
    }

    @Override
    public String toString() {
        return ("Package " + packageName + " (id=" + id + ") {\n"
                + classInformations.stream().map(ClassInformation::toString).collect(Collectors.joining(",\n"))
        ).replace("\n", "\n    ") + "\n}";
    }

    /**
     * @param className  indicates the name of the Classname of the ClassInformation that is wanted
     * @return the ClassInformation for the specified Classname
     */
    public ClassInformation getClassByName(String className){
        int counter=0;
        boolean found=false;
        ClassInformation [] ci=classInformations.toArray(new ClassInformation[classInformations.size()]);
        while(counter<ci.length && !found){
            if(ci[counter].getClassName().equals(className)){
                found=true;
            }
            else{
             counter=counter+1;
            }
        }
        if(found){
            return ci[counter];
        }
        else return null;
    }

    /**
     * Method sorts all ClassInformation of a PackageInformation in lexicographical order of Classnames
     * @return sorted List of ClassInformation
     */
    public List<ClassInformation> getSortedClassInformation(){
        ArrayList<ClassInformation> newList=new ArrayList<>();
        ClassInformation[] ca=classInformations.toArray(new ClassInformation[classInformations.size()]);
        Collections.addAll(newList,ca);
        Collections.sort(newList ,new SortComparator());
        return newList;
    }
}
