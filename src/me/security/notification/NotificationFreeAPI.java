package me.security.notification;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;

/**
 * Notification implementation of the Free sms api
 * 
 * @see https://mobile.free.fr/moncompte/index.php?page=options
 * @author Geraldes Jocelyn
 * @since 24/11/2019
 */
public class NotificationFreeAPI extends NotificationSender {

	public static NotificationFreeAPI generateFromFile() throws IOException {
		File freePwd = new File("free.password");
		System.out.println("Parsing " + freePwd.getCanonicalPath() + " file");

		if (!freePwd.exists() || !freePwd.canRead())
			throw new FileNotFoundException("Free password file doesn't exist or cannot be readed");

		List<String> freeInfo = Files.readAllLines(freePwd.toPath());

		if (freeInfo.size() != 2)
			throw new FileNotFoundException("Free password file doesn't respect defined format : must have 2 lines");

		try {
			return new NotificationFreeAPI(Integer.parseInt(freeInfo.get(0)), freeInfo.get(1));
		} catch (NumberFormatException e) {
			throw new FileNotFoundException("Free password file have invalid first line : must be a number");
		}
	}

	private final int user;
	private final String password;

	/**
	 * Create an instance of FreeAPI, multiple instances of a specific user can be instanciated
	 * 
	 * @param user     The account number from Free account manager (format must be: 12345678)
	 * @param password Free mobile password (must be 14 characters)
	 * @throws IllegalArgumentException user must be 8 numbers
	 * @throws IllegalArgumentException passwords must be 14 characters
	 */
	public NotificationFreeAPI(int user, String password) throws IllegalArgumentException {
		if (user >= 100000000 || user <= 00100000)
			throw new IllegalArgumentException("User id not valid size"); // Magic values to mask FreeAPI user
		if (password == null)
			throw new IllegalArgumentException("Password is null");
		if (password.length() != 14)
			throw new IllegalArgumentException("Password not valid size");// Password will always be 14 chars
		this.user = user;
		this.password = password;

		/*
		 * We have no way to verify if user and password are valid from Free because
		 * Free doesn't allow empty messages or no messages...
		 */
	}

	/**
	 * Send a notification to this FreeAPI
	 * 
	 * @param message The message to send
	 * @throws ClientProtocolException message must not be null and not empty
	 * @throws IOException             in case of a problem or the connection was aborted
	 * @throws ClientProtocolException in case of an http protocol error
	 * @throws IllegalStateException   The response from free api was different from "HTTP 200 OK".
	 */
	@Override
	public void trigger(String message) throws ClientProtocolException, IOException, IllegalArgumentException, IllegalStateException {
		if (message == null || message.length() == 0) throw new IllegalArgumentException("Message cannot be null or empty.");
		HttpClient httpClient = HttpClientBuilder.create().build();
		HttpGet request = new HttpGet("https://smsapi.free-mobile.fr/sendmsg?user=" + user + "&pass=" + password + "&msg=" + URLEncoder.encode(message, "UTF-8"));
		HttpResponse hr = httpClient.execute(request);

		switch (hr.getStatusLine().getStatusCode()) {

		case 200:// Yeah good response!
			break;

		case 400:// Missing parameter from url ? This will never append
			throw new IllegalStateException("Missing parameter response from free api...");

		case 402:// Too much sms
			throw new IllegalStateException("Too much sms sended to free api.. Must be errorneous loop somewhere");

		case 403:// Service not activated on account
			throw new IllegalStateException("Service is not activated on user account.");

		case 500:// Internal server error
			throw new IllegalStateException("Internal server error from FreeAPI... Maybe they are down ?");

		default:
			throw new IllegalStateException("Response from Free doesn't validate : " + hr.getStatusLine().getStatusCode());
		}
	}

	/**
	 * Simply calls trigger(String message) because they is no use of multiple
	 * values with Free API
	 * 
	 * @param values Concatenated all values into spaced format
	 */
	@Override
	public void trigger(List<String> values) throws Exception {
		trigger(values.stream().collect(Collectors.joining(" ")));
	}

	@Override
	public boolean equals(Object o) {
		if (o == null) return false;
		if (!(o instanceof NotificationFreeAPI)) return false;
		
		NotificationFreeAPI notif = (NotificationFreeAPI) o;
		return notif.user == this.user && notif.password.equals(this.password);
	}

}
