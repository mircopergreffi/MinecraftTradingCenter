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

    public TradingCenterMenu(int containerId, Inventory inventory, FriendlyByteBuf extraData) {
        this(containerId, inventory);
    }

    public TradingCenterMenu(int containerId, Inventory inventory) {
        super(ModMenuTypes.TRADING_CENTER_MENU.get(), containerId);
        this.player = inventory.player;
        this.level = inventory.player.level();

        // Player inventory slots
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(inventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }

        // Player hotbar
        for (int k = 0; k < 9; ++k) {
            this.addSlot(new Slot(inventory, k, 8 + k * 18, 142));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
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

    public void createTrade(int wanted, int givenCount, ItemStack givenItem) {
        if (!level.isClientSide) {
            ModMessages.sendToPlayer(new CreateTradePacket(wanted, givenCount, givenItem), (ServerPlayer) player);
        }
    }

    public void executeTrade(int tradeId, int multiplier) {
        if (!level.isClientSide) {
            ModMessages.sendToPlayer(new ExecuteTradePacket(tradeId, multiplier), (ServerPlayer) player);
        }
    }

    public void withdrawEmeralds(int amount) {
        if (!level.isClientSide) {
            ModMessages.sendToPlayer(new WithdrawEmeraldsPacket(amount), (ServerPlayer) player);
        }
    }

    public int getBankBalance() {
        return bankBalance;
    }

    public void setBankBalance(int balance) {
        this.bankBalance = balance;
    }
}

