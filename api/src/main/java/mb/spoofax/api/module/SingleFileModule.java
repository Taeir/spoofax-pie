package mb.spoofax.api.module;

import mb.spoofax.api.module.payload.Payload;

import java.io.File;
import java.util.Map;

public interface SingleFileModule<Key> extends SModule<Key> {
    /**
     * The language of this module.
     * This method can return null if the language of this module is unknown.
     *
     * @return
     *      the source language of this module
     */
    public String getLanguage();

    /**
     * The file where this module originates from.
     * Note that multiple modules can originate from the same file.
     * This method can return null if the origin information is unavailable.
     *
     * @return
     *      the file this module originates from
     */
    public File getFile();

    @Override
    public LanguageModuleKey getId();

    /**
     * NOTE: The returned list will be updated with new payloads when they become available.
     *
     * @return
     *      a Map with all payloads that this module currently has available
     */
    public Map<String, Payload<Key>> getPayloads();

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
    public default <T extends Payload<Key>> T getPayload(String type) {
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
    public default void addPayload(Payload<Key> payload)  {
        getPayloads().put(payload.getType(), payload);
    }

    /**
     * Removes the given payload from this module.
     *
     * @param payload
     *      the payload to remove
     */
    public default void removePayload(Payload<Key> payload) {
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
