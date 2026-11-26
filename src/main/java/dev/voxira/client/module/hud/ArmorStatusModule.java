package dev.voxira.client.module.hud;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

/** Armor + main hand with remaining durability. */
public class ArmorStatusModule extends HudModule {
    private static final EquipmentSlot[] SLOTS = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET, EquipmentSlot.MAINHAND
    };
    private static final int ROW = 18;

    public ArmorStatusModule() {
        super("armor_status", "Armor Status", "Armor and held item with durability.", true, 4, 140);
    }

    @Override
    public int width() {
        return 18 + MC.font.width("0000") + 4;
    }

    @Override
    public int height() {
        return ROW * SLOTS.length;
    }

    @Override
    protected void draw(GuiGraphicsExtractor ctx) {
        LocalPlayer player = MC.player;
        if (player == null) {
            ctx.fill(0, 0, width(), height(), BACKGROUND);
            ctx.text(MC.font, "Armor", 3, 4, TEXT, true);
            return;
        }
        for (int i = 0; i < SLOTS.length; i++) {
            ItemStack stack = player.getItemBySlot(SLOTS[i]);
            if (stack.isEmpty()) {
                continue;
            }
            int y = i * ROW;
            ctx.item(stack, 0, y);
            if (stack.isDamageableItem()) {
                int left = stack.getMaxDamage() - stack.getDamageValue();
                float ratio = left / (float) stack.getMaxDamage();
                int color = ratio > 0.5f ? 0xFF55FF55 : ratio > 0.2f ? 0xFFFFAA00 : 0xFFFF5555;
                ctx.text(MC.font, String.valueOf(left), 20, y + 5, color, true);
            } else if (stack.getCount() > 1) {
                ctx.text(MC.font, String.valueOf(stack.getCount()), 20, y + 5, TEXT, true);
            }
        }
    }
}
