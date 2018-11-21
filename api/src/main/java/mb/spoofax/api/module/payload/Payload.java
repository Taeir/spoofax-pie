package mb.spoofax.api.module.payload;

import mb.spoofax.api.module.SingleFileModule;

import java.io.Serializable;

/**
 * Interface to represent module payloads. For example, Text is a payload that a module can have.
 *
 * @param <Key>
 *      the type of unique identifiers for modules
 */
public interface Payload<Key> extends Serializable {
    /**
     * This method returns the key of the module. This is the (unique) identifier for the module.
     *
     * @return
     *      the key of this module
     */
    public default Key getKey() {
        return getModule().getKey();
    }

    /**
     * @return
     *      the module this payload belongs to
     */
    public SingleFileModule<Key> getModule();

    /**
     * The language of this payload.
     * This method can return null if the language of this payload is unknown.
     *
     * @return
     *      the source language of this payload
     */
    public String getLanguage();

    /**
     * @return
     *      the type of the payload
     *
     * @see PayloadTypes
     */
    public String getType();
}
