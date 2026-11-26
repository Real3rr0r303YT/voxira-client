package dev.voxira.client.module.utility;

import dev.voxira.client.module.Category;
import dev.voxira.client.module.Module;
import dev.voxira.client.module.setting.NumberSetting;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;

/**
 * Adds a "Reconnect" button with countdown to the disconnect screen.
 * Never reconnects after AutoLog left on purpose.
 */
public class AutoReconnectModule extends Module {
    private final NumberSetting delay = setting(new NumberSetting("delay", "Delay", 5, 1, 60, 1, "s"));

    private ServerData lastServer;

    public AutoReconnectModule() {
        super("auto_reconnect", "Auto Reconnect", "Rejoins the last server after a disconnect.", Category.UTILITY, false);
    }

    /** Called every tick (even when disabled) to remember the current server. */
    public void trackServer() {
        ServerData current = MC.getCurrentServer();
        if (current != null && MC.level != null) {
            lastServer = current;
        }
    }

    public ServerData lastServer() {
        return lastServer;
    }

    public int delayTicks() {
        return delay.intValue() * 20;
    }

    public void reconnect() {
        if (lastServer == null) {
            return;
        }
        ConnectScreen.startConnecting(new JoinMultiplayerScreen(new TitleScreen()), MC,
                ServerAddress.parseString(lastServer.ip), lastServer, false, null);
    }
}
