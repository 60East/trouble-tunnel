package com.crankuptheamps.ttunnel;

import com.crankuptheamps.ttunnel.filters.Filter;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;

public class FilterFactory {

    private final Properties[] configs;
    private final String DEFAULT_PACKAGE= "com.crankuptheamps.ttunnel.filters.";

    public FilterFactory(Properties[] configs) {
        this.configs = configs;
    }

    public Filter[] getInstances(final ConnectionProcessor connectionProcessor) {
        final Filter[] instances = new Filter[configs.length];
        for (int i = 0 ; i < configs.length ; ++i) {
            instances[i] = createInstance(configs[i].getProperty("type"), configs[i], connectionProcessor);
        }
        return instances;
    }

     Filter createInstance(final String type, final Properties config, final ConnectionProcessor connectionProcessor) {
         Class cl = classFor(type);
         Constructor constructor;
         try {
             constructor = cl.getConstructor(ConnectionProcessor.class, Properties.class);
             return (Filter) constructor.newInstance(connectionProcessor,config);
         } catch (InvocationTargetException e) {
            throw new Error("Filter constructor  for " + cl.getName() + " threw an exception: " + e.getTargetException().getMessage());
         } catch (Exception e) {
             throw new Error("trouble creating an instance of'" + cl.getName() + "'. verify that it implements: public <init>(ConnectionProcessor, java.util.Properties): " + e.getMessage());
         }
     }

    private Class classFor(final String name) {
        if (name == null) throw new NullPointerException("missing required 'type' filter config parameter");
        Class cl = null;
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException e) {
        }
        try {
            return Class.forName(DEFAULT_PACKAGE + name + "Filter");
        } catch (ClassNotFoundException e) {
        }
        if (cl == null) {
            throw new Error("failed to load class for Filter type '" + name + "'");
        }
        return cl;
    }

}
