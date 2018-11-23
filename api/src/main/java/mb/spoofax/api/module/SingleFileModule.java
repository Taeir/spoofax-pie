package mb.spoofax.api.module;

import mb.pie.vfs.path.PPath;
import mb.spoofax.api.module.payload.IPayloadable;

/**
 * A module representing a single file. Modules of this kind have the following properties:
 * <ul>
 *     <li>identifier: LanguageModuleKey</li>
 *     <li>file: The file the module represents</li>
 *     <li>payloads: The different representations of the module</li>
 * </ul>
 */
public interface SingleFileModule extends SModule, IPayloadable {
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

    /**
     * The file where this module originates from.
     * Note that multiple modules can originate from the same file.
     * This method can return null if the origin information is unavailable.
     *
     * @return
     *      the file this module originates from
     */
    public PPath getFile();

    @Override
    public LanguageModuleKey getId();
}
