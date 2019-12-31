package springbook.user.dao;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator;
import org.springframework.jdbc.support.SQLExceptionTranslator;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import springbook.user.domain.Level;
import springbook.user.domain.User;

import org.junit.Test;
import org.junit.runner.JUnitCore;

import javax.sql.DataSource;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;


import java.sql.SQLException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/springbook/applicationContext.xml")
public class UserDaoTest {
    private UserDaoJdbc dao;
    private User user,user1,user2;

    @Autowired
    private ApplicationContext context;

    @Autowired
    DataSource dataSource;

    @Before
    public void setUp(){
        this.dao = this.context.getBean("userDao", UserDaoJdbc.class);
        this.user = new User("kim","hero","920124", Level.GOLD,100,40
                ,"kimyeongung92@gmail.com");
        this.user1 = new User("wisdom","k","1234",Level.BASIC,1,0
                ,"widdom@gmail.com");
        this.user2 = new User("kang","TH","4321",Level.SILVER,55,10,
                "xognstl@naver.com");
        System.out.println(this.context);
        System.out.println(this);
    }

    @Test
    public void addAndGet() throws SQLException {

        dao.deleteAll();
        assertThat(dao.getCount(),is(0));

        dao.add(user);
        assertThat(dao.getCount(),is(1));

        dao.add(user1);
        assertThat(dao.getCount(),is(2));

        dao.add(user2);
        assertThat(dao.getCount(),is(3));

        User userget1 = dao.get(user.getId());
        checkSameUser(user,userget1);

        User userget2 = dao.get(user1.getId());
        checkSameUser(user1,userget2);

    }

    @Test(expected = EmptyResultDataAccessException.class)
    public void getUserFailure()throws SQLException{
        dao.deleteAll();
        assertThat(dao.getCount(),is(0));

        dao.get("unknowun_id"); // exception point

    }

    @Test
    public void getAll()throws SQLException{
        dao.deleteAll();

        List<User> users0 = dao.getAll();
        assertThat(users0.size(),is(0));

        dao.add(user); // kim
        List<User> users = dao.getAll();
        assertThat(users.size(),is(1));
        checkSameUser(user,users.get(0));

        dao.add(user1); // wisdom
        List<User> users1 = dao.getAll();
        assertThat(users1.size(),is(2));
        checkSameUser(user,users1.get(0));
        checkSameUser(user1,users1.get(1));

        dao.add(user2); // kang
        List<User> users2 = dao.getAll();
        assertThat(users2.size(),is(3));
        checkSameUser(user2,users2.get(0));
        checkSameUser(user,users2.get(1));
        checkSameUser(user1,users2.get(2));

    }

    @Test
    public void update(){
        dao.deleteAll();
        dao.add(user);
        dao.add(user1);

        user.setName("Bruce");
        user.setPassword("2222");
        user.setLevel(Level.GOLD);
        user.setLogin(1000);
        user.setRecommend(999);
        dao.update(user);

        User userupdate = dao.get(user.getId());
        checkSameUser(user,userupdate);

        User user1update = dao.get(user1.getId());
        checkSameUser(user1,user1update);
    }

    @Test(expected = DataAccessException.class)
    public void duplicateKey(){
        dao.deleteAll();
        dao.add(user);
        dao.add(user); // exception point
    }

    @Test
    public void sqlExceptionTranslate(){
        dao.deleteAll();

        try {
            dao.add(user);
            dao.add(user);
        }catch (DuplicateKeyException ex){
            SQLException sqlEx = (SQLException)ex.getRootCause();
            SQLExceptionTranslator set =
                    new SQLErrorCodeSQLExceptionTranslator(this.dataSource);

            //assertThat(set.translate(null,null,sqlEx),is(DuplicateKeyException.class));
        }
    }

    private void checkSameUser(User user1,User user2){
        assertThat(user1.getId(),is(user2.getId()));
        assertThat(user1.getName(),is(user2.getName()));
        assertThat(user1.getPassword(),is(user2.getPassword()));
        assertThat(user1.getLevel(),is(user2.getLevel()));
        assertThat(user1.getLogin(),is(user2.getLogin()));
        assertThat(user1.getRecommend(),is(user2.getRecommend()));

    }

    public static void main(String[] args){
        JUnitCore.main("springbook.user.dao.UserDaoTest");
    }
}
