package me.security;

import java.io.File;
import java.io.IOException;

import me.security.managers.DatabaseManager;
import me.security.managers.NotificationManager;
import me.security.managers.ServerSecurity;
import me.security.notification.NotificationFreeAPI;
import me.security.notification.NotificationIFTTT;

public class App {

	/**
	 * The main entry point of RaspSecurity. Utilization of the argument --simulated
	 * is needed if running this on Windows
	 */
	public static void main(String[] args) throws UnsatisfiedLinkError, IOException {
		System.out.println("Launching RaspSecurityServer... (" + new File(".").getAbsolutePath() + ")");

		DatabaseManager db = DatabaseManager.generateFromFile();
		NotificationManager notif = new NotificationManager();

		try {
			NotificationFreeAPI fm = NotificationFreeAPI.generateFromFile();
			notif.add(fm);
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			NotificationIFTTT ifttt = NotificationIFTTT.generateFromFile();
			notif.add(ifttt);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		new ServerSecurity(db, notif);

		// Adding closing mechanism to shutdown DB connection
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			System.out.println("Closing RaspSecurityServer...");
		}));

		System.out.println("Started successfuly.");
	}
	
}
