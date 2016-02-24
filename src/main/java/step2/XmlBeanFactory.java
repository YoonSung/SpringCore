package step2;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

public class XmlBeanFactory {

    private static final String BEAN_ELEMENT = "bean";
    private static final String CLASS_ATTRIBUTE = "class";
    private static final String ID_ATTRIBUTE = "id";
    private static final String PROPERTY_ELEMENT = "property";
    private static final String NAME_ATTRIBUTE = "name";

    /**
     * Map of Bean objects, keyed by id attribute
     */
    private Map<String, BeanDefinition> beanDefinitionHash = new HashMap();
    private Map beanHash = new HashMap();

    public <T> T getBean(String key, Class<T> claz) {
        return (T) getBean(key);
    }
    public Object getBean(String key) {
        return getBeanInternal(key);
    }

    private Object getBeanInternal(String key) {
        if (key == null)
            throw new IllegalArgumentException("Bean name null is not allowed");

        if (beanHash.containsKey(key)) {
            return beanHash.get(key);
        }

        Object newlyCreatedBean = createBean(key);
        beanHash.put(key, newlyCreatedBean);
        return newlyCreatedBean;
    }

    private Object createBean(String key) {
        try {
            BeanDefinition beanDefinition = getBeanDefinition(key);
            return beanDefinition.getBeanClass().newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Cannot instantiate [bean name : " + key+ "]; is it an interface or an abstract class?");
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Cannot instantiate [bean name : " + key + "]; has class definition changed? Is there a public constructor?", ex);
        }
    }

    private void applyPropertyValues(Object newlyCreatedBean, PropertyValues propertyValues, String beanName) {

    }

    private BeanDefinition getBeanDefinition(String key) {
        return beanDefinitionHash.get(key);
    }

    public XmlBeanFactory(String fileName) {
        try {
            loadBeanDefinitions(fileName);
        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException(String.format("Cant not open file : %s", fileName));
        }
    }

    public XmlBeanFactory(InputStream inputStream) {
        loadBeanDefinitions(inputStream);
    }

    private void loadBeanDefinitions(String location) throws FileNotFoundException {
        loadBeanDefinitions(new FileInputStream(location));
    }

    private void loadBeanDefinitions(InputStream inputStream) {
        if (inputStream == null)
            throw new IllegalArgumentException("InputStream cannot be null: expected an XML file");

        try (InputStream is = inputStream){
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(is);

            loadBeanDefinitions(doc);
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Parsing XML Exception !");

        } catch (SAXException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Invalid XML Document !");

        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Parsing XML Document Exception !");
        }
    }

    private void loadBeanDefinitions(Document doc) {
        Element root = doc.getDocumentElement();
        NodeList nl = root.getElementsByTagName(BEAN_ELEMENT);

        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            loadBeanDefinition((Element) n);
        }
    }

    private void loadBeanDefinition(Element element) {
        String id = element.getAttribute(ID_ATTRIBUTE);
        if (id == null || "".equals(id))
            throw new IllegalArgumentException("Bean without id attribute");
        String classCanonicalName = element.getAttribute(CLASS_ATTRIBUTE);

        PropertyValues propertyValues = createPropertyValues(element);
        BeanDefinition beanDefinition = createBeanDefinition(element, id, propertyValues);
        registerBeanDefinition(id, beanDefinition);
    }

    private PropertyValue createPropertyValue(Element propElement) {
        String propertyName = propElement.getAttribute(NAME_ATTRIBUTE);
        if (propertyName == null || "".equals(propertyName))
            throw new IllegalArgumentException("Property without a name");

        Object propertyValue = getValue(propElement);

        return new PropertyValue(propertyName, propertyValue);
    }

    private Object getValue(Element propElement) {
        //nested value, 예를들어 List, Map등의 판별을 통해 데이터를 만들어내야 하지만
        //학습을 위한 구현이므로 기본적인 literal value에 대해서만 구현한다.
        return propElement.getNodeValue();
    }

    private PropertyValues createPropertyValues(Element beanElement) {
        PropertyValues propertyValues = new PropertyValues();
        NodeList nl = beanElement.getElementsByTagName(PROPERTY_ELEMENT);
        for (int i = 0 ; i < nl.getLength() ; i++) {
            Element propElement = (Element) nl.item(i);
            PropertyValue propertyValue = createPropertyValue(propElement);
            propertyValues.addPropertyValue(propertyValue);
        }

        return propertyValues;
    }

    private BeanDefinition createBeanDefinition(Element element, String id, PropertyValues propertyValues) {
        if (!element.hasAttribute(CLASS_ATTRIBUTE))
            throw new IllegalArgumentException("Bean without class attribute");
        String classname = element.getAttribute(CLASS_ATTRIBUTE);
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try {
            return new BeanDefinition(Class.forName(classname, true, classLoader), propertyValues);
        } catch (ClassNotFoundException e) {
            throw new UnsupportedOperationException("Error creating bean with name [" + id + "]: class '" + classname + "' not found", e);
        }
    }

    private void registerBeanDefinition(String id, BeanDefinition beanDefinition) {
        beanDefinitionHash.put(id, beanDefinition);
    }
}
