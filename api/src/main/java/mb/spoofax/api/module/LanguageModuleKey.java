package mb.spoofax.api.module;

import java.util.Objects;

/**
 * A path for module keys with an associated language.
 */
public class LanguageModuleKey extends ModuleKey {
    /**
     * The separator for the language, prepended to the path.
     */
    public static final String LANGUAGE_SEPARATOR = ":";
    private final String language;

    /**
     * Creates a new module key with the given name and parent, for the given language.
     *
     * @param name
     *      the name of the module
     * @param parent
     *      the parent of the module
     * @param language
     *      the language
     */
    public LanguageModuleKey(String name, LanguageModuleKey parent, String language) {
        super(name, parent);
        this.language = language;
    }

    /**
     * Creates a new module key from the given full path and the given language.
     *
     * @param path
     *      the full path
     * @param language
     *      the language
     */
    public LanguageModuleKey(String path, String language) {
        super(path);
        this.language = language;
    }

    /**
     * @return
     *      the language
     */
    public String getLanguage() {
        return language;
    }

    @Override
    public LanguageModuleKey getParent() {
        return (LanguageModuleKey) super.getParent();
    }

    @Override
    public String getPath() {
        if (parent == null) return language + LANGUAGE_SEPARATOR + name;

        return parent.getPath() + PATH_SEPARATOR + name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LanguageModuleKey other = (LanguageModuleKey) o;
        return Objects.equals(name, other.name) &&
                Objects.equals(parent, other.parent) &&
                Objects.equals(language, other.language);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, parent, language);
    }

    @Override
    public String toString() {
        return getPath();
    }

    /**
     * This method compares the given module keys and returns if they are equal when their languages are ignored.
     *
     * @param a
     *      the first module key
     * @param b
     *      the second module key
     *
     * @return
     *      if the given module keys are equal when ignoring their languages
     */
    public static boolean equalsLanguageAgnostic(ModuleKey a, ModuleKey b) {
        if (a == b) return true;
        if (a == null || b == null) return false;
        if (!a.name.equals(b.name)) return false;

        return equalsLanguageAgnostic(a.parent, b.parent);
    }
}
