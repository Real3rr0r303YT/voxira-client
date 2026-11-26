package dev.voxira.client.module;

import dev.voxira.client.module.hud.ArmorStatusModule;
import dev.voxira.client.module.hud.CoordinatesModule;
import dev.voxira.client.module.hud.CpsModule;
import dev.voxira.client.module.hud.FpsModule;
import dev.voxira.client.module.hud.HudModule;
import dev.voxira.client.module.hud.KeystrokesModule;
import dev.voxira.client.module.hud.ClockModule;
import dev.voxira.client.module.hud.PingModule;
import dev.voxira.client.module.hud.ServerModule;
import dev.voxira.client.module.hud.SpeedModule;
import dev.voxira.client.module.utility.AutoLogModule;
import dev.voxira.client.module.utility.AutoReconnectModule;
import dev.voxira.client.module.utility.CustomMenuModule;
import dev.voxira.client.module.visual.CrosshairModule;
import dev.voxira.client.module.visual.LowHealthModule;
import dev.voxira.client.module.visual.FullbrightModule;
import dev.voxira.client.module.visual.ZoomModule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public final class ModuleManager {
    private final List<Module> modules = new ArrayList<>();

    public final FpsModule fps = add(new FpsModule());
    public final CpsModule cps = add(new CpsModule());
    public final CoordinatesModule coordinates = add(new CoordinatesModule());
    public final KeystrokesModule keystrokes = add(new KeystrokesModule());
    public final ArmorStatusModule armorStatus = add(new ArmorStatusModule());
    public final PingModule ping = add(new PingModule());
    public final ClockModule clock = add(new ClockModule());
    public final SpeedModule speed = add(new SpeedModule());
    public final ServerModule server = add(new ServerModule());
    public final ZoomModule zoom = add(new ZoomModule());
    public final FullbrightModule fullbright = add(new FullbrightModule());
    public final CrosshairModule crosshair = add(new CrosshairModule());
    public final LowHealthModule lowHealth = add(new LowHealthModule());
    public final CustomMenuModule customMenu = add(new CustomMenuModule());
    public final AutoReconnectModule autoReconnect = add(new AutoReconnectModule());
    public final AutoLogModule autoLog = add(new AutoLogModule());

    private <T extends Module> T add(T module) {
        modules.add(module);
        return module;
    }

    public List<Module> all() {
        return Collections.unmodifiableList(modules);
    }

    public List<HudModule> hudModules() {
        List<HudModule> result = new ArrayList<>();
        for (Module m : modules) {
            if (m instanceof HudModule hud) {
                result.add(hud);
            }
        }
        return result;
    }

    public Optional<Module> byId(String id) {
        return modules.stream().filter(m -> m.id().equals(id)).findFirst();
    }

    public void tick() {
        for (Module m : modules) {
            if (m.isEnabled()) {
                m.onTick();
            }
        }
    }
}
