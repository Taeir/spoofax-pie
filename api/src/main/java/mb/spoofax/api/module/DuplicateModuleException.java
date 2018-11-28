package mb.spoofax.api.module;

import java.util.List;

public class DuplicateModuleException extends RuntimeException {
    private List<ModuleKey> duplicates;
    public DuplicateModuleException(String msg, List<ModuleKey> duplicates) {
        super(msg);
        this.duplicates = duplicates;
    }

    public List<ModuleKey> getDuplicates() {
        return duplicates;
    }
}
