package io.github.thecsdev.betterstats.util.stats;

import static io.github.thecsdev.tcdcommons.api.util.TextUtils.literal;
import static io.github.thecsdev.tcdcommons.api.util.TextUtils.translatable;

import java.util.HashSet;
import java.util.Objects;

import org.jetbrains.annotations.ApiStatus.Experimental;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Nullable;

import io.github.thecsdev.betterstats.BetterStats;
import io.github.thecsdev.betterstats.BetterStatsConfig;
import io.github.thecsdev.betterstats.network.BetterStatsNetworkHandler;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stat;
import net.minecraft.stat.Stats;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/**
 * A {@link Class} that handles announcing players doing
 * things for the first time in the current world.<br/>
 * For example, mining a diamond ore for the first time.
 */
@Experimental //planning to hopefully expose this to the APIs someday
public final @Internal class StatAnnouncementSystem
{
	// ==================================================
	private static final String P                = "betterstats.util.stats.statannouncementsystem."; //prefix
	public static final String TXT_FIRST_MINED   = P + "mined_first_time";
	public static final String TXT_FIRST_CRAFTED = P + "crafted_first_time";
	public static final String TXT_FIRST_KILLED  = P + "killed_first_time";
	//
	private static final Text WATERMARK;
	private static final BetterStatsConfig BSSC;
	// --------------------------------------------------
	public static final HashSet<Block> FIRST_MINED_BLOCKS;
	public static final HashSet<Item> FIRST_CRAFTED_ITEMS;
	// --------------------------------------------------
	private StatAnnouncementSystem() {}
	static
	{
		//access to the config
		if(!BetterStats.isModInitialized())
			throw new ExceptionInInitializerError(
					"Attempted to initialize " +
					StatAnnouncementSystem.class.getSimpleName() +
					" prior to " + BetterStats.class.getSimpleName() + "'s initialization.");
		BSSC = BetterStats.getInstance().getConfig();
		
		//define the 'betterstats' watermark
		{
			//create the hover event for the watermark
			final var hoverText = literal(BetterStats.getModName()).formatted(Formatting.YELLOW)
					.append("\n")
					.append(literal(BetterStats.getModID()).formatted(Formatting.GRAY));
			final var hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText);
			final var clickEvent = new ClickEvent(ClickEvent.Action.OPEN_URL, BetterStats.URL_SOURCES);
			
			//create the watermark text
			final var text = literal("[≡]").formatted(Formatting.DARK_PURPLE);
			text.setStyle(text.getStyle().withHoverEvent(hoverEvent).withClickEvent(clickEvent));
			WATERMARK = text;
		}
		
		//define the sets
		FIRST_MINED_BLOCKS = new HashSet<Block>();
		FIRST_CRAFTED_ITEMS = new HashSet<Item>();
		
		//initialize default set entries
		FIRST_MINED_BLOCKS.add(Blocks.DIAMOND_ORE);
		FIRST_MINED_BLOCKS.add(Blocks.DEEPSLATE_DIAMOND_ORE);
		FIRST_MINED_BLOCKS.add(Blocks.ANCIENT_DEBRIS);
		FIRST_MINED_BLOCKS.add(Blocks.DEEPSLATE_COAL_ORE);
		FIRST_MINED_BLOCKS.add(Blocks.DRAGON_EGG); //how the...
		
		FIRST_CRAFTED_ITEMS.add(Items.WOODEN_PICKAXE);
		FIRST_CRAFTED_ITEMS.add(Items.DIAMOND_PICKAXE);
		FIRST_CRAFTED_ITEMS.add(Items.BEACON);
		FIRST_CRAFTED_ITEMS.add(Items.NETHERITE_BLOCK);
		FIRST_CRAFTED_ITEMS.add(Items.ENDER_EYE);
	}
	// ==================================================
	/**
	 * An {@link Internal} method responsible for handling {@link Stat}
	 * value changes for {@link ServerPlayerEntity}s.
	 * @param player The {@link ServerPlayerEntity} whose {@link Stat} value is changing.
	 * @param stat The {@link Stat} in question.
	 * @param oldValue The old value of the {@link Stat}.
	 * @param newValue The new value being assigned to the {@link Stat}.
	 * @throws NullPointerException If an argument is {@code null}.
	 * @apiNote {@link Internal}. Do not use or call this.
	 */
	public static final @Internal void __handleStatChange(
			ServerPlayerEntity player, Stat<?> stat,
			int oldValue, int newValue) throws NullPointerException
	{
		//check if this feature is enabled
		if(!BSSC.enableServerSAS) return;
		
		//for optimization purposes, check the numbers first
		else if(oldValue == 0 && newValue > 0)
		{
			//requirements
			Objects.requireNonNull(player);
			Objects.requireNonNull(stat);
			
			//do not do this for creative-mode players
			if(player.getAbilities().creativeMode)
				return;
			
			//handle "first mined"
			if(stat.getType() == Stats.MINED && FIRST_MINED_BLOCKS.contains(stat.getValue()))
				broadcastFirstMine(player, (Block)stat.getValue());
			
			//handle "first crafted"
			else if(stat.getType() == Stats.CRAFTED && FIRST_CRAFTED_ITEMS.contains(stat.getValue()))
				broadcastFirstCraft(player, (Item)stat.getValue());
		}
	}
	// --------------------------------------------------
	private static final MutableText formatEntityText(Entity entity)
	{
		Objects.requireNonNull(entity);
		final MutableText pText = literal("").append(entity.getDisplayName()).formatted(Formatting.YELLOW);
		pText.setStyle(pText.getStyle().withHoverEvent(new HoverEvent(
				HoverEvent.Action.SHOW_TEXT,
				literal("")
					.append(entity.getDisplayName())
					.append("\n")
					.append(literal(Objects.toString(entity.getUuid()).formatted(Formatting.GRAY)))
			)));
		return pText;
	}
	
	private static final MutableText formatBlockText(Block block)
	{
		Objects.requireNonNull(block);
		final var blockId = Objects.toString(Registries.BLOCK.getId(block));
		final MutableText bText = literal("").append(block.getName()).formatted(Formatting.GREEN);
		bText.setStyle(bText.getStyle().withHoverEvent(new HoverEvent(
				HoverEvent.Action.SHOW_TEXT,
				literal("")
					.append(block.getName())
					.append("\n")
					.append(literal(blockId).formatted(Formatting.GRAY))
			)));
		return bText;
	}
	
	private static final MutableText formatItemText(Item item)
	{
		Objects.requireNonNull(item);
		final var blockId = Objects.toString(Registries.ITEM.getId(item));
		final MutableText iText = literal("").append(item.getName()).formatted(Formatting.GREEN);
		iText.setStyle(iText.getStyle().withHoverEvent(new HoverEvent(
				HoverEvent.Action.SHOW_TEXT,
				literal("")
					.append(item.getName())
					.append("\n")
					.append(literal(blockId).formatted(Formatting.GRAY))
			)));
		return iText;
	}
	// ==================================================
	/**
	 * Broadcasts a "first mined" event to all players in the server.
	 * @param player A {@link ServerPlayerEntity} that mined a {@link Block} for their first time.
	 * @param minedBlock The {@link Block} they mined for their first time.
	 * @throws NullPointerException If an argument is {@code null}.
	 */
	public static final void broadcastFirstMine(ServerPlayerEntity player, Block minedBlock) throws NullPointerException
	{
		final var pText = formatEntityText(player);
		final var bText = formatBlockText(minedBlock);
		broadcastBssMessage(player.getServer(),
				literal("").append(WATERMARK).append(" ").append(translatable(TXT_FIRST_MINED, pText, bText)),
				literal("").append(WATERMARK).append(" ")
					.append(pText).append(" just mined their first ").append(bText).append("."));
	}
	
	/**
	 * Broadcasts a "first crafted" event to all players in the server.
	 * @param player A {@link ServerPlayerEntity} that crafted an {@link Item} for their first time.
	 * @param craftedItem The {@link Item} they crafted for their first time.
	 * @throws NullPointerException If an argument is {@code null}.
	 */
	public static final void broadcastFirstCraft(ServerPlayerEntity player, Item craftedItem) throws NullPointerException
	{
		final var pText = formatEntityText(player);
		final var iText = formatItemText(craftedItem);
		broadcastBssMessage(player.getServer(),
				literal("").append(WATERMARK).append(" ").append(translatable(TXT_FIRST_CRAFTED, pText, iText)),
				literal("").append(WATERMARK).append(" ")
					.append(pText).append(" just crafted their first ").append(iText).append("."));
	}
	// --------------------------------------------------
	/**
	 * Broadcasts a stat announcement to all users in the server.<br/><br/>
	 * Users with BSS installed will receive the {@link Text} with translation keys and everything,
	 * whereas non-BSS users will only receive the plain English literal {@link Text}.
	 * This is done for compatibility reasons, so that everyone can actually read the broadcast
	 * properly, given {@link BetterStats} is an optional mod.
	 * @param server The {@link MinecraftServer} where the broadcast will take place.
	 * @param withBssTranslations The {@link Text} given to users who have {@link BetterStats} installed.
	 * @param allLiteral A fallback {@link Text} for users without {@link BetterStats} installed. Such
	 * users do not have the BSS translation keys, and as such, those keys cannot be used for them.
	 * @throws NullPointerException If an argument is {@code null}.
	 */
	private static final void broadcastBssMessage(
			MinecraftServer server,
			Text withBssTranslations, Text allLiteral) throws NullPointerException
	{
		//requirements
		Objects.requireNonNull(server);
		Objects.requireNonNull(withBssTranslations);
		Objects.requireNonNull(allLiteral);
		
		//handle single-player (also account for LAN)
		if(server instanceof IntegratedServer && server.getCurrentPlayerCount() == 1)
		{
			server.getPlayerManager().broadcast(withBssTranslations, false);
			return;
		}
		
		//first of all, no matter what, ensure thread-safety
		server.executeSync(() ->
		{
			//iterate all players in the server
			server.getPlayerManager().getPlayerList().forEach(player ->
			{
				//handle sending the message
				final @Nullable var pp = BetterStatsNetworkHandler.PlayerPrefs.get(player);
				final boolean hasBss = (pp != null && pp.hasBss);
				final var msg = (hasBss? withBssTranslations : allLiteral);
				player.sendMessage(msg, false);
			});
		});
	}
	// ==================================================
}
