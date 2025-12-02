package com.minecrafttradingcenter.network.packet;

import com.minecrafttradingcenter.data.TradeEntry;
import com.minecrafttradingcenter.screen.TradingCenterMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class SendTradesPacket {
    private final List<TradeEntry> trades;
    private final int bankBalance;

    public SendTradesPacket(List<TradeEntry> trades, int bankBalance) {
        this.trades = trades;
        this.bankBalance = bankBalance;
    }

    public SendTradesPacket(FriendlyByteBuf buf) {
        CompoundTag tag = buf.readNbt();
        if (tag == null) {
            this.trades = new ArrayList<>();
            this.bankBalance = 0;
            return;
        }
        ListTag tradesList = tag.getList("Trades", 10);
        this.trades = new ArrayList<>();
        for (int i = 0; i < tradesList.size(); i++) {
            trades.add(TradeEntry.load(tradesList.getCompound(i)));
        }
        this.bankBalance = tag.getInt("BankBalance");
    }

    public void toBytes(FriendlyByteBuf buf) {
        ListTag tradesList = new ListTag();
        for (TradeEntry entry : trades) {
            tradesList.add(entry.save(new CompoundTag()));
        }
        CompoundTag tag = new CompoundTag();
        tag.put("Trades", tradesList);
        tag.putInt("BankBalance", bankBalance);
        buf.writeNbt(tag);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            if (Minecraft.getInstance().player != null &&
                Minecraft.getInstance().player.containerMenu instanceof TradingCenterMenu menu) {
                menu.setTrades(trades);
                menu.setBankBalance(bankBalance);
            }
        });
        return true;
    }
}

