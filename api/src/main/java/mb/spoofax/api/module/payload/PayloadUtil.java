package mb.spoofax.api.module.payload;

import java.util.function.Consumer;

public class PayloadUtil {

    /**
     * Loops over all interfaces implemented by the given class, and calls the given consumer on each of them.
     *
     * @param clazz
     *      the class to loop over the interfaces of
     * @param consumer
     *      the consumer to call on each interface
     */
    public static void foreachInterface(Class<?> clazz, Consumer<Class<?>> consumer) {
        Class<?> currentClazz = clazz;
        do {
            for (Class<?> interfaceClass : currentClazz.getInterfaces()) {
                consumer.accept(interfaceClass);
            }
        } while ((currentClazz = clazz.getSuperclass()) != null);
    }
}
