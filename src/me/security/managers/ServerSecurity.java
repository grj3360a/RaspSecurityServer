package me.security.managers;

public class ServerSecurity {

	private final DatabaseManager db;
	private final NotificationManager notif;
	
	public ServerSecurity(DatabaseManager db, NotificationManager notif) {
		if(db == null) throw new IllegalArgumentException();
		if(notif == null) throw new IllegalArgumentException();
		
		this.db = db;
		this.notif = notif;
	}

	public NotificationManager getNotif() {
		return this.notif;
	}

	public DatabaseManager getDb() {
		return this.db;
	}

}
