package core.information;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;
import util.Utils;
import java.util.*;
import java.util.stream.Collectors;

/**
 * The class Behavior information contains static dependency information about {@link javassist.CtBehavior}.
 */
@NodeEntity
public class MethodInformation implements Comparable<MethodInformation> {

    @Id
    @GeneratedValue
    private Long id;
    private String name;
    @Relationship(type = "USES")
    private Set<PackageInformation> packageDependencies;
    @Relationship(type = "USES")
    private Set<ClassInformation> classDependencies;
    @Relationship(type = "USES")
    private Set<MethodInformation> methodDependencies;

    /**
     * Instantiates a new Method information.
     *
     * @param name          the name of the method
     * @param isConstructor true if this method is a constructor
     */
    @Deprecated
    public MethodInformation(String name, boolean isConstructor) {
        this(name, new TreeSet<>(), new TreeSet<>(), new TreeSet<>());
    }

    /**
     * Instantiates a new Method information.
     *
     * @param name                the name of the method
     * @param packageDependencies the referenced packages
     * @param classDependencies   the referenced classes
     * @param methodDependencies  the referenced method
     * @param isConstructor       true if method is constructor
     */
    @Deprecated
    public MethodInformation(String name, Set<PackageInformation> packageDependencies, Set<ClassInformation> classDependencies, Set<MethodInformation> methodDependencies, boolean isConstructor) {
        this.name = name;
        this.packageDependencies = packageDependencies;
        this.classDependencies = classDependencies;
        this.methodDependencies = methodDependencies;
    }

    /**
     * Instantiates a new Method information.
     *
     * @param name the name of the method
     */
    public MethodInformation(String name) {
        this(name, new TreeSet<>(), new TreeSet<>(), new TreeSet<>());
    }

    /**
     * Instantiates a new Method information.
     *
     * @param name                the name of the method
     * @param packageDependencies the referenced packages
     * @param classDependencies   the referenced classes
     * @param methodDependencies  the referenced method
     */
    public MethodInformation(String name, Set<PackageInformation> packageDependencies, Set<ClassInformation> classDependencies, Set<MethodInformation> methodDependencies) {
        this.name = name;
        this.packageDependencies = packageDependencies;
        this.classDependencies = classDependencies;
        this.methodDependencies = methodDependencies;
    }

    /**
     * Should not be used is for Spring Data
     */
    public MethodInformation() {
        this.packageDependencies = new TreeSet<>();
        this.classDependencies = new TreeSet<>();
        this.methodDependencies = new TreeSet<>();
    }

    public Set<PackageInformation> getPackageDependencies() {
        return packageDependencies;
    }

    /**
     * Set the referenced packages.
     *
     * @param packageDependencies the referenced packages
     */
    public void setPackageDependencies(Set<PackageInformation> packageDependencies) {
        this.packageDependencies = packageDependencies;
    }

    public Set<ClassInformation> getClassDependencies() {
        return classDependencies;
    }

    /**
     * Gets name of the methods.
     *
     * @return the name of the method
     */
    public String getName() {
        return name;
    }

    /**
     * Set the referenced classes
     *
     * @param classDependencies the referenced classes
     */
    public void setClassDependencies(Set<ClassInformation> classDependencies) {
        this.classDependencies = classDependencies;
    }

    public Set<MethodInformation> getMethodDependencies() {
        return methodDependencies;
    }

    /**
     * Set the referenced methods
     *
     * @param methodDependencies the referenced methods
     */
    public void setMethodDependencies(Set<MethodInformation> methodDependencies) {
        this.methodDependencies = methodDependencies;
    }

    public boolean isConstructor() {
        return name.indexOf('<') != -1;
    }

    @Override
    public int compareTo(MethodInformation methodInformation) {
        return Comparator.comparing(MethodInformation::isConstructor)
                .thenComparing(MethodInformation::getName)
                .thenComparing(MethodInformation::getPackageDependencies, Utils.setComparator(Comparator.comparing(PackageInformation::getPackageName)))
                .thenComparing(MethodInformation::getClassDependencies, Utils.setComparator(Comparator.comparing(ClassInformation::getClassName)))
                .thenComparing(MethodInformation::getMethodDependencies, Utils.setComparator(Comparator.comparing(MethodInformation::getName)))
                .compare(this, methodInformation);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof MethodInformation && ((MethodInformation) obj).compareTo(this) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return ("Method " + name + " (id=" + id + ", " + (isConstructor() ? "constructor" : "no-constructor") + ") {\n"
                + "packageDependencies=[" + packageDependencies.stream().map(PackageInformation::getPackageName).collect(Collectors.joining(", ")) + "]\n"
                + "classDependencies=[" + classDependencies.stream().map(ClassInformation::getClassName).collect(Collectors.joining(", ")) + "]\n"
                + "methodDependencies=[" + methodDependencies.stream().map(MethodInformation::getName).collect(Collectors.joining(", ")) + "]"
        ).replace("\n", "\n    ") + "\n}";
    }


    /**
     * Method which returns The classname without packages
     * @return String
     */
    public String getShortMethodName(){
        String s=getName();
        String res=s;
        for (int i=0;i<s.length();i++){
            if(s.charAt(i)=='.'){
                res=s.substring(i+1);
            }
        }
        return res;
    }


    /**
     * Method to search for the Class in which the current Method resides.
     * Since we only have a downward dependency Tree a List of all ClassInformation has to be given as Parameter
     * @param piList mentioned parameter
     * @return ClassInformation in which the Method resides
     */

    public ClassInformation getClassInformation(List<ClassInformation> piList){
        for (ClassInformation pi : piList) {
            if (pi.getMethodInformations().contains(this)) {
                return pi;
            }
        }
        return null;
    }

}
