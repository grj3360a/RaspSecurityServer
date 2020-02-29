import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import notification.FreeAPITest;
import notification.IFTTTTest;

@RunWith(Suite.class)
@SuiteClasses({ FreeAPITest.class, IFTTTTest.class })
public class NotificationTests {
}
