package mb.spoofax.api.module;

import java.io.Serializable;
import java.util.List;

public interface SModule<Key> extends Serializable {
    /**
     * This method returns the key of the module. This is the (unique) identifier for the module.
     *
     * @return
     *      the key of this module
     */
    public Key getKey();

    /**
     * This method returns the full identifier for this module.
     *
     * @return
     *      the id of this module
     */
    public ModuleKey getId();

    /**
     * This method returns the list of module identifiers that this module depends on.
     *
     * @return
     *      the list of dependencies
     */
    public List<ModuleKey> getDependencies();
}
