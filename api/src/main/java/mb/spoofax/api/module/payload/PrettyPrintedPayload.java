package mb.spoofax.api.module.payload;

import mb.spoofax.api.module.SModule;
import mb.spoofax.api.module.SingleFileModule;

public abstract class PrettyPrintedPayload extends TextPayload {

    public PrettyPrintedPayload(SModule module, String text) {
        super(module, text);
    }

    public PrettyPrintedPayload(SingleFileModule module) {
        super(module);
    }

    @Override
    public String getType() {
        return PayloadTypes.TEXT_PRETTY_PRINTED;
    }
}
