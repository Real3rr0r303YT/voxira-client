package dev.voxira.client.module.utility;

import dev.voxira.client.module.Category;
import dev.voxira.client.module.Module;
import dev.voxira.client.module.setting.BoolSetting;
import dev.voxira.client.module.setting.NumberSetting;
import net.minecraft.client.gui.screens.DisconnectedScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Enemy;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/**
 * Leaves the world automatically when it gets dangerous. Every trigger can be
 * switched on or off and tuned in the settings.
 */
public class AutoLogModule extends Module {
    /** Ticks to wait after joining before any trigger fires (3 seconds). */
    private static final int GRACE_TICKS = 60;

    private final BoolSetting lowHealth = setting(new BoolSetting("low_health", "Low health", true));
    private final NumberSetting healthHearts = setting(new NumberSetting("health_hearts", "Health threshold", 1, 0.5, 10, 0.5, "hearts"));
    private final BoolSetting requireHostile = setting(new BoolSetting("require_hostile", "Only with hostile mob near", true));
    private final NumberSetting hostileRadius = setting(new NumberSetting("hostile_radius", "Hostile mob radius", 8, 2, 32, 1, "blocks"));

    private final BoolSetting playerNearby = setting(new BoolSetting("player_nearby", "Player nearby", false));
    private final NumberSetting playerRadius = setting(new NumberSetting("player_radius", "Player radius", 32, 4, 128, 1, "blocks"));

    private final BoolSetting playerTeleport = setting(new BoolSetting("player_teleport", "Player teleports to you", false));
    private final NumberSetting teleportRadius = setting(new NumberSetting("teleport_radius", "Teleport radius", 12, 2, 48, 1, "blocks"));

    private final BoolSetting disableAfter = setting(new BoolSetting("disable_after", "Turn off after leaving", true));

    private ClientLevel trackedLevel;
    private int ticksInWorld;
    private final Set<UUID> knownPlayers = new HashSet<>();
    private boolean triggeredThisSession;

    public AutoLogModule() {
        super("auto_log", "AutoLog", "Leaves automatically on low health or when players come near.", Category.SAFETY, false);
    }

    /** True after AutoLog left on purpose, until the next world is joined. AutoReconnect stays quiet then. */
    public boolean leftOnPurpose() {
        return triggeredThisSession;
    }

    @Override
    protected void onEnable() {
        resetTracking(null);
    }

    @Override
    public void onTick() {
        LocalPlayer self = MC.player;
        ClientLevel level = MC.level;
        if (self == null || level == null) {
            resetTracking(null);
            return;
        }
        if (level != trackedLevel) {
            resetTracking(level);
            triggeredThisSession = false;
        }
        ticksInWorld++;
        if (ticksInWorld < GRACE_TICKS || !self.isAlive()) {
            rememberPlayers(level, self);
            return;
        }

        String reason = check(level, self);
        if (reason != null) {
            leave(reason);
            return;
        }
        rememberPlayers(level, self);
    }

    private String check(ClientLevel level, LocalPlayer self) {
        if (lowHealth.get()) {
            float hearts = self.getHealth() / 2f;
            if (hearts <= healthHearts.floatValue()) {
                if (!requireHostile.get()) {
                    return String.format(Locale.ROOT, "Health dropped to %.1f hearts", hearts);
                }
                Entity mob = nearestHostile(level, self, hostileRadius.floatValue());
                if (mob != null) {
                    return String.format(Locale.ROOT, "Health %.1f hearts with %s %.0f blocks away",
                            hearts, mob.getName().getString(), self.distanceTo(mob));
                }
            }
        }

        for (AbstractClientPlayer other : level.players()) {
            if (other == self) {
                continue;
            }
            float distance = self.distanceTo(other);
            String name = other.getName().getString();

            if (playerTeleport.get() && distance <= teleportRadius.floatValue()
                    && !knownPlayers.contains(other.getUUID())) {
                return String.format(Locale.ROOT, "%s appeared %.0f blocks away", name, distance);
            }
            if (playerNearby.get() && distance <= playerRadius.floatValue()) {
                return String.format(Locale.ROOT, "%s is %.0f blocks away", name, distance);
            }
        }
        return null;
    }

    private Entity nearestHostile(ClientLevel level, LocalPlayer self, float radius) {
        Entity nearest = null;
        float best = radius;
        for (Entity entity : level.entitiesForRendering()) {
            if (entity instanceof Enemy && entity.isAlive()) {
                float d = self.distanceTo(entity);
                if (d <= best) {
                    best = d;
                    nearest = entity;
                }
            }
        }
        return nearest;
    }

    /**
     * A player counts as "teleported in" when they show up inside the teleport radius
     * without having been loaded before. Players who walk in are seen far away first.
     */
    private void rememberPlayers(ClientLevel level, LocalPlayer self) {
        knownPlayers.clear();
        for (AbstractClientPlayer other : level.players()) {
            if (other != self) {
                knownPlayers.add(other.getUUID());
            }
        }
    }

    private void resetTracking(ClientLevel level) {
        trackedLevel = level;
        ticksInWorld = 0;
        knownPlayers.clear();
    }

    private void leave(String reason) {
        boolean singleplayer = MC.isLocalServer();
        triggeredThisSession = true;
        if (disableAfter.get()) {
            setEnabled(false);
        }
        resetTracking(null);

        Component message = Component.literal("Voxira AutoLog: " + reason);
        MC.disconnectFromWorld(message);

        Screen parent = singleplayer ? new TitleScreen() : new JoinMultiplayerScreen(new TitleScreen());
        MC.gui.setScreen(new DisconnectedScreen(parent, Component.literal("AutoLog"), message));
    }
}
