package io.github.thecsdev.betterstats.util.stats;

import static io.github.thecsdev.betterstats.api.util.stats.SUGeneralStat.getGeneralStatText;
import static io.github.thecsdev.tcdcommons.api.util.TextUtils.literal;

import java.util.HashSet;
import java.util.Objects;

import org.jetbrains.annotations.ApiStatus.Experimental;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Nullable;

import io.github.thecsdev.betterstats.BetterStats;
import io.github.thecsdev.betterstats.BetterStatsConfig;
import io.github.thecsdev.betterstats.BetterStatsProperties;
import io.github.thecsdev.betterstats.network.BetterStatsServerPlayNetworkHandler;
import io.github.thecsdev.betterstats.util.BST;
import io.github.thecsdev.tcdcommons.TCDCommons;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
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
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;

/**
 * A {@link Class} that handles announcing players doing
 * things for the first time in the current world.<br/>
 * For example, mining a diamond ore for the first time.
 */
@Experimental //planning to hopefully expose this to the APIs someday
public final @Internal class StatAnnouncementSystem
{
	// ==================================================
	private static final Text WATERMARK;
	private static final BetterStatsConfig BSSC;
	private static final SASConfig SASC;
	// --------------------------------------------------
	public static final HashSet<Block>         FIRST_MINED_BLOCKS;
	public static final HashSet<Item>          FIRST_CRAFTED_ITEMS;
	public static final HashSet<EntityType<?>> FIRST_KILLED_ENTITIES;
	public static final HashSet<EntityType<?>> FIRST_KILLED_BY_ENTITIES;
	public static final HashSet<Identifier>    FIRST_CUSTOM_STATS; //aka "general stats"
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
		SASC = Objects.requireNonNull(BSSC.sasConfig, "SAS config didn't load properly. This shouldn't even happen..");
		
		//define the 'betterstats' watermark
		{
			//create the hover event for the watermark
			final var hoverText = literal(BetterStats.getModName()).formatted(Formatting.YELLOW)
					.append("\n")
					.append(literal(BetterStats.getModID()).formatted(Formatting.GRAY));
			final var hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText);
			final var clickEvent = new ClickEvent(ClickEvent.Action.OPEN_URL, BetterStatsProperties.URL_MODRINTH);
			
			//create the watermark text
			final var text = literal("[≡]").formatted(Formatting.DARK_PURPLE);
			text.setStyle(text.getStyle().withHoverEvent(hoverEvent).withClickEvent(clickEvent));
			WATERMARK = text;
		}
		
		//define the sets
		FIRST_MINED_BLOCKS = new HashSet<Block>();
		FIRST_CRAFTED_ITEMS = new HashSet<Item>();
		FIRST_KILLED_ENTITIES = new HashSet<EntityType<?>>();
		FIRST_KILLED_BY_ENTITIES = new HashSet<EntityType<?>>();
		FIRST_CUSTOM_STATS = new HashSet<Identifier>();
		
		//initialize set entries
		for(final var fmbId : SASC.firstMinedBlocks)
		try
		{
			final var id = Identifier.of(fmbId);
			final @Nullable var fmb = Registries.BLOCK.getOptionalValue(id).orElse(null);
			if(fmb == null) continue;
			FIRST_MINED_BLOCKS.add(fmb);
		}
		catch(InvalidIdentifierException e) { continue; }
		
		for(final var fciId : SASC.firstCraftedItems)
		try
		{
			final var id = Identifier.of(fciId);
			final @Nullable var fci = Registries.ITEM.getOptionalValue(id).orElse(null);
			if(fci == null) continue;
			FIRST_CRAFTED_ITEMS.add(fci);
		}
		catch(InvalidIdentifierException e) { continue; }
		
		for(final var fkeId : SASC.firstKilledEntities)
		try
		{
			final var id = Identifier.of(fkeId);
			final @Nullable var fke = Registries.ENTITY_TYPE.getOptionalValue(id).orElse(null);
			if(fke == null) continue;
			FIRST_KILLED_ENTITIES.add(fke);
		}
		catch(InvalidIdentifierException e) { continue; }
		
		for(final var fkbeId : SASC.firstKilledByEntities)
		try
		{
			final var id = Identifier.of(fkbeId);
			final @Nullable var fkbe = Registries.ENTITY_TYPE.getOptionalValue(id).orElse(null);
			if(fkbe == null) continue;
			FIRST_KILLED_BY_ENTITIES.add(fkbe);
		}
		catch(InvalidIdentifierException e) { continue; }
		
		for(final var fcsId : SASC.firstCustomStats)
		try
		{
			final var id = Identifier.of(fcsId);
			final @Nullable var fcs = Registries.CUSTOM_STAT.getOptionalValue(id).orElse(null);
			if(fcs == null) continue;
			FIRST_CUSTOM_STATS.add(fcs);
		}
		catch(InvalidIdentifierException e) { continue; }
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
			
			//handle first "killed"
			else if(stat.getType() == Stats.KILLED && FIRST_KILLED_ENTITIES.contains(stat.getValue()))
				broadcastFirstKilled(player, (EntityType<?>)stat.getValue());
			
			//handle first "killed by"
			else if(stat.getType() == Stats.KILLED_BY && FIRST_KILLED_BY_ENTITIES.contains(stat.getValue()))
				broadcastFirstKilledBy(player, (EntityType<?>)stat.getValue());
			
			//handle "first death"
			else if(stat.getType() == Stats.CUSTOM &&
					Objects.equals(stat.getValue(), Stats.DEATHS) &&
					FIRST_CUSTOM_STATS.contains(stat.getValue()))
				broadcastFirstDeath(player);
			
			//handle custom stats
			else if(stat.getType() == Stats.CUSTOM && FIRST_CUSTOM_STATS.contains(stat.getValue()))
			{
				@SuppressWarnings("unchecked")
				final var cStat = (Stat<Identifier>)stat;
				broadcastFirstCustomStat(player, cStat, stat.format(newValue));
			}
		}
	}
	// --------------------------------------------------
	private static final MutableText formatPlayerText(PlayerEntity entity)
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
		final var itemId = Objects.toString(Registries.ITEM.getId(item));
		final MutableText iText = literal("").append(item.getName()).formatted(Formatting.GREEN);
		iText.setStyle(iText.getStyle().withHoverEvent(new HoverEvent(
				HoverEvent.Action.SHOW_TEXT,
				literal("")
					.append(item.getName())
					.append("\n")
					.append(literal(itemId).formatted(Formatting.GRAY))
			)));
		return iText;
	}
	
	private static final MutableText formatEntityTypeText(EntityType<?> entityType)
	{
		Objects.requireNonNull(entityType);
		final var entityTypeId = Objects.toString(Registries.ENTITY_TYPE.getId(entityType));
		final MutableText pText = literal("").append(entityType.getName()).formatted(Formatting.YELLOW);
		pText.setStyle(pText.getStyle().withHoverEvent(new HoverEvent(
				HoverEvent.Action.SHOW_TEXT,
				literal("")
					.append(entityType.getName())
					.append("\n")
					.append(literal(entityTypeId).formatted(Formatting.GRAY)))
			));
		return pText;
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
		final var pText = formatPlayerText(player);
		final var bText = formatBlockText(minedBlock);
		broadcastBssMessage(player.getServer(),
				literal("").append(WATERMARK).append(" ").append(BST.sas_firstMine(pText, bText)),
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
		final var pText = formatPlayerText(player);
		final var iText = formatItemText(craftedItem);
		broadcastBssMessage(player.getServer(),
				literal("").append(WATERMARK).append(" ").append(BST.sas_firstCraft(pText, iText)),
				literal("").append(WATERMARK).append(" ")
					.append(pText).append(" just crafted their first ").append(iText).append("."));
	}
	
	/**
	 * Broadcasts a "first killed" event to all players in the server.
	 * @param player A {@link ServerPlayerEntity} that killed an {@link EntityType} for their first time.
	 * @param victimType The {@link EntityType} that was killed.
	 * @throws NullPointerException If an argument is {@code null}.
	 */
	public static final void broadcastFirstKilled(ServerPlayerEntity player, EntityType<?> victimType)
	{
		final var pText = formatPlayerText(player);
		final var etText = formatEntityTypeText(victimType);
		broadcastBssMessage(player.getServer(),
				literal("").append(WATERMARK).append(" ").append(BST.sas_firstKill(pText, etText)),
				literal("").append(WATERMARK).append(" ").append(pText)
					.append(" just killed a ").append(etText).append(" for their first time."));
	}
	
	/**
	 * Broadcasts a "first killed by" event to all players in the server.
	 * @param player A {@link ServerPlayerEntity} that got killed by an {@link EntityType} for their first time.
	 * @param killerType The {@link EntityType} that killed the player.
	 * @throws NullPointerException If an argument is {@code null}.
	 */
	public static final void broadcastFirstKilledBy(ServerPlayerEntity player, EntityType<?> killerType)
	{
		final var pText = formatPlayerText(player);
		final var etText = formatEntityTypeText(killerType);
		broadcastBssMessage(player.getServer(),
				literal("").append(WATERMARK).append(" ").append(BST.sas_firstDeathTo(pText, etText)),
				literal("").append(WATERMARK).append(" ").append(pText)
					.append(" just died to a ").append(etText).append(" for their first time."));
	}
	
	/**
	 * Broadcasts a "first death" event to all players in the server.
	 * @param player A {@link ServerPlayerEntity} that died for their first time.
	 * @throws NullPointerException If an argument is {@code null}.
	 */
	public static final void broadcastFirstDeath(ServerPlayerEntity player)
	{
		final var hardcore = player.getServer().isHardcore();
		final var literalBrightSide = hardcore ? " On the bright side, it likely won't happen again." : "";
		
		final var pText = formatPlayerText(player);
		final var translatableText = hardcore ? BST.sas_firstDeath_hc1(pText) : BST.sas_firstDeath(pText);
		broadcastBssMessage(player.getServer(),
				literal("").append(WATERMARK).append(" ").append(translatableText),
				literal("").append(WATERMARK).append(" ")
				.append(pText).append(" died for their first time." + literalBrightSide));
	}
	
	/**
	 * Broadcasts a player increasing the value of a "custom stat" for their first time.
	 * @param player The {@link ServerPlayerEntity} whose general/custom stat increased.
	 * @param stat The general/custom {@link Stat} in question.
	 * @param statValue The new {@link Stat} value.
	 * @throws NullPointerException If an argument is {@code null}.
	 */
	public static final void broadcastFirstCustomStat(
			ServerPlayerEntity player, Stat<Identifier> stat, String statValue) throws NullPointerException
	{
		final var pText = formatPlayerText(player);
		final var sText = literal("").append(getGeneralStatText(stat)).formatted(Formatting.GRAY);
		final var vText = literal(statValue).formatted(Formatting.GREEN);
		broadcastBssMessage(player.getServer(),
				literal("").append(WATERMARK).append(" ").append(BST.sas_custom(pText, sText, vText)),
				literal("").append(WATERMARK).append(" ").append(pText)
					.append(" just increased their ").append(sText).append(" stat value to ")
					.append(vText).append("."));
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
		if((TCDCommons.isClient() && server instanceof IntegratedServer) && server.getCurrentPlayerCount() == 1)
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
				final @Nullable var pp = BetterStatsServerPlayNetworkHandler.of(player);
				final boolean hasBss = (pp != null && pp.hasBssInstalled);
				final var msg = (hasBss? withBssTranslations : allLiteral);
				player.sendMessage(msg, false);
			});
		});
	}
	// ==================================================
}
