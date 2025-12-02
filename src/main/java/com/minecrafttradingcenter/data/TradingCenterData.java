package com.minecrafttradingcenter.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TradingCenterData extends SavedData {
    private final Map<Integer, TradeEntry> trades = new HashMap<>();
    private final Map<String, Integer> bankBalances = new HashMap<>(); // Player name -> emerald count
    private int nextTradeId = 1;

    public TradingCenterData() {
    }

    public TradingCenterData(CompoundTag tag) {
        ListTag tradesList = tag.getList("Trades", 10);
        for (int i = 0; i < tradesList.size(); i++) {
            CompoundTag tradeTag = tradesList.getCompound(i);
            TradeEntry entry = TradeEntry.load(tradeTag);
            trades.put(entry.getTradeId(), entry);
        }

        CompoundTag bankTag = tag.getCompound("Bank");
        for (String key : bankTag.getAllKeys()) {
            bankBalances.put(key, bankTag.getInt(key));
        }

        nextTradeId = tag.getInt("NextTradeId");
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag tradesList = new ListTag();
        for (TradeEntry entry : trades.values()) {
            tradesList.add(entry.save(new CompoundTag()));
        }
        tag.put("Trades", tradesList);

        CompoundTag bankTag = new CompoundTag();
        for (Map.Entry<String, Integer> entry : bankBalances.entrySet()) {
            bankTag.putInt(entry.getKey(), entry.getValue());
        }
        tag.put("Bank", bankTag);

        tag.putInt("NextTradeId", nextTradeId);
        return tag;
    }

    public static TradingCenterData get(ServerLevel level) {
        DimensionDataStorage storage = level.getDataStorage();
        return storage.computeIfAbsent(TradingCenterData::new, TradingCenterData::new, "trading_center_data");
    }

    public int createTrade(String seller, int wanted, net.minecraft.world.item.ItemStack given, int tradeCount) {
        int tradeId = nextTradeId++;
        TradeEntry entry = new TradeEntry(tradeId, seller, wanted, given, tradeCount);
        trades.put(tradeId, entry);
        setDirty();
        return tradeId;
    }

    public List<TradeEntry> getAllTrades() {
        return new ArrayList<>(trades.values());
    }

    public TradeEntry getTrade(int tradeId) {
        return trades.get(tradeId);
    }

    public boolean executeTrade(int tradeId, int multiplier) {
        TradeEntry entry = trades.get(tradeId);
        if (entry == null || entry.getAvailableTrades() < multiplier) {
            return false;
        }

        entry.executeTrade(multiplier);
        int emeraldAmount = entry.getWanted() * multiplier;
        addToBank(entry.getSeller(), emeraldAmount);
        setDirty();
        return true;
    }

    public void addToBank(String playerName, int emeralds) {
        bankBalances.put(playerName, bankBalances.getOrDefault(playerName, 0) + emeralds);
        setDirty();
    }

    public int getBankBalance(String playerName) {
        return bankBalances.getOrDefault(playerName, 0);
    }

    public int withdrawFromBank(String playerName, int amount) {
        int current = bankBalances.getOrDefault(playerName, 0);
        int withdrawn = Math.min(current, amount);
        bankBalances.put(playerName, current - withdrawn);
        if (bankBalances.get(playerName) == 0) {
            bankBalances.remove(playerName);
        }
        setDirty();
        return withdrawn;
    }
}

