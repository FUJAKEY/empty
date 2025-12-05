package com.example.botmod.bot;

import java.util.ArrayList;
import java.util.List;

public class BotManager {
    private static final List<FakeBot> bots = new ArrayList<>();

    public static void addBot(String name) {
        // Prevent duplicates
        for (FakeBot b : bots) {
            if (b.getName().equals(name)) return;
        }
        bots.add(new FakeBot(name));
    }

    public static void removeBot(String name) {
        FakeBot toRemove = null;
        for (FakeBot b : bots) {
            if (b.getName().equals(name)) {
                toRemove = b;
                break;
            }
        }
        if (toRemove != null) {
            toRemove.disconnect();
            bots.remove(toRemove);
        }
    }

    public static List<FakeBot> getBots() {
        return bots;
    }
}
