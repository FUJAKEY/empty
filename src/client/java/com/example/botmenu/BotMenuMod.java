package com.example.botmenu;

import com.example.botmenu.state.BotRegistry;
import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BotMenuMod implements ClientModInitializer {
    public static final String MOD_ID = "botmenu";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    private static BotRegistry registry;

    @Override
    public void onInitializeClient() {
        registry = BotRegistry.load();
        LOGGER.info("Bot Menu initialized with {} saved bots", registry.getBots().size());
    }

    public static BotRegistry getRegistry() {
        if (registry == null) {
            registry = BotRegistry.load();
        }
        return registry;
    }
}
