package com.minecrafttradingcenter.network.packet;

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

public class WithdrawEmeraldsPacket {
    private final int amount;

    public WithdrawEmeraldsPacket(int amount) {
        this.amount = amount;
    }

    public WithdrawEmeraldsPacket(FriendlyByteBuf buf) {
        this.amount = buf.readInt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(amount);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null && player.level() instanceof ServerLevel serverLevel) {
                TradingCenterData data = TradingCenterData.get(serverLevel);
                int withdrawn = data.withdrawFromBank(player.getScoreboardName(), amount);
                
                if (withdrawn > 0) {
                    // Give emeralds to player
                    int remaining = withdrawn;
                    while (remaining > 0) {
                        int stackSize = Math.min(64, remaining);
                        ItemStack emeraldStack = new ItemStack(Items.EMERALD, stackSize);
                        if (!player.getInventory().add(emeraldStack)) {
                            player.drop(emeraldStack, false);
                        }
                        remaining -= stackSize;
                    }
                }
                
                // Send updated bank balance
                int bankBalance = data.getBankBalance(player.getScoreboardName());
                ModMessages.sendToPlayer(new SendTradesPacket(data.getAllTrades(), bankBalance), player);
            }
        });
        return true;
    }
}

