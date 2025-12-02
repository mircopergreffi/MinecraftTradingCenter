package com.minecrafttradingcenter.network;

import com.minecrafttradingcenter.MinecraftTradingCenter;
import com.minecrafttradingcenter.network.packet.CreateTradePacket;
import com.minecrafttradingcenter.network.packet.ExecuteTradePacket;
import com.minecrafttradingcenter.network.packet.RequestTradesPacket;
import com.minecrafttradingcenter.network.packet.SendTradesPacket;
import com.minecrafttradingcenter.network.packet.WithdrawEmeraldsPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class ModMessages {
    private static SimpleChannel INSTANCE;
    private static int packetId = 0;
    private static int id() {
        return packetId++;
    }

    public static void register() {
        SimpleChannel net = NetworkRegistry.ChannelBuilder
                .named(new ResourceLocation(MinecraftTradingCenter.MOD_ID, "messages"))
                .networkProtocolVersion(() -> "1.0")
                .clientAcceptedVersions(s -> true)
                .serverAcceptedVersions(s -> true)
                .simpleChannel();

        INSTANCE = net;

        net.messageBuilder(RequestTradesPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(RequestTradesPacket::new)
                .encoder(RequestTradesPacket::toBytes)
                .consumerMainThread(RequestTradesPacket::handle)
                .add();

        net.messageBuilder(SendTradesPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(SendTradesPacket::new)
                .encoder(SendTradesPacket::toBytes)
                .consumerMainThread(SendTradesPacket::handle)
                .add();

        net.messageBuilder(CreateTradePacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(CreateTradePacket::new)
                .encoder(CreateTradePacket::toBytes)
                .consumerMainThread(CreateTradePacket::handle)
                .add();

        net.messageBuilder(ExecuteTradePacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(ExecuteTradePacket::new)
                .encoder(ExecuteTradePacket::toBytes)
                .consumerMainThread(ExecuteTradePacket::handle)
                .add();

        net.messageBuilder(WithdrawEmeraldsPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(WithdrawEmeraldsPacket::new)
                .encoder(WithdrawEmeraldsPacket::toBytes)
                .consumerMainThread(WithdrawEmeraldsPacket::handle)
                .add();
    }

    public static <MSG> void sendToServer(MSG message) {
        INSTANCE.sendToServer(message);
    }

    public static <MSG> void sendToPlayer(MSG message, ServerPlayer player) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), message);
    }
}

