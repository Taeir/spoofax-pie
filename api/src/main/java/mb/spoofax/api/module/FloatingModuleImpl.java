package mb.spoofax.api.module;

import mb.pie.vfs.path.PPath;
import mb.spoofax.api.module.payload.Payload;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @see FloatingModule
 */
public class FloatingModuleImpl implements FloatingModule {
    private LanguageModuleKey key;

    private final Map<String, Payload> payloads;
    private final Set<ModuleKey> dependencies;

    /**
     * @param key
     *      the language module key
     */
    public FloatingModuleImpl(LanguageModuleKey key) {
        this.key = key;
        this.payloads = new HashMap<>();
        this.dependencies = new HashSet<>();
    }

    /**
     * @param key
     *      the language module key
     * @param payloads
     *      the map for storing the payloads
     * @param dependencies
     *      the set for storing the dependencies
     */
    protected FloatingModuleImpl(LanguageModuleKey key, Map<String, Payload> payloads, Set<ModuleKey> dependencies) {
        this.key = key;
        this.payloads = payloads;
        this.dependencies = dependencies;
    }

    @Override
    public LanguageModuleKey getId() {
        return key;
    }

    @Override
    public Map<String, Payload> getPayloads() {
        return payloads;
    }

    @Override
    public Set<ModuleKey> getDependencies() {
        //TODO Dependencies need to be identified in one of the steps and then added here.
        return dependencies;
    }
}
