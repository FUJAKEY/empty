package com.example.botmod.mixin;

import com.example.botmod.gui.BotManagerScreen;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameMenuScreen.class)
public class GameMenuScreenMixin extends Screen {
    protected GameMenuScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "initWidgets", at = @At("TAIL"))
    private void addBotButton(CallbackInfo ci) {
        this.addDrawableChild(ButtonWidget.builder(Text.of("Add bot to server"), button -> {
            this.client.setScreen(new BotManagerScreen(this));
        }).dimensions(this.width - 120, this.height - 25, 115, 20).build());
    }
}
