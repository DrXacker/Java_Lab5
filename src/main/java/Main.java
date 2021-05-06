import injector.Injector;
import other.SomeBean;

public class Main {
    public static void main(String[] args) {
        Injector injector = new Injector();

        SomeBean someBean = new SomeBean();
        injector.inject(someBean);

        someBean.foo();
    }
}