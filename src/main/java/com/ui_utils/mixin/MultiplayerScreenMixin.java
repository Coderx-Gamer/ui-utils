package com.ui_utils.mixin;

import com.ui_utils.gui.CustomButtonWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.session.Session;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.ui_utils.SharedVariables;
import net.fabricmc.loader.api.FabricLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Mixin(MultiplayerScreen.class)
public class MultiplayerScreenMixin extends Screen {
    private MultiplayerScreenMixin() {
        super(null);
    }

    @Inject(at = @At("RETURN"), method = "init", require = 0)
    private void replaceViaFabricPlusButton(CallbackInfo ci) {
        if (!SharedVariables.enabled) return;
        if (!FabricLoader.getInstance().isModLoaded("viafabricplus")) return;
        
        net.minecraft.client.gui.widget.ButtonWidget vfpButton = null;
        for (var child : this.children()) {
            if (child instanceof net.minecraft.client.gui.widget.ButtonWidget button) {
                String msg = button.getMessage().getString();
                if (msg.contains("ViaFabricPlus") || msg.equals("ViaFabricPlus")) {
                    vfpButton = button;
                    break;
                }
            }
        }
        
        if (vfpButton != null) {
            final net.minecraft.client.gui.widget.ButtonWidget originalButton = vfpButton;
            
            this.remove(vfpButton);
            
            int margin = 5;
            int spacing = 4;
            int buttonWidth = 50;
            int buttonHeight = 14;
            int rightX = this.width - margin - buttonWidth;
            int bottomY = this.height - 60;
            
            this.addDrawableChild(CustomButtonWidget.createSmall(rightX, bottomY + buttonHeight + spacing, buttonWidth, Text.of("Via+"), (button) -> {
                originalButton.onPress();
            }));
        }
    }

    @Inject(at = @At("TAIL"), method = "init")
    public void init(CallbackInfo ci) {
        if (SharedVariables.enabled) {
            MinecraftClient mc = MinecraftClient.getInstance();
            int margin = 5;
            int spacing = 4;
            int buttonWidth = 50;
            int buttonHeight = 14;
            int bottomY = this.height - 60;
            
            this.addDrawableChild(CustomButtonWidget.createSmall(margin, bottomY, buttonWidth, Text.of("Bypass"), (button) -> {
                SharedVariables.bypassResourcePack = !SharedVariables.bypassResourcePack;
                button.setMessage(Text.of(SharedVariables.bypassResourcePack ? "§aBypass" : "Bypass"));
            }));

            this.addDrawableChild(CustomButtonWidget.createSmall(margin, bottomY + buttonHeight + spacing, buttonWidth, Text.of("Deny"), (button) -> {
                SharedVariables.resourcePackForceDeny = !SharedVariables.resourcePackForceDeny;
                button.setMessage(Text.of(SharedVariables.resourcePackForceDeny ? "§aDeny" : "Deny"));
            }));
            
            int rightX = this.width - margin - buttonWidth;
            
            this.addDrawableChild(CustomButtonWidget.createSmall(rightX, bottomY, buttonWidth, Text.of("User"), (button) -> {
                mc.setScreen(new UsernameScreen((MultiplayerScreen)(Object)this, mc));
            }));
            
            if (!FabricLoader.getInstance().isModLoaded("viafabricplus")) {
                this.addDrawableChild(CustomButtonWidget.createSmall(rightX, bottomY + buttonHeight + spacing, buttonWidth, Text.of("Via+"), (button) -> {
                    boolean isViaFabric = FabricLoader.getInstance().isModLoaded("viafabric");
                    
                    if (isViaFabric) {
                        try {
                            Class<?> viaScreenClass = Class.forName("com.viaversion.fabric.mc121.gui.ViaConfigScreen");
                            Screen screen = (Screen) viaScreenClass.getConstructor(Screen.class).newInstance((MultiplayerScreen)(Object)this);
                            mc.setScreen(screen);
                        } catch (Exception e1) {
                            try {
                                Class<?> viaScreenClass = Class.forName("com.github.creeper123123321.viafabric.gui.ViaConfigScreen");
                                Screen screen = (Screen) viaScreenClass.getConstructor(Screen.class).newInstance((MultiplayerScreen)(Object)this);
                                mc.setScreen(screen);
                            } catch (Exception e2) {
                                mc.setScreen(new ViaNotFoundScreen((MultiplayerScreen)(Object)this, "Error opening ViaFabric"));
                            }
                        }
                    } else {
                        mc.setScreen(new ViaNotFoundScreen((MultiplayerScreen)(Object)this, "ViaFabric(Plus) not installed!"));
                    }
                }));
            }
        }
    }
    
    private static class UsernameScreen extends Screen {
        private final Screen parent;
        private final MinecraftClient mc;
        private net.minecraft.client.gui.widget.TextFieldWidget usernameField;
        
        protected UsernameScreen(Screen parent, MinecraftClient mc) {
            super(Text.of("Set Username"));
            this.parent = parent;
            this.mc = mc;
        }
        
        @Override
        protected void init() {
            int centerX = this.width / 2;
            int centerY = this.height / 2;
            
            this.usernameField = new net.minecraft.client.gui.widget.TextFieldWidget(this.textRenderer, centerX - 100, centerY - 20, 200, 20, Text.of("Username"));
            this.usernameField.setText(mc.getSession().getUsername());
            this.usernameField.setMaxLength(16);
            this.addDrawableChild(this.usernameField);
            
            this.addDrawableChild(net.minecraft.client.gui.widget.ButtonWidget.builder(Text.of("Apply"), (button) -> {
                String newName = this.usernameField.getText().trim();
                if (!newName.isEmpty()) {
                    try {
                        Session oldSession = mc.getSession();
                        Session newSession = new Session(newName, oldSession.getUuidOrNull(), oldSession.getAccessToken(), Optional.empty(), Optional.empty(), Session.AccountType.MOJANG);
                        java.lang.reflect.Field sessionField = MinecraftClient.class.getDeclaredField("session");
                        sessionField.setAccessible(true);
                        sessionField.set(mc, newSession);
                    } catch (Exception ignored) {}
                }
                mc.setScreen(parent);
            }).dimensions(centerX - 100, centerY + 10, 95, 20).build());
            
            this.addDrawableChild(net.minecraft.client.gui.widget.ButtonWidget.builder(Text.of("Cancel"), (button) -> {
                mc.setScreen(parent);
            }).dimensions(centerX + 5, centerY + 10, 95, 20).build());
        }
        
        @Override
        public void render(net.minecraft.client.gui.DrawContext context, int mouseX, int mouseY, float delta) {
            super.render(context, mouseX, mouseY, delta);
            context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, this.height / 2 - 40, 0xFFFFFF);
        }
    }
    
    private static class ViaNotFoundScreen extends Screen {
        private final Screen parent;
        private final String message;
        
        protected ViaNotFoundScreen(Screen parent, String message) {
            super(Text.of("ViaFabricPlus"));
            this.parent = parent;
            this.message = message;
        }
        
        @Override
        protected void init() {
            int centerX = this.width / 2;
            int centerY = this.height / 2;
            
            this.addDrawableChild(net.minecraft.client.gui.widget.ButtonWidget.builder(Text.of("OK"), (button) -> {
                MinecraftClient.getInstance().setScreen(parent);
            }).dimensions(centerX - 50, centerY + 20, 100, 20).build());
        }
        
        @Override
        public void render(net.minecraft.client.gui.DrawContext context, int mouseX, int mouseY, float delta) {
            super.render(context, mouseX, mouseY, delta);
            context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, this.height / 2 - 30, 0xFFFFFF);
            context.drawCenteredTextWithShadow(this.textRenderer, Text.of(message), this.width / 2, this.height / 2, 0xFF5555);
        }
    }
}
