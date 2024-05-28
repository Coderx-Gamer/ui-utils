package org.uiutils.gui;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.text.Text;

public class UpdateScreen extends Screen {

    public UpdateScreen(Text title) {
        super(title);
    }

    @Override
    protected void init() {
        super.init();
        Text message1 = Text.of("In order to update UI-Utils, first quit the game then");
        Text message2 = Text.of("delete the old UI-Utils jar file, and replace it with the new one you got on the website.");
        int centerX = this.width / 2;

        this.addDrawableChild(new TextWidget(centerX - textRenderer.getWidth(message1) / 2, 80, textRenderer.getWidth(message1), 20, message1, this.textRenderer));
        this.addDrawableChild(new TextWidget(centerX - textRenderer.getWidth(message2) / 2, 95, textRenderer.getWidth(message2), 20, Text.of(message2), this.textRenderer));

        int quitX = centerX - 85;
        int backX = centerX + 5;

        this.addDrawableChild(ButtonWidget.builder(Text.of("Quit"), (button) -> {
            this.client.stop();
        }).width(80).position(quitX, 145).build());

        this.addDrawableChild(ButtonWidget.builder(Text.of("Back"), (button) -> {
            this.client.setScreen(null);
        }).width(80).position(backX, 145).build());
    }

}