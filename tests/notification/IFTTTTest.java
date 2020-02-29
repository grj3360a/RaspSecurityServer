package notification;

import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import me.security.notification.NotificationIFTTT;

public class IFTTTTest {

	private NotificationIFTTT ifttt;

	@Before
	public void setUp() throws Exception {
		this.ifttt = NotificationIFTTT.generateFromFile();
		if (this.ifttt == null)
			fail("Cannot test this class without valid identification");
	}

	@After
	public void tearDown() throws Exception {
		this.ifttt = null;
	}

	/*
	 * Constructor related
	 */

	@Test(expected = IllegalArgumentException.class)
	public void testConstructorKeyNull() {
		new NotificationIFTTT("Event", null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testConstructorKeyInvalidSize() {
		new NotificationIFTTT("Event", "Anything");
	}

	@Test
	public void testConstructorKeyValidSize() {
		new NotificationIFTTT("Event", "1234567890123456789012");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testConstructorEventNull() {
		new NotificationIFTTT(null, "1234567890123456789012");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testConstructorEventEmpty() {
		new NotificationIFTTT("", "1234567890123456789012");
	}

	/*
	 * Triggers
	 */

	@Test
	public void testTrigger() throws Exception {
		this.ifttt.trigger(Arrays.asList("Arg1", "Arg2"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testTriggerNull() throws Exception {
		this.ifttt.trigger((List<String>) null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testTriggerEmpty() throws Exception {
		this.ifttt.trigger(Collections.emptyList());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testTriggerFilledWithNull() throws Exception {
		this.ifttt.trigger(Arrays.asList(null, null));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testTriggerFilledWithEmptyString() throws Exception {
		this.ifttt.trigger(Arrays.asList("", ""));
	}

}
