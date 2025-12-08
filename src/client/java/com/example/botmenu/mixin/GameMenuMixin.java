package com.example.botmenu.mixin;

import com.example.botmenu.client.BotManagerScreen;
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
public abstract class GameMenuMixin extends Screen {
    protected GameMenuMixin(Text title) {
        super(title);
    }

    @Inject(method = "initWidgets", at = @At("TAIL"))
    private void addBotButton(CallbackInfo ci) {
        int x = this.width - 110;
        int y = this.height - 45;
        ButtonWidget botButton = ButtonWidget.builder(Text.literal("Add Bot to Server"), btn -> {
            MinecraftClient.getInstance().setScreen(new BotManagerScreen((Screen) (Object) this));
        }).dimensions(x, y, 100, 20).build();
        addDrawableChild(botButton);
    }
}
