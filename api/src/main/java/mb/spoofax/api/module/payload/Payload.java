package mb.spoofax.api.module.payload;

import mb.spoofax.api.module.ModuleKey;
import mb.spoofax.api.module.SingleFileModule;

import java.io.Serializable;

/**
 * Interface to represent module payloads. For example, Text is a payload that a module can have.
 */
public interface Payload extends Serializable {
    /**
     * This method returns the id of the module. This is the (unique) identifier for the module.
     *
     * @return
     *      the key of this module
     */
    public default ModuleKey getId() {
        return getModule().getId();
    }

    /**
     * @return
     *      the module this payload belongs to
     */
    public SingleFileModule getModule();

    /**
     * The language of this payload.
     * This method can return null if the language of this payload is unknown.
     *
     * @return
     *      the source language of this payload
     */
    public default String getLanguage() {
        return getModule().getLanguage();
    }

    /**
     * @return
     *      the type of the payload
     *
     * @see PayloadTypes
     */
    public String getType();
}
