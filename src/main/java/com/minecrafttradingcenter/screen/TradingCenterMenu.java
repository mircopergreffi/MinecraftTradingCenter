package com.minecrafttradingcenter.screen;

import com.minecrafttradingcenter.data.TradeEntry;
import com.minecrafttradingcenter.data.TradingCenterData;
import com.minecrafttradingcenter.network.ModMessages;
import com.minecrafttradingcenter.network.packet.CreateTradePacket;
import com.minecrafttradingcenter.network.packet.ExecuteTradePacket;
import com.minecrafttradingcenter.network.packet.RequestTradesPacket;
import com.minecrafttradingcenter.network.packet.WithdrawEmeraldsPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.List;

public class TradingCenterMenu extends AbstractContainerMenu {
    private final Player player;
    private final Level level;
    private List<TradeEntry> trades;
    private int bankBalance = 0;
    private final SimpleContainer tradeItemContainer = new SimpleContainer(1);

    public TradingCenterMenu(int containerId, Inventory inventory, FriendlyByteBuf extraData) {
        this(containerId, inventory);
    }

    public TradingCenterMenu(int containerId, Inventory inventory) {
        super(ModMenuTypes.TRADING_CENTER_MENU.get(), containerId);
        this.player = inventory.player;
        this.level = inventory.player.level();

        // Trade item slot (for items to sell) - bottom left of upper grid
        // Position: row 6 (bottom), column 0 (left) = y = 18 + 5*18 = 108, x = 8
        this.addSlot(new TradeItemSlot(tradeItemContainer, 0, 8, 108));

        // Player inventory slots - aligned with GUI texture
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(inventory, j + i * 9 + 9, 8 + j * 18, 140 + i * 18));
            }
        }

        // Player hotbar
        for (int k = 0; k < 9; ++k) {
            this.addSlot(new Slot(inventory, k, 8 + k * 18, 198));
        }
    }
    
    public ItemStack getTradeItem() {
        return tradeItemContainer.getItem(0);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();
            if (index == 0) {
                // Trade item slot - move to player inventory
                if (!this.moveItemStackTo(itemstack1, 1, 37, true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // Player inventory - move to trade slot if empty
                if (index >= 1 && index < 37) {
                    if (!this.moveItemStackTo(itemstack1, 0, 1, false)) {
                        if (index < 28) {
                            if (!this.moveItemStackTo(itemstack1, 28, 37, false)) {
                                return ItemStack.EMPTY;
                            }
                        } else if (index >= 28 && index < 37 && !this.moveItemStackTo(itemstack1, 1, 28, false)) {
                            return ItemStack.EMPTY;
                        }
                    }
                }
            }

            if (itemstack1.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return itemstack;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (!level.isClientSide) {
            // Drop trade item if menu is closed
            ItemStack itemstack = tradeItemContainer.removeItemNoUpdate(0);
            if (!itemstack.isEmpty()) {
                player.drop(itemstack, false);
            }
        }
    }

    public Player getPlayer() {
        return player;
    }

    public Level getLevel() {
        return level;
    }

    public void setTrades(List<TradeEntry> trades) {
        this.trades = trades;
    }

    public List<TradeEntry> getTrades() {
        return trades;
    }

    public int getBankBalance() {
        return bankBalance;
    }

    public void setBankBalance(int balance) {
        this.bankBalance = balance;
    }
}

