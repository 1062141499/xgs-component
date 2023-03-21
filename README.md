# xgs-component XGS基础组件之幂等性组件
自主研发的幂等组件
（一）为什么要把幂等抽象成组件
    1、幂等处理逻辑与业务处理逻辑分离，职责划分清晰
    2、幂等逻辑抽象成组件，复用方便
    3、可以更灵活地与现有设施搭配使用
（一）处理流程


（二）幂等组件关键涉及的关键要素的说明


（三）幂等配置层级
    1、接口级配置
    2、全局配置
    3、默认配置
    
    


  配置生效优先级，服务启动的时候校验：
    1、如果存在接口级配置，则接口级配置生效
    2、否则，如果存在全局级配置，则全局级配置生效
    3、否则，如果存在默认配置，则默认配置生效
    4、否则，如果不是必配项，则结束
    5、否则，抛异常

（四）工程类之间的关系图


（五）幂等代码工程结构

      xgs-component-idempotent    幂等组件项级工程
          xgs-component-idempotent-core     提供核心抽象组件
              IdempotentRecord         --  幂等记录
              IdempotentRecordStore    --  幂等记录存取
              IdempotentRequestContext --  请求上下文
              TryAgainRule             --  再试规则
              IdempotentKeyGetter      --  幂等key获取策略
              IdempotentRequestProcessorTemplate   --- 幂等请求模板方法类
          xgs-component-idempotent-context-aop
              提供IdempotentRequestContext的具体实现 IdempotentAspectContext
              提供IdempotentRequestProcessorTemplate的具体实现 IdempotentAopRequestProcessor
              提供@Idempotent 注解
          xgs-component-idempotent-context-rocketmq（规划中）
          xgs-component-idempotent-context-xxljob（规划中）
          xgs-component-idempotent-context-pulsar（规划中）
          xgs-component-idempotent-store-jdbc
              提供IdempotentRecordStore的实现JdbcIdempotentRecordStore
              提供IdempotentRecord子类JdbcIdempotentRecord
          xgs-component-idempotent-store-redis
              提供IdempotentRecordStore的实现RedisIdempotentRecordStore
              提供IdempotentRecord子类RedisIdempotentRecord

（六）使用示例

    6.1、参照bop-demo的xgs-idempotent-demo模块
    
    6.2、引入maven依赖
        <dependency>
                    <groupId>com.xgs</groupId>
                    <artifactId>xgs-component-idempotent-context-aop</artifactId>
                </dependency>
                <dependency>
                    <groupId>com.xgs</groupId>
                    <artifactId>xgs-component-idempotent-store-redis</artifactId>
                </dependency>
6.3、添加配置
    idempotent:
      common:
        app-name: ${spring.application.name}
        error-tip-msg: '幂等处理异常'
        distributed-lock-ttl-secs: 3000
      redis:
        redisson:
          single-server-config:
            address: 'redis://192.168.4.110:6379'
            
6.4、bean方法使用@Idempotent注解
    @RequestMapping("/test1")
        @Idempotent(keyGetterClass= DemoKeyGetter.class)
        public String test1(@RequestParam("idemkey") String idemkey, @RequestParam("idemkey222") String idemkey222){
            return idemkey;
        }
        
6.5、提供IdempotentKeyGetter实现（也可以直接使用预提供的，请见附1）
    /**
     * 总是取第一个参数作为幂等key
     */
    public class DemoKeyGetter implements IdempotentKeyGetter {
        @Override
        public String parseRecordKey(IdempotentRequestContext requestContext) {
            Object[] params = requestContext.getParams();
            if(params == null){
                return null;
            }
            for (Object param : params) {
                if(param!=null){
                    return param.toString();
                }
            }
            return null;
        }
    }
    
（七）附录

    附1、预提供的IdempotentKeyGetter
    
    IdKeyGetter
        获取ApiRequest.data 的 id的属性作为【幂等记录key】（幂等记录唯一标识）
    JsonPathKeyGetter
        JsonPath 表达式指定ApiRequest.data的，JsonPath 表达式 $.store.book[0].author
    OrderIdKeyGetter
        获取apiRequest.getData 的 orderId
    SeqIdKeyGetter
        获取apiRequest.getData 的seqId
    Sha1KeyGetter
        将ApiRequest的Data整体做sha1
        
    附2、@Idempotent的属性说明
    
    moduleName
        幂等模块名，在同一个【幂等应用名】下唯一，可以不填，不填则从接口名作为模块名
    keyGetterClass
        幂等记录key获取策略，所配置的class必需有无参构造方法。否则考虑用keyGetterBeanName
    keyGetterBeanName
        幂等记录key获取策略实例bean名称
    retryRuleClass
        再试规则，所配置的class必需有无参构造方法。否则考虑用retryRuleBeanName
    retryRuleBeanName
        再试规则bean命称
    hitPolicy
        命中处理策略
        默认 IdempotentHitPolicyEnum.ReturnLastResultAndWrapperExceptionPolicy
    recordTtlSecs
        幂等记录ttl
        
    附3、命中处理策略
   
    IdempotentHitPolicyEnum.ReturnLastResultPolicy
        返回上次结果，如果上次执行异常则将异常尽可能原样抛出。
    IdempotentHitPolicyEnum.ReturnLastResultAndWrapperExceptionPolicy
        返回上次结果，如果上次执行异常，则先包装一层幂等的异常后再抛出。
    IdempotentHitPolicyEnum.ThrowIdemException
        直接抛出幂等组件异常
        
    附4、预提供的再试规则
    
    AlwaysTryAgainRuleImpl
      总是再试
    NeverTryAgainRuleImpl
      总是不再试
    OnlyOnErrorTryAgainRuleImpl
      仅在前一次请求出异常的情况下再试
    TryUntilMaxCount
      达到最大次后不再重试
    TryUntilMaxFailCount
      达到最大失败次后不再重试
      
八 遇到的问题
    1、序列化与反序化：redisson的jackson反序列化不支持没有无参构造方法的类的问题
    2、序列化与反序化：redis本身不支持long类型题
