package mb.spoofax.api.module;

import mb.spoofax.api.module.payload.Payload;

import java.io.File;
import java.util.List;
import java.util.Map;

public class ProjectModuleImpl<Key> implements ProjectModule<Key> {
    private Key key;
    private ModuleKey id;

    public ProjectModuleImpl(Key key, ModuleKey id) {
        this.key = key;
        this.id = id;
    }

    @Override
    public Key getKey() {
        return key;
    }

    public ModuleKey getId() {
        return id;
    }

    @Override
    public List<ModuleKey> getDependencies() {
        return null;
    }
}
