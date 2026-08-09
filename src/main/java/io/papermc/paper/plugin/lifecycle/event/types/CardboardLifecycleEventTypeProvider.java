package io.papermc.paper.plugin.lifecycle.event.types;

import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEvent;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventOwner;
import io.papermc.paper.plugin.lifecycle.event.registrar.ReloadableRegistrarEvent;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.tag.PostFlattenTagRegistrar;
import io.papermc.paper.tag.PreFlattenTagRegistrar;

/**
 * Backs the lifecycle event API. Found through {@code META-INF/services}; without it every
 * reference to {@link LifecycleEvents} fails to initialise.
 */
public class CardboardLifecycleEventTypeProvider implements LifecycleEventTypeProvider {

    @Override
    public <O extends LifecycleEventOwner, E extends LifecycleEvent> LifecycleEventType.Monitorable<O, E> monitor(String name, Class<? extends O> ownerType) {
        return new CardboardMonitorableEventType<>(name);
    }

    @Override
    public <O extends LifecycleEventOwner, E extends LifecycleEvent> LifecycleEventType.Prioritizable<O, E> prioritized(String name, Class<? extends O> ownerType) {
        return new CardboardPrioritizableEventType<>(name);
    }

    @Override
    public TagEventTypeProvider tagProvider() {
        return new CardboardTagEventTypeProvider();
    }

    /**
     * Tag rewriting happens during plugin bootstrap, which Cardboard does not run. The event types
     * are still handed out so that touching them does not crash; they simply never fire.
     */
    private static final class CardboardTagEventTypeProvider implements TagEventTypeProvider {

        @Override
        public <T> LifecycleEventType.Prioritizable<BootstrapContext, ReloadableRegistrarEvent<PreFlattenTagRegistrar<T>>> preFlatten(RegistryKey<T> registryKey) {
            return new CardboardPrioritizableEventType<>("tags/pre_flatten/" + registryKey);
        }

        @Override
        public <T> LifecycleEventType.Prioritizable<BootstrapContext, ReloadableRegistrarEvent<PostFlattenTagRegistrar<T>>> postFlatten(RegistryKey<T> registryKey) {
            return new CardboardPrioritizableEventType<>("tags/post_flatten/" + registryKey);
        }
    }
}
