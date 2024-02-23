package io.github.thecsdev.betterstats;

import java.util.Locale;

import io.github.thecsdev.betterstats.util.stats.SASConfig;
import io.github.thecsdev.tcdcommons.api.config.AutoConfig;
import io.github.thecsdev.tcdcommons.api.config.annotation.NonSerialized;
import io.github.thecsdev.tcdcommons.api.config.annotation.SerializedAs;

public class BetterStatsConfig extends AutoConfig
{
	// ==================================================
	public static @NonSerialized boolean DEBUG_MODE = false;
	
	/**
	 * Indicates whether or not the user consents to this mod
	 * communicating with the server the user is playing on.
	 * @apiNote CHANGING THE VALUE OF THIS VARIABLE TO {@code true}
	 * MUST NOT AND SHALL NOT BE DONE WITHOUT THE USER'S CONSENT!
	 */
	public static @NonSerialized boolean CLIENT_NET_CONSENT = false;
	
	/**
	 * Defines whether or not certain features are available for
	 * a given user, such as specific API endpoints, and the
	 * user being shown certain links in GUI interfaces.
	 */
	public static final @NonSerialized boolean RESTRICTED_MODE;
	// --------------------------------------------------
	public @SerializedAs("client-guiSmoothScroll") boolean guiSmoothScroll = true;
	public @SerializedAs("client-guiMobsFollowCursor") boolean guiMobsFollowCursor = true;
	public @SerializedAs("client-trustAllServersBssNet") boolean trustAllServersBssNet = true;
	public @SerializedAs("server-registerCommands") boolean registerCommands = true;
	public @SerializedAs("server-enableSAS") boolean enableServerSAS = true;
	public @SerializedAs("server-sasConfig") SASConfig sasConfig = new SASConfig();
	// ==================================================
	public BetterStatsConfig(String name) { super(name); }
	static
	{
		//define RESTRICTED_MODE value
		final var lc = Locale.getDefault().getCountry().toLowerCase();
		final var rc = "cn".equals(lc);
		RESTRICTED_MODE = rc; //TODO - How do I check if the client is authenticated with Mojang?
	}
	// ==================================================
}