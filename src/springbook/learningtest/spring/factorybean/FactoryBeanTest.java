package springbook.learningtest.spring.factorybean;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/springbook/applicationContext.xml")
public class FactoryBeanTest {
    @Autowired
    ApplicationContext context;

    @Test
    public void getMessageFromFactoryBean(){
        Object message = context.getBean("message");
        assertEquals(message,Message.class);
        assertEquals(((Message)message).getText(),"Factory Bean");
    }

    @Test
    public void getFactoryBean() throws Exception{
        Object factory = context.getBean("&message");
        assertEquals(factory,MessageFactoryBean.class);
    }
}
