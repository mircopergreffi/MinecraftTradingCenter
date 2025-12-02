package com.minecrafttradingcenter.network.packet;

import com.minecrafttradingcenter.data.TradeEntry;
import com.minecrafttradingcenter.data.TradingCenterData;
import com.minecrafttradingcenter.network.ModMessages;
import com.minecrafttradingcenter.network.packet.SendTradesPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ExecuteTradePacket {
    private final int tradeId;
    private final int multiplier;

    public ExecuteTradePacket(int tradeId, int multiplier) {
        this.tradeId = tradeId;
        this.multiplier = multiplier;
    }

    public ExecuteTradePacket(FriendlyByteBuf buf) {
        this.tradeId = buf.readInt();
        this.multiplier = buf.readInt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(tradeId);
        buf.writeInt(multiplier);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null && player.level() instanceof ServerLevel serverLevel) {
                TradingCenterData data = TradingCenterData.get(serverLevel);
                TradeEntry trade = data.getTrade(tradeId);
                
                if (trade == null) {
                    // Trade doesn't exist
                    return;
                }
                
                if (trade.getAvailableTrades() < multiplier) {
                    // Not enough available trades
                    return;
                }
                
                int requiredEmeralds = trade.getWanted() * multiplier;
                
                // Count emeralds in player's inventory
                int emeraldCount = 0;
                for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                    ItemStack stack = player.getInventory().getItem(i);
                    if (!stack.isEmpty() && stack.is(Items.EMERALD)) {
                        emeraldCount += stack.getCount();
                    }
                }
                
                if (emeraldCount < requiredEmeralds) {
                    // Not enough emeralds
                    return;
                }
                
                // Remove emeralds
                int toRemove = requiredEmeralds;
                for (int i = 0; i < player.getInventory().getContainerSize() && toRemove > 0; i++) {
                    ItemStack stack = player.getInventory().getItem(i);
                    if (!stack.isEmpty() && stack.is(Items.EMERALD)) {
                        int removeAmount = Math.min(stack.getCount(), toRemove);
                        stack.shrink(removeAmount);
                        toRemove -= removeAmount;
                    }
                }
                
                // Give items
                ItemStack givenItem = trade.getGiven();
                int totalGiven = givenItem.getCount() * multiplier;
                
                while (totalGiven > 0) {
                    int stackSize = Math.min(givenItem.getMaxStackSize(), totalGiven);
                    ItemStack stack = givenItem.copy();
                    stack.setCount(stackSize);
                    if (!player.getInventory().add(stack)) {
                        player.drop(stack, false);
                    }
                    totalGiven -= stackSize;
                }
                
                // Execute trade
                data.executeTrade(tradeId, multiplier);
                
                // Send updated trades
                int bankBalance = data.getBankBalance(player.getScoreboardName());
                ModMessages.sendToPlayer(new SendTradesPacket(data.getAllTrades(), bankBalance), player);
            }
        });
        return true;
    }
}

