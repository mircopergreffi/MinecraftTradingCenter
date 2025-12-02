package com.minecrafttradingcenter.network.packet;

import com.minecrafttradingcenter.data.TradingCenterData;
import com.minecrafttradingcenter.network.ModMessages;
import com.minecrafttradingcenter.network.packet.SendTradesPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class CreateTradePacket {
    private final int wanted;
    private final int givenCount;
    private final ItemStack givenItem;
    private final int tradeCount;

    public CreateTradePacket(int wanted, int givenCount, ItemStack givenItem) {
        this.wanted = wanted;
        this.givenCount = givenCount;
        this.givenItem = givenItem;
        this.tradeCount = 0; // Will be calculated on server
    }

    public CreateTradePacket(FriendlyByteBuf buf) {
        this.wanted = buf.readInt();
        this.givenCount = buf.readInt();
        this.givenItem = buf.readItem();
        this.tradeCount = buf.readInt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(wanted);
        buf.writeInt(givenCount);
        buf.writeItem(givenItem);
        buf.writeInt(tradeCount);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null && player.level() instanceof ServerLevel serverLevel) {
                TradingCenterData data = TradingCenterData.get(serverLevel);
                
                // Count items in player's inventory
                int availableCount = 0;
                ItemStack searchItem = givenItem.copy();
                searchItem.setCount(1);
                
                for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                    ItemStack stack = player.getInventory().getItem(i);
                    if (!stack.isEmpty() && ItemStack.isSameItemSameTags(stack, searchItem)) {
                        availableCount += stack.getCount();
                    }
                }
                
                int calculatedTradeCount = availableCount / givenCount;
                
                if (calculatedTradeCount > 0 && availableCount >= givenCount * calculatedTradeCount) {
                    // Remove items from inventory
                    int toRemove = givenCount * calculatedTradeCount;
                    for (int i = 0; i < player.getInventory().getContainerSize() && toRemove > 0; i++) {
                        ItemStack stack = player.getInventory().getItem(i);
                        if (!stack.isEmpty() && ItemStack.isSameItemSameTags(stack, searchItem)) {
                            int removeAmount = Math.min(stack.getCount(), toRemove);
                            stack.shrink(removeAmount);
                            toRemove -= removeAmount;
                        }
                    }
                    
                    // Create trade
                    ItemStack tradeItem = givenItem.copy();
                    tradeItem.setCount(givenCount);
                    data.createTrade(player.getScoreboardName(), wanted, tradeItem, calculatedTradeCount);
                    
                    // Send updated trades to client
                    int bankBalance = data.getBankBalance(player.getScoreboardName());
                    ModMessages.sendToPlayer(new SendTradesPacket(data.getAllTrades(), bankBalance), player);
                }
            }
        });
        return true;
    }
}

