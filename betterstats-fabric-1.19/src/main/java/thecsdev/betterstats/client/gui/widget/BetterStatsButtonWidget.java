package thecsdev.betterstats.client.gui.widget;

import static thecsdev.betterstats.BetterStats.tt;
import static thecsdev.betterstats.client.BetterStatsClient.MCClient;
import static thecsdev.betterstats.config.BSConfig.BSS_BTN_IMG;
import static thecsdev.betterstats.config.BSConfig.SEEN_BSS;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import thecsdev.betterstats.BetterStats;
import thecsdev.betterstats.client.BetterStatsClient;
import thecsdev.betterstats.client.gui.screen.BetterStatsScreen;
import thecsdev.betterstats.client.gui.util.GuiUtils;
import thecsdev.betterstats.config.BSConfig;

public class BetterStatsButtonWidget extends ButtonWidget
{
	// ==================================================
	//the tooltip text that shows when hovering over the stats button
	public static final TooltipSupplier TOOLTIP = new TooltipSupplier() {
		@Override
		public void onTooltip(ButtonWidget button, MatrixStack matrices, int mouseX, int mouseY) {
			GuiUtils.drawTooltip(matrices, mouseX, mouseY, null);
		}
	};
	
	public static final Identifier STATS_BTN_BG_TEXTURE = new Identifier(BetterStats.ModID, "textures/gui/stats_btn_bg.png");
	// ==================================================
	public BetterStatsButtonWidget(int x, int y, int width, int height, Screen parent)
	{
		//construct super
		super(x, y, width, height,
				tt("gui.stats"),
				btn ->
				{
					//keep track of the flag
					if(!SEEN_BSS)
					{
						SEEN_BSS = true;
						BSConfig.saveProperties();
					}
					
					//set the screen
					openBSS(parent);
				},
				TOOLTIP);
	}
	// ==================================================
	@Override
	public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta)
	{
		//render the button background
		MinecraftClient minecraftClient = BetterStatsClient.MCClient;
	    TextRenderer textRenderer = minecraftClient.textRenderer;
	    RenderSystem.setShader(GameRenderer::getPositionTexShader);
	    RenderSystem.setShaderTexture(0, WIDGETS_TEXTURE);
	    RenderSystem.setShaderColor(1, 1, 1, this.alpha);
	    int i = getYImage(isHovered());
	    RenderSystem.enableBlend();
	    RenderSystem.defaultBlendFunc();
	    RenderSystem.enableDepthTest();
	    drawTexture(matrices, this.x, this.y, 0, 46 + i * 20, this.width / 2, this.height);
	    drawTexture(matrices, this.x + this.width / 2, this.y, 200 - this.width / 2, 46 + i * 20, this.width / 2, this.height);
	    renderBackground(matrices, minecraftClient, mouseX, mouseY);
	    int j = this.active ? 16777215 : 10526880;
		
	    //render the button image
	    if(BSS_BTN_IMG)
	    	GuiUtils.drawTexture(STATS_BTN_BG_TEXTURE, this.x, this.y, this.width, this.height);
	    
	    //render the text
	    drawCenteredText(matrices, textRenderer, getMessage(),
	    		this.x + this.width / 2,
	    		this.y + (this.height - 8) / 2,
	    		j | MathHelper.ceil(this.alpha * 255.0F) << 24);
	}
	
	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta)
	{
		//render the button
		super.render(matrices, mouseX, mouseY, delta);
		
		//draw the ping
		if(!SEEN_BSS) GuiUtils.drawButtonPing(this);
	}
	// ==================================================
	public static void openBSS(Screen parent)
	{
		//set the screen
		BetterStatsScreen.CACHE_TAB = null; //clear the "cache" before opening
		MCClient.setScreen(new BetterStatsScreen(parent, MCClient.player.getStatHandler()));
	}
	// ==================================================
}