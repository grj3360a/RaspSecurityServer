package me.security.managers;

import java.util.Date;

import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

public class FirebaseManager {
	
	private final FirebaseApp app;
	
	public FirebaseManager() {
		FirebaseOptions options = new FirebaseOptions.Builder()
			    .setCredentials(GoogleCredentials.create(new AccessToken("s9WeINBHIHK6DZ3pnp3gF8fAZi00dyMFVEj43jpq", new Date(Integer.MAX_VALUE))))
			    .setDatabaseUrl("https://raspsecurity-5092d.firebaseio.com/")
			    .build();

		this.app = FirebaseApp.initializeApp(options);
	}
	
}
