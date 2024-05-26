package net.minestom.server.notifications;

import net.kyori.adventure.text.Component;
import net.minestom.server.advancements.FrameType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.jetbrains.annotations.NotNull;

final class BuilderImpl implements Notification.Builder {
    private Component title;
    private FrameType type;
    private ItemStack icon;


    @Override
    public Notification.Builder title(@NotNull Component component) {
        this.title = component;
        return this;
    }

    @Override
    public Notification.Builder frameType(@NotNull FrameType frameType) {
        this.type = frameType;
        return this;
    }

    @Override
    public Notification.Builder icon(@NotNull Material material) {
        this.icon = ItemStack.of(material);
        return this;
    }

    @Override
    public Notification.Builder icon(@NotNull ItemStack itemStack) {
        this.icon = itemStack;
        return this;
    }

    @Override
    public Notification build() {
        return new NotificationImpl(title, type, icon);
    }
}
