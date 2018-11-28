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
        super(genName(path), genParent(path, language));
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

        return getParent().getPath() + PATH_SEPARATOR + name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LanguageModuleKey other = (LanguageModuleKey) o;

        boolean x = Objects.equals(name, other.name) &&
                Objects.equals(getParent(), other.getParent()) &&
                Objects.equals(language, other.language);
        if (!x && other.toString().equals(this.toString())) {
            System.out.println("TAICO: WARNING: uneqal, but equal strings");
            System.out.println(other.toString());
        }
        return x;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, getParent(), language);
    }

    @Override
    public String toString() {
        return getPath();
    }

    public String toIdentityString() {
        if (parent == null) {
            return "L=" + language + ", N=" + name + ", P=null";
        } else {
            return "L=" + language + ", N=" + name + ", P=" + getParent().toIdentityString();
        }
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

    /**
     * Determines the name of a module from a path.
     *
     * @param path
     *      the path
     *
     * @return
     *      the name component of the path
     */
    private static String genName(String path) {
        String[] parts = path.split(PATH_SEPARATOR, 2);
        if (parts.length == 1) {
            return parts[0];
        } else {
            return parts[1];
        }
    }

    /**
     * Determines the parent from a path.
     *
     * @param path
     *      the path
     * @param language
     *      the language
     *
     * @return
     *      the parent of the path
     */
    private static LanguageModuleKey genParent(String path, String language) {
        String[] parts = path.split(PATH_SEPARATOR, 2);
        if (parts.length == 1) {
            return null;
        } else {
            return new LanguageModuleKey(parts[0], language);
        }
    }
}
