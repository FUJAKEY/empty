package com.example.botmod.bot;

import java.util.ArrayList;
import java.util.List;

public class BotManager {
    private static final BotManager INSTANCE = new BotManager();
    private final List<Bot> bots = new ArrayList<>();

    private BotManager() {}

    public static BotManager getInstance() {
        return INSTANCE;
    }

    public List<Bot> getBots() {
        return bots;
    }

    public void addBot(String nickname) {
        bots.add(new Bot(nickname));
    }

    public void removeBot(Bot bot) {
        if (bot.isConnected()) {
            bot.disconnect();
        }
        bots.remove(bot);
    }

    public void tick() {
        for (Bot bot : bots) {
            bot.tick();
        }
    }
}
