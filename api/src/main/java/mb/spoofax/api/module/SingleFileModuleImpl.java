package mb.spoofax.api.module;

import mb.pie.vfs.path.PPath;
import mb.spoofax.api.module.payload.Payload;

import java.util.*;

public class SingleFileModuleImpl implements SingleFileModule {
    private PPath file;
    private LanguageModuleKey key;

    private Map<String, Payload> payloads = new HashMap<>();
    private Set<ModuleKey> dependencies = new HashSet<>();

    /**
     * A base module for the given file with the given key.
     *
     * @param key
     *      the language module key
     * @param file
     *      the file of the module
     */
    public SingleFileModuleImpl(LanguageModuleKey key, PPath file) {
        this.key = key;
        //TODO Determine the ID from the file, based on the offset from the project root?
        this.file = file;
    }

    @Override
    public LanguageModuleKey getId() {
        return key;
    }

    @Override
    public PPath getFile() {
        return file;
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
