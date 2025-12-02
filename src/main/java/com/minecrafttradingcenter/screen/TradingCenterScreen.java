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

import java.util.List;

public class TradingCenterScreen extends AbstractContainerScreen<TradingCenterMenu> {
    private static final ResourceLocation GUI_TEXTURE = new ResourceLocation("textures/gui/container/generic_54.png");
    private EditBox priceField;
    private EditBox withdrawField;
    private EditBox tradeMultiplierField;
    private int selectedTradeId = -1;
    private int scrollOffset = 0;
    private static final int TRADES_PER_PAGE = 15; // Fits in 5 rows (18 pixels per row, ~12 pixels per trade entry)

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

        // Price input field and Create Trade button - side by side at top
        this.priceField = new EditBox(this.font, leftPos + 8, topPos + 6, 60, 16, Component.literal("Price"));
        this.priceField.setMaxLength(10);
        this.priceField.setValue("1");
        this.priceField.setHint(Component.literal("Emeralds"));
        this.addWidget(this.priceField);

        this.addRenderableWidget(Button.builder(Component.translatable("gui.minecrafttradingcenter.create_trade"), button -> {
            createTrade();
        }).bounds(leftPos + 75, topPos + 4, 90, 20).build());

        // Scroll buttons for trade list (in the first 5 rows area)
        this.addRenderableWidget(Button.builder(Component.literal("▲"), button -> {
            if (scrollOffset > 0) scrollOffset--;
        }).bounds(leftPos + 230, topPos + 20, 20, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("▼"), button -> {
            List<TradeEntry> trades = menu.getTrades();
            if (trades != null && scrollOffset + TRADES_PER_PAGE < trades.size()) {
                scrollOffset++;
            }
        }).bounds(leftPos + 230, topPos + 80, 20, 20).build());

        // Buy Trade and Withdraw buttons - side by side at bottom
        this.tradeMultiplierField = new EditBox(this.font, leftPos + 8, topPos + 180, 50, 16, Component.literal("Multiplier"));
        this.tradeMultiplierField.setMaxLength(10);
        this.tradeMultiplierField.setValue("1");
        this.tradeMultiplierField.setHint(Component.literal("x"));
        this.addWidget(this.tradeMultiplierField);

        this.addRenderableWidget(Button.builder(Component.translatable("gui.minecrafttradingcenter.buy_trade"), button -> {
            executeTrade();
        }).bounds(leftPos + 65, topPos + 178, 80, 20).build());

        this.withdrawField = new EditBox(this.font, leftPos + 152, topPos + 180, 50, 16, Component.literal("Amount"));
        this.withdrawField.setMaxLength(10);
        this.withdrawField.setValue("0");
        this.withdrawField.setHint(Component.literal("Amount"));
        this.addWidget(this.withdrawField);

        this.addRenderableWidget(Button.builder(Component.translatable("gui.minecrafttradingcenter.withdraw_emeralds"), button -> {
            withdrawEmeralds();
        }).bounds(leftPos + 209, topPos + 178, 80, 20).build());
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        int i = this.leftPos;
        int j = this.topPos;
        
        // Draw main GUI background using Minecraft's standard container texture
        guiGraphics.blit(GUI_TEXTURE, i, j, 0, 0, this.imageWidth, 125);
        guiGraphics.blit(GUI_TEXTURE, i, j + 125, 0, 126, this.imageWidth, 96);
        
        // Draw trades list background - first 5 rows of upper grid (RGB 139,139,139)
        // Rows are 18 pixels tall, so 5 rows = 90 pixels, starting at y=18
        guiGraphics.fill(i + 8, j + 18, i + 248, j + 108, 0xFF8B8B8B);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // Title
        guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 0x404040, false);
        
        // Trade list - rendered in first 5 rows of upper grid (y=18 to y=108, 90 pixels total)
        List<TradeEntry> trades = menu.getTrades();
        if (trades != null && !trades.isEmpty()) {
            int startY = topPos + 20; // Start slightly below the top of the background
            int endIndex = Math.min(scrollOffset + TRADES_PER_PAGE, trades.size());
            for (int i = scrollOffset; i < endIndex; i++) {
                TradeEntry trade = trades.get(i);
                int y = startY + (i - scrollOffset) * 6; // 6 pixels per line to fit in 5 rows (90 pixels / 15 = 6)
                
                // Only render if within the first 5 rows (y < 108)
                if (y < topPos + 108) {
                    // Highlight selected trade
                    if (selectedTradeId == trade.getTradeId()) {
                        guiGraphics.fill(leftPos + 8, topPos + y - 1, leftPos + 248, topPos + y + 5, 0x40FFFFFF);
                    }
                    
                    // Draw trade info (compact format)
                    String sellerText = trade.getSeller().length() > 8 ? trade.getSeller().substring(0, 8) + "..." : trade.getSeller();
                    String itemName = trade.getGiven().getDisplayName().getString();
                    if (itemName.length() > 12) itemName = itemName.substring(0, 12) + "...";
                    
                    int color = (selectedTradeId == trade.getTradeId()) ? 0x00FF00 : 0xFFFFFF;
                    guiGraphics.drawString(this.font, 
                        String.format("%d em × %d %s [%d/%d] - %s", 
                            trade.getWanted(), 
                            trade.getGiven().getCount(), 
                            itemName,
                            trade.getAvailableTrades(),
                            trade.getTrades(),
                            sellerText),
                        leftPos + 10, topPos + y, color, false);
                }
            }
        } else {
            guiGraphics.drawString(this.font, Component.translatable("gui.minecrafttradingcenter.no_trades"), leftPos + 10, topPos + 30, 0x888888, false);
        }

        // Bank balance - show near trade item slot
        int bankBalance = menu.getBankBalance();
        guiGraphics.drawString(this.font, Component.translatable("gui.minecrafttradingcenter.bank_balance") + ": " + bankBalance, leftPos + 35, topPos + 112, 0x00FF00, false);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
        
        // Render tooltips for trade entries
        List<TradeEntry> trades = menu.getTrades();
        if (trades != null) {
            int startY = topPos + 20;
            int endIndex = Math.min(scrollOffset + TRADES_PER_PAGE, trades.size());
            for (int i = scrollOffset; i < endIndex; i++) {
                TradeEntry trade = trades.get(i);
                int y = startY + (i - scrollOffset) * 6;
                if (y < topPos + 108 && mouseX >= leftPos + 8 && mouseX < leftPos + 248 && mouseY >= topPos + y && mouseY < topPos + y + 6) {
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
        if (priceField.isFocused() || withdrawField.isFocused() || tradeMultiplierField.isFocused()) {
            return super.keyPressed(keyCode, scanCode, modifiers);
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }

        if (priceField.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        if (withdrawField.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        if (tradeMultiplierField.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }

        // Click on trade row to select (in the first 5 rows area)
        List<TradeEntry> trades = menu.getTrades();
        if (trades != null) {
            int startY = topPos + 20;
            int endIndex = Math.min(scrollOffset + TRADES_PER_PAGE, trades.size());
            for (int i = scrollOffset; i < endIndex; i++) {
                TradeEntry trade = trades.get(i);
                int y = startY + (i - scrollOffset) * 6;
                if (y < topPos + 108 && mouseX >= leftPos + 8 && mouseX < leftPos + 248 && mouseY >= topPos + y && mouseY < topPos + y + 6) {
                    selectedTradeId = trade.getTradeId();
                    return true;
                }
            }
        }

        return false;
    }

    private void createTrade() {
        try {
            int price = Integer.parseInt(priceField.getValue());
            if (price <= 0) {
                return;
            }

            ItemStack tradeItem = menu.getTradeItem();
            if (tradeItem.isEmpty()) {
                return;
            }

            int itemCount = tradeItem.getCount();
            if (itemCount <= 0) {
                return;
            }

            // Count how many of this item the player has in inventory
            int availableCount = countItemsInInventory(tradeItem.getItem());
            if (availableCount < itemCount) {
                return;
            }

            // Create the trade packet - use the item from the slot and the price
            ItemStack tradeItemCopy = tradeItem.copy();
            ModMessages.sendToServer(new CreateTradePacket(price, itemCount, tradeItemCopy));
            
            // Clear the price field after creating (slot will be cleared when items are removed)
            priceField.setValue("1");
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
            
            // Verify trade still exists and has enough available
            List<TradeEntry> trades = menu.getTrades();
            if (trades != null) {
                TradeEntry selectedTrade = null;
                for (TradeEntry trade : trades) {
                    if (trade.getTradeId() == selectedTradeId) {
                        selectedTrade = trade;
                        break;
                    }
                }
                
                if (selectedTrade == null) {
                    selectedTradeId = -1;
                    return;
                }
                
                if (selectedTrade.getAvailableTrades() < multiplier) {
                    return;
                }
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

    private int countItemsInInventory(net.minecraft.world.item.Item item) {
        Inventory inventory = menu.getPlayer().getInventory();
        int count = 0;
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (!stack.isEmpty() && stack.is(item)) {
                count += stack.getCount();
            }
        }
        // Also count the item in the trade slot
        ItemStack tradeItem = menu.getTradeItem();
        if (!tradeItem.isEmpty() && tradeItem.is(item)) {
            count += tradeItem.getCount();
        }
        return count;
    }
}
