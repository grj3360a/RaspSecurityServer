/**
 * 
 */
package managers;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import me.security.managers.NotificationManager;
import me.security.notification.NotificationFreeAPI;

/**
 * @author Ekinoxx
 *
 */
public class NotifManagerTest {

	private NotificationManager notif;

	@Before
	public void setUp() throws Exception {
		this.notif = new NotificationManager();
	}

	@After
	public void tearDown() throws Exception {
		this.notif = null;
	}

	@Test
	public void testAdd() throws Exception {
		this.notif.add(NotificationFreeAPI.generateFromFile());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testAddNull() {
		this.notif.add(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testAddAlreadyContains() throws Exception {
		this.notif.add(NotificationFreeAPI.generateFromFile());
		this.notif.add(NotificationFreeAPI.generateFromFile());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testTriggerFreeNull() {
		this.notif.triggerFree(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testTriggerFreeEmpty() {
		this.notif.triggerFree("");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testTriggerIFTTTNoValues() {
		this.notif.triggerIFTTT();
	}

	@Test(expected = IllegalArgumentException.class)
	public void testTriggerIFTTT4Values() {
		this.notif.triggerIFTTT("", "", "", "");
	}

	@Test
	public void testToStringEmpty() {
		assertEquals("Currently active notifications system:\n", this.notif.toString());
	}

	@Test
	public void testToStringWithFree() throws Exception {
		this.notif.add(NotificationFreeAPI.generateFromFile());
		assertEquals(
				"Currently active notifications system:\n" + " - me.security.notification.NotificationFreeAPI\n" + "",
				this.notif.toString());
	}

}
