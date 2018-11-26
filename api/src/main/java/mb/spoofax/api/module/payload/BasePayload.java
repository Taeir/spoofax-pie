package mb.spoofax.api.module.payload;

import mb.spoofax.api.module.SModule;

public abstract class BasePayload implements Payload {
    private final SModule module;

    /**
     * Creates a new payload for the given module.
     *
     * @param module
     *      the module
     */
    public BasePayload(SModule module) {
        if (!(module instanceof IPayloadable)) {
            throw new IllegalArgumentException("Only modules that implement IPayloadable can have payloads!");
        }

        this.module = module;
        ((IPayloadable) this.module).addPayload(this);
    }

    @Override
    public SModule getModule() {
        return this.module;
    }
}
