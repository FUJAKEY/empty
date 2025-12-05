package com.example.botmod.gui;

import com.example.botmod.bot.BotManager;
import com.example.botmod.bot.FakeBot;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.text.Text;
import java.util.List;

public class BotManagerScreen extends Screen {

    private final Screen parent;
    private BotListWidget botList;
    private TextFieldWidget nameField;
    private TextFieldWidget ipField;
    private ButtonWidget joinButton;
    private ButtonWidget deleteButton;

    public BotManagerScreen(Screen parent) {
        super(Text.literal("Bot Manager"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        this.botList = new BotListWidget(this.client, this.width, this.height - 90, 40, 24);
        this.addDrawableChild(this.botList);

        int midX = this.width / 2;

        // Name Field
        this.nameField = new TextFieldWidget(this.textRenderer, midX - 100, this.height - 85, 150, 20, Text.literal("Nickname"));
        this.nameField.setPlaceholder(Text.literal("Nickname"));
        this.addDrawableChild(this.nameField);

        // Add Button
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Add"), button -> {
            String name = this.nameField.getText().trim();
            if (!name.isEmpty()) {
                BotManager.addBot(name);
                this.botList.refresh();
                this.nameField.setText("");
            }
        }).dimensions(midX + 60, this.height - 85, 40, 20).build());

        // IP Field
        this.ipField = new TextFieldWidget(this.textRenderer, midX - 100, this.height - 60, 200, 20, Text.literal("Target IP"));
        this.ipField.setPlaceholder(Text.literal("Target Server IP"));

        // Default IP
        if (this.client.getCurrentServerEntry() != null) {
            this.ipField.setText(this.client.getCurrentServerEntry().address);
        } else if (this.client.isInSingleplayer()) {
            this.ipField.setText("localhost");
        }

        this.addDrawableChild(this.ipField);

        // Join/Offline Button
        this.joinButton = this.addDrawableChild(ButtonWidget.builder(Text.literal("Join/Offline"), button -> {
            BotListWidget.BotEntry entry = this.botList.getSelectedOrNull();
            if (entry != null) {
                String targetIp = this.ipField.getText().trim();
                if (targetIp.isEmpty()) targetIp = "localhost";

                entry.toggleConnection(targetIp);
                this.updateButtons();
            }
        }).dimensions(midX - 100, this.height - 30, 98, 20).build());
        this.joinButton.active = false;

        // Delete Button
        this.deleteButton = this.addDrawableChild(ButtonWidget.builder(Text.literal("Delete"), button -> {
            BotListWidget.BotEntry entry = this.botList.getSelectedOrNull();
            if (entry != null) {
                BotManager.removeBot(entry.bot.getName());
                this.botList.refresh();
                this.updateButtons();
            }
        }).dimensions(midX + 2, this.height - 30, 98, 20).build());
        this.deleteButton.active = false;

        // Populate list
        this.botList.refresh();
    }

    private void updateButtons() {
        BotListWidget.BotEntry entry = this.botList.getSelectedOrNull();
        if (entry != null) {
            this.deleteButton.active = true;
            this.joinButton.active = true;
            if (entry.bot.isConnected()) {
                this.joinButton.setMessage(Text.literal("Offline"));
            } else {
                this.joinButton.setMessage(Text.literal("Join"));
            }
        } else {
            this.deleteButton.active = false;
            this.joinButton.active = false;
            this.joinButton.setMessage(Text.literal("Join/Offline"));
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 20, 0xFFFFFF);
    }

    @Override
    public void close() {
        this.client.setScreen(this.parent);
    }

    // Inner class for the list
    class BotListWidget extends AlwaysSelectedEntryListWidget<BotListWidget.BotEntry> {

        public BotListWidget(MinecraftClient client, int width, int height, int top, int itemHeight) {
            super(client, width, height, top, itemHeight);
        }

        public void refresh() {
            this.clearEntries();
            List<FakeBot> bots = BotManager.getBots();
            for (FakeBot bot : bots) {
                this.addEntry(new BotEntry(bot));
            }
        }

        class BotEntry extends AlwaysSelectedEntryListWidget.Entry<BotEntry> {
            final FakeBot bot;

            public BotEntry(FakeBot bot) {
                this.bot = bot;
            }

            @Override
            public Text getNarration() {
                return Text.literal(bot.getName());
            }

            @Override
            public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
                int color = bot.isConnected() ? 0x00FF00 : 0xFFFFFF;
                context.drawTextWithShadow(BotManagerScreen.this.textRenderer, bot.getName(), x + 10, y + 5, color);
            }

            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                BotListWidget.this.setSelected(this);
                BotManagerScreen.this.updateButtons();
                return true;
            }

            public void toggleConnection(String ip) {
                if (bot.isConnected()) {
                    bot.disconnect();
                } else {
                     bot.connect(ip);
                }
            }
        }
    }
}
