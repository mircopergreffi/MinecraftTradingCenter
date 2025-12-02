package com.minecrafttradingcenter.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public class TradeEntry {
    private int tradeId;
    private String seller;
    private int wanted; // emeralds
    private ItemStack given; // items to give
    private int trades; // available trades
    private int traded; // completed trades

    public TradeEntry() {
        this.tradeId = 0;
        this.seller = "";
        this.wanted = 0;
        this.given = ItemStack.EMPTY;
        this.trades = 0;
        this.traded = 0;
    }

    public TradeEntry(int tradeId, String seller, int wanted, ItemStack given, int trades) {
        this.tradeId = tradeId;
        this.seller = seller;
        this.wanted = wanted;
        this.given = given.copy();
        this.given.setCount(given.getCount());
        this.trades = trades;
        this.traded = 0;
    }

    public CompoundTag save(CompoundTag tag) {
        tag.putInt("TradeId", tradeId);
        tag.putString("Seller", seller);
        tag.putInt("Wanted", wanted);
        tag.put("Given", given.save(new CompoundTag()));
        tag.putInt("Trades", trades);
        tag.putInt("Traded", traded);
        return tag;
    }

    public static TradeEntry load(CompoundTag tag) {
        TradeEntry entry = new TradeEntry();
        entry.tradeId = tag.getInt("TradeId");
        entry.seller = tag.getString("Seller");
        entry.wanted = tag.getInt("Wanted");
        entry.given = ItemStack.of(tag.getCompound("Given"));
        entry.trades = tag.getInt("Trades");
        entry.traded = tag.getInt("Traded");
        return entry;
    }

    public int getTradeId() {
        return tradeId;
    }

    public String getSeller() {
        return seller;
    }

    public int getWanted() {
        return wanted;
    }

    public ItemStack getGiven() {
        return given.copy();
    }

    public int getTrades() {
        return trades;
    }

    public int getTraded() {
        return traded;
    }

    public int getAvailableTrades() {
        return trades - traded;
    }

    public void executeTrade(int amount) {
        this.traded = Math.min(this.traded + amount, this.trades);
    }

    public boolean isComplete() {
        return traded >= trades;
    }
}

