package com.example.botmenu.state;

import com.example.botmenu.BotMenuMod;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class BotRegistry {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type BOT_LIST_TYPE = new TypeToken<List<BotProfile>>() {}.getType();
    private static final Path STORAGE = MinecraftClient.getInstance().runDirectory.toPath()
            .resolve("config").resolve("botmenu.json");

    private final List<BotProfile> bots = new ArrayList<>();

    public static BotRegistry load() {
        BotRegistry registry = new BotRegistry();
        if (Files.exists(STORAGE)) {
            try (Reader reader = Files.newBufferedReader(STORAGE)) {
                List<BotProfile> stored = GSON.fromJson(reader, BOT_LIST_TYPE);
                if (stored != null) {
                    registry.bots.addAll(stored);
                }
            } catch (IOException e) {
                BotMenuMod.LOGGER.error("Failed to read bot registry", e);
            }
        }
        return registry;
    }

    public void save() {
        try {
            Files.createDirectories(STORAGE.getParent());
            try (Writer writer = Files.newBufferedWriter(STORAGE)) {
                GSON.toJson(bots, BOT_LIST_TYPE, writer);
            }
        } catch (IOException e) {
            BotMenuMod.LOGGER.error("Failed to save bot registry", e);
        }
    }

    public List<BotProfile> getBots() {
        return Collections.unmodifiableList(bots);
    }

    public boolean addBot(String name) {
        if (name == null || name.isBlank()) {
            return false;
        }
        BotProfile profile = new BotProfile(name.trim());
        if (bots.contains(profile)) {
            return false;
        }
        bots.add(profile);
        save();
        return true;
    }

    public boolean removeBot(BotProfile profile) {
        boolean removed = bots.remove(profile);
        if (removed) {
            save();
        }
        return removed;
    }

    public Optional<BotProfile> findByName(String name) {
        return bots.stream().filter(bot -> bot.getName().equalsIgnoreCase(name)).findFirst();
    }

    public void setConnectionState(BotProfile profile, boolean connected) {
        profile.setConnected(connected);
        save();
    }

    public void sendToast(String message) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client != null && client.inGameHud != null) {
            client.inGameHud.getChatHud().addMessage(Text.literal("[BotMenu] " + message));
        }
    }
}
