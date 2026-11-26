package dev.voxira.client.module.utility;

import dev.voxira.client.module.Category;
import dev.voxira.client.module.Module;

/** Replaces the vanilla title screen with the Voxira one. */
public class CustomMenuModule extends Module {
    public CustomMenuModule() {
        super("custom_menu", "Custom Main Menu", "Voxira title screen instead of the vanilla one.", Category.UTILITY, true);
    }
}
