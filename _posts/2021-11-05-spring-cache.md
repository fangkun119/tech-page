---
title: Spring Cache的使用及定制
author: fangkun119
date: 2021-11-08 19:50:00 +0800
categories: [Java,Spring]
tags: [spring, cache]
pin: true
math: true
mermaid: true
image:
  path: /imgs/cover/spring_logo.png
  lqip: data:image/webp;base64,UklGRpoAAABXRUJQVlA4WAoAAAAQAAAADwAABwAAQUxQSDIAAAARL0AmbZurmr57yyIiqE8oiG0bejIYEQTgqiDA9vqnsUSI6H+oAERp2HZ65qP/VIAWAFZQOCBCAAAA8AEAnQEqEAAIAAVAfCWkAALp8sF8rgRgAP7o9FDvMCkMde9PK7euH5M1m6VWoDXf2FkP3BqV0ZYbO6NA/VFIAAAA
  alt: Responsive rendering of Chirpy theme on multiple devices.
---

{: .no_toc }

<details close markdown="block">
  <summary>
    Table of contents
  </summary>
  {: .text-delta }
- TOC
{:toc}
</details>

# 1. 基本概念

Spring Cache的主要目标是通过缓存方法的结果来减少计算成本，提高性能，特别是对于那些计算成本较高的方法。通过使用Spring Cache，你可以轻松地在Spring应用程序中实现缓存，提高系统的响应速度和效率。包含如下基本概念

**缓存**：缓存是一种存储机制，用于将数据保存在某个地方，并以更快的方式提供服务。它可以存储方法的计算结果，以便在后续请求中快速获取，而不必重新计算。

**缓存管理器**：缓存管理器是Spring Cache的核心组件，它定义了缓存的类型和行为。Spring Cache支持多种缓存管理器，如EhCache、Caffeine、Redis等，你可以选择适合你的需求的缓存管理器。

**缓存注解**：Spring Cache提供了一组注解，用于在方法上标记缓存的行为。常用的注解包括：

- `@Cacheable`：标记一个方法可以被缓存，方法的结果将被缓存起来，以便后续请求可以直接获取。
- `@CacheEvict`：用于清除缓存中的数据。
- `@CachePut`：用于更新缓存中的数据。

**缓存失效策略**：Spring Cache允许你定义缓存的失效策略，即缓存数据何时应该过期并被重新加载。

**缓存命中**：当请求数据与缓存中的数据匹配时，发生缓存命中，可以直接从缓存中获取数据，而不需要执行实际的业务逻辑。

**参考文档**：

1. [Spring Cache的基本使用与实现原理详解](https://www.eolink.com/news/post/61240.html)
2. [Spring Cache-缓存概述及使用](https://blog.51cto.com/u_15239532/2836430)
3. [Spring官方文档 - Caching](https://docs.spring.io/spring-framework/docs/current/reference/html/integration.html#cache)
4. [Caffeine官方文档](https://github.com/ben-manes/caffeine)

# 2. Demo项目

使用Caffeine作为缓存管理器，创建一个Demo项目

创建Spring Boot项目：使用IDEA的Initializer，Maven，选择如下模块

~~~txt
spring-boot-starter-web
spring-boot-devtools
spring-boot-starter-test
~~~

随后按照如下步骤添加Caffenine

(1) 添加依赖

~~~xml
<dependencies>
    <!-- 其他依赖 -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-cache</artifactId>
    </dependency>
    <dependency>
        <groupId>com.github.ben-manes.caffeine</groupId>
        <artifactId>caffeine</artifactId>
    </dependency>
</dependencies>
~~~

(2) 配置缓存和缓存管理器

~~~java
public class CacheNames {
    public static final String GREETINGS_CACHE = "greetings_cache";
}
~~~

~~~java
@Configuration
@EnableCaching
public class CacheConfig {
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(CacheNames.GREETINGS_CACHE);
        cacheManager.setCaffeine(
            Caffeine.newBuilder()
            	// 缓存失效时间设置为10秒
            	.expireAfterWrite(10, TimeUnit.SECONDS)); 
        return cacheManager;
    }
}
~~~

(3) Service类

~~~java
@Service
public class GreetingService {
    @Cacheable(value = CacheNames.GREETINGS_CACHE, key = "#id") // 定义缓存名称和键
    public String getGreeting(int id) {
        try {
			// 模拟慢速操作
            // 等待2秒钟
            Thread.sleep(2000); 
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return "Hello, User " + id;
    }
}
~~~

(4) Controller类

~~~java
@RestController
public class GreetingController {
    @Autowired
    private GreetingService greetingService;

    @GetMapping("/greeting")
    public String greeting(@RequestParam int id) {
        return greetingService.getGreeting(id);
    }
}
~~~

完整代码：

其他参考：[[SpringBoot 使用Caffeine 本地缓存](http://www.mydlq.club/article/56/)]，[[Caching Data with Spring](https://spring.io/guides/gs/caching)], [[A Guide To Caching in Spring](https://www.baeldung.com/spring-cache-tutorial)]

# 3. 配置缓存管理器

## 3.1 缓存配置项

配置一个缓存管理器，可以定义了缓存的类型和行为。常见的缓存管理器包括EhCache、Caffeine、和Redis。在Spring Boot中，你可以在配置文件中指定缓存管理器。以Caffeine Cache为例，包含如下配置项

当配置Caffeine缓存管理器时，下表列出了常见的配置项及其功能。

| 配置项            | 功能                 |
| ----------------- | -------------------- |
| initialCapacity   | 缓存初始容量         |
| maximumSize       | 缓存的最大容量       |
| maximumWeight     | 缓存最大权重         |
| weigher           | 缓存权重计算器       |
| expireAfterWrite  | 写入后过期时间       |
| expireAfterAccess | 访问后过期时间       |
| refreshAfterWrite | 写入后自动刷新时间   |
| recordStats       | 记录缓存统计信息     |
| weakKeys          | 使用弱引用存储键     |
| weakValues        | 使用弱引用存储值     |
| softValues        | 使用软引用存储值     |
| removalListener   | 移除监听器           |
| executor          | 异步刷新缓存的执行器 |
| writer            | 缓存写入后的监听器   |
| listener          | 缓存项监听器         |

下面是使用这些配置项的例子

~~~java
@Configuration
@EnableCaching
public class CacheConfig {
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(CacheNames.GREETINGS_CACHE);
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .initialCapacity(100)
                .maximumSize(1000)
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .expireAfterAccess(5, TimeUnit.MINUTES)
                .recordStats()
                .removalListener((key, value, cause) -> System.out.println("Key removed: " + key))
        );
        return cacheManager;
    }
}
~~~

相关文档：[Caffeine GitHub - Caffeine API](https://github.com/ben-manes/caffeine/wiki) 

## 3.2 Cache Names

在IDEA中运行上面的代码，会发现执行“new CaffeineCacheManager(CacheNames.GREETING_CACHE)”时可以传入多个cache name，这有两种用途

用途1：缓存存储空间隔离

~~~java
@Bean
public CacheManager cacheManager() {
    // users、products两个缓存虽然配置相同，但是拥有各自独立的缓存存储
    CaffeineCacheManager cacheManager = new CaffeineCacheManager("users", "products");
    cacheManager.setCaffeine(Caffeine.newBuilder()
            .initialCapacity(100)
            .maximumSize(1000)
    );
    return cacheManager;
}
~~~

用途2：为不同缓存设定不同的配置

~~~java
@Bean
public CacheManager cacheManager() {
    CaffeineCacheManager cacheManager = new CaffeineCacheManager("userAuth", "productSearch");
    // 针对不同业务逻辑配置不同的缓存规则
    cacheManager.setCaffeine("userAuth", Caffeine.newBuilder().expireAfterWrite(10, TimeUnit.MINUTES));
    cacheManager.setCaffeine("productSearch", Caffeine.newBuilder().expireAfterAccess(5, TimeUnit.MINUTES));
    return cacheManager;
}
~~~

## 3.3 Cache Stats

在3.1的例子中，使用`Caffeine.newBuilder().recordStats()`开启了缓存记录功能，使用该功能，可以随时查看缓存状态，例子如下

~~~java
@Service
public class CacheService {

    private final CacheManager cacheManager;

    public CacheService(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @SuppressWarnings("unchecked")
    public Map<String, String> getAllCacheStat() {
        Map<String, String> cacheStats = new HashMap<>();
        // 遍历所有缓存名称
        for (String cacheName : cacheManager.getCacheNames()) {
            // 获取本地缓存
            Cache<Object, Object> caffeineCache
                    = (Cache<Object, Object>) Objects.requireNonNull(cacheManager.getCache(cacheName)).getNativeCache();
            // 获取缓存统计信息
            long size = caffeineCache.estimatedSize();
            cacheStats.put(cacheName, String.format("size:%d; detail:%s", size, caffeineCache.stats()));
        }
        return cacheStats;
    }
}
~~~

# 4. 使用缓存注解

在方法上使用Spring Cache的注解来启用缓存。`@Cacheable`用于标记一个方法可以被缓存，`@CacheEvict`用于清除缓存，`@CachePut`用于更新缓存。这些注解可以帮助你定义缓存的行为。

## 4.1 @Cacheble注解

### (1) 用途和例子

该注解注解允许方法的返回值被缓存，并提供了各种参数来定制缓存行为

~~~java
@Service
public class ExampleService {
    @Cacheable(value = CacheNames.GREETING_CACHE, key = "#id")
    public String getGreeting(int id) {
        return "Hello, User " + id;
    }
}

@Bean
public CacheManager cacheManager() {
    CaffeineCacheManager cacheManager = new CaffeineCacheManager(CacheNames.GREETING_CACHE);
    cacheManager.setCaffeine(CacheNames.GREETING_CACHE, Caffeine.newBuilder().expireAfterWrite(10, TimeUnit.MINUTES));
    return cacheManager;
}
~~~

### (2) 注解参数

| 参数            | 描述                                               | 使用方法                                | 例子                                                         |
| --------------- | -------------------------------------------------- | --------------------------------------- | ------------------------------------------------------------ |
| `value`         | 缓存的名称，用于定位缓存管理器                     | `value = "myCache"`                     | `@Cacheable(value = "myCache", key = "#id")`                 |
| `key`           | SpEL表达式，指定缓存的键，用于在缓存中定位对应的值 | `key = "#id"`                           | `@Cacheable(value = "myCache", key = "#id")`                 |
| `condition`     | SpEL 表达式，用于决定是否执行方法                  | `condition = "#result != null"`         | `@Cacheable(value = "myCache", key = "#id", condition = "#result != null")` |
| `unless`        | 与 `condition` 相反，决定是否不执行方法            | `unless = "#result == null"`            | `@Cacheable(value = "myCache", key = "#id", unless = "#result == null")` |
| `keyGenerator`  | 指定自定义的键生成器                               | `keyGenerator = "myKeyGenerator"`       | `@Cacheable(value = "myCache", key = "#id", keyGenerator = "myKeyGenerator")` |
| `cacheManager`  | 指定使用的缓存管理器                               | `cacheManager = "caffeineCacheManager"` | `@Cacheable(value = "myCache", key = "#id", cacheManager = "caffeineCacheManager")` |
| `cacheResolver` | 指定使用的缓存解析器                               | `cacheResolver = "myCacheResolver"`     | `@Cacheable(value = "myCache", key = "#id", cacheResolver = "myCacheResolver")` |
| `sync`          | 是否使用同步模式                                   | `sync = true`                           | `@Cacheable(value = "myCache", key = "#id", sync = true)`    |

### (3) 默认Key Generator的问题

如果不指定“keyGenerator”，Spring会使用默认的SimpleKeyGenerator，它只会使用params来计算Key

~~~java
public class SimpleKeyGenerator implements KeyGenerator {
	@Override
	public Object generate(Object target, Method method, Object... params) {
		return generateKey(params);
	}

	public static Object generateKey(Object... params) {
		if (params.length == 0) {
			return SimpleKey.EMPTY;
		}
		if (params.length == 1) {
			Object param = params[0];
			if (param != null && !param.getClass().isArray()) {
				return param;
			}
		}
		return new SimpleKey(params);
	}
}
~~~

这意味着，两个不同的方法，如果参数相同，它们之间的结果会被相互干扰。例如下面的两个方法

~~~java
@Service
public class GreetingService {
    // 下面两个方法的调用，会被cache相互干扰
    
    @Cacheable(value = CacheNames.GREETINGS_CACHE, key = "#id")
    public String getGreetingByID(int id) {
        simulateSlowOperation();
        return "Hello, User " + id + "(ID) !";
    }

    @Cacheable(value = CacheNames.GREETINGS_CACHE, key = "#code")
    public String getGreetingBySecretCode(int code) {
        simulateSlowOperation();
        return "Hello, User " + code + "(Secret Code) !";
    }
}
~~~

### (4) 自定义Key Generator

使用自定义Key Generator，可以指定cache key的生成逻辑，下面是一个简单的演示

~~~java
@Component("firstNameKeyGenerator")
public class FirstNameKeyGenerator implements KeyGenerator {
    @SuppressWarnings("NullableProblems")
    @Override
    public Object generate(Object target, Method method, Object... params) {
        // target: com.example.spring.service.GreetingService
        // method: getGreetingByFirstName
        // params: [${param}]
        return "FirstName:" + params[0];
    }
}

@Component("lastNameKeyGenerator")
public class LastNameKeyGenerator implements KeyGenerator {
    @SuppressWarnings("NullableProblems")
    @Override
    public Object generate(Object target, Method method, Object... params) {
        return "LastName:" + params[0];
    }
}

@Service
public class GreetingService {
    // 下面两个方法不会，不会被Cache相互干扰

    @Cacheable(value = CacheNames.GREETINGS_CACHE, keyGenerator = "firstNameKeyGenerator")
    public String getGreetingByFirstName(String firstName) {
        simulateSlowOperation();
        return "Hello, " + firstName + "(First Name)!";
    }

    @Cacheable(value = CacheNames.GREETINGS_CACHE, keyGenerator = "lastNameKeyGenerator")
    public String getGreetingByLastName(String lastName) {
        simulateSlowOperation();
        return "Hello, " + lastName + "(Last Name)!";
    }
}
~~~

### (5) 不保证原子性

关于原子性，`@Cacheable` 默认不提供原子性保证。写缓存和执行方法主体是两个独立的操作。即使在执行方法体成功后，如果写缓存失败，不会回滚方法的执行。

## 4.2 @CacheEvict注解

### (1) 用途和例子

`@CacheEvict` 是 Spring 框架中用于清除缓存的注解。它可以应用在方法上，用于指定在调用该方法时清除指定的缓存或所有缓存。

~~~java
@CacheEvict(value = "myCache", key = "#id")
public void clearCacheById(String id) {
    // Method logic
}
~~~

参考文档

1. [Cache Eviction in Spring Boot](https://www.baeldung.com/spring-boot-evict-cache)
2. [Spring Framework - Declarative Annotation-based Caching](https://docs.spring.io/spring-framework/docs/3.2.x/spring-framework-reference/html/cache.html)
3. [Spring @CacheEvict example](https://www.bezkoder.com/spring-cacheevict/)

### (2) 注解参数

| 参数               | 描述                                                         | 示例                                                         |
| ------------------ | ------------------------------------------------------------ | ------------------------------------------------------------ |
| `value`            | 对应 `CacheManager` 中的缓存名称                             | `@CacheEvict(value = "myCache")`                             |
| `key`              | 缓存的键，支持 SpEL 表达式                                   | `@CacheEvict(value = "myCache", key = "#id")`                |
| `keyGenerator`     | 自定义缓存键生成器，需实现 `org.springframework.cache.interceptor.KeyGenerator` 接口 | `@CacheEvict(value = "myCache", keyGenerator = "myKeyGenerator")` |
| `condition`        | 执行清除操作的条件，支持 SpEL 表达式                         | `@CacheEvict(value = "myCache", condition = "#result != null")` |
| `allEntries`       | 是否清除缓存中的所有条目，默认为 `false`                     | `@CacheEvict(value = "myCache", allEntries = true)`          |
| `beforeInvocation` | 是否在方法执行前清除缓存，默认为 `false`                     | `@CacheEvict(value = "myCache", beforeInvocation = true)`    |

### (3) 执行顺序

Spring 中的 `@CacheEvict` 注解用于清除缓存。该注解的行为取决于 `beforeInvocation` 参数的设置。

**`beforeInvocation = false`（默认值）：** 在调用方法之后清除缓存。首先执行方法体，然后清除缓存。如果方法抛出异常，缓存清除操作不会执行。

```java
@CacheEvict(value = "myCache", key = "#id")
public void clearCache(String id) {
    // Method logic
}
```

**`beforeInvocation = true`：** 在调用方法之前清除缓存。首先清除缓存，然后执行方法体。如果方法抛出异常，缓存清除操作已经执行，不会回滚。

```java
@CacheEvict(value = "myCache", key = "#id", beforeInvocation = true)
public void clearCache(String id) {
    // Method logic
}
```

### (4) 不保证原子性

`@CacheEvict` 默认不提供原子性保证。即使清除缓存的操作失败（例如，在 `beforeInvocation = false` 模式下，方法执行成功但清除缓存失败），不会回滚方法的执行。这是因为清除缓存和方法执行是两个独立的操作。

如果需要实现缓存清除和方法执行的原子性，可以考虑使用 `@Caching` 注解，结合 `@CacheEvict` 和 `@Cacheable`。这样可以通过一个方法实现缓存清除和读取的原子性操作。

## 4.3 @CachePut注解

### (1) 功能和例子

`@CachePut` 是 Spring 框架中用于更新缓存的注解。注解会强制执行被标注方法的逻辑，并将其结果放入缓存中。

~~~java
@CachePut(value = "myCache", key = "#id")
public String updateCache(String id, String newValue) {
    // Method logic
    return newValue;
}
~~~

参考文档

- [Spring Cache Abstraction](https://docs.spring.io/spring-framework/docs/4.3.5.RELEASE/spring-framework-reference/html/cache.html#cache-annotations-put)
- [Mastering Spring Cache: Annotations Guide](https://medium.com/@AlexanderObregon/a-practical-introduction-to-cacheable-cacheevict-and-other-cache-related-annotations-in-spring-f000f4331e2e)

### (2) 参数列表

| 参数               | 描述                                                         | 示例                                                         |
| ------------------ | ------------------------------------------------------------ | ------------------------------------------------------------ |
| `value`            | 缓存的名称，对应 `CacheManager` 中的缓存名称                 | `@CachePut(value = "myCache")`                               |
| `key`              | 缓存的键，支持 SpEL 表达式                                   | `@CachePut(value = "myCache", key = "#id")`                  |
| `keyGenerator`     | 自定义缓存键生成器，需实现 `org.springframework.cache.interceptor.KeyGenerator` 接口 | `@CachePut(value = "myCache", keyGenerator = "myKeyGenerator")` |
| `condition`        | 执行更新操作的条件，支持 SpEL 表达式                         | `@CachePut(value = "myCache", condition = "#result != null")` |
| `unless`           | 执行更新操作的条件，与 `condition` 相反，当条件为 `true` 时不进行缓存更新 | `@CachePut(value = "myCache", unless = "#result == null")`   |
| `allEntries`       | 是否更新缓存中的所有条目，默认为 `false`                     | `@CachePut(value = "myCache", allEntries = true)`            |
| `beforeInvocation` | 是否在方法执行前更新缓存，默认为 `false`                     | `@CachePut(value = "myCache", beforeInvocation = true)`      |

### (3) 不保证原子性

关于原子性，`@CachePut` 默认不提供原子性保证。即使更新缓存的操作失败（例如，在 `beforeInvocation = false` 模式下，方法执行成功但更新缓存失败），不会回滚方法的执行。这是因为更新缓存和方法执行是两个独立的操作。

## 4.4 @Caching注解

### (1) 功能

`@Caching` 注解是 Spring 框架中用于对多个缓存操作进行组合的注解。它允许在一个方法上同时使用多个缓存注解，如 `@Cacheable`、`@CachePut`、`@CacheEvict`。

### (2) 注解参数

下表列出了 `@Caching` 注解的参数以及它们的使用方法：

| 参数               | 描述                                           | 示例                                                         |
| ------------------ | ---------------------------------------------- | ------------------------------------------------------------ |
| `cacheable`        | 用于定义 `@Cacheable` 注解的属性。             | `@Caching(cacheable = {@Cacheable(value="cache1"), @Cacheable(value="cache2")})` |
| `put`              | 用于定义 `@CachePut` 注解的属性。              | `@Caching(put = {@CachePut(value="cache1"), @CachePut(value="cache2")})` |
| `evict`            | 用于定义 `@CacheEvict` 注解的属性。            | `@Caching(evict = {@CacheEvict(value="cache1"), @CacheEvict(value="cache2")})` |
| `cacheSpelContext` | 用于设置缓存表达式的上下文。                   | `@Caching(cacheable = {@Cacheable(value="cache1", key="#param1"), @Cacheable(value="cache2", key="#param2")}, cacheSpelContext = CacheSpelContext.EVALUATION_CONTEXT)` |
| `beforeInvocation` | 设置是否在方法执行前清空缓存，默认为 `false`。 | `@Caching(evict = {@CacheEvict(value="cache1", beforeInvocation=true)})` |

### (2) 例子

将@Cachable与@CachePut（或@CacheEvict）组合使用

~~~java
@Service
@CacheConfig(cacheNames = "exampleCache")
public class MyCacheService {
    // 如果缓存中存在数据，@Cacheable 注解负责返回缓存的数据，而不执行方法主体
    // 如果缓存中不存在数据，@CachePut 注解负责执行方法主体，将结果写入缓存
    @Caching(
        cacheable = {@Cacheable(key = "#param")},
        put = {@CachePut(key = "#param", unless = "#result == null")}
    )
    public String getAndCacheData(String param) {
        // This method will be executed only if the data is not present in the cache
        // If the data is not present, it will be fetched, cached, and returned
        // If the data is present, this method will not be executed, and the cached value will be returned
        return fetchDataFromDatabase(param);
    }

    // 如果缓存中存在数据，@Cacheable 注解负责返回缓存的数据，并且 @CacheEvict 注解负责清除缓存
    // 如果缓存中不存在数据，
    @Caching(
        cacheable = {@Cacheable(key = "#param")},
        evict = {@CacheEvict(key = "#param")}
    )
    public String getAndClearCache(String param) {
        // This method will be executed regardless of whether the data is present in the cache
        // If the data is present, it will be returned, and the cache will be cleared
        // If the data is not present, this method will be executed, and the result will be cached
        return fetchDataFromDatabase(param);
    }
    
	
    private String fetchDataFromDatabase(String param) {
        // Simulating fetching data from a database
        // This logic will be executed regardless of whether the data is present in the cache
        // In a real-world scenario, you would replace this with actual data retrieval logic
        System.out.println("Fetching data from the database for param: " + param);
        return "Data for " + param;
    }
}
~~~

### (3) 不保证原子性

使用 `@Caching` 注解时，写缓存和执行方法主体的顺序取决于注解中各个缓存操作的顺序。通常情况下，Spring 会按照注解中声明的顺序执行各个缓存操作，首先执行缓存写入操作，然后执行方法主体。这两个操作并不一定封装在一个原子操作内。

在上面的例子中、如果多个线程同时调用 `getAndCacheData` 方法，并且缓存中尚未有相应的数据，它们可能会同时执行方法体。这可能导致多个线程同时从数据库中获取数据，并将结果写入缓存。

为了防止这种情况，可以使用Spring的同步机制，例如使用`@Synchronized` 注解，来确保在缓存中不存在时，只有一个线程执行方法体。这可以防止多个线程同时执行数据库查询和缓存写入，从而确保线程安全。

~~~java
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Caching;
import org.springframework.scheduling.annotation.Synchronized;
import org.springframework.stereotype.Service;

@Service
@CacheConfig(cacheNames = "exampleCache")
public class MyCacheService {

    @Synchronized
    @Caching(
        cacheable = {@Cacheable(key = "#param")},
        put = {@CachePut(key = "#param", unless = "#result == null")}
    )
    public String getAndCacheData(String param) {
        // This method will be executed only if the data is not present in the cache
        // If the data is not present, it will be fetched, cached, and returned
        // If the data is present, this method will not be executed, and the cached value will be returned
        return fetchDataFromDatabase(param);
    }

    private String fetchDataFromDatabase(String param) {
        // Simulating fetching data from a database
        // This logic will be executed only if the data is not present in the cache
        // In a real-world scenario, you would replace this with actual data retrieval logic
        System.out.println("Fetching data from the database for param: " + param);
        return "Data for " + param;
    }
}
~~~

# 5. 高级主题 

## 5.1 自定义缓存管理器

### (1) 步骤

实现自定义的缓存管理器，需要执行以下步骤：

(1) 创建一个类并实现 Spring 的 CacheManager，提供对缓存的管理方法

(2) 实现 Cache 接口（可选），如果希望使用自定义的缓存实现

(3) 注册自定义 CacheManager到Spring容器

### (2) 例子

下面是一个简单的例子

(1) 实现CacheManager及Cache接口

~~~java
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class MyCacheManager implements CacheManager {

    private final Map<String, Cache> cacheMap = new HashMap<>();

    @Override
    public Cache getCache(String name) {
        // 如果缓存存在，则返回缓存；否则创建一个新的缓存实例并返回
        return cacheMap.computeIfAbsent(name, Cache::new);
    }

    @Override
    public Collection<String> getCacheNames() {
        // 返回所有缓存的名称
        return cacheMap.keySet();
    }

    // 自定义缓存实现
    private static class Cache implements org.springframework.cache.Cache {

        private final Map<Object, Object> store = new HashMap<>();
        private final String name;

        public Cache(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public Object getNativeCache() {
            return store;
        }

        @Override
        public ValueWrapper get(Object key) {
            Object value = store.get(key);
            return (value != null) ? () -> value : null;
        }

        @Override
        public <T> T get(Object key, Class<T> type) {
            return (T) store.get(key);
        }

        @Override
        public <T> T get(Object key, Callable<T> valueLoader) {
            return (T) store.computeIfAbsent(key, k -> {
                try {
                    return valueLoader.call();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }

        @Override
        public void put(Object key, Object value) {
            store.put(key, value);
        }

        @Override
        public void evict(Object key) {
            store.remove(key);
        }

        @Override
        public void clear() {
            store.clear();
        }
    }
}
~~~

注册缓存管理器

~~~java
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        return new MyCacheManager();
    }
}
~~~

使用缓存

~~~java
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class MyService {
    @Cacheable(value = "myCache", key = "#key")
    public String getData(String key) {
        // 模拟从数据库或其他数据源获取数据的逻辑
        System.out.println("Executing getData method for key: " + key);
        return "Data for key: " + key;
    }
}
~~~

## 5.2 缓存失效策略

### (1) 基于固定时间的失效策略

通过方法注解来实现

~~~java
@Service
public class MyService {
	// 缓存项在写入后10分钟失效
    @Cacheable(value = "myCache", key = "#key", expireAfterWrite = 10, timeUnit = TimeUnit.MINUTES)
    public String getCachedData(String key) {
        return fetchDataFromDataSource(key);
    }
}
~~~

通过配置来实现

~~~java
@Configuration
@EnableCaching
public class CacheConfig {
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager("myCache");
        // 缓存项在写入后10分钟失效
        cacheManager.setCaffeine(Caffeine.newBuilder().expireAfterWrite(10, TimeUnit.MINUTES));
        return cacheManager;
    }
}
~~~

### (2) 基于最后一次访问的失效策略

通过方法注解来实现

~~~java
@Service
public class MyService {
	// 缓存项在最后一次访问后5分钟失效
    @Cacheable(value = "myCache", key = "#key", expireAfterAccess = 5, timeUnit = TimeUnit.MINUTES)
    public String getCachedData(String key) {
        return fetchDataFromDataSource(key);
    }
}
~~~

通过配置来实现

~~~java
@Configuration
@EnableCaching
public class CacheConfig {
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager("myCache");
        cacheManager.setCaffeine(Caffeine.newBuilder().expireAfterAccess(5, TimeUnit.MINUTES));
        return cacheManager;
    }
}
~~~

### (3) 基于数据变化的失效策略

通过方法注解来实现

~~~java
@Service
public class MyService {
	// 数据发生变化时触发缓存失效
    @CacheEvict(value = "myCache", key = "#key")
    public void updateData(String key) {
        // 更新数据的逻辑
    }
}
~~~

## 5.3 缓存监控

缓存的监控通常包括监视缓存的命中率、缓存的大小、缓存的命中与未命中次数等指标。Spring Boot提供了Actuator模块，它可以用于监控和管理应用程序。以下是一些在Spring Boot中监控缓存的方法：

### (1) Actuator Endpoints

Spring Boot Actuator提供了一组内置的监控端点，其中包括与缓存相关的端点。通过访问`/actuator/caches`端点，你可以获取有关所有缓存的信息，包括缓存的命中率、未命中率、命中次数、未命中次数等。

依赖

~~~xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
~~~

配置

```yaml
management:
  endpoints:
    web:
      exposure:
        include: 'caches'
```

这个配置将`caches`端点暴露给Web。

### (2) Spring Boot Admin

Spring Boot Admin是一个用于监控Spring Boot应用程序的开源项目。它提供了一个直观的用户界面，可以查看缓存的状态、指标和其他应用程序相关的信息。

```xml
<dependency>
    <groupId>de.codecentric</groupId>
    <artifactId>spring-boot-admin-starter-server</artifactId>
</dependency>
```

在主类上添加`@EnableAdminServer`注解，然后你可以在`http://localhost:8080`上访问Spring Boot Admin界面。

```java
import de.codecentric.boot.admin.server.config.EnableAdminServer;

@SpringBootApplication
@EnableAdminServer
public class SpringBootAdminApplication {
    public static void main(String[] args) {
        SpringApplication.run(SpringBootAdminApplication.class, args);
    }
}
```

### (3) **Micrometer**

Micrometer是一个用于度量和监控应用程序的度量库。它与Spring Boot集成得很好，可以使用不同的监控系统，例如Prometheus、Graphite等。你可以使用Micrometer来记录缓存相关的指标。

```xml
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>
```

在`application.properties`或`application.yml`中配置Prometheus注册表：

```yaml
management:
  metrics:
    export:
      prometheus:
        enabled: true
```

你可以通过访问`/actuator/prometheus`来查看所有的Micrometer度量指标，包括与缓存相关的。

这些方法可以帮助你监控应用程序中的缓存，确保它们按预期工作，并且可以在需要时采取适当的措施

