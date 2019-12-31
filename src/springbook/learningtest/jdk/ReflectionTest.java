package springbook.learningtest.jdk;

import org.junit.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.is;

public class ReflectionTest {
    @Test
    public void invokeMethod() throws Exception{
        String name = "Spring";
        assertThat(name.length(),is(6)); // length()

        Method lengthMethod = String.class.getMethod("length");
        assertThat((Integer)lengthMethod.invoke(name),is(6));

        assertThat(name.charAt(0),is('S')); // charAt()

        Method charAtMethod = String.class.getMethod("charAt", int.class);
        assertThat((Character)charAtMethod.invoke(name,0),is('S'));
    }

    @Test
    public void simpleProxy() {
        Hello hello = new HelloTarget(); // target access
        assertThat(hello.SayHello("Hero"),is("Hello Hero"));
        assertThat(hello.SayHi("Hero"),is("Hi Hero"));
        assertThat(hello.SayThankYou("Hero"),is("Thank You Hero"));
    }

    @Test
    public void HelloUpper(){
        Hello proxiedHello = (Hello) Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class[] {Hello.class},
                new UppercaseHandler(new HelloTarget()));
        assertThat(proxiedHello.SayHello("Hero"),is("HELLO HERO"));
        assertThat(proxiedHello.SayHi("Hero"),is("HI HERO"));
        assertThat(proxiedHello.SayThankYou("Hero"),is("THANK YOU HERO"));
    }
}
