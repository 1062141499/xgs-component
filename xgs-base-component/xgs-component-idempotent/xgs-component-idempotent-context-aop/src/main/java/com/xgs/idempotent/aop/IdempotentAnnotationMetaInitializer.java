package com.xgs.idempotent.aop;


import com.xgs.idempotent.aop.annotation.Idempotent;
import com.xgs.idempotent.components.IdempotentRecordStore;
import com.xgs.idempotent.components.tryAgainRuleImpl.OnlyOnNotBuzExTryAgainRuleImpl;
import com.xgs.idempotent.utils.IdempotentAnnotationScanner;
import com.xgs.idempotent.components.IdempotentHitPolicyEnum;
import com.xgs.idempotent.components.IdempotentKeyGetter;
import com.xgs.idempotent.components.TryAgainRule;
import com.xgs.idempotent.config.IdempotentProperties;
import com.xgs.idempotent.constants.IdempotentErrorCodeEnum;
import com.xgs.idempotent.exception.IdempotentException;
import com.xgs.idempotent.inner.utils.DefaultConfigUtils;
import com.xgs.idempotent.inner.utils.GlobalConfigUtils;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.util.Assert;

/**
 * 幂等服务元数据初始化器
 * @author xiongguoshuang
 */
@Slf4j
public class IdempotentAnnotationMetaInitializer implements InitializingBean, ApplicationContextAware {

    private final String[] basePackages;

    private final IdempotentProperties idempotentProperties;

    private final IdempotentRecordStore idempotentRecordStore;

    public IdempotentAnnotationMetaInitializer(String[] basePackages,
                                               IdempotentProperties idempotentProperties,
                                               IdempotentRecordStore idempotentRecordStore){
        this.basePackages = basePackages;
        this.idempotentProperties = idempotentProperties;
        this.idempotentRecordStore = idempotentRecordStore;
    }


    private Map<String /*impl*/, Idempotent> idempotentMetaImplMap = null;

    private List<Idempotent> idempotentMetaList = null;

    public Idempotent getIdempotentMetaByImplMethodName(String key){
        return idempotentMetaImplMap.get(key);
    }

    private void initialIdempotentMetaList(){
        this.idempotentMetaImplMap = new ConcurrentHashMap<>();
        this.idempotentMetaList = new ArrayList<>();

        Set<Class> classSet = new IdempotentAnnotationScanner().scan(basePackages, Idempotent.class);

        for(Class clazz: classSet){

            for(Method method: clazz.getDeclaredMethods()){
                Set<Idempotent> idempotentSet = AnnotatedElementUtils.getMergedRepeatableAnnotations(method, Idempotent.class);
                if(idempotentSet != null && !idempotentSet.isEmpty()){
                    for(Idempotent idempotent : idempotentSet){
                        String key = clazz.getName() + "." + method.getName();
                        this.idempotentMetaImplMap.put(key, idempotent);
                        this.idempotentMetaList.add(idempotent);
                    }
                }
            }
        }

    }


    public GlobalConfigUtils initialGlobalLevelConfig(){

        GlobalConfigUtils globalConfigUtils = GlobalConfigUtils.getInstance();
        if(StringUtils.isNoneBlank(idempotentProperties.getGlobalRetryRuleClass())
                || StringUtils.isNoneBlank(idempotentProperties.getGlobalRetryRuleBeanName())){
            TryAgainRule tryAgainRule = this.getInstanceByBeanNameOrReflect(
                    idempotentProperties.getGlobalRetryRuleClass(),
                    idempotentProperties.getGlobalRetryRuleBeanName(),
                    TryAgainRule.class
            );
            globalConfigUtils.setRetryRule(tryAgainRule);
        }
        if(StringUtils.isNoneBlank(idempotentProperties.getGlobalKeyGetterClass())
                        || StringUtils.isNoneBlank(idempotentProperties.getGlobalKeyGetterBeanName())
        ){
            IdempotentKeyGetter idempotentKeyGetter = this.getInstanceByBeanNameOrReflect(
                    idempotentProperties.getGlobalKeyGetterClass(),
                    idempotentProperties.getGlobalKeyGetterBeanName(),
                    IdempotentKeyGetter.class
            );
            globalConfigUtils.setKeyGetter(idempotentKeyGetter);
        }


        String globalHitPolicy = idempotentProperties.getGlobalHitPolicy();
        IdempotentHitPolicyEnum idempotentHitPolicyEnum = IdempotentHitPolicyEnum.findByCode(globalHitPolicy);
        globalConfigUtils.setHitPolicy(idempotentHitPolicyEnum);

        if(StringUtils.isNoneBlank(idempotentProperties.getGlobalKeyGetterClass())
                || StringUtils.isNoneBlank(idempotentProperties.getGlobalKeyGetterBeanName())
        ){
            IdempotentKeyGetter idempotentKeyGetter = this.getInstanceByBeanNameOrReflect(
                    idempotentProperties.getGlobalKeyGetterClass(),
                    idempotentProperties.getGlobalKeyGetterBeanName(),
                    IdempotentKeyGetter.class
            );
            globalConfigUtils.setKeyGetter(idempotentKeyGetter);
        }


        globalConfigUtils.setRecordTtlSecs(idempotentProperties.getRecordTtlSecs());

        globalConfigUtils.setDistributedLockTtlSecs(idempotentProperties.getDistributedLockTtlSecs());


        globalConfigUtils.setAppName(idempotentProperties.getAppName());
        globalConfigUtils.setLimitMsg(idempotentProperties.getErrorTipMsg());

        globalConfigUtils.setTryDistributedLockTimeOutMillis(idempotentProperties.getTryDistributedLockTimeOutMillis());

        globalConfigUtils.setIdempotentSwitch(idempotentProperties.isIdempotentSwitch());

        return globalConfigUtils;
    }


    public DefaultConfigUtils inDefaultConfigUtils(){
        DefaultConfigUtils defaultConfigUtils = DefaultConfigUtils.getInstance();

        defaultConfigUtils.setRetryRule(new OnlyOnNotBuzExTryAgainRuleImpl());
        /**
         * 幂等记录ttl时间(秒)
         * 1 天
         */
        defaultConfigUtils.setRecordTtlSecs(24*60*60L);

        /**
         * 分布式锁ttl(秒)
         * 1分钟
         */
        defaultConfigUtils.setDistributedLockTtlSecs(60L);


        /**
         * 尝试获取超时时间（毫秒）
         * 3秒
         */
        defaultConfigUtils.setTryDistributedLockTimeOutMillis(3000L);

        /**
         *  幂等命中策略
         * @return
         */
        defaultConfigUtils.setHitPolicy(IdempotentHitPolicyEnum.ReturnLastResultAndWrapperExceptionPolicy);

        return defaultConfigUtils;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        initialIdempotentMetaList();
        inDefaultConfigUtils();
        initialGlobalLevelConfig();
        //校验所有
        checkAllConfig();
    }


    /**
     *
     * @param implClazz        实现类
     * @param beanName     bean的命称
     * @param supperClazz  父类或者接口
     * @param <T>
     * @return
     */
    public  <T> T getInstanceByBeanNameOrReflect(Class<? extends T> implClazz, String beanName, Class<T> supperClazz) {
        Assert.notNull(supperClazz,"supperClazz 不可能为null");

        if(StringUtils.isNoneBlank(beanName)){
            try{
                T bean = null;
                if(implClazz != null){
                    bean = applicationContext.getBean(beanName, implClazz);
                }else{
                    bean = applicationContext.getBean(beanName, supperClazz);
                }
                return bean;
            }catch (Throwable throwable){
                //
                throw IdempotentException.wrapThrowableWithArguments(
                        IdempotentErrorCodeEnum.WRAP_OTHER_UNKNOWN_ERROR.getCode(),
                        "通过获取bean的方式初始化失败" ,
                        throwable,
                        implClazz, beanName, supperClazz
                );
            }
        }else if(implClazz != null){
            //implClazz不为空，则通过反射初始化
            if(implClazz.isInterface() || Modifier.isAbstract(implClazz.getModifiers())){
                throw new RuntimeException("implClazz " + implClazz.getName() + " 必需是一个具体的实现类，不能是接口或抽象类");
            }
            try {
                T newInstance = implClazz.newInstance();
                return newInstance;
            } catch (Throwable throwable) {
                //通过反射初始化失败
                throw IdempotentException.wrapThrowableWithArguments(
                        IdempotentErrorCodeEnum.WRAP_OTHER_UNKNOWN_ERROR.getCode(),
                        "通过反射初始化失败" ,
                        throwable,
                        implClazz, beanName, supperClazz
                );
            }
        }
        return null;
    }


    /**
     *
     * @param implClazzName        实现类
     * @param beanName     bean的命称
     * @param supperClazz  父类或者接口
     * @param <T>
     * @return
     */
    public  <T> T getInstanceByBeanNameOrReflect(String implClazzName, String beanName, Class<T> supperClazz) {
        Assert.notNull(supperClazz,"supperClazz 不可能为null");
        Class<? extends T> objectClass = this.reflectClassByClassName(implClazzName, supperClazz);
        return getInstanceByBeanNameOrReflect(objectClass,beanName,supperClazz);
    }



    /**
     * 从给定的全路径类名中通过反射方式获取实例
     * @param implClazzName
     * @param <T>
     * @return
     */
    private <T> Class<? extends T> reflectClassByClassName(String implClazzName, Class<T> supperClazz){

        Class<T> ignoreHandlerClass = null;
        try {
            ignoreHandlerClass = (Class<T>)Class.forName(implClazzName);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return ignoreHandlerClass;
    }

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public IdempotentRecordStore getIdempotentRecordStore(){
        return this.idempotentRecordStore;
    }

    /**
     * 校验所有配置
     *
     * 1、如果存在接口级配置，则接口级配置生效
     * 2、否则，如果存在全局级配置，则全局级配置生效
     * 3、否则，如果存在默认配置，则默认配置生效
     * 4、否则，如果不是必配项，则结束
     * 5、否则，抛异常
     */
    private void checkAllConfig(){

        //1、校验是否存在record
        if(this.idempotentRecordStore == null){
            throw IdempotentException.error(IdempotentErrorCodeEnum.INITIAL_CHECK_FAIL.getCode(),"idempotentRecordStore 不能为空");
        }
        //2、校验幂等应用名
        if(StringUtils.isBlank(GlobalConfigUtils.getInstance().getAppName())){
            throw IdempotentException.error(IdempotentErrorCodeEnum.INITIAL_CHECK_FAIL.getCode(),"appName 不能为空");
        }

    }
}
