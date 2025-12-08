package com.example.botmenu.client;

import com.example.botmenu.BotMenuMod;
import com.example.botmenu.state.BotConnectionService;
import com.example.botmenu.state.BotProfile;
import com.example.botmenu.state.BotRegistry;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class BotManagerScreen extends Screen {
    private final Screen parent;
    private final BotRegistry registry;
    private final BotConnectionService connectionService;
    private TextFieldWidget nameField;
    private ButtonWidget addButton;
    private ButtonWidget deleteButton;
    private ButtonWidget joinButton;
    private ButtonWidget leaveButton;
    private final List<ButtonWidget> botButtons = new ArrayList<>();
    private final List<BotProfile> displayedBots = new ArrayList<>();
    private BotProfile selected;

    public BotManagerScreen(Screen parent) {
        super(Text.translatable("botmenu.title"));
        this.parent = parent;
        this.registry = BotMenuMod.getRegistry();
        this.connectionService = new BotConnectionService(registry);
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int top = 40;

        nameField = new TextFieldWidget(textRenderer, centerX - 100, top, 200, 20, Text.literal("bot name"));
        addDrawableChild(nameField);

        addButton = addDrawableChild(ButtonWidget.builder(Text.literal("Add"), btn -> addBot())
                .dimensions(centerX - 100, top + 25, 95, 20).build());
        deleteButton = addDrawableChild(ButtonWidget.builder(Text.literal("Delete"), btn -> deleteSelected())
                .dimensions(centerX + 5, top + 25, 95, 20).build());

        joinButton = addDrawableChild(ButtonWidget.builder(Text.literal("Join"), btn -> joinSelected())
                .dimensions(centerX - 100, top + 50, 95, 20).build());
        leaveButton = addDrawableChild(ButtonWidget.builder(Text.literal("Leave"), btn -> leaveSelected())
                .dimensions(centerX + 5, top + 50, 95, 20).build());

        refreshBotButtons();
        updateButtons();
    }

    private void addBot() {
        if (registry.addBot(nameField.getText())) {
            nameField.setText("");
            refreshBotButtons();
        } else {
            registry.sendToast("Не удалось добавить бота. Убедись, что имя уникально.");
        }
    }

    private void deleteSelected() {
        if (selected != null && registry.removeBot(selected)) {
            selected = null;
            refreshBotButtons();
        }
        updateButtons();
    }

    private void joinSelected() {
        if (selected != null) {
            if (connectionService.connect(selected)) {
                updateButtons();
            }
        }
    }

    private void leaveSelected() {
        if (selected != null) {
            if (connectionService.disconnect(selected)) {
                updateButtons();
            }
        }
    }

    private void refreshBotButtons() {
        botButtons.forEach(this::remove);
        botButtons.clear();
        displayedBots.clear();

        int startY = 90;
        int centerX = this.width / 2;
        int index = 0;
        for (BotProfile profile : registry.getBots()) {
            int y = startY + index * 24;
            ButtonWidget widget = ButtonWidget.builder(Text.literal(formatBotLabel(profile)), btn -> {
                selected = profile;
                updateButtons();
            }).dimensions(centerX - 100, y, 200, 20).build();
            botButtons.add(widget);
            addDrawableChild(widget);
            displayedBots.add(profile);
            index++;
        }
    }

    private String formatBotLabel(BotProfile profile) {
        return profile.getName() + (connectionService.isConnected(profile) ? " (online)" : " (offline)");
    }

    private void updateButtons() {
        boolean hasSelection = selected != null;
        deleteButton.active = hasSelection;
        joinButton.active = hasSelection && !connectionService.isConnected(selected);
        leaveButton.active = hasSelection && connectionService.isConnected(selected);

        for (int i = 0; i < botButtons.size(); i++) {
            BotProfile profile = displayedBots.get(i);
            botButtons.get(i).setMessage(Text.literal(formatBotLabel(profile)));
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("Bot Manager"), this.width / 2, 15, 0xFFFFFF);
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void close() {
        this.client.setScreen(parent);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
