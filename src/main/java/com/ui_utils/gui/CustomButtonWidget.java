package com.ui_utils.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class CustomButtonWidget extends ButtonWidget {
    public CustomButtonWidget(int x, int y, int width, int height, Text message, PressAction onPress) {
        super(x, y, width, height, message, onPress, DEFAULT_NARRATION_SUPPLIER);
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        boolean hovered = this.isHovered();
        int bg = hovered ? UITheme.BACKGROUND_HOVER : UITheme.BACKGROUND;
        int border = hovered ? UITheme.BORDER_HOVER : UITheme.BORDER;
        
        context.fill(this.getX() - 1, this.getY() - 1, this.getX() + this.width + 1, this.getY() + this.height + 1, UITheme.BORDER_OUTER);
        context.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, bg);
        UITheme.drawBorder(context, this.getX(), this.getY(), this.width, this.height, border);
        
        if (hovered) {
            context.fill(this.getX(), this.getY() + this.height - 2, this.getX() + this.width, this.getY() + this.height, UITheme.BORDER_ACCENT);
        }
        
        int textColor = this.active ? UITheme.TEXT : UITheme.TEXT_DIM;
        context.drawCenteredTextWithShadow(MinecraftClient.getInstance().textRenderer, this.getMessage(), this.getX() + this.width / 2, this.getY() + (this.height - 8) / 2, textColor);
    }

    public static CustomButtonWidget create(int x, int y, int width, Text message, PressAction onPress) {
        return new CustomButtonWidget(x, y, width, 16, message, onPress);
    }

    public static CustomButtonWidget createSmall(int x, int y, int width, Text message, PressAction onPress) {
        return new CustomButtonWidget(x, y, width, 14, message, onPress);
    }
}
