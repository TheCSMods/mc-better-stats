package io.github.thecsdev.betterstats.client.gui.screen;

import static io.github.thecsdev.betterstats.BetterStats.getModID;
import static io.github.thecsdev.betterstats.client.BetterStatsClient.MC_CLIENT;
import static io.github.thecsdev.betterstats.util.io.BetterStatsWebApiUtils.GSON;
import static io.github.thecsdev.tcdcommons.api.util.io.HttpUtils.fetchSync;

import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Objects;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpException;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonObject;

import io.github.thecsdev.betterstats.api.client.gui.screen.BetterStatsScreen;
import io.github.thecsdev.betterstats.api.util.io.RAMStatsProvider;
import io.github.thecsdev.betterstats.util.io.BetterStatsWebApiUtils;
import io.github.thecsdev.tcdcommons.api.util.TextUtils;
import io.github.thecsdev.tcdcommons.api.util.io.HttpUtils.FetchOptions;
import io.github.thecsdev.tcdcommons.api.util.io.cache.CachedResource;
import io.github.thecsdev.tcdcommons.api.util.io.cache.CachedResourceManager;
import io.github.thecsdev.tcdcommons.api.util.io.cache.IResourceFetchTask;
import io.netty.buffer.Unpooled;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.thread.ThreadExecutor;

public class QuickShareDownloadScreen extends QuickShareScreen
{
	// ==================================================
	private @Internal final String quickShareCode;
	// --------------------------------------------------
	private @Internal volatile boolean   __started = false;
	private @Internal volatile int       __stage   = 0;
	private @Internal volatile Throwable __error   = null;
	// ==================================================
	public QuickShareDownloadScreen(@Nullable Screen parent, String quickShareCode)
		throws NullPointerException
	{
		super(parent, TextUtils.translatable("betterstats.gui.qs_screen.download.title"));
		Objects.requireNonNull(quickShareCode);
		quickShareCode = quickShareCode.toLowerCase(Locale.ENGLISH);
		if(!quickShareCode.endsWith(QSC_SUFFIX)) quickShareCode += QSC_SUFFIX;
		this.quickShareCode = quickShareCode;
	}
	// ==================================================
	protected final @Override void init()
	{
		//start the operation
		__start__stage1();
		
		//FIXME - IMPLEMENT GUI
	}
	// ==================================================
	private @Internal void __start_onError(@Nullable Exception exception)
	{
		this.__stage = -1;
		this.__error = exception;
		new Exception("Failed to retrieve a quick-shared MCBS file '" +
				this.quickShareCode + "'", exception).printStackTrace();
		refresh();
	}
	// --------------------------------------------------
	private @Internal void __start__stage1()
	{
		//prepare
		if(this.__started) return;
		this.__started = true;
		this.__stage = 1;
		//note: do not call `refresh()` here
		
		//fetch the API links
		BetterStatsWebApiUtils.fetchBssApiLinksAsync(MC_CLIENT,
				json -> __start__stage2(json),
				error -> __start_onError(error));
	}
	

	private @Internal void __start__stage2(final JsonObject links)
	{
		//prepare
		this.__stage = 2;
		refresh();
		
		//parse the user-input identifier
		@Nullable Identifier id = null;
		try { id = Identifier.of(getModID(), "quick_share/downloads/" + this.quickShareCode); }
		catch(Exception e) { __start_onError(e); return; }
		
		//fetch
		CachedResourceManager.getResourceAsync(id, new IResourceFetchTask<byte[]>()
		{
			public final @Override ThreadExecutor<?> getMinecraftClientOrServer() { return MC_CLIENT; }
			public final @Override Class<byte[]> getResourceType() { return byte[].class; }
			public final @Override CachedResource<byte[]> fetchResourceSync() throws Exception
			{
				//fetch the download link
				@Nullable JsonObject            downloadUrlData = null;
				@Nullable CloseableHttpResponse response        = null;
				try
				{
					//perform the http request
					response = fetchSync(links.get("quickshare_gdu").getAsString(), new FetchOptions()
					{
						public final @Override String method() { return "POST"; }
						public final @Override Object body()
						{
							final var json = new JsonObject();
							json.addProperty("file", QuickShareDownloadScreen.this.quickShareCode);
							return json;
						}
					});
					
					//collect the response message
					String responseMessage = "";
					if(response.getEntity() != null)
						responseMessage = EntityUtils.toString(response.getEntity());
					
					//handle the response status code
					final var statusCode = response.getStatusLine().getStatusCode();
					final var statusMessage = response.getStatusLine().getReasonPhrase();
					if(statusCode != 200)
						throw new HttpException(
							"BSS API server response message:\n----------\n" + responseMessage + "\n----------",
							new HttpResponseException(statusCode, statusMessage));
					
					//parse the response json
					downloadUrlData = GSON.fromJson(responseMessage, JsonObject.class);
				}
				finally { if(response != null) IOUtils.closeQuietly(response); }
				
				//prepare to download the MCBS file
				QuickShareDownloadScreen.this.__stage = 3;
				QuickShareDownloadScreen.this.refresh();
				
				final var url     = downloadUrlData.get("url").getAsString();
				final var method  = downloadUrlData.get("method").getAsString();
				//final var expires = Instant.parse(downloadUrlData.get("expires").getAsString()); -- download url expiration is ignored for now
				final var headers = downloadUrlData.get("headers").getAsJsonObject()
						.entrySet().stream()
						.map(entry -> new BasicHeader(entry.getKey(), entry.getValue().getAsString()))
						.toArray(BasicHeader[]::new);
				
				//checks
				if(!Objects.equals(method, "GET"))
					throw new UnsupportedOperationException("BSS API server told me to perform HTTP " + method +
							" to download the quick-share file, but I only support HTTP GET.");
				
				//perform the download
				try
				{
					//fetch
					response = fetchSync(url, new FetchOptions()
					{
						public final @Override String   method() { return method; }
						public final @Override Header[] headers() { return headers; }
					});
					
					//handle non-200 status codes
					final var statusCode = response.getStatusLine().getStatusCode();
					final var statusMessage = response.getStatusLine().getReasonPhrase();
					if(statusCode != 200)
					{
						//read the response body
						@Nullable String responseBody = "";
						if(response.getEntity() != null)
							responseBody = EntityUtils.toString(response.getEntity());
						
						//throw the exception
						throw new HttpException(
								"Cloud server response message:\n----------\n" +
									responseBody + "\n----------",
								new HttpResponseException(statusCode, statusMessage));
					}
					
					//handle the response body
					final byte[] responseBody = (response.getEntity() != null) ?
							EntityUtils.toByteArray(response.getEntity()) : null;
					if(responseBody == null)
						throw new HttpException("Cloud server responded with an empty file with no data inside of it.");
					
					//finally, conclude
					@Nullable Instant expires_file = null;
					try { expires_file = Instant.parse(downloadUrlData.get("expires_file").getAsString()); }
					catch(Exception parseExc) { expires_file = Instant.now().plus(Duration.ofHours(48)); }
					
					return new CachedResource<byte[]>(responseBody, responseBody.length, expires_file);
				}
				finally { if(response != null) IOUtils.closeQuietly(response); }
			}
			public final @Override void onError(Exception error) { __start_onError(error); }
			public final @Override void onReady(byte[] result) { __start__stage3(result); }
		});
	}
	
	private @Internal void __start__stage3(final byte[] mcbs)
	{
		try
		{
			final var buffer = new PacketByteBuf(Unpooled.wrappedBuffer(mcbs));
			final var stats  = new RAMStatsProvider(buffer, true);
			final var bss    = new BetterStatsScreen(null, stats);
			MC_CLIENT.setScreen(bss.getAsScreen());
		}
		catch(Exception exc) { __start_onError(exc); }
	}
	// ==================================================
}