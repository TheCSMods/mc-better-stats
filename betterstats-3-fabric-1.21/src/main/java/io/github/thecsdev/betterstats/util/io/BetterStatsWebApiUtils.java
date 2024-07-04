package io.github.thecsdev.betterstats.util.io;

import static io.github.thecsdev.betterstats.BetterStats.getModID;
import static io.github.thecsdev.betterstats.BetterStatsProperties.URL_REMOTE_APIS;
import static io.github.thecsdev.betterstats.client.BetterStatsClient.MC_CLIENT;
import static io.github.thecsdev.tcdcommons.api.util.io.HttpUtils.fetchSync;

import java.time.Duration;
import java.time.Instant;
import java.util.function.Consumer;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpException;
import org.apache.http.client.HttpResponseException;
import org.apache.http.util.EntityUtils;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Nullable;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import io.github.thecsdev.betterstats.BetterStats;
import io.github.thecsdev.tcdcommons.api.util.io.cache.CachedResource;
import io.github.thecsdev.tcdcommons.api.util.io.cache.CachedResourceManager;
import io.github.thecsdev.tcdcommons.api.util.io.cache.IResourceFetchTask;
import net.minecraft.util.Identifier;
import net.minecraft.util.thread.ThreadExecutor;

/**
 * {@link Internal} utilities for {@link BetterStats}'s HTTP APIs.
 */
@Internal
public final class BetterStatsWebApiUtils
{
	// ==================================================
	public static final @Internal Gson GSON = new Gson();
	// ==================================================
	private BetterStatsWebApiUtils() {}
	// ==================================================
	/**
	 * Asynchronously fetches {@link BetterStats}'s API URLs.
	 */
	public static final void fetchBssApiLinksAsync(
			ThreadExecutor<?> minecraftClientOrServer,
			Consumer<JsonObject> onReady,
			Consumer<Exception> onError) throws NullPointerException
	{
		fetchBssApiLinks(true, minecraftClientOrServer, onReady, onError);
	}
	
	/**
	 * Fetches {@link BetterStats}'s API URLs.
	 */
	public static final void fetchBssApiLinks(
			boolean isAsync,
			ThreadExecutor<?> minecraftClientOrServer,
			Consumer<JsonObject> onReady,
			Consumer<Exception> onError) throws NullPointerException
	{
		//prepare to fetch
		final var crId = Identifier.of(getModID(), "links.json");
		final var irft = new IResourceFetchTask<JsonObject>()
		{
			public final @Override ThreadExecutor<?> getMinecraftClientOrServer() { return MC_CLIENT; }
			public final @Override Class<JsonObject> getResourceType() { return JsonObject.class; }
			public final @Override CachedResource<JsonObject> fetchResourceSync() throws Exception
			{
				//perform the http request
				final var httpResult = fetchSync(URL_REMOTE_APIS);
				@Nullable String httpResultStr = null;
				try
				{
					//throw an exception if the server does not respond with status 200
					final int statusCode = httpResult.getStatusLine().getStatusCode();
					if(statusCode != 200)
						throw new HttpResponseException(statusCode, httpResult.getStatusLine().getReasonPhrase());
					
					//read the response body as string
					final @Nullable var httpResultEntity = httpResult.getEntity();
					if(httpResultEntity == null) throw new HttpException("Missing HTTP response body.");
					httpResultStr = EntityUtils.toString(httpResultEntity);
				}
				finally { IOUtils.closeQuietly(httpResult); }
				
				//return the result
				return new CachedResource<JsonObject>(
						GSON.fromJson(httpResultStr, JsonObject.class),
						httpResultStr.length(),
						Instant.now().plus(Duration.ofMinutes(42)));
			}
			public final @Override void onReady(JsonObject result) { onReady.accept(result); }
			public final @Override void onError(Exception error) { onError.accept(error); }
		};
		
		//fetch
		if(isAsync) CachedResourceManager.getResourceAsync(crId, irft);
		else CachedResourceManager.getResourceSync(crId, irft);
	}
	// ==================================================
}