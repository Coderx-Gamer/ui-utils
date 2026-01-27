package com.ui_utils.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class UpdateScreen extends Screen {
    public UpdateScreen(Text title) {
        super(title);
    }

    @Override
    protected void init() {
        super.init();
        int centerX = this.width / 2;
        int btnWidth = 70;
        int spacing = 8;

        this.addDrawableChild(CustomButtonWidget.create(centerX - btnWidth - spacing / 2, 130, btnWidth, Text.of("Quit"), (button) -> {
            this.client.stop();
        }));

        this.addDrawableChild(CustomButtonWidget.create(centerX + spacing / 2, 130, btnWidth, Text.of("Back"), (button) -> {
            this.client.setScreen(null);
        }));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        int centerX = this.width / 2;

        UITheme.drawPanelWithAccent(context, centerX - 170, 60, 340, 100);

        String line1 = "To update UI-Utils:";
        String line2 = "1. Quit the game";
        String line3 = "2. Delete old UI-Utils jar";
        String line4 = "3. Add the new jar to mods folder";

        context.drawCenteredTextWithShadow(this.textRenderer, line1, centerX, 72, UITheme.TEXT);
        context.drawText(this.textRenderer, line2, centerX - 155, 90, UITheme.TEXT_DIM, false);
        context.drawText(this.textRenderer, line3, centerX - 155, 103, UITheme.TEXT_DIM, false);
        context.drawText(this.textRenderer, line4, centerX - 155, 116, UITheme.TEXT_DIM, false);
    }
}