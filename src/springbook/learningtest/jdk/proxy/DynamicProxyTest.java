package springbook.learningtest.jdk.proxy;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.junit.Test;
import org.springframework.aop.ClassFilter;
import org.springframework.aop.Pointcut;
import org.springframework.aop.framework.ProxyFactoryBean;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.aop.support.NameMatchMethodPointcut;
import springbook.learningtest.jdk.Hello;
import springbook.learningtest.jdk.HelloTarget;
import springbook.learningtest.jdk.UppercaseHandler;

import java.lang.reflect.Proxy;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class DynamicProxyTest {
    @Test
    public void simpleProxy(){
        Hello proxiedHello = (Hello) Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class[] {Hello.class},
                new UppercaseHandler(new HelloTarget())
        );
        assertThat(proxiedHello.SayHello("Hero"),is("HELLO HERO"));
        assertThat(proxiedHello.SayHi("Hero"),is("HI HERO"));
        assertThat(proxiedHello.SayThankYou("Hero"),is("THANK YOU HERO"));
    }

    @Test
    public void proxyFactoryBean(){
        ProxyFactoryBean pfBean = new ProxyFactoryBean();
        pfBean.setTarget(new HelloTarget());
        pfBean.addAdvice(new UpperCaseAdviser());

        Hello proxiedHello = (Hello)pfBean.getObject();

        assertThat(proxiedHello.SayHello("Hero"),is("HELLO HERO"));
        assertThat(proxiedHello.SayHi("Hero"),is("HI HERO"));
        assertThat(proxiedHello.SayThankYou("Hero"),is("THANK YOU HERO"));
    }

    @Test
    public void pointCutAdvisor(){
        ProxyFactoryBean pfBean = new ProxyFactoryBean();
        pfBean.setTarget(new HelloTarget());
        // new pointcut : Create pointcuts that provide algorithms to compare method names to select targets
        NameMatchMethodPointcut pointcut = new NameMatchMethodPointcut();
        pointcut.setMappedName("SayH*");

        // Advisor = pointcut(method select algorithm + advise(add-on))
        pfBean.addAdvisor(new DefaultPointcutAdvisor(pointcut,new UpperCaseAdviser()));

        Hello proxiedHello = (Hello)pfBean.getObject();
        assertThat(proxiedHello.SayHello("Hero"),is("HELLO HERO"));
        assertThat(proxiedHello.SayHi("Hero"),is("HI HERO"));
        assertThat(proxiedHello.SayThankYou("Hero"),is("Thank You Hero"));



    }

    @Test
    public void ClassNamePointcutAdvisor(){
        // set-up pointcut
        NameMatchMethodPointcut classMethodPointcut = new NameMatchMethodPointcut(){
            public ClassFilter getClassFilter(){
                return aClass -> aClass.getSimpleName().startsWith("HelloT");
            }
        };
        classMethodPointcut.setMappedName("SayH*");

        // test
        checkAdvised(new HelloTarget(),classMethodPointcut,true); // adapted class

        class HelloWorld extends HelloTarget{}
        checkAdvised(new HelloWorld(),classMethodPointcut,false); // not adapted class

        class HelloTHero extends HelloTarget{}
        checkAdvised(new HelloTHero(),classMethodPointcut,true); // adapted class
    }

    private void checkAdvised(Object target, Pointcut pointcut,boolean adviced){
        ProxyFactoryBean pfBean = new ProxyFactoryBean();
        pfBean.setTarget(target);
        pfBean.addAdvisor(new DefaultPointcutAdvisor(pointcut,new UpperCaseAdviser()));
        Hello proxiedHello = (Hello)pfBean.getObject();

        if(adviced){
            assertThat(proxiedHello.SayHello("Hero"),is("HELLO HERO"));
            assertThat(proxiedHello.SayHi("Hero"),is("HI HERO"));
            assertThat(proxiedHello.SayThankYou("Hero"),is("Thank You Hero"));
        } else {
            assertThat(proxiedHello.SayHello("Hero"),is("Hello Hero"));
            assertThat(proxiedHello.SayHi("Hero"),is("Hi Hero"));
            assertThat(proxiedHello.SayThankYou("Hero"),is("Thank You Hero"));
        }

    }

    static class UpperCaseAdviser implements MethodInterceptor {

        @Override
        public Object invoke(MethodInvocation methodInvocation) throws Throwable {
            String ret = (String)methodInvocation.proceed();
            return ret.toUpperCase();
        }
    }
}
