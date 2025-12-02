package com.minecrafttradingcenter.network.packet;

import com.minecrafttradingcenter.data.TradeEntry;
import com.minecrafttradingcenter.data.TradingCenterData;
import com.minecrafttradingcenter.network.ModMessages;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;
import java.util.function.Supplier;

public class RequestTradesPacket {
    public RequestTradesPacket() {
    }

    public RequestTradesPacket(FriendlyByteBuf buf) {
    }

    public void toBytes(FriendlyByteBuf buf) {
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null && player.level() instanceof ServerLevel serverLevel) {
                TradingCenterData data = TradingCenterData.get(serverLevel);
                List<TradeEntry> trades = data.getAllTrades();
                int bankBalance = data.getBankBalance(player.getScoreboardName());
                ModMessages.sendToPlayer(new SendTradesPacket(trades, bankBalance), player);
            }
        });
        return true;
    }
}

