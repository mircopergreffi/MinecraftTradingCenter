package com.minecrafttradingcenter.item;

import com.minecrafttradingcenter.network.ModMessages;
import com.minecrafttradingcenter.network.packet.RequestTradesPacket;
import com.minecrafttradingcenter.screen.TradingCenterMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class TradingLaptopItem extends Item {
    public TradingLaptopItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            player.openMenu(new MenuProvider() {
                @Override
                public Component getDisplayName() {
                    return Component.translatable("gui.minecrafttradingcenter.trading_center");
                }

                @Nullable
                @Override
                public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
                    return new TradingCenterMenu(containerId, inventory);
                }
            });
        }
        return InteractionResultHolder.success(player.getItemInHand(hand));
    }
}

