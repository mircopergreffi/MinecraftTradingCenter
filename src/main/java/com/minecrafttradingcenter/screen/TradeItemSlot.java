package com.minecrafttradingcenter.screen;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class TradeItemSlot extends Slot {
    public TradeItemSlot(Container container, int slot, int x, int y) {
        super(container, slot, x, y);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return true; // Allow any item
    }

    @Override
    public int getMaxStackSize() {
        return 64;
    }
}

