package com.xgs.idempotent.utils;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.util.ClassUtils;
import org.springframework.util.SystemPropertyUtils;


/**
 * @description: 注解扫描器
 * @author: xiongguoshuang
 * @date: 2020/04/26
 * https://blog.csdn.net/weixin_33739541/article/details/89745084
 */
public class IdempotentAnnotationScanner {

    /**
     * include 过滤器列表
     */
    private final List<TypeFilter> includeFilters = new LinkedList<>();
    /**
     * exclude 过滤器列表
     */
    private final List<TypeFilter> excludeFilters = new LinkedList<>();

    private ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
    private MetadataReaderFactory metadataReaderFactory = new CachingMetadataReaderFactory(this.resourcePatternResolver);

    /**
     * 扫描提定包列表下的注解，返回方法加上注解的Class列表（注意，注解是加上class的方法上，不是加在class上）
     *
     * @param basePackages 包名列表
     * @param annotation   注解
     * @return
     */
    public Set<Class> scan(String[] basePackages, Class<? extends Annotation> annotation) {
        this.addIncludeFilter(new MyAnnotationTypeFilter(annotation));
        Set<Class> classes = new HashSet<>();
        for (String basePackage : basePackages) {
            classes.addAll(this.doScan(basePackage));
        }
        return classes;
    }

    public void addIncludeFilter(TypeFilter includeFilter) {
        this.includeFilters.add(includeFilter);
    }

    public void addExcludeFilter(TypeFilter excludeFilter) {
        this.excludeFilters.add(0, excludeFilter);
    }


    private Set<Class> doScan(String basePackage) {
        Set<Class> classes = new HashSet<>();
        try {
            String packageSearchPath = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX
                    + ClassUtils.convertClassNameToResourcePath(SystemPropertyUtils.resolvePlaceholders(basePackage))
                    + "/**/*.class";
            Resource[] resources = this.resourcePatternResolver.getResources(packageSearchPath);

            if (resources != null) {
                for (Resource resource : resources) {
                    if (resource.isReadable()) {
                        MetadataReader metadataReader = this.metadataReaderFactory.getMetadataReader(resource);
                        if ((includeFilters.isEmpty() && excludeFilters.isEmpty())
                                || this.matches(metadataReader)) {
                            try {
                                classes.add(Class.forName(metadataReader.getClassMetadata().getClassName()));
                            } catch (ClassNotFoundException e) {
                                // just ignore ok
                            }
                        }
                    }
                }
            }
        } catch (IOException ex) {
            throw new BeanDefinitionStoreException("I/O failure during classpath scanning", ex);
        }
        return classes;
    }

    protected boolean matches(MetadataReader metadataReader) throws IOException {
        for (TypeFilter tf : this.excludeFilters) {
            if (tf.match(metadataReader, this.metadataReaderFactory)) {
                return false;
            }
        }
        for (TypeFilter tf : this.includeFilters) {
            if (tf.match(metadataReader, this.metadataReaderFactory)) {
                return true;
            }
        }
        return false;
    }


    public static class MyAnnotationTypeFilter implements TypeFilter {
        private Class<? extends Annotation> annotation;

        public MyAnnotationTypeFilter(Class<? extends Annotation> annotation) {
            this.annotation = annotation;
        }

        @Override
        public boolean match(MetadataReader metadataReader, MetadataReaderFactory metadataReaderFactory) throws IOException {
            try {
                Class clazz = Class.forName(metadataReader.getClassMetadata().getClassName());
                for (Method method : clazz.getDeclaredMethods()) {
                    Set<?> annotationSet = AnnotatedElementUtils.getMergedRepeatableAnnotations(method, annotation);
                    if (annotationSet != null && !annotationSet.isEmpty()) {
                        return true;
                    }
                }
            } catch (ClassNotFoundException e) {
                //just ignore
            }
            return false;
        }
    }
}
