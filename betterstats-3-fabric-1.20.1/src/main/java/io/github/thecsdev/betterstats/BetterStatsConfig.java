package io.github.thecsdev.betterstats;

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
	public static @NonSerialized boolean LEGAL_NET_CONSENT = false;
	// --------------------------------------------------
	public @SerializedAs("client-guiMobsFollowCursor") boolean guiMobsFollowCursor = true;
	public @SerializedAs("server-enableSAS") boolean enableServerSAS = true;
	// ==================================================
	public BetterStatsConfig(String name) { super(name); }
	// ==================================================
}