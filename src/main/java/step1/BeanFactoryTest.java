package step1;

import bean.A;
import org.junit.Test;

import java.io.InputStream;

import static org.junit.Assert.*;

public class BeanFactoryTest {

    @Test
    public void registerBean() {
        InputStream inputStream = ClassLoader.getSystemResourceAsStream("step1.xml");
        XmlBeanFactory beanFactory = new XmlBeanFactory(inputStream);
        A a = beanFactory.getBean("A", A.class);
        assertEquals(a.name, "A");
        a.name = "AA";

        A reloadedA = beanFactory.getBean("A", A.class);
        assertEquals(reloadedA.name, "AA");
    }
}