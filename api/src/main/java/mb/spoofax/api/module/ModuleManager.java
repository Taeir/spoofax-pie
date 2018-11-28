package mb.spoofax.api.module;

import mb.pie.vfs.path.PPath;

import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Class which manages the modules and the storage.
 */
public class ModuleManager {
    //TODO Should eventually not be a singleton but injected in the project (GUICE)
    private static ModuleManager instance = new ModuleManager();
    public static ModuleManager getInstance() {
        return instance;
    }

    private Map<ModuleKey, SModule> modules = new ConcurrentHashMap<>();
    private PPath modulesLocation;


    private ModuleManager() {}

    /**
     * Gets the module with the given key.
     *
     * @param key
     *      the key
     *
     * @return
     *      the module with the given key, or null if no such module exists
     */
    public SModule getModule(ModuleKey key) {
        return modules.get(key);
    }

    public SModule getModuleOrCreate(LanguageModuleKey key, Supplier<SModule> supplier) {
        SModule module = modules.get(key);
        if (module != null) return module;

        module = supplier.get();
        modules.put(key, module);
        return module;
    }

    /**
     * NOTE: The given map is a copy of the internal modules.
     *
     * @return
     *      a map containing all modules
     */
    public Map<ModuleKey, SModule> getAllModules() {
        return new HashMap<>(modules);
    }

    //TODO Register for updates to map of modules?

    /**
     * Adds multiple modules.
     *
     * @param modules
     *      the modules to add
     *
     * @throws DuplicateModuleException
     *      If a module already exists with the same key as one of the given modules.
     */
    public void addModules(List<? extends SModule> modules) {
        List<ModuleKey> duplicates = new ArrayList<>();
        for (SModule module : modules) {
            SModule oldModule = this.modules.putIfAbsent(module.getId(), module);
            if (oldModule != null && oldModule != module) {
                duplicates.add(module.getId());
            }
        }

        if (!duplicates.isEmpty()) {
            throw new DuplicateModuleException("Modules " + duplicates + " already exist!", duplicates);
        }
    }

    /**
     * Adds a module.
     *
     * @param module
     *      the module to add
     *
     * @throws DuplicateModuleException
     *      If a module already exists with the same key as the given module.
     */
    public void addModule(SModule module) throws DuplicateModuleException {
        SModule oldModule = modules.putIfAbsent(module.getId(), module);
        if (oldModule != null && oldModule != module) {
            throw new DuplicateModuleException("Module " + module.getId() + " already exists!", Collections.singletonList(module.getId()));
        }
    }

    /**
     * Creates a new module with the given key and language.
     *
     * The module is automatically added to the manager.
     *
     * @param key
     *      the key of the module
     *
     * @return
     *      the created module
     */
    public SModule createModule(LanguageModuleKey key) {
        SModule module = new FloatingModuleImpl(key);
        addModule(module);
        return module;
    }

    /**
     * Removes the module with the given key.
     *
     * @param key
     *      the key
     *
     * @return
     *      true if a module was removed, false otherwise
     */
    public boolean removeModule(ModuleKey key) {
        SModule module = modules.remove(key);

        if (module == null) return false;

        //Delete the module file if it exists
        if (modulesLocation != null)  {
            try {
                Files.deleteIfExists(module.getModuleFile().toPath());
            } catch (IOException e) {
                //Silently swallow exception
            }
        }
        return true;
    }

    /**
     * Searches for all modules with the given key, while ignoring the language.
     *
     * @param key
     *      the key
     *
     * @return
     *      a set of all eligible modules
     */
    public Set<SModule> findModuleLanguageAgnostic(ModuleKey key) {
        if (key == null) throw new NullPointerException("Argument must not be null.");

        SModule module = modules.get(key);
        if (module != null) return Collections.singleton(module);

        Set<SModule> options = new HashSet<>();
        for (Entry<ModuleKey, SModule> entry : modules.entrySet()) {
            if (!LanguageModuleKey.equalsLanguageAgnostic(entry.getKey(), key)) continue;

            options.add(entry.getValue());
        }

        return options;
    }

    public SModule findModule(LanguageModuleKey key) {
        return getModule(key);
    }

    /**
     * Saves the given module to file.
     *
     * @param module
     *      the module to save
     *
     * @throws IOException
     */
    public void saveModule(SModule module) throws IOException {
        File file = getModuleFile(module);

        //Create the folders for the module
        if (!file.getParentFile().exists() && !file.getParentFile().mkdirs()) {
            throw new IOException("Unable to create folders for module " + module.getId() + ". (Folders: " + file.getParentFile() + ")");
        }

        try (ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(file)))) {
            oos.writeObject(module);
        }
    }

    /**
     * Gets the file corresponding to the given module.
     *
     * @param module
     *      the module
     *
     * @return
     *      the file corresponding to the given module
     *
     * @throws IllegalStateException
     *      If the modules location has not been set.
     */
    public File getModuleFile(SModule module) throws IllegalStateException {
        requireModulesLocation();

        PPath path = modulesLocation.resolve(module.getId().getPath());
        return path.getJavaPath().toFile();
    }

    /**
     * Loads all modules in the modules directory.
     *
     * @return
     *      a collection of all modules that were loaded
     *
     * @throws IOException
     *      If reading one or more modules fails.
     * @throws IllegalStateException
     *      If the modules location has not been set.
     */
    public Collection<SModule> loadModules() throws IOException, IllegalStateException {
        requireModulesLocation();

        return Files.walk(modulesLocation.getJavaPath()).map(path -> {
            if (!Files.isRegularFile(path)) return null;

            System.out.println("Visiting path " + path);
            SModule module = null;
            try (ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(path.toFile())))) {
                module = (SModule) ois.readObject();
                addModule(module);
            } catch (IOException e) {
                System.err.println("Failed to read module from " + path);
                e.printStackTrace();
                //TODO TAICO Delete module file
            } catch (ClassNotFoundException e) {
                //TODO logging
                System.err.println("Type of module at " + path + " is unknown");
                e.printStackTrace();
            }
            return module;
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    /**
     * Saves all modules to file.
     *
     * This method will attempt to save all the given modules and will throw one single IOException at the end if
     * saving failed for any of the modules. The thrown exception will have a {@link MultiException} as inner
     * exception.
     *
     * @throws IOException
     *      If saving one or more modules fails.
     * @throws IllegalStateException
     *      If the location for storing modules has not been set.
     */
    public void saveModules() throws IOException, IllegalStateException {
        requireModulesLocation();

        Map<SModule, IOException> failed = new HashMap<>();
        for (SModule module : modules.values()) {
            try {
                module.save();
            } catch (IOException ex) {
                failed.put(module, ex);
            }
        }

        if (!failed.isEmpty()) {
            throw new IOException(new MultiException(failed.values()));
        }
    }

    /**
     * Sets the location where modules are stored to the given location.
     *
     * @param location
     *      the location where modules are stored
     */
    public void setModulesLocation(PPath location) {
        this.modulesLocation = location;
    }

    /**
     * @throws IllegalStateException
     *      If the location for storing modules has not been set.
     */
    private void requireModulesLocation() {
        if (modulesLocation == null) throw new IllegalStateException("The modules location has not been set");
    }
}
