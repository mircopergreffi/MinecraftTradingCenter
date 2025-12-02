package com.minecrafttradingcenter.screen;

import com.minecrafttradingcenter.data.TradeEntry;
import com.minecrafttradingcenter.network.ModMessages;
import com.minecrafttradingcenter.network.packet.CreateTradePacket;
import com.minecrafttradingcenter.network.packet.ExecuteTradePacket;
import com.minecrafttradingcenter.network.packet.RequestTradesPacket;
import com.minecrafttradingcenter.network.packet.WithdrawEmeraldsPacket;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

public class TradingCenterScreen extends AbstractContainerScreen<TradingCenterMenu> {
    private static final ResourceLocation GUI_TEXTURE = new ResourceLocation("textures/gui/container/generic_54.png");
    private EditBox wantedField;
    private EditBox givenField;
    private EditBox withdrawField;
    private EditBox tradeMultiplierField;
    private int selectedTradeId = -1;
    private int scrollOffset = 0;
    private static final int TRADES_PER_PAGE = 6;
    private ItemStack selectedItemForTrade = ItemStack.EMPTY;

    public TradingCenterScreen(TradingCenterMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 256;
        this.imageHeight = 240;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void init() {
        super.init();
        this.leftPos = (this.width - this.imageWidth) / 2;
        this.topPos = (this.height - this.imageHeight) / 2;
        
        // Request trades from server when screen opens
        ModMessages.sendToServer(new RequestTradesPacket());

        // Create Trade Section - Top Left
        this.wantedField = new EditBox(this.font, leftPos + 10, topPos + 25, 60, 16, Component.literal("Emeralds"));
        this.wantedField.setMaxLength(10);
        this.wantedField.setValue("1");
        this.wantedField.setHint(Component.literal("Emeralds"));
        this.addWidget(this.wantedField);

        this.givenField = new EditBox(this.font, leftPos + 80, topPos + 25, 60, 16, Component.literal("Count"));
        this.givenField.setMaxLength(10);
        this.givenField.setValue("1");
        this.givenField.setHint(Component.literal("Count"));
        this.addWidget(this.givenField);

        // Buttons
        this.addRenderableWidget(Button.builder(Component.translatable("gui.minecrafttradingcenter.create_trade"), button -> {
            createTrade();
        }).bounds(leftPos + 150, topPos + 23, 90, 20).build());

        // Trade List Section - Middle
        // Scroll buttons
        this.addRenderableWidget(Button.builder(Component.literal("▲"), button -> {
            if (scrollOffset > 0) scrollOffset--;
        }).bounds(leftPos + 230, topPos + 50, 20, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("▼"), button -> {
            List<TradeEntry> trades = menu.getTrades();
            if (trades != null && scrollOffset + TRADES_PER_PAGE < trades.size()) {
                scrollOffset++;
            }
        }).bounds(leftPos + 230, topPos + 150, 20, 20).build());

        // Buy Trade Section - Bottom Left
        this.tradeMultiplierField = new EditBox(this.font, leftPos + 10, topPos + 180, 60, 16, Component.literal("Multiplier"));
        this.tradeMultiplierField.setMaxLength(10);
        this.tradeMultiplierField.setValue("1");
        this.tradeMultiplierField.setHint(Component.literal("x"));
        this.addWidget(this.tradeMultiplierField);

        this.addRenderableWidget(Button.builder(Component.translatable("gui.minecrafttradingcenter.buy_trade"), button -> {
            executeTrade();
        }).bounds(leftPos + 80, topPos + 178, 80, 20).build());

        // Bank Section - Bottom Right
        this.withdrawField = new EditBox(this.font, leftPos + 170, topPos + 180, 60, 16, Component.literal("Amount"));
        this.withdrawField.setMaxLength(10);
        this.withdrawField.setValue("0");
        this.withdrawField.setHint(Component.literal("Amount"));
        this.addWidget(this.withdrawField);

        this.addRenderableWidget(Button.builder(Component.translatable("gui.minecrafttradingcenter.withdraw_emeralds"), button -> {
            withdrawEmeralds();
        }).bounds(leftPos + 170, topPos + 200, 80, 20).build());
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        int i = this.leftPos;
        int j = this.topPos;
        
        // Draw main GUI background using Minecraft's standard container texture
        guiGraphics.blit(GUI_TEXTURE, i, j, 0, 0, this.imageWidth, 125);
        guiGraphics.blit(GUI_TEXTURE, i, j + 125, 0, 126, this.imageWidth, 96);
        
        // Draw separator lines
        guiGraphics.fill(i + 8, j + 48, i + 248, j + 49, 0xFF000000);
        guiGraphics.fill(i + 8, j + 48, i + 248, j + 47, 0xFF555555);
        
        // Draw trade list background
        guiGraphics.fill(i + 8, j + 50, i + 230, j + 170, 0xFF1A1A1A);
        guiGraphics.fill(i + 9, j + 51, i + 229, j + 169, 0xFF2A2A2A);
        
        // Draw selected item slot background (for creating trades)
        if (!selectedItemForTrade.isEmpty()) {
            guiGraphics.fill(i + 150, j + 48, i + 168, j + 66, 0xFF404040);
            guiGraphics.fill(i + 151, j + 49, i + 167, j + 65, 0xFF606060);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // Title
        guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 0x404040, false);
        
        // Section headers
        guiGraphics.drawString(this.font, Component.translatable("gui.minecrafttradingcenter.create_trade"), leftPos + 10, topPos + 12, 0xFFFFFF, false);
        guiGraphics.drawString(this.font, Component.translatable("gui.minecrafttradingcenter.wanted"), leftPos + 10, topPos + 35, 0xC0C0C0, false);
        guiGraphics.drawString(this.font, Component.translatable("gui.minecrafttradingcenter.given"), leftPos + 80, topPos + 35, 0xC0C0C0, false);
        
        // Available Trades header
        guiGraphics.drawString(this.font, Component.translatable("gui.minecrafttradingcenter.available"), leftPos + 10, topPos + 52, 0xFFFFFF, false);
        
        // Trade list
        List<TradeEntry> trades = menu.getTrades();
        if (trades != null && !trades.isEmpty()) {
            int yOffset = 65;
            int endIndex = Math.min(scrollOffset + TRADES_PER_PAGE, trades.size());
            for (int i = scrollOffset; i < endIndex; i++) {
                TradeEntry trade = trades.get(i);
                int y = yOffset + (i - scrollOffset) * 16;
                
                // Highlight selected trade
                if (selectedTradeId == trade.getTradeId()) {
                    guiGraphics.fill(leftPos + 8, topPos + y - 1, leftPos + 230, topPos + y + 15, 0x40FFFFFF);
                }
                
                // Draw trade info
                String sellerText = trade.getSeller().length() > 12 ? trade.getSeller().substring(0, 12) + "..." : trade.getSeller();
                String itemName = trade.getGiven().getDisplayName().getString();
                if (itemName.length() > 15) itemName = itemName.substring(0, 15) + "...";
                
                int color = (selectedTradeId == trade.getTradeId()) ? 0x00FF00 : 0xFFFFFF;
                guiGraphics.drawString(this.font, 
                    String.format("%d em × %d %s (%d/%d)", 
                        trade.getWanted(), 
                        trade.getGiven().getCount(), 
                        itemName,
                        trade.getAvailableTrades(),
                        trade.getTrades()),
                    leftPos + 10, topPos + y, color, false);
                
                // Draw seller name in smaller text
                guiGraphics.drawString(this.font, sellerText, leftPos + 10, topPos + y + 9, 0x888888, false);
            }
        } else {
            guiGraphics.drawString(this.font, Component.translatable("gui.minecrafttradingcenter.no_trades"), leftPos + 10, topPos + 65, 0x888888, false);
        }

        // Bank balance
        int bankBalance = menu.getBankBalance();
        guiGraphics.drawString(this.font, Component.translatable("gui.minecrafttradingcenter.bank_balance") + ": " + bankBalance, leftPos + 170, topPos + 165, 0x00FF00, false);
        
        // Draw selected item for trade creation
        if (!selectedItemForTrade.isEmpty()) {
            guiGraphics.renderItem(selectedItemForTrade, leftPos + 152, topPos + 50);
            guiGraphics.renderItemDecorations(this.font, selectedItemForTrade, leftPos + 152, topPos + 50);
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
        
        // Render tooltips for trade entries
        List<TradeEntry> trades = menu.getTrades();
        if (trades != null) {
            int yOffset = 65;
            int endIndex = Math.min(scrollOffset + TRADES_PER_PAGE, trades.size());
            for (int i = scrollOffset; i < endIndex; i++) {
                TradeEntry trade = trades.get(i);
                int y = topPos + yOffset + (i - scrollOffset) * 16;
                if (mouseX >= leftPos + 8 && mouseX < leftPos + 230 && mouseY >= y && mouseY < y + 16) {
                    guiGraphics.renderTooltip(this.font, 
                        List.of(
                            Component.literal("Seller: " + trade.getSeller()),
                            Component.literal("Price: " + trade.getWanted() + " Emeralds"),
                            Component.literal("Items: " + trade.getGiven().getCount() + "x " + trade.getGiven().getDisplayName().getString()),
                            Component.literal("Available: " + trade.getAvailableTrades() + "/" + trade.getTrades())
                        ),
                        java.util.Optional.empty(), 
                        mouseX, mouseY);
                }
            }
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (wantedField.isFocused() || givenField.isFocused() || withdrawField.isFocused() || tradeMultiplierField.isFocused()) {
            return super.keyPressed(keyCode, scanCode, modifiers);
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }

        if (wantedField.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        if (givenField.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        if (withdrawField.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        if (tradeMultiplierField.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }

        // Click on trade row to select
        List<TradeEntry> trades = menu.getTrades();
        if (trades != null) {
            int yOffset = 65;
            int endIndex = Math.min(scrollOffset + TRADES_PER_PAGE, trades.size());
            for (int i = scrollOffset; i < endIndex; i++) {
                TradeEntry trade = trades.get(i);
                int y = topPos + yOffset + (i - scrollOffset) * 16;
                if (mouseX >= leftPos + 8 && mouseX < leftPos + 230 && mouseY >= y && mouseY < y + 16) {
                    selectedTradeId = trade.getTradeId();
                    return true;
                }
            }
        }
        
        // Click on inventory to select item for trade
        if (mouseX >= leftPos + 8 && mouseX < leftPos + 176 && mouseY >= topPos + 84 && mouseY < topPos + 240) {
            // Calculate which slot was clicked
            int slotX = (int)(mouseX - leftPos - 8) / 18;
            int slotY = (int)(mouseY - topPos - 84) / 18;
            if (slotX >= 0 && slotX < 9 && slotY >= 0 && slotY < 3) {
                int slotIndex = slotY * 9 + slotX + 9; // +9 to skip hotbar
                Inventory inventory = menu.getPlayer().getInventory();
                if (slotIndex < inventory.getContainerSize()) {
                    ItemStack stack = inventory.getItem(slotIndex);
                    if (!stack.isEmpty()) {
                        selectedItemForTrade = stack.copy();
                        selectedItemForTrade.setCount(1);
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private void createTrade() {
        try {
            int wanted = Integer.parseInt(wantedField.getValue());
            int givenCount = Integer.parseInt(givenField.getValue());

            if (wanted <= 0 || givenCount <= 0) {
                return;
            }

            if (selectedItemForTrade.isEmpty()) {
                // Try to find item in inventory
                selectedItemForTrade = findItemInInventory();
                if (selectedItemForTrade.isEmpty()) {
                    return;
                }
            }

            // Verify we have enough items
            int availableCount = countItemsInInventory(selectedItemForTrade.getItem());
            if (availableCount < givenCount) {
                return;
            }

            // Create the trade packet
            ItemStack tradeItem = selectedItemForTrade.copy();
            tradeItem.setCount(givenCount);
            ModMessages.sendToServer(new CreateTradePacket(wanted, givenCount, tradeItem));
            
            // Clear fields after creating
            wantedField.setValue("1");
            givenField.setValue("1");
            selectedItemForTrade = ItemStack.EMPTY;
        } catch (NumberFormatException e) {
            // Invalid input
        }
    }

    private void executeTrade() {
        if (selectedTradeId == -1) {
            return;
        }

        try {
            int multiplier = Integer.parseInt(tradeMultiplierField.getValue());
            if (multiplier <= 0) {
                return;
            }

            ModMessages.sendToServer(new ExecuteTradePacket(selectedTradeId, multiplier));
        } catch (NumberFormatException e) {
            // Invalid input
        }
    }

    private void withdrawEmeralds() {
        try {
            int amount = Integer.parseInt(withdrawField.getValue());
            if (amount > 0) {
                ModMessages.sendToServer(new WithdrawEmeraldsPacket(amount));
                withdrawField.setValue("0");
            }
        } catch (NumberFormatException e) {
            // Invalid input
        }
    }

    private ItemStack findItemInInventory() {
        Inventory inventory = menu.getPlayer().getInventory();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (!stack.isEmpty() && !stack.is(Items.EMERALD)) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    private int countItemsInInventory(net.minecraft.world.item.Item item) {
        Inventory inventory = menu.getPlayer().getInventory();
        int count = 0;
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (!stack.isEmpty() && stack.is(item)) {
                count += stack.getCount();
            }
        }
        return count;
    }
}
