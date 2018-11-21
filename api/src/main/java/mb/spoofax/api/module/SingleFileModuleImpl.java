package mb.spoofax.api.module;

import mb.spoofax.api.module.payload.Payload;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SingleFileModuleImpl implements SingleFileModule {
    private File file;
    private LanguageModuleKey lmk;

    private Map<String, Payload> payloads = new HashMap<>();

    /**
     * A base module for the given file with the given key.
     *
     * @param lmk
     *      the language module key
     * @param file
     *      the file of the module
     */
    public SingleFileModuleImpl(LanguageModuleKey lmk, File file) {
        this.lmk = lmk;
        //TODO Determine the ID from the file, based on the offset from the project root?
        this.file = file;

    }

    @Override
    public LanguageModuleKey getId() {
        return lmk;
    }

    @Override
    public String getLanguage() {
        return lmk.getLanguage();
    }

    @Override
    public File getFile() {
        return file;
    }

    @Override
    public Map<String, Payload> getPayloads() {
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
