package notification;

import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import me.security.notification.NotificationFreeAPI;

public class FreeAPITest {

	private NotificationFreeAPI free;

	@Before
	public void setUp() throws Exception {
		this.free = NotificationFreeAPI.generateFromFile();
		if (this.free == null)
			fail("Cannot test this class without valid identification");
	}

	@After
	public void tearDown() throws Exception {
		this.free = null;
	}

	/*
	 * Constructor related
	 */

	@Test
	public void testConstructorUserValid() {
		new NotificationFreeAPI(12345678, "12345678901234");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testConstructorUserTooShort() {
		new NotificationFreeAPI(12, "password");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testConstructorUserTooLong() {
		new NotificationFreeAPI(Integer.MAX_VALUE, "password");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testConstructorPasswordNull() {
		new NotificationFreeAPI(12345678, null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testConstructorPasswordInvalidSize() {
		new NotificationFreeAPI(12345678, "123456789012345678");
	}

	/*
	 * Trigger related
	 */

	@Test
	public void testTriggerValid() throws Exception {
		this.free.trigger("Hello world!");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testTriggerNull() throws Exception {
		this.free.trigger((String) null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testTriggerEmpty() throws Exception {
		this.free.trigger("");
	}

}
