package mb.spoofax.api.module;

import mb.spoofax.api.module.payload.Payload;

import java.io.File;
import java.util.List;
import java.util.Map;

public class ProjectModuleImpl implements ProjectModule {
    private ModuleKey id;

    public ProjectModuleImpl(ModuleKey id) {
        this.id = id;
    }

    public ModuleKey getId() {
        return id;
    }

    @Override
    public List<ModuleKey> getDependencies() {
        return null;
    }
}
