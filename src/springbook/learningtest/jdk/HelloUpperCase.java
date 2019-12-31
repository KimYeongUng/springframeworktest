package springbook.learningtest.jdk;

public class HelloUpperCase implements Hello {
    // target Object
    Hello hello;

    public HelloUpperCase(Hello hello) {
        this.hello = hello;
    }

    @Override
    public String SayHello(String name) {
        return hello.SayHello(name).toUpperCase();
    }

    @Override
    public String SayHi(String name) {
        return hello.SayHi(name).toUpperCase();
    }

    @Override
    public String SayThankYou(String name) {
        return hello.SayThankYou(name).toUpperCase();
    }
}
