package mb.spoofax.api.module.payload;

import mb.spoofaxmodules.SModule;

public abstract class PrettyPrintedPayload<Key> extends TextPayload<Key> {

    public PrettyPrintedPayload(SModule<Key> module) {
        super(module);
    }

    @Override
    public String getType() {
        return PayloadTypes.TEXT_PRETTY_PRINTED;
    }
}
