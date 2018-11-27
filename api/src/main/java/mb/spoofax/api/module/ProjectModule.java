package mb.spoofax.api.module;

public interface ProjectModule extends SModule {
    @Override
    public default String getLanguage() {
        return null;
    }
}
