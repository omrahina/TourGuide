package tourGuide.service;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import tourGuide.data.InternalDataHelper;
import tourGuide.user.User;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class InternalUserServiceTest {

    @InjectMocks
    private InternalUserService internalUserService;

    @BeforeClass
    public static void setUp(){
        Locale.setDefault(new Locale("en", "US"));
    }

    @Before
    public void setUpEachTest(){
        InternalDataHelper.initializeInternalUsers();
    }

    @Test
    public void addUser_create_ok() {
        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        User user2 = new User(UUID.randomUUID(), "jon2", "000", "jon2@tourGuide.com");

        internalUserService.addUser(user);
        internalUserService.addUser(user2);

        assertEquals(user, InternalDataHelper.getInternalUserMap().get(user.getUserName()));
        assertEquals(user2, InternalDataHelper.getInternalUserMap().get(user2.getUserName()));
        assertEquals(InternalDataHelper.getInternalUserNumber() + 2, InternalDataHelper.getInternalUserMap().size());
    }

    @Test
    public void addUser_already_exists_ok() {
        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");

        Assert.assertNull(InternalDataHelper.getInternalUserMap().get(user.getUserName()));
        assertEquals(InternalDataHelper.getInternalUserNumber() , InternalDataHelper.getInternalUserMap().size());

        internalUserService.addUser(user);

        assertEquals(user, InternalDataHelper.getInternalUserMap().get(user.getUserName()));
        assertEquals(InternalDataHelper.getInternalUserNumber() + 1, InternalDataHelper.getInternalUserMap().size());

        internalUserService.addUser(user);

        assertEquals(InternalDataHelper.getInternalUserNumber() + 1, InternalDataHelper.getInternalUserMap().size());
    }

    @Test
    public void getAllUsers() {
        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        User user2 = new User(UUID.randomUUID(), "jon2", "000", "jon2@tourGuide.com");

        internalUserService.addUser(user);
        internalUserService.addUser(user2);

        List<User> allUsers = internalUserService.getAllUsers();

        assertTrue(allUsers.contains(user));
        assertTrue(allUsers.contains(user2));
    }

}
