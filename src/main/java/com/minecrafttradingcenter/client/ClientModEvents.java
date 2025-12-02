package com.minecrafttradingcenter.client;

import com.minecrafttradingcenter.screen.ModMenuTypes;
import com.minecrafttradingcenter.screen.TradingCenterScreen;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import com.minecrafttradingcenter.MinecraftTradingCenter;

@Mod.EventBusSubscriber(modid = MinecraftTradingCenter.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEvents {
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(ModMenuTypes.TRADING_CENTER_MENU.get(), TradingCenterScreen::new);
        });
    }
}

