package io.github.thecsdev.betterstats.client.gui.screen;

import static io.github.thecsdev.betterstats.BetterStats.getModID;
import static io.github.thecsdev.betterstats.client.BetterStatsClient.MC_CLIENT;
import static io.github.thecsdev.betterstats.util.io.BetterStatsWebApiUtils.GSON;
import static io.github.thecsdev.tcdcommons.api.util.TextUtils.literal;
import static io.github.thecsdev.tcdcommons.api.util.TextUtils.translatable;
import static io.github.thecsdev.tcdcommons.api.util.io.HttpUtils.fetchSync;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Objects;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Nullable;

import com.google.common.base.Charsets;
import com.google.gson.JsonObject;

import io.github.thecsdev.betterstats.api.util.io.IStatsProvider;
import io.github.thecsdev.betterstats.api.util.io.StatsProviderIO;
import io.github.thecsdev.betterstats.util.BST;
import io.github.thecsdev.betterstats.util.io.BetterStatsWebApiUtils;
import io.github.thecsdev.tcdcommons.api.client.gui.other.TLabelElement;
import io.github.thecsdev.tcdcommons.api.client.gui.screen.TStackTraceScreen;
import io.github.thecsdev.tcdcommons.api.client.gui.widget.TButtonWidget;
import io.github.thecsdev.tcdcommons.api.util.enumerations.HorizontalAlignment;
import io.github.thecsdev.tcdcommons.api.util.io.HttpUtils.FetchOptions;
import io.github.thecsdev.tcdcommons.api.util.io.cache.CachedResource;
import io.github.thecsdev.tcdcommons.api.util.io.cache.CachedResourceManager;
import io.github.thecsdev.tcdcommons.api.util.io.cache.IResourceFetchTask;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.Unpooled;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.thread.ThreadExecutor;

public final class QuickShareUploadScreen extends QuickShareScreen
{
	// ==================================================
	private static final String CRLF = "\r\n";
	// --------------------------------------------------
	private @Internal volatile boolean   __started        = false;
	private @Internal volatile int       __stage          = 0;
	private @Internal volatile Throwable __error          = null;
	private @Internal volatile String    __quickShareCode = null; //is present only after the upload successfully finishes
	// --------------------------------------------------
	private final IStatsProvider stats;
	// ==================================================
	public QuickShareUploadScreen(@Nullable Screen parent, IStatsProvider stats)
			throws NullPointerException
	{
		super(parent, BST.gui_qsscreen_upload_title());
		this.stats = Objects.requireNonNull(stats);
	}
	// ==================================================
	protected final @Override void init()
	{
		//start the operation
		__start__stage1();
		
		//the primary label
		final var lbl = new TLabelElement(0, 0, getWidth(), getHeight());
		lbl.setTextHorizontalAlignment(HorizontalAlignment.CENTER);
		lbl.setTextColor(0xffffff00);
		addChild(lbl, false);
		
		//the primary label text
		switch(this.__stage)
		{
			case 0: lbl.setText(BST.gui_qsscreen_upload_stage0()); break;
			case 1: lbl.setText(BST.gui_qsscreen_upload_stage1()); break;
			case 2: lbl.setText(BST.gui_qsscreen_upload_stage2()); break;
			case 3: lbl.setText(BST.gui_qsscreen_upload_stage3()); break;
			case 4:
				//set final stage label text
				final var codeStr = StringUtils.removeEnd("" + this.__quickShareCode, QSC_SUFFIX)
					.toUpperCase(Locale.ENGLISH);
				final var codeTxt = literal(codeStr).formatted(Formatting.WHITE);
				lbl.setText(BST.gui_qsscreen_upload_stage4(codeTxt));
				
				//add a "Done" button
				final var btn_done = new TButtonWidget((getWidth() / 2) - 75, getHeight() - 30, 150, 20);
				btn_done.setText(translatable("gui.done"));
				btn_done.setOnClick(__ -> close());
				addChild(btn_done, false);
				break;
			default: break;
		}
	}
	// ==================================================
	private @Internal void __start_onError(@Nullable Exception exception)
	{
		this.__stage = -1;
		this.__error = exception;
		if(!isOpen()) return; //break the operation if the user closed the screen
		
		final var exc = new Exception("Failed to quick-share an MCBS file.", exception);
		exc.printStackTrace();
		MC_CLIENT.setScreen(new TStackTraceScreen(getParentScreen(), exc).getAsScreen());
		//refresh();
	}
	// --------------------------------------------------
	private @Internal void __start__stage1()
	{
		//prepare
		if(this.__started) return;
		this.__started = true;
		this.__stage = 1;
		if(!isOpen()) return; //break the operation if the user closed the screen
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
		if(!isOpen()) return; //break the operation if the user closed the screen
		refresh();
		
		//fetch the upload link
		CachedResourceManager.getResourceAsync(
				Identifier.of(getModID(), "quick_share/latest_upload_url.json"),
				new IResourceFetchTask<JsonObject>()
		{
			public ThreadExecutor<?> getMinecraftClientOrServer() { return MC_CLIENT; }
			public Class<JsonObject> getResourceType() { return JsonObject.class; }
			public CachedResource<JsonObject> fetchResourceSync() throws Exception
			{
				//obtain the API url
				@Nullable String endpoint = null;
				try { endpoint = links.get("quickshare_guu").getAsString(); }
				catch(Exception e) { throw new IOException("Failed to parse BSS API url.", e); }
				
				//send an http request to the endpoint
				final var httpResult = fetchSync(endpoint, new FetchOptions()
				{
					public final @Override String   method() { return "POST"; }
					public final @Override Header[] headers() { return new Header[] { new BasicHeader("Content-Type", "application/json") }; }
					public final @Override Object   body() { return "{}"; }
				});
				@Nullable String httpResultStr = null;
				try
				{
					//read the response body as string
					final @Nullable var httpResultEntity = httpResult.getEntity();
					if(httpResultEntity == null) throw new IOException("Missing HTTP response body.");
					httpResultStr = EntityUtils.toString(httpResultEntity);
					
					//throw an exception if the server does not respond with status 200
					final int statusCode = httpResult.getStatusLine().getStatusCode();
					if(statusCode != 200)
						throw new IOException(
							"BSS API server response message:\n----------\n" + httpResultStr + "\n----------",
							new HttpResponseException(statusCode, httpResult.getStatusLine().getReasonPhrase()));
				}
				finally { IOUtils.closeQuietly(httpResult); }
				
				//handle the result
				final var result = GSON.fromJson(httpResultStr, JsonObject.class);
				@Nullable Instant expires = null;
				
				try { expires = Instant.parse(result.get("expires").getAsString()); }
				catch(Exception insParseErr) { expires = Instant.now().plusSeconds(30); }
				
				return new CachedResource<JsonObject>(result, httpResultStr.length(), expires);
			}
			public void onError(Exception error) { __start_onError(error); }
			public void onReady(JsonObject result) { __start__stage3(result); }
		});
	}
	
	private @Internal void __start__stage3(final JsonObject uploadUrlData)
	{
		//prepare
		this.__stage = 3;
		if(!isOpen()) return; //break the operation if the user closed the screen
		refresh();
		
		//perform the upload
		CachedResourceManager.getResourceAsync(
				Identifier.of(getModID(), "quick_share/uploads/" + System.currentTimeMillis() + ".txt"),
				new IResourceFetchTask<String>()
		{
			public ThreadExecutor<?> getMinecraftClientOrServer() { return MC_CLIENT; }
			public Class<String> getResourceType() { return String.class; }
			public CachedResource<String> fetchResourceSync() throws Exception
			{
				//obtain upload request information
				final String        url      = uploadUrlData.get("url").getAsString();
				final String        method   = uploadUrlData.get("method").getAsString().toUpperCase(Locale.ENGLISH);
				final String        filename = uploadUrlData.get("filename").getAsString();
				final var           fields   = uploadUrlData.get("fields").getAsJsonObject();
				final BasicHeader[] headers  = uploadUrlData.get("headers").getAsJsonObject()
					.entrySet().stream()
					.map(entry -> new BasicHeader(entry.getKey(), entry.getValue().getAsString()))
					.toArray(BasicHeader[]::new);
				
				//checks
				if(!Objects.equals(method, "POST"))
					throw new UnsupportedOperationException("BSS API server told me to perform HTTP " + method +
							" to upload the quick-share file, but I only support HTTP POST.");
				
				//construct and upload the HTTP request body
				final var multipartBoundary = "----WebKitFormBoundary@" + System.currentTimeMillis();
				final var httpBody = new PacketByteBuf(Unpooled.buffer());
				@Nullable CloseableHttpResponse response = null;
				try
				{
					//add multipart form fields to the request body
					for(final var field : fields.entrySet())
					{
						final var str = "--" + multipartBoundary + CRLF +
							"Content-Disposition: form-data; name=\"" + field.getKey() + "\"" + CRLF +
							"Content-Type: text/plain" + CRLF + CRLF +
							field.getValue().getAsString() + CRLF;
						httpBody.writeBytes(str.getBytes(Charsets.UTF_8));
					}
					{
						final var str = "--" + multipartBoundary + CRLF +
							"Content-Disposition: form-data; name=\"file\"; filename=\"" + filename + "\"" + CRLF +
							"Content-Type: application/octet-stream" + CRLF + CRLF;
						httpBody.writeBytes(str.getBytes(Charsets.UTF_8));
						StatsProviderIO.write(httpBody, QuickShareUploadScreen.this.stats);
						httpBody.writeBytes(CRLF.getBytes(Charsets.UTF_8));
					}
					httpBody.writeBytes(("--" + multipartBoundary + "--" + CRLF).getBytes(Charsets.UTF_8));
					
					//perform the request
					response = fetchSync(url, new FetchOptions()
					{
						public final @Override String   method() { return method; }
						public final @Override Header[] headers() { return headers; }
						public final @Override Object   body()
						{
							return new InputStreamEntity(
									new ByteBufInputStream(httpBody),
									ContentType.parse("multipart/form-data; boundary=" + multipartBoundary));
						}
						
					});
					
					//read the response body
					@Nullable String responseBody = "";
					if(response.getEntity() != null)
						responseBody = EntityUtils.toString(response.getEntity());
					
					//handle non-2XX responses
					final var statusCode = response.getStatusLine().getStatusCode();
					final var statusReason = response.getStatusLine().getReasonPhrase();
					if(statusCode < 200 || statusCode > 299)
						throw new IOException(
							"Cloud server response message:\n----------\n" +
								responseBody + "\n----------",
							new HttpResponseException(statusCode, statusReason));
					
					//finally, conclude
					QuickShareUploadScreen.this.__quickShareCode = filename;
					final var result = new StringBuilder();
					result.append("HTTP " + method + " " + url + CRLF);
					result.append("Response: " + statusCode + " " + statusReason + CRLF);
					result.append("Response body:" + CRLF + CRLF + responseBody);
					
					return new CachedResource<String>(
							result.toString(),
							result.length(),
							Instant.now().plus(Duration.ofDays(7)));
				}
				finally
				{
					try { httpBody.release(); } catch(Exception e) {}
					if(response != null) IOUtils.closeQuietly(response);
				}
			}
			public void onError(Exception error) { __start_onError(error); }
			public void onReady(String result) { __start__stage4(); }
		});
	}
	
	private @Internal void __start__stage4()
	{
		this.__stage = 4;
		if(!isOpen()) return; //break the operation if the user closed the screen
		refresh();
	}
	// ==================================================
}