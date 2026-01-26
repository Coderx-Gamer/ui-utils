package com.ui_utils.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

public class CustomTextFieldWidget extends TextFieldWidget {
    private String placeholder;

    public CustomTextFieldWidget(TextRenderer textRenderer, int x, int y, int width, int height, Text text) {
        super(textRenderer, x, y, width, height, text);
        this.setDrawsBackground(false);
        this.placeholder = text.getString();
    }

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        UITheme.drawInputBackground(context, this.getX() - 2, this.getY() - 2, this.width + 4, this.height + 4, this.isFocused());
        super.renderWidget(context, mouseX, mouseY, delta);
        if (this.getText().isEmpty() && !this.isFocused() && this.placeholder != null && !this.placeholder.isEmpty()) {
            context.drawText(MinecraftClient.getInstance().textRenderer, this.placeholder, this.getX() + 2, this.getY() + 2, UITheme.TEXT_DIM, false);
        }
    }

    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
    }

    public static CustomTextFieldWidget create(TextRenderer textRenderer, int x, int y, int width, Text placeholder) {
        CustomTextFieldWidget field = new CustomTextFieldWidget(textRenderer, x, y, width, 12, placeholder);
        field.setMaxLength(255);
        return field;
    }

    public static CustomTextFieldWidget create(TextRenderer textRenderer, int x, int y, int width, int height, Text placeholder) {
        CustomTextFieldWidget field = new CustomTextFieldWidget(textRenderer, x, y, width, height, placeholder);
        field.setMaxLength(255);
        return field;
    }
}
