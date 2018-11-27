package mb.spoofax.api.module;

import java.util.Set;

public class ProjectModuleImpl implements ProjectModule {
    private ModuleKey id;

    public ProjectModuleImpl(ModuleKey id) {
        this.id = id;
    }

    public ModuleKey getId() {
        return id;
    }

    @Override
    public Set<ModuleKey> getDependencies() {
        return null;
    }
}
