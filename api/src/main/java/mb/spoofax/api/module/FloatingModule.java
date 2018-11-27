package mb.spoofax.api.module;

import mb.spoofax.api.module.payload.IPayloadable;

/**
 * A floating module is a module that has representations, but is not bound to any physical file or URL or other location.
 * A floating module has a language associated with it.
 *
 * <p>Floating modules have the following properties:
 * <ul>
 *     <li>identifier: LanguageModuleKey</li>
 *     <li>payloads: The different representations</li>
 * </ul>
 */
public interface FloatingModule extends SModule, IPayloadable {
    /**
     * The language of this module.
     * This method can return null if the language of this module is unknown.
     *
     * @return
     *      the source language of this module
     */
    public default String getLanguage() {
        return getId().getLanguage();
    }

    @Override
    public LanguageModuleKey getId();
}
