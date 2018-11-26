package mb.spoofax.api.module.payload;

import mb.spoofax.api.module.SModule;

public abstract class FlowPayload extends BasePayload {
    public FlowPayload(SModule module) {
        super(module);
    }

    @Override
    public String getType() {
        return PayloadTypes.FLOW_GRAPH;
    }

    /**
     * TODO
     * @return
     *      the flow analysis graph of this Flow payload
     */
    public abstract Object getFlowGraph();
}
