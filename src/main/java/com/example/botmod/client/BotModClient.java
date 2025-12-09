package com.example.botmod.client;

import net.fabricmc.api.ClientModInitializer;

public class BotModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Ticking is now handled via MinecraftClientMixin
    }
}
