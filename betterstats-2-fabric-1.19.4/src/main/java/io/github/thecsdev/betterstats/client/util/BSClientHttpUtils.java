package io.github.thecsdev.betterstats.client.util;

import java.io.IOException;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import io.github.thecsdev.betterstats.BetterStats;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

public final class BSClientHttpUtils
{
	// ==================================================
	private static final int TIMEOUT = 5;
	// ==================================================
	protected BSClientHttpUtils() {}
	// ==================================================
	/**
	 * Sends an HTTP GET request to a remote web-page, returning the
	 * result contents in a {@link String}.
	 * @param url The URL to send the request to.
	 * @param onReady Invoked on the main thread. The {@link Boolean} is true if the
	 * request succeeded, and false if the request failed. The {@link String} is the
	 * returned contents from the server, and is null when the request failed.
	 */
	public static void getAsync(String url, BiConsumer<Boolean, String> onReady)
	{
		//initialize
		Objects.requireNonNull(url);
		Objects.requireNonNull(onReady);
		
		//Minecraft client instance needed
		final var mc = MinecraftClient.getInstance();
		
		//execute the task asynchronously
		Util.getIoWorkerExecutor().execute(() ->
		{
			RequestConfig config = RequestConfig.custom()
			  .setConnectTimeout(TIMEOUT * 1000)
			  .setConnectionRequestTimeout(TIMEOUT * 1000)
			  .setSocketTimeout(TIMEOUT * 1000).build();

			CloseableHttpClient httpClient = HttpClients.custom()
			  .setDefaultRequestConfig(config)
			  .build();

			HttpGet httpGet = new HttpGet(url);
			httpGet.setHeader(HttpHeaders.USER_AGENT, BetterStats.getInstance().contact_sources);
			
			boolean success = false; String result = null;
			try
			{
				CloseableHttpResponse response = httpClient.execute(httpGet);
				result = EntityUtils.toString(response.getEntity());
				success = true;
			}
			catch (IOException e) {}
			
			//onReady must be invoked on the main thread
			final boolean a = success; final String b = result;
			mc.execute(() -> onReady.accept(a, b));
		});
	}
	// --------------------------------------------------
	/**
	 * Sends an HTTP GET request to this mod's repository, which
	 * then returns the list of http-client-side badges to show
	 * on the BSS screen for a given player.
	 * @param playerUUID The {@link UUID} of the player. Must not be null.
	 * @param onReady Invoked when a response is ready. The {@link Boolean}
	 * is false when the request fails, and the {@link Identifier} {@link Set}
	 * is null when the request fails.
	 */
	public static void getRemotePlayerBadgesAsync(
			final UUID playerUUID,
			final BiConsumer<Boolean, Set<Identifier>> onReady)
	{
		//initialize
		Objects.requireNonNull(playerUUID);
		Objects.requireNonNull(onReady);
		
		//obtain the webhook url for the playerUUID
		final var puidStr = playerUUID.toString();
		final var puidHash = DigestUtils.sha256Hex(puidStr);
		final var webhookUrl = BetterStats.getInstance().contact_playerBadgeWebhook +
				"/api/v1/player_badges/" +
				puidHash + ".txt";
		
		//make the web request
		getAsync(webhookUrl, (success, response) ->
		{
			//-----(note: we are already on the main thread from here on)
			//handle failures
			if(!success || StringUtils.isBlank(response))
			{
				onReady.accept(false, null);
				return;
			}
			
			//handle success - parse response
			response = response.trim();
			final var badges = new HashSet<Identifier>();
			final var badgeList = response.split("\\R");
			for(String badgeItem : badgeList)
			{
				try { badges.add(new Identifier(badgeItem)); }
				catch(Exception e) {}
			}
			
			//invoke onReady
			onReady.accept(true, badges);
		});
	}
	// ==================================================
}