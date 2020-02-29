package me.security.notification;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;

import com.google.gson.Gson;

/**
 * Notification implementation of the IFFT api
 * 
 * @see https://ifttt.com/applets/106799825d
 * @author Geraldes Jocelyn
 * @since 24/11/2019
 */
public class NotificationIFTTT extends NotificationSender {

	public static NotificationIFTTT generateFromFile() throws IOException {
		File iftttPwd = new File("ifttt.password");
		System.out.println("Parsing " + iftttPwd.getCanonicalPath() + " file");

		if (!iftttPwd.exists() || !iftttPwd.canRead())
			throw new FileNotFoundException("IFTTT password file doesn't exist or can't be readed");

		List<String> iftttInfo = Files.readAllLines(iftttPwd.toPath());

		if (iftttInfo.size() != 2)
			throw new FileNotFoundException("IFTTT password file doesn't respect defined format");

		return new NotificationIFTTT(iftttInfo.get(0), iftttInfo.get(1));
	}

	private static final Gson GSON = new Gson();

	private String event;
	private String key;

	public NotificationIFTTT(String event, String key) {
		if (event == null)
			throw new IllegalArgumentException("Event must not be null");
		if (event.length() == 0)
			throw new IllegalArgumentException("Event must not be empty");
		if (key == null)
			throw new IllegalArgumentException("Key must not be null");
		if (key.length() != 22)
			throw new IllegalArgumentException("Key must be of size 22 characters");

		this.event = event;
		this.key = key;
	}

	@Override
	public void trigger(String message) throws Exception {
		this.trigger(Arrays.asList(message));
	}

	@Override
	public void trigger(List<String> values) throws Exception {
		if (values == null) throw new IllegalArgumentException("values is null");
		if (values.isEmpty()) throw new IllegalArgumentException("values list is empty");
		for (String v : values)
			if (v == null || v.length() == 0)
				throw new IllegalArgumentException("values contains null or empty value");

		HttpClient httpClient = HttpClientBuilder.create().build();
		HttpPost request = new HttpPost("https://maker.ifttt.com/trigger/" + this.event + "/with/key/" + this.key);
		request.addHeader("content-type", "application/json; charset=UTF-8");
		request.setEntity(new StringEntity(buildJson(values), StandardCharsets.UTF_8));
		HttpResponse hr = httpClient.execute(request);

		if (hr.getStatusLine().getStatusCode() != 200)
			throw new Exception("Response from IFTTT doesn't validate!");
	}

	/**
	 * Build json with IFTTT format
	 * 
	 * @param values The values to format
	 * @return A json formatted HashMap of these values
	 */
	private String buildJson(List<String> values) {
		if (values.size() > 3) throw new IllegalArgumentException("IFTTT doesn't accept more than 3 values.");
		HashMap<String, String> map = new HashMap<String, String>();

		for (int i = 0; i < values.size(); i++) {
			map.put("value" + (i + 1), values.get(i));
		}

		return GSON.toJson(map);
	}

	@Override
	public boolean equals(Object o) {
		if (o == null)
			return false;
		if (!(o instanceof NotificationIFTTT))
			return false;
		
		NotificationIFTTT notif = (NotificationIFTTT) o;
		return notif.event.equals(this.event) && notif.key.equals(this.key);
	}

}