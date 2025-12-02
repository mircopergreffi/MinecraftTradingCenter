package com.minecrafttradingcenter.item;

import com.minecrafttradingcenter.MinecraftTradingCenter;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, MinecraftTradingCenter.MOD_ID);

    public static final RegistryObject<Item> TRADING_LAPTOP = ITEMS.register("trading_laptop",
            () -> new TradingLaptopItem(new Item.Properties().stacksTo(1)));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}

