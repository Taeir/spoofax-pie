package mb.spoofax.api.module.payload;

import java.util.Map;

/**
 * Interface for representing something with payloads.
 */
public interface IPayloadable {
    /**
     * NOTE: The returned list will be updated with new payloads when they become available.
     *
     * @return
     *      a Map with all payloads that this module currently has available
     */
    public Map<String, Payload> getPayloads();

    /**
     * Gets the payload of a specific type.
     *
     * @param type
     *      the type of the payload
     * @param <T>
     *      the type of payload to get
     *
     * @return
     *      the payload with the given type, or null if this payload does not exist yet
     */
    @SuppressWarnings("unchecked")
    public default <T extends Payload> T getPayload(String type) {
        return (T) getPayloads().get(type);
    }

    /**
     * @param type
     *      the payload type
     *
     * @return
     *      if this module has a payload of the given type
     */
    public default boolean hasPayload(String type) {
        return getPayloads().containsKey(type);
    }

    /**
     * Adds the given payload to this module. The payload will be added under all the different payloads that it
     * implements.
     *
     * @param payload
     *      the payload
     */
    public default void addPayload(Payload payload)  {
        getPayloads().put(payload.getType(), payload);
    }

    /**
     * Removes the given payload from this module.
     *
     * @param payload
     *      the payload to remove
     */
    public default void removePayload(Payload payload) {
        getPayloads().remove(payload.getType(), payload);
    }

    /**
     * Removes the payload of the given type from this module.
     *
     * @param type
     *      the payload type
     */
    public default void removePayload(String type) {
        getPayloads().remove(type);
    }
}
