package mb.spoofax.api.module;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class which manages the modules and the storage.
 */
public class ModuleManager {
    //TODO Should eventually not be a singleton but static in the project
    private static ModuleManager instance = new ModuleManager();
    public static ModuleManager getInstance() {
        return instance;
    }

    private Map<ModuleKey, SModule> modules = new ConcurrentHashMap<>();


    private ModuleManager() {}

    /**
     * Gets the module with the given key.
     *
     * @param key
     *      the key
     *
     * @return
     *      the module with the given key, or null if no such module exists
     */
    public SModule getModule(ModuleKey key) {
        return modules.get(key);
    }

    /**
     * NOTE: The given map is a copy of the internal modules.
     *
     * @return
     *      a map containing all modules
     */
    public Map<ModuleKey, SModule> getAllModules() {
        return new HashMap<>(modules);
    }

    //TODO Register for updates to map of modules?

    /**
     * Adds a module.
     *
     * @param module
     *      the module to add
     *
     * @throws DuplicateModuleException
     *      If a module already exists with the same key as the given module.
     */
    public void addModule(SModule module) throws DuplicateModuleException {
        SModule oldModule = modules.putIfAbsent(module.getId(), module);
        if (oldModule != null) throw new DuplicateModuleException("Module " + module.getId() + " already exists!");
    }

    /**
     * Creates a new module with the given key and language.
     *
     * The module is automatically added to the manager.
     *
     * @param key
     *      the key of the module
     *
     * @return
     *      the created module
     */
    public SModule createModule(LanguageModuleKey key) {
        SModule module = new FloatingModuleImpl(key);
        addModule(module);
        return module;
    }

    /**
     * Removes the module with the given key.
     *
     * @param key
     *      the key
     *
     * @return
     *      true if a module was removed, false otherwise
     */
    public boolean removeModule(ModuleKey key) {
        SModule module = modules.remove(key);
        return module != null;
    }

    /**
     * Searches for all modules with the given key, while ignoring the language.
     *
     * @param key
     *      the key
     *
     * @return
     *      a set of all eligible modules
     */
    public Set<SModule> findModuleLanguageAgnostic(ModuleKey key) {
        if (key == null) throw new NullPointerException("Argument must not be null.");

        SModule module = modules.get(key);
        if (module != null) return Collections.singleton(module);

        Set<SModule> options = new HashSet<>();
        for (Entry<ModuleKey, SModule> entry : modules.entrySet()) {
            if (!LanguageModuleKey.equalsLanguageAgnostic(entry.getKey(), key)) continue;

            options.add(entry.getValue());
        }

        return options;
    }

    public SModule findModule(LanguageModuleKey key) {
        return getModule(key);
    }
}
