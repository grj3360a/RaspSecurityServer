package me.security.managers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import me.security.notification.NotificationFreeAPI;
import me.security.notification.NotificationIFTTT;
import me.security.notification.NotificationSender;

/**
 * @author Geraldes Jocelyn
 * @since 24/11/2019
 */
public class NotificationManager {

	private final List<NotificationSender> senders;

	public NotificationManager() {
		this.senders = new ArrayList<NotificationSender>();
		System.out.println("NotificationManager created successfuly.");
	}

	/**
	 * Add the specified NotificationSender to senders list This will permit this
	 * NotificationManager to trigger ns with triggerAll for example
	 * 
	 * @param ns {@link NotificationSender} The notification sensor to add to the
	 *           list of senders
	 * @throws IllegalArgumentException ns must not be null and not already
	 *                                  contained in the senders list
	 */
	public void add(NotificationSender ns) throws IllegalArgumentException {
		if (ns == null)
			throw new IllegalArgumentException("Null NotificationSender");
		if (this.senders.contains(ns))
			throw new IllegalArgumentException("NotificationSender is already in senders list");
		this.senders.add(ns);
		System.out.println("Adding " + ns.getClass().getName() + " to NotificationSender list");
	}

	/**
	 * Trigger all NotificationSender in senders with the message param
	 * 
	 * @param message Message to send to NotificationSender
	 * @throws IllegalArgumentException message must not be empty or null
	 */
	public void triggerAll(String message) throws IllegalArgumentException {
		triggerSpecific(null, message);
	}

	/**
	 * Trigger all NotificationSender in senders with values param Note: Multiple
	 * values will never send multiple notification on NotificationSender side.
	 * 
	 * @param values Values to send to NotificationSender
	 * @throws IllegalArgumentException values must not be empty or null
	 */
	public void triggerAll(List<String> values) throws IllegalArgumentException {
		triggerSpecific(null, values);
	}

	/**
	 * Trigger every NotificationFreeAPI object in the senders list with a message
	 * 
	 * @param message The message to send to all NotificationFreeAPI
	 * @see NotificationFreeAPI
	 * @throws IllegalArgumentException message must not be empty or null
	 */
	public void triggerFree(String message) throws IllegalArgumentException {
		triggerSpecific(NotificationFreeAPI.class, message);
	}

	/**
	 * Trigger every NotificationIFTTT object in the senders list with a message
	 * 
	 * @param message The message to send to all NotificationFreeAPI
	 * @see NotificationIFTTT
	 * @throws IllegalArgumentException Must contains between 1 and 3 values
	 */
	public void triggerIFTTT(String... values) throws IllegalArgumentException {
		if (values.length == 0)
			throw new IllegalArgumentException("Cannot send no values in IFTTT");
		if (values.length > 3)
			throw new IllegalArgumentException("Cannot send more than 3 values in IFTTT");
		triggerSpecific(NotificationIFTTT.class, Arrays.asList(values));
	}

	/**
	 * Trigger every targeted object with clazz in the senders list with a message
	 * 
	 * @param clazz   Specific class of targeted NotificationSender,<br>
	 *                must be a parent of NotificatioSender class,<br>
	 *                clazz can be null if wanted to trigger all implementation
	 * @param message The message to send to targeted NotificationSender
	 * @throws IllegalArgumentException message cannot be null or empty
	 */
	private void triggerSpecific(Class<? extends NotificationSender> clazz, String message)
			throws IllegalArgumentException {
		if (message == null)
			throw new IllegalArgumentException("Cannot trigger with null message");
		if (message.length() == 0)
			throw new IllegalArgumentException("Cannot trigger with empty message");

		for (NotificationSender ns : this.senders) {
			if (clazz == null || clazz.isInstance(ns)) {
				try {
					ns.trigger(message);
				} catch (Exception e) {
					e.printStackTrace();
					this.senders.remove(ns);
				}
			}
		}
	}

	/**
	 * Trigger every targeted object with clazz in the senders list with some values
	 * 
	 * @param clazz   Specific class of targeted NotificationSender,<br>
	 *                must be a parent of NotificatioSender class,<br>
	 *                clazz can be null if wanted to trigger all implementation
	 * @param message The values to send to targeted NotificationSender
	 * @throws IllegalArgumentException values cannot be null or empty
	 */
	private void triggerSpecific(Class<? extends NotificationSender> clazz, List<String> values)
			throws IllegalArgumentException {
		if (values == null)
			throw new IllegalArgumentException("values is null");
		if (values.isEmpty())
			throw new IllegalArgumentException("values list is empty");
		for (String v : values)
			if (v == null || v.length() == 0)
				throw new IllegalArgumentException("values contains null or empty value");

		for (NotificationSender ns : this.senders) {
			if (clazz == null || clazz.isInstance(ns)) {
				try {
					ns.trigger(values);
				} catch (Exception e) {
					e.printStackTrace();
					this.senders.remove(ns);
				}
			}
		}
	}

	@Override
	public String toString() {
		String s = "Currently active notifications system:\n";
		for (NotificationSender ns : this.senders) {
			s += " - " + ns.getClass().getName() + "\n";
		}
		return s;
	}

}
