package mb.spoofax.api.module.payload;


import mb.spoofax.api.module.SModule;

public abstract class TSGPayload extends BasePayload {
    public TSGPayload(SModule module) {
        super(module);
    }

    @Override
    public String getType() {
        return PayloadTypes.SCOPE_GRAPH;
    }

    //TODO These methods should not recreate the payloads in the case it already exists.
    public abstract FlowPayload analyzeFlow();

    /**
     * TODO
     * @return
     *      the scope graph of this TSG payload
     */
    public abstract Object getScopeGraph();
}
