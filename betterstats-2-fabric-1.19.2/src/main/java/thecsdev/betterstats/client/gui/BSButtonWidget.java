package thecsdev.betterstats.client.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import thecsdev.betterstats.client.gui.screen.BetterStatsScreen;
import thecsdev.tcdcommons.api.util.TextUtils;

public class BSButtonWidget extends ButtonWidget
{
	// ==================================================
	protected static final Text TXT = TextUtils.fTranslatable("gui.stats");
	protected static final PressAction ON_PRESS = btn ->
	{
		final MinecraftClient client = MinecraftClient.getInstance();
		client.setScreen(new BetterStatsScreen(client.currentScreen));
	};
	// ==================================================
	public BSButtonWidget(int x, int y, int width, int height)
	{
		super(x, y, width, height, TXT, ON_PRESS, EMPTY);
	}
	// ==================================================
	public ButtonWidget btn_backToGame = null;
	// --------------------------------------------------
	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta)
	{
		super.render(matrices, mouseX, mouseY, delta);
		if(btn_backToGame != null)
			this.y = btn_backToGame.y + btn_backToGame.getHeight() + 4;
	}
	// ==================================================
}