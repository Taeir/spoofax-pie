package mb.spoofax.api.module;

import java.io.Serializable;
import java.util.Objects;

/**
 * A path for module keys.
 */
public class ModuleKey implements Serializable {
    /**
     * The separator for paths.
     */
    public static final String PATH_SEPARATOR = "/";

    protected final String name;
    protected final ModuleKey parent;

    /**
     * Creates a new module key with the given name and parent.
     *
     * @param name
     *      the name of the module
     * @param parent
     *      the parent of the module
     */
    public ModuleKey(String name, ModuleKey parent) {
        this.name = name;
        this.parent = parent;
    }

    /**
     * Creates a new module key from the given full path.
     *
     * @param path
     *      the full path
     */
    public ModuleKey(String path) {
        String[] parts = path.split(PATH_SEPARATOR, 2);
        if (parts.length == 1) {
            this.name = path;
            this.parent = null;
        } else {
            this.name = parts[0];
            this.parent = new ModuleKey(parts[1]);
        }
    }

    /**
     * @return
     *      the name of this key
     */
    public String getName() {
        return name;
    }

    /**
     * @return
     *      the parent path
     */
    public ModuleKey getParent() {
        return parent;
    }

    /**
     * @return
     *      the full path of this {@link ModuleKey}
     */
    public String getPath() {
        if (parent == null) return name;

        return parent.getPath() + PATH_SEPARATOR + name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ModuleKey moduleKey = (ModuleKey) o;
        return Objects.equals(name, moduleKey.name) &&
                Objects.equals(parent, moduleKey.parent);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, parent);
    }

    @Override
    public String toString() {
        return getPath();
    }
}
