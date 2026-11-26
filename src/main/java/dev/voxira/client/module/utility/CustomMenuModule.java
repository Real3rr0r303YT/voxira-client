package dev.voxira.client.module.utility;

import dev.voxira.client.module.Category;
import dev.voxira.client.module.Module;

/** Ersetzt den Vanilla-Titelbildschirm durch das Voxira-Hauptmenü. */
public class CustomMenuModule extends Module {
    public CustomMenuModule() {
        super("custom_menu", "Custom Main Menu", "Voxira-Hauptmenü statt Vanilla-Titelbildschirm.", Category.UTILITY, true);
    }
}
