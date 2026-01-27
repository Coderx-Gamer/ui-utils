package com.ui_utils.gui;

import net.minecraft.client.gui.DrawContext;

public class UITheme {
    public static final int BACKGROUND = 0xE6101010;
    public static final int BACKGROUND_HOVER = 0xF01a1a1a;
    public static final int BORDER = 0xFF3f3f46;
    public static final int BORDER_HOVER = 0xFF52525b;
    public static final int BORDER_ACCENT = 0xFF6366f1;
    public static final int BORDER_OUTER = 0xFF18181b;
    public static final int TEXT = 0xFFfafafa;
    public static final int TEXT_DIM = 0xFF71717a;
    public static final int INPUT_BG = 0xE6141414;
    public static final int INPUT_BORDER = 0xFF3f3f46;
    public static final int INPUT_BORDER_FOCUS = 0xFF6366f1;

    public static void drawPanel(DrawContext context, int x, int y, int width, int height) {
        context.fill(x - 1, y - 1, x + width + 1, y + height + 1, BORDER_OUTER);
        context.fill(x, y, x + width, y + height, BACKGROUND);
        drawBorder(context, x, y, width, height, BORDER);
    }

    public static void drawPanelWithAccent(DrawContext context, int x, int y, int width, int height) {
        context.fill(x - 1, y - 1, x + width + 1, y + height + 1, BORDER_OUTER);
        context.fill(x, y, x + width, y + height, BACKGROUND);
        context.fill(x, y, x + 2, y + height, BORDER_ACCENT);
        drawBorder(context, x, y, width, height, BORDER);
    }

    public static void drawBorder(DrawContext context, int x, int y, int width, int height, int color) {
        context.fill(x, y, x + width, y + 1, color);
        context.fill(x, y + height - 1, x + width, y + height, color);
        context.fill(x, y, x + 1, y + height, color);
        context.fill(x + width - 1, y, x + width, y + height, color);
    }

    public static void drawDoubleBorder(DrawContext context, int x, int y, int width, int height, int innerColor, int outerColor) {
        context.fill(x - 1, y - 1, x + width + 1, y, outerColor);
        context.fill(x - 1, y + height, x + width + 1, y + height + 1, outerColor);
        context.fill(x - 1, y - 1, x, y + height + 1, outerColor);
        context.fill(x + width, y - 1, x + width + 1, y + height + 1, outerColor);
        drawBorder(context, x, y, width, height, innerColor);
    }

    public static void drawInputBackground(DrawContext context, int x, int y, int width, int height, boolean focused) {
        context.fill(x - 1, y - 1, x + width + 1, y + height + 1, BORDER_OUTER);
        context.fill(x, y, x + width, y + height, INPUT_BG);
        drawBorder(context, x, y, width, height, focused ? INPUT_BORDER_FOCUS : INPUT_BORDER);
    }
}
