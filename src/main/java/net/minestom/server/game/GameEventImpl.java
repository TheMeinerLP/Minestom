package net.minestom.server.game;

import net.kyori.adventure.key.Key;
import net.minestom.server.registry.Registry;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Represents a game event implementation.
 * Used for a wide variety of events, from weather to bed use to game mode to demo messages.
 *
 * @version 1.0.0
 * @since 1.6.0
 * @author themeinerlp
 */
record GameEventImpl(Registry.GameEventEntry registry, Key namespace, int id) implements GameEvent {
    private static final AtomicInteger INDEX = new AtomicInteger();
    private static final Registry.Container<GameEvent> CONTAINER = Registry.createStaticContainer(Registry.Resource.GAMEPLAY_TAGS, GameEventImpl::createImpl);

    /**
     * Creates a new {@link GameEventImpl} with the given namespace and properties.
     *
     * @param namespace the namespace
     * @param properties the properties
     * @return a new {@link GameEventImpl}
     */
    private static GameEventImpl createImpl(String namespace, Registry.Properties properties) {
        return new GameEventImpl(Registry.gameEventEntry(namespace, properties));
    }

    /**
     * Creates a new {@link GameEventImpl} with the given registry.
     *
     * @param registry the registry
     */
    private GameEventImpl(Registry.GameEventEntry registry) {
        this(registry, registry.namespace(), INDEX.getAndIncrement());
    }

    /**
     * Gets the game events from the registry.
     *
     * @return the game events
     */
    static Collection<GameEvent> values() {
        return CONTAINER.values();
    }

    /**
     * Gets a game event by its namespace ID.
     *
     * @param namespace the namespace ID
     * @return the game event or null if not found
     */
    public static GameEvent get(@NotNull String namespace) {
        return CONTAINER.get(namespace);
    }

    /**
     * Gets a game event by its namespace ID.
     *
     * @param namespace the namespace ID
     * @return the game event or null if not found
     */
    static GameEvent getSafe(@NotNull String namespace) {
        return CONTAINER.getSafe(namespace);
    }
}
