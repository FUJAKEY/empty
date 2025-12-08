package com.example.botmenu.state;

import com.example.botmenu.BotMenuMod;
import net.minecraft.client.MinecraftClient;

import java.util.HashSet;
import java.util.Set;

public class BotConnectionService {
    private final Set<String> connectedBots = new HashSet<>();
    private final BotRegistry registry;

    public BotConnectionService(BotRegistry registry) {
        this.registry = registry;
    }

    public boolean connect(BotProfile profile) {
        if (connectedBots.contains(profile.getName())) {
            return false;
        }
        if (MinecraftClient.getInstance().getNetworkHandler() == null) {
            BotMenuMod.LOGGER.warn("Cannot connect bot while client is offline");
            registry.sendToast("Нельзя подключить бота без активного подключения к серверу.");
            return false;
        }
        connectedBots.add(profile.getName());
        profile.setConnected(true);
        registry.save();
        registry.sendToast("Бот " + profile.getName() + " помечен как подключенный (эмуляция).");
        return true;
    }

    public boolean disconnect(BotProfile profile) {
        if (!connectedBots.remove(profile.getName())) {
            return false;
        }
        profile.setConnected(false);
        registry.save();
        registry.sendToast("Бот " + profile.getName() + " помечен как отключенный.");
        return true;
    }

    public boolean isConnected(BotProfile profile) {
        return connectedBots.contains(profile.getName()) || profile.isConnected();
    }
}
