package springbook.user.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.PlatformTransactionManager;

import springbook.user.dao.UserDao;
import springbook.user.dao.UserDaoJdbc;
import springbook.user.domain.Level;
import springbook.user.domain.User;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/springbook/applicationContext.xml")
public class UserServiceTest {

    @Autowired UserService userService;
    @Autowired UserServiceImpl userServiceImpl;
    @Autowired ApplicationContext context;
    @Autowired DataSource dataSource;
    @Autowired PlatformTransactionManager transactionManager;
    @Autowired MailSender mailSender;
    //@Autowired UserService testUserService;

    private UserDao dao;

    List<User> users;

    @Before
    public void Setup(){
        this.dao = context.getBean("userDao",UserDaoJdbc.class);
        users = Arrays.asList(
                new User("hero","kimhero","p1", Level.BASIC, UserServiceImpl.MIN_LOGIN_FOR_SILVER-1,0,"email1"),
                new User("a","namea","p1", Level.BASIC, UserServiceImpl.MIN_LOGIN_FOR_SILVER,0,"email2"),
                new User("b","nameb","p1", Level.SILVER,60, UserServiceImpl.MIN_RECOMMEND_FOR_GOLD-1,"email3"),
                new User("c","namec","p1", Level.SILVER,60, UserServiceImpl.MIN_RECOMMEND_FOR_GOLD,"email4"),
                new User("d","named","p1", Level.GOLD,100,Integer.MAX_VALUE,"email5")
        );
    }

    @Test
    public void upgradeLevels(){
        UserServiceImpl userServiceImpl = new UserServiceImpl();

        MockUserDao mockUserDao = new MockUserDao(this.users);
        userServiceImpl.setUserDao(mockUserDao);

        MockMailSender mockMailSender = new MockMailSender();
        userServiceImpl.setMailSender(mockMailSender);

        userServiceImpl.upgradeLevels();

        List<User> updated = mockUserDao.getUpdated();
        assertThat(updated.size(),is(2));
        checkUserAndLevel(updated.get(0),"a",Level.SILVER);
        checkUserAndLevel(updated.get(1),"c",Level.GOLD);

        List<String> request = mockMailSender.getRequests();
        assertThat(request.size(),is(2));
        assertThat(request.get(0),is(users.get(1).getEmail()));
        assertThat(request.get(1),is(users.get(3).getEmail()));
    }

    @Test
    public void upgradeLevelsDao(){
        dao.deleteAll();
        for(User user:users)
            dao.add(user);


        MockMailSender mockMailSender = new MockMailSender();
        userServiceImpl.setMailSender(mockMailSender);

        userService.upgradeLevels();

        checkLevelUpgraded(users.get(0),false);
        checkLevelUpgraded(users.get(1),true);
        checkLevelUpgraded(users.get(2),false);
        checkLevelUpgraded(users.get(3),true);
        checkLevelUpgraded(users.get(4),false);

        List<String> request = mockMailSender.getRequests();
        assertThat(request.size(),is(2));
        assertThat(request.get(0),is(users.get(1).getEmail()));
        assertThat(request.get(1),is(users.get(3).getEmail()));
    }

    @Test
    public void add(){
        dao.deleteAll();

        User userwithlevel = users.get(4);
        User userwithoutlevel = users.get(0);
        userwithoutlevel.setLevel(null);

        userService.add(userwithlevel);
        userService.add(userwithoutlevel);

        User withlevelread = dao.get(userwithlevel.getId());
        User withoutlevelread = dao.get(userwithoutlevel.getId());

        assertThat(withlevelread.getLevel(),is(userwithlevel.getLevel()));
        assertThat(withoutlevelread.getLevel(),is(Level.BASIC));
    }

    @Test
    public void upgradeLevel() throws Exception {
        dao.deleteAll();
        for(User user:users) dao.add(user);

        userService.upgradeLevels();

        checkLevelUpgraded(users.get(0),false);
        checkLevelUpgraded(users.get(1),true);
        checkLevelUpgraded(users.get(2),false);
        checkLevelUpgraded(users.get(3),true);
        checkLevelUpgraded(users.get(4),false);
    }

    @Test
    @DirtiesContext
    public void upgradeAllOrNothing()throws Exception{ // p.458
        TestUserService testUserService = new TestUserService(users.get(3).getId());
        testUserService.setUserDao(dao);
        testUserService.setMailSender(mailSender);

        TxProxyFactoryBean txProxyFactoryBean = context.getBean("&userService",TxProxyFactoryBean.class);
        txProxyFactoryBean.setTarget(testUserService);
        UserService txUserService = (UserService)txProxyFactoryBean.getObject();

        dao.deleteAll();

        for(User user:users)
            dao.add(user);

        try{
            txUserService.upgradeLevels();
            fail("TestUserService expected");
        }catch (TestUserServiceException e){

        }

        checkLevelUpgraded(users.get(1),false);

    }

    private void checkLevelUpgraded(User user, boolean upgraded){
        User userUpgrade = dao.get(user.getId());
        if(upgraded){
            assertThat(userUpgrade.getLevel(),is(user.getLevel().nextLevel()));
        }else{
            assertThat(userUpgrade.getLevel(),is(user.getLevel()));
        }

    }

    private void checkLevel(User user,Level expectedLv){
        User userUpdate = dao.get(user.getId());
        assertThat(userUpdate.getLevel(),is(expectedLv));
    }

    private void checkUserAndLevel(User updated,String expectId,Level expectedLv){
        assertThat(updated.getId(),is(expectId));
        assertThat(updated.getLevel(),is(expectedLv));
    }

    static class TestUserService extends UserServiceImpl {
        private String id;

        public TestUserService(String id) {
            this.id = id;
        }

        public void upgradeLevel(User user){
            if(user.getId().equals(this.id))
                throw new TestUserServiceException();

            super.upgradeLevel(user);
        }
    }

    static class TestUserServiceException extends RuntimeException{

    }

    static class MockMailSender implements MailSender{
        private List<String> requests = new ArrayList<>();

        public List<String> getRequests(){
            return requests;
        }

        @Override
        public void send(SimpleMailMessage simpleMessage) throws MailException {
            requests.add(simpleMessage.getTo()[0]);
        }

        @Override
        public void send(SimpleMailMessage[] simpleMessages) throws MailException {

        }
    }

    static class MockUserDao implements UserDao{
        private List<User> users;
        private List<User> updated = new ArrayList();

        private MockUserDao(List<User> users){
            this.users = users;
        }

        public List<User> getUpdated(){
            return this.updated;
        }

        public List<User> getAll(){
            return this.users;
        }

        public void update(User user) {
            updated.add(user);
        }

        public void add(User user){ throw new UnsupportedOperationException();}
        public void deleteAll(){throw new UnsupportedOperationException();}
        public User get(String id){throw new UnsupportedOperationException();}
        public int getCount(){throw new UnsupportedOperationException();}

    }
}
