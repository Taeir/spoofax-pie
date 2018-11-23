package mb.spoofax.api.module;

import java.io.Serializable;
import java.util.Set;

public interface SModule extends Serializable {
    /**
     * This method returns the full identifier for this module.
     *
     * @return
     *      the id of this module
     */
    public ModuleKey getId();

    /**
     * This method returns the set of module identifiers that this module depends on.
     *
     * @return
     *      the set of dependencies
     */
    public Set<ModuleKey> getDependencies();

    /**
     * Updates the set of dependencies to the given set.
     *
     * @param dependencies
     *      the new set of dependencies
     */
    public default void updateDependencies(Set<ModuleKey> dependencies) {
        getDependencies().retainAll(dependencies);
        getDependencies().addAll(dependencies);
        //TODO Send update?
    }

    /**
     * Adds the given module to the dependencies.
     *
     * @param module
     *      the module key
     */
    public default void addDependency(ModuleKey module) {
        getDependencies().add(module);
    }

    /**
     * Removes the given module from the dependencies.
     *
     * @param module
     *      the module key
     */
    public default void removeDependency(ModuleKey module) {
        getDependencies().remove(module);
    }
}
