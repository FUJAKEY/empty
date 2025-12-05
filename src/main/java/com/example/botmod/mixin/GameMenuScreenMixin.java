package com.example.botmod.mixin;

import com.example.botmod.gui.BotManagerScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameMenuScreen.class)
public abstract class GameMenuScreenMixin extends Screen {

    protected GameMenuScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void addBotButton(CallbackInfo ci) {
        int buttonWidth = 100;
        int buttonHeight = 20;
        int x = this.width - buttonWidth - 5;
        int y = this.height - buttonHeight - 5;

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Add to Server"), button -> {
            MinecraftClient.getInstance().setScreen(new BotManagerScreen(this));
        }).dimensions(x, y, buttonWidth, buttonHeight).build());
    }
}
