package step5;

import java.util.*;

public class DefaultListableBeanFactory extends AbstractBeanFactory implements ListableBeanFactory {

    private Map<String, BeanDefinition> beanDefinitionHash = new HashMap();

    @Override
    public String[] getBeanDefinitionNames() {
        Set keys = beanDefinitionHash.keySet();
        String[] names = new String[keys.size()];
        Iterator itr = keys.iterator();
        int i = 0;
        while (itr.hasNext()) {
            names[i++] = (String) itr.next();
        }
        return names;
    }

    @Override
    public int getBeanDefinitionCount() {
        return beanDefinitionHash.size();
    }

    protected void preInstantiate() {
        String[] beanNames = getBeanDefinitionNames();
        for (int i = 0; i < beanNames.length; i++) {
            getBean(beanNames[i]);
        }
    }

    public DefaultListableBeanFactory(BeanFactory parentBeanFactory) {
        super(parentBeanFactory);
    }

    public void registerBeanDefinition(String id, BeanDefinition beanDefinition) {
        beanDefinitionHash.put(id, beanDefinition);
    }

    public BeanDefinition getBeanDefinition(String key) {
        return beanDefinitionHash.get(key);
    }
}
