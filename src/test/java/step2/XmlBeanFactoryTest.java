package step2;

import bean.B;
import org.junit.Test;

import java.io.InputStream;

import static org.junit.Assert.*;

public class XmlBeanFactoryTest {
    @Test
    public void registerBeanWithProperty() {
        InputStream inputStream = ClassLoader.getSystemResourceAsStream("step2.xml");
        step1.XmlBeanFactory beanFactory = new step1.XmlBeanFactory(inputStream);
        B b = beanFactory.getBean("B", B.class);
        assertEquals(b.name, "Yoonsung");
        b.name = "BB";

        B reloadedB = beanFactory.getBean("B", B.class);
        assertEquals(reloadedB.name, "BB");
    }
}