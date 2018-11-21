package mb.spoofax.api.module;

import mb.spoofax.api.module.payload.Payload;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SingleFileModuleImpl<Key> implements SingleFileModule<Key> {
    private Key key;
    private String language;
    private File file;
    private LanguageModuleKey lmk;

    private Map<String, Payload<Key>> payloads = new HashMap<>();

    /**
     * A base module for the given file with the given key.
     *
     * @param key
     *      the key of the module
     * @param lmk
     *      the language module key
     * @param file
     *      the file of the module
     */
    public SingleFileModuleImpl(Key key, LanguageModuleKey lmk, File file) {
        this.key  = key;
        //TODO Determine the ID from the file, based on the offset from the project root?
        this.file = file;
        this.lmk = lmk;
    }

    @Override
    public Key getKey() {
        return key;
    }

    @Override
    public LanguageModuleKey getId() {
        return lmk;
    }

    @Override
    public String getLanguage() {
        return language;
    }

    @Override
    public File getFile() {
        return file;
    }

    @Override
    public Map<String, Payload<Key>> getPayloads() {
        return payloads;
    }

    /**
     * @return a list of module keys this module depends on
     */
    @Override
    public List<ModuleKey> getDependencies() {
        //TODO Dependencies need to be identified in one of the steps and then added here.
        return null;
    }


}
