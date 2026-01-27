package com.ui_utils.mixin;

import com.ui_utils.gui.ChatTextFieldWidget;
import com.ui_utils.gui.CustomTextFieldWidget;
import com.ui_utils.gui.UITheme;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import com.ui_utils.MainClient;
import com.ui_utils.SharedVariables;

import java.util.regex.Pattern;

@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin extends Screen {
    private HandledScreenMixin() {
        super(null);
    }

    @Shadow
    protected abstract boolean handleHotbarKeyPressed(int keyCode, int scanCode);
    @Shadow
    protected abstract void onMouseClick(Slot slot, int slotId, int button, SlotActionType actionType);
    @Shadow
    @Nullable
    protected Slot focusedSlot;

    @Unique
    private static final MinecraftClient mc = MinecraftClient.getInstance();
    @Unique
    private CustomTextFieldWidget chatField;

    @Inject(at = @At("TAIL"), method = "init")
    public void init(CallbackInfo ci) {
        if (SharedVariables.enabled) {
            MainClient.createWidgets(mc, this);

            this.chatField = new ChatTextFieldWidget(this.textRenderer, 6, this.height - 18, 140, 12, Text.of("Chat..."));
            this.chatField.setText("");
            this.chatField.setMaxLength(256);
            this.addDrawableChild(this.chatField);
        }
    }

    @Inject(at = @At("HEAD"), method = "keyPressed", cancellable = true)
    public void keyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if (this.chatField != null && this.chatField.isFocused()) {
            if (this.chatField.keyPressed(keyCode, scanCode, modifiers)) {
                cir.setReturnValue(true);
                return;
            }
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                this.chatField.setFocused(false);
                cir.setReturnValue(true);
                return;
            }
            cir.setReturnValue(true);
            return;
        }
        
        if (super.keyPressed(keyCode, scanCode, modifiers)) {
            cir.setReturnValue(true);
            cir.cancel();
        } else if (MainClient.mc.options.inventoryKey.matchesKey(keyCode, scanCode)) {
            this.close();
            cir.setReturnValue(true);
            cir.cancel();
        } else {
            this.handleHotbarKeyPressed(keyCode, scanCode);
            if (this.focusedSlot != null && this.focusedSlot.hasStack()) {
                if (mc.options.pickItemKey.matchesKey(keyCode, scanCode)) {
                    this.onMouseClick(this.focusedSlot, this.focusedSlot.id, 0, SlotActionType.CLONE);
                } else if (mc.options.dropKey.matchesKey(keyCode, scanCode)) {
                    this.onMouseClick(this.focusedSlot, this.focusedSlot.id, hasControlDown() ? 1 : 0, SlotActionType.THROW);
                }
            }
        }
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (this.chatField != null && this.chatField.isFocused()) {
            return this.chatField.charTyped(chr, modifiers);
        }
        return super.charTyped(chr, modifiers);
    }

    @Inject(at = @At("TAIL"), method = "render")
    public void render(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (SharedVariables.enabled) {
            MainClient.createText(mc, context, this.textRenderer);
        }
    }
}
