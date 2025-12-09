package com.example.botmod.gui;

import com.example.botmod.bot.Bot;
import com.example.botmod.bot.BotManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.text.Text;

public class BotManagerScreen extends Screen {
    private final Screen parent;
    private BotListWidget botList;
    private TextFieldWidget nicknameField;
    private ButtonWidget joinButton;
    private ButtonWidget leaveButton;
    private ButtonWidget deleteButton;

    public BotManagerScreen(Screen parent) {
        super(Text.of("Bot Manager"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        this.botList = new BotListWidget(this.client, this.width, this.height - 80, 40, 36);
        this.addDrawableChild(this.botList);

        this.nicknameField = new TextFieldWidget(this.textRenderer, this.width / 2 - 100, this.height - 70, 200, 20, Text.of("Nickname"));
        this.nicknameField.setMaxLength(16);
        this.addDrawableChild(this.nicknameField);

        this.addDrawableChild(ButtonWidget.builder(Text.of("Add Bot"), button -> {
            String nick = nicknameField.getText();
            if (!nick.isEmpty()) {
                BotManager.getInstance().addBot(nick);
                this.botList.refresh();
                this.nicknameField.setText("");
            }
        }).dimensions(this.width / 2 - 100, this.height - 45, 98, 20).build());

        this.deleteButton = this.addDrawableChild(ButtonWidget.builder(Text.of("Delete"), button -> {
            BotEntry entry = this.botList.getSelectedOrNull();
            if (entry != null) {
                BotManager.getInstance().removeBot(entry.bot);
                this.botList.refresh();
                updateButtons();
            }
        }).dimensions(this.width / 2 + 2, this.height - 45, 98, 20).build());

        this.joinButton = this.addDrawableChild(ButtonWidget.builder(Text.of("Join"), button -> {
            BotEntry entry = this.botList.getSelectedOrNull();
            if (entry != null && !entry.bot.isConnected()) {
                if (this.client.getCurrentServerEntry() != null) {
                   entry.bot.connect(this.client.getCurrentServerEntry().address, 25565); // Default port if not parsed
                   // Parsing logic is needed for real usage, simplified here
                   String address = this.client.getCurrentServerEntry().address;
                   String[] parts = address.split(":");
                   String ip = parts[0];
                   int port = 25565;
                   if (parts.length > 1) {
                       try { port = Integer.parseInt(parts[1]); } catch (NumberFormatException ignored) {}
                   }
                   entry.bot.connect(ip, port);
                }
            }
        }).dimensions(this.width / 2 - 100, this.height - 25, 98, 20).build());

        this.leaveButton = this.addDrawableChild(ButtonWidget.builder(Text.of("Leave"), button -> {
             BotEntry entry = this.botList.getSelectedOrNull();
             if (entry != null && entry.bot.isConnected()) {
                 entry.bot.disconnect();
             }
        }).dimensions(this.width / 2 + 2, this.height - 25, 98, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.of("Back"), button -> {
            this.client.setScreen(this.parent);
        }).dimensions(this.width / 2 - 50, this.height - 5, 100, 20).build());

        updateButtons();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 20, 0xFFFFFF);
    }

    public void updateButtons() {
        BotEntry entry = this.botList.getSelectedOrNull();
        if (entry == null) {
            this.joinButton.active = false;
            this.leaveButton.active = false;
            this.deleteButton.active = false;
        } else {
            this.deleteButton.active = true;
            this.joinButton.active = !entry.bot.isConnected();
            this.leaveButton.active = entry.bot.isConnected();
        }
    }

    private class BotListWidget extends AlwaysSelectedEntryListWidget<BotEntry> {
        public BotListWidget(MinecraftClient client, int width, int height, int top, int itemHeight) {
            super(client, width, height, top, itemHeight);
            refresh();
        }

        public void refresh() {
            this.clearEntries();
            for (Bot bot : BotManager.getInstance().getBots()) {
                this.addEntry(new BotEntry(bot));
            }
        }
    }

    private class BotEntry extends AlwaysSelectedEntryListWidget.Entry<BotEntry> {
        private final Bot bot;

        public BotEntry(Bot bot) {
            this.bot = bot;
        }

        @Override
        public Text getNarration() {
            return Text.of(bot.getNickname());
        }

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            context.drawText(client.textRenderer, bot.getNickname(), x + 5, y + 5, 0xFFFFFF, false);
            String status = bot.isConnected() ? "Connected" : "Disconnected";
            int color = bot.isConnected() ? 0x00FF00 : 0xFF0000;
            context.drawText(client.textRenderer, status, x + 100, y + 5, color, false);

            if (!bot.isConnected() && bot.getDisconnectReason() != null) {
                 context.drawText(client.textRenderer, bot.getDisconnectReason().getString(), x + 160, y + 5, 0xFFAAAA, false);
            }
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            botList.setSelected(this);
            updateButtons();
            return true;
        }
    }

    @Override
    public void tick() {
        BotManager.getInstance().tick();
        // Periodically refresh button states or list if status changes
        if (this.botList.getSelectedOrNull() != null) {
             updateButtons();
        }
    }
}
