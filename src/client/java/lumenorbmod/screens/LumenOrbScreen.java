package lumenorbmod.screens;

import lumenorbmod.screenhandler.LumenOrbScreenHandler;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class LumenOrbScreen extends HandledScreen<LumenOrbScreenHandler> {
    // path to the gui texture.
    private static final Identifier TEXTURE = Identifier.ofVanilla("textures/gui/container/dispenser.png");

    public LumenOrbScreen(LumenOrbScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        context.drawTexture(
                RenderPipelines.GUI_TEXTURED,
                TEXTURE,
                this.x,
                this.y,
                /* u */ 0f,
                /* v */ 0f,
                this.backgroundWidth,
                this.backgroundHeight,
                /* textureWidth */ 256,
                /* textureHeight */ 256
        );
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        drawMouseoverTooltip(context, mouseX, mouseY);
    }

    @Override
    protected void init() {
        super.init();
        // Center the title
        titleX = (backgroundWidth - textRenderer.getWidth(title)) / 2;
    }
}
