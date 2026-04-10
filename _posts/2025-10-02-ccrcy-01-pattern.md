---
title: Java并发编程01(A)：设计模式   
author: fangkun119
date: 2025-10-02 12:00:00 +0800
categories: [Java, Java 并发编程]
tags: [Java, Java 并发编程]
pin: false
math: true
mermaid: true
image:
  path: /imgs/cover/java_concurrency.jpg
  lqip: data:image/webp;base64,UklGRpoAAABXRUJQVlA4WAoAAAAQAAAADwAABwAAQUxQSDIAAAARL0AmbZurmr57yyIiqE8oiG0bejIYEQTgqiDA9vqnsUSI6H+oAERp2HZ65qP/VIAWAFZQOCBCAAAA8AEAnQEqEAAIAAVAfCWkAALp8sF8rgRgAP7o9FDvMCkMde9PK7euH5M1m6VWoDXf2FkP3BqV0ZYbO6NA/VFIAAAA
  alt: java concurrency
---

{: .no_toc }

<details close markdown="block">
  <summary>
    目录
  </summary>
  {: .text-delta }
- TOC
{:toc}
</details>

## 1. 文章概要

本文系统讲解 **Java 并发编程设计模式**，聚焦以下知识模块：

| 知识模块 | 说明 |
| ---- | ---- |
| **线程终止** | 两阶段终止模式：interrupt 唤醒 + 标志位检查，实现线程池与监控线程的优雅退出 |
| **避免共享** | 不变性、写时复制、线程本地存储三种模式，从源头消除并发竞争 |
| **线程协作** | 守护挂起、避免执行两种多线程 if 控制模式，实现条件等待与快速返回 |
| **线程分工** | Thread-Per-Message、Worker Thread、生产者-消费者三种分工模式，提升并发处理效率 |

> 本文以**实践案例**为主线，通过**监控线程终止**、**自动保存**、**服务端并发处理**等典型场景，帮助读者掌握 Java 并发编程的设计模式与最佳实践。

核心内容提炼([`Youtube`](https://youtu.be/_ERNAtzYTGM) `|` [`B站`](https://www.bilibili.com/video/BV13RQMBuEH9)):

<iframe width="560" height="315" src="https://www.youtube.com/embed/_ERNAtzYTGM?si=ezLGPNo4QqAcljlj" title="YouTube video player" frameborder="0" allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share" referrerpolicy="strict-origin-when-cross-origin" allowfullscreen></iframe>


## 2. 优雅终止线程的设计模式

### 2.1 两阶段终止（Two-phase Termination）模式

#### (1) 模式介绍

**两阶段终止模式**将线程终止过程分解为**发送终止请求**和**等待线程停止**两个阶段，从而实现线程的优雅终止：

##### ① 第一阶段：发送终止请求

Java 线程进入终止状态前必须处于 **RUNNABLE** 状态。若线程当前处于休眠或等待状态，需先通过 `interrupt()` 方法将其唤醒，使其转换为 **RUNNABLE** 状态。

<img src="/imgs/ccrcy-01-pattern/45a34bc136e67098b888f0f7a6c6eb0c_MD5.jpg" style="display: block; width: 100%;" alt="两阶段终止模式：线程从等待状态通过interrupt()唤醒到RUNNABLE状态">

##### ② 第二阶段：等待线程终止

线程恢复到 **RUNNABLE** 状态后，通过检查**终止标志位**主动退出 `run()` 方法，完成终止流程。

**模式优势：**

| 优势 | 说明 |
| --- | --- |
| **优雅终止** | 避免 `stop()` 方法导致的资源泄漏和数据不一致问题 |
| **安全可控** | 终止前可执行必要的清理工作，确保资源正确释放 |
| **灵活配置** | 可根据实际场景设置终止条件和清理逻辑 |

#### (2) 应用场景

两阶段终止模式主要适用于**终止前需要执行清理工作**的线程场景：

| 应用场景 | 终止需求 | 模式价值 |
| --- | --- | --- |
| **服务器应用** | 处理大量请求，终止时需正确保存状态和释放资源 | 避免数据丢失和资源泄漏 |
| **大规模并发系统** | 线程数量多，需统一协调终止 | 保证所有线程正确关闭和资源释放 |
| **定时任务** | 任务执行完毕后需清理资源 | 实现任务线程的优雅退出 |
| **数据处理系统** | 处理完所有数据后需终止线程 | 确保处理完成后再释放资源 |
| **消息订阅系统** | 订阅结束后需终止订阅线程 | 避免消息丢失和连接泄漏 |

> **使用注意**：应用该模式时，需正确设计终止条件和清理工作，避免线程安全问题和资源泄漏。

### 2.2 实践案例

#### (2) 优雅终止监控线程

执行**长时间监控**或**轮询操作**的线程，在终止前需要**安全释放资源**并**完成清理工作**。

**基础实现：标志位控制**

下面的示例通过 `volatile` 标志位实现线程的优雅终止：

```java
public class MonitorThread extends Thread { // 在监控线程中添加一个volatile类型的标志变量，用于标识是否需要终止线程的执行
    private volatile boolean terminated = false;

    public void run() {
        while (!terminated) { // 执行监控操作
            System.out.println("监控线程正在执行监控操作...");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        // 执行清理操作
        System.out.println("监控线程正在执行清理操作...");
        releaseResources();
    }

    public void terminate() { // 设置标志变量为true，并等待一段时间
        terminated = true;
        try {
            join(5000); // 等待5秒钟,期间监控线程会检查terminated的状态
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void releaseResources() { // 释放资源和进行必要的清理工作
        System.out.println("监控线程正在释放资源和进行必要的清理工作...");
    }

    public static void main(String[] args) throws InterruptedException {
        MonitorThread thread = new MonitorThread();
        // 启动监控线程
        thread.start();
        // 主线程休眠期间，监控线程在执行监控操作
        Thread.sleep(10000);
        // 终止监控线程
        thread.terminate();
        Thread.sleep(100000);
    }
}
```

**实现方式对比：**

| 实现方式 | 终止机制 | 特点 |
| --- | --- | --- |
| **标志位控制** | `volatile boolean` | 线程自行检查标志位，主动退出 |
| **标志位 + 中断** | `volatile boolean + interrupt()` | 响应中断唤醒，支持阻塞状态退出 |

**改进实现：标志位 + 中断机制**

上述标志位方式存在局限性：线程处于**休眠状态**时无法及时检测标志位变化。引入**中断机制**可以让线程从阻塞状态唤醒，通过**双重检查**（中断状态 + 标志位）实现更可靠的线程终止。

```java
public class MonitorThread2 extends Thread {
    // 在监控线程中添加一个volatile类型的标志变量，用于标识是否需要终止线程的执行
    private volatile boolean terminated = false;

    public void run() {
        while (!Thread.interrupted() && !terminated) {
            // 执行监控操作
            System.out.println("监控线程正在执行监控操作...");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                System.out.println("监控线程被中断，准备退出...");
                Thread.currentThread().interrupt();
                e.printStackTrace();
            }
        }
        // 执行清理操作
        System.out.println("监控线程正在执行清理操作...");
        releaseResources();
    }

    public void terminate() {
        // 设置标志变量为true，并等待一段时间
        terminated = true;
        try {
            join(5000); // 等待5秒钟,期间监控线程会检查terminated的状态
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void releaseResources() {
        // 释放资源和进行必要的清理工作
        System.out.println("监控线程正在释放资源和进行必要的清理工作...");
    }

    public static void main(String[] args) throws InterruptedException {
        MonitorThread2 thread = new MonitorThread2();
        // 启动监控线程
        thread.start();
        // 主线程休眠期间，监控线程在执行监控操作
        Thread.sleep(10000);
        // 为监控线程设置中断标志位
        thread.interrupt();
        // 终止监控线程
        // thread.terminate();
        Thread.sleep(100000);
    }
}
```

#### (3) 优雅终止线程池

Java 并发实践中，**线程池**的应用远多于手动创建线程，因此如何优雅终止线程池成为实际开发中的常见问题。

**线程池**提供了两种终止方法：

| 方法 | 行为特征 | 适用场景 |
| --- | --- | --- |
| **shutdown()** | 停止接受新任务，等待队列中所有任务执行完毕后关闭 | 需要保证任务完整执行 |
| **shutdownNow()** | 停止接受新任务，尝试中断正在执行的任务，返回未执行任务列表 | 需要快速停止，任务可中断 |

<img src="/imgs/ccrcy-01-pattern/ab83bf0e7bfe3caa36270dbc56700c29_MD5.jpg" style="display: block; width: 100%;" alt="线程池终止方法对比：shutdown()和shutdownNow()的差异">

**关键差异**：

- **shutdown()**：线程池不再接受新任务，但会继续执行队列中的任务直到队列为空，正在执行的任务会等待其完成
- **shutdownNow()**：线程池不再接受新任务，并尝试通过 **interrupt()** 中断正在执行的任务。由于线程可以选择忽略中断请求，中断不保证成功

**实践建议**：

生产环境通常采用**两阶段终止**：先调用 **shutdown()** 等待任务完成，若超时则调用 **shutdownNow()** 强制终止。

```java
public class ThreadPoolDemo {
    public static void main(String[] args) throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(5);

        for (int i = 0; i < 10; i++) {
            executorService.submit(() -> {
                try {
                    // 执行任务操作
                    System.out.println(Thread.currentThread().getName() + "正在执行任务...");
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    // 重新设置中断状态
                    Thread.currentThread().interrupt();
                    e.printStackTrace();
                } finally {
                    System.out.println(Thread.currentThread().getName() + "任务执行完毕");
                }
            });
        }

        // 停止线程池接受新的任务，但不能强制停止已经提交的任务
        executorService.shutdown();

        // 等待线程池中的任务执行完毕，或者超时时间到达
        boolean terminated = executorService.awaitTermination(8, TimeUnit.SECONDS);

        if (!terminated) {
            // 如果线程池中还有未执行完毕的任务，则调用线程池的shutdownNow方法，中断所有正在执行任务的线程
            // 如果有还没开始执行的任务，则返回未执行的任务列表
            List<Runnable> tasks = executorService.shutdownNow();
            System.out.println("剩余未执行的任务数：" + tasks.size());
        }
    }
}
```

**执行说明**：上述示例中，线程池依次执行 10 个任务后优雅终止，若 8 秒内未完成则强制中断剩余任务。

### 2.3 实践要点

应用两阶段终止模式时，需注意以下问题：

| 问题 | 原因 | 处理方式 |
| --- | --- | --- |
| **仅检查标志位** | 线程处于**休眠状态**时无法检测标志位变化 | 用**中断机制**唤醒线程 |
| **仅检查中断状态** | 第三方类库可能未正确处理中断异常 | 增加**自定义终止标志位** |

**线程池终止方法对比**：

| 方法 | 行为特征 | 适用场景 |
| --- | --- | --- |
| **shutdown()** | 停止接受新任务，等待队列任务执行完毕 | 需要保证任务完整执行 |
| **shutdownNow()** | 尝试中断正在执行的任务，返回未执行任务列表 | 需要快速停止 |

> **使用注意**：`shutdownNow()` 会强制中断线程，使用前需确认任务支持中断处理。

## 3. 避免共享的设计模式

### 3.1 概述

多线程并发编程中，**共享可变状态**是引发线程安全问题的根源。通过**避免共享**可以从根本上消除并发冲突。以下是三种基于此思路的设计模式：

| 模式 | 设计思路 | 实践要点 |
| --- | --- | --- |
| **不变性** | 创建状态不可改变的对象 | 确保对象及其引用的属性均不可变 |
| **写时复制** | 修改时创建副本，读操作访问原数据 | 适用于读多写少场景，关注复制开销 |
| **线程本地存储** | 为每个线程提供独立存储空间 | 线程池场景需注意内存泄漏 |

### 3.2 写时复制（Copy-on-Write）模式

#### (1) 模式介绍

**基本原理：**

**Copy-on-Write 模式**通过**写时复制**机制避免多线程竞争：修改数据时先创建副本，在副本上完成修改后再替换原数据。这种设计让**读操作完全无锁**，从而提升并发性能。

<img src="/imgs/ccrcy-01-pattern/0ba01f6edd70046136f97b908da80f61_MD5.jpg" style="display: block; width: 100%;" alt="Copy-on-Write模式：写操作复制副本，读操作无锁访问">

Copy-on-Write 最初应用于**不可变对象**的写操作，但其适用范围已扩展到多个领域。Java 中的 **String**、**Integer**、**Long** 等包装类都基于此机制实现。

**性能特点：**

| 维度 | 优势 | 劣势 |
| --- | --- | --- |
| **并发性能** | **读操作无锁**，性能极高 | 写操作需复制，开销较大 |
| **内存消耗** | 读场景无额外开销 | 每次修改复制新对象，增加内存占用 |
| **适用场景** | **读多写少**的场景 | 写频繁场景不适用 |

#### (2) 使用场景

Copy-on-Write 模式特别适合**读多写少**的并发场景，通过**读操作无锁**实现高性能访问：

| 应用领域 | 典型案例 | 核心特征 |
| --- | --- | --- |
| **Java 并发容器** | **CopyOnWriteArrayList**、**CopyOnWriteArraySet** | 写操作复制数组，读操作直接访问 |
| **操作系统** | Linux **fork()** 系统调用 | 父子进程共享物理页，写入时才复制 |
| **函数式编程** | 数据修改操作 | 基于不可变性，每次修改产生新对象 |
| **服务治理** | RPC 框架路由表、注册中心 | 查询频率远高于变更频率 |

<img src="/imgs/ccrcy-01-pattern/0fc9604b74141920e4e70cbf0b526607_MD5.jpg" style="display: block; width: 100%;" alt="Copy-on-Write应用场景：Java并发容器、操作系统fork()、服务路由表">

**典型应用说明：**

**Java 并发容器**：写操作时复制整个底层数组，读操作无需加锁即可访问原始数组，适合遍历操作远多于修改操作的场景。

**操作系统 fork()**：传统 Unix 实现需要复制整个地址空间（例如 1GB），Linux 通过写时复制优化，父子进程最初共享物理页，仅在实际写入时才复制相关页面，显著提升进程创建效率。

**函数式编程**：强调**不可变性**，变量一次赋值后不再修改，这种编程范式天然符合 Copy-on-Write 模式的思想。

**服务路由表**：RPC 框架（如 Dubbo）和服务注册中心（如 Nacos）使用此模式维护服务地址列表。这类场景中，路由表变更频率较低（通常为分钟级），而查询频率极高（QPS 级），且能容忍短暂的数据不一致延迟。

### 3.2 不变性（Immutability）模式——想破坏也破坏不了

#### (1) 模式介绍

**不变性模式**源于一个简单的观察：**只读不写**的共享变量不会产生并发问题。该模式通过创建状态不可变的对象，从根本上避免了多线程环境下的同步开销。

**模式优势：**

| 优势维度 | 具体价值 |
| --- | --- |
| **线程安全** | 无需同步机制，自然避免竞态条件和数据竞争 |
| **性能优化** | 状态不变便于缓存和共享，消除加锁开销 |
| **可读性** | 对象含义清晰，状态变化可预测，降低理解成本 |
| **可测试性** | 无副作用，单元测试简单可靠，易于验证 |

**使用限制：**

| 限制场景 | 影响说明 |
| --- | --- |
| **频繁修改** | 每次修改需创建新对象，增加内存分配和垃圾回收压力 |
| **引用关系** | 需确保引用对象也不可变，避免通过引用修改内部状态 |

#### (2) 使用场景

不变性模式适用于**状态无需改变**的并发场景，通过消除可变性来简化并发控制：

| 应用场景 | 典型案例 | 模式价值 |
| --- | --- | --- |
| **缓存数据** | 多线程共享的查询结果、计算缓存 | **无锁读取**，避免同步开销，提升并发性能 |
| **配置对象** | 系统参数、业务规则配置 | **运行期保持稳定**，防止意外修改，降低维护成本 |
| **值对象** | 金额、日期、坐标等业务实体 | **保证状态一致性**，降低出错风险，提高代码可靠性 |

#### (3) 实现要点

不变性模式通过 **final 修饰符**、**属性私有化**和**只读方法**确保对象状态不可变。实现时需满足以下要求：

| 要点 | 说明 |
| --- | --- |
| **属性不可变** | 所有属性在创建后不可修改 |
| **引用关系处理** | 注意对象间的引用关系，避免通过引用修改内部状态 |

**JDK 中的不可变类**

**String**、**Long**、**Integer**、**Double** 等包装类都遵循不变性原则，其线程安全性完全由不可变性保证。这些类严格遵守以下要求：

<img src="/imgs/ccrcy-01-pattern/5b85e76835aed7d262702e8abbf85c37_MD5.jpg" style="display: block; width: 100%;" alt="JDK不可变类实现要求：类和属性都是final，所有方法都是只读的">

| 要求 | 说明 |
| --- | --- |
| **类和属性都是 final** | 防止被子类继承和属性被重新赋值 |
| **所有方法都是只读的** | 不提供任何修改状态的方法，确保创建后状态不变 |

#### (4) 注意事项

应用不变性模式时，需注意以下两点：

- **final 属性不保证对象不可变性**
- **不可变对象需要正确发布**

##### ① final 属性的不可变性边界

**final 的保护范围仅限于引用本身**，无法延伸到对象内部状态。

Java 中 final 修饰的属性在赋值后，**引用地址不可修改**，但如果属性类型是**可变对象**，其内部状态仍可被外部修改。应用不变性模式时，需要明确**不可变性边界**：确认属性对象是否也具备不可变性。

下例中，`Bar` 的 `foo` 属性虽为 final，但仍可通过 `setAge()` 修改 `foo` 的内部属性 `age`：

```java
class Foo {
    int age = 0;
    int name = "abc";
}

final class Bar {
    final Foo foo;

    void setAge(int a) {
        foo.age = a;
    }
}
```

##### ② 不可变对象的正确发布

不可变对象本身线程安全，但**持有其引用的对象**未必线程安全。

下例中，`Foo` 类具备不可变性，是线程安全的；但 `Bar` 类并非线程安全。`Bar` 持有对 `Foo` 的引用 `foo`，多线程环境下修改该引用时无法保证**可见性**和**原子性**。

```java
// Foo线程安全
final class Foo {
    final int age = 0;
    final String name = "abc";
}

// Bar线程不安全
class Bar {
    Foo foo;

    void setFoo(Foo f) {
        this.foo = f;
    }
}
```


### 3.4 线程本地存储（Thread-Specific Storage）模式——没有共享就没有伤害

#### (1) 模式介绍

**线程本地存储模式**为每个线程创建独立的存储空间，通过数据隔离避免并发冲突。Java 标准库中的 **ThreadLocal** 类提供了该模式的实现。

**核心机制**：ThreadLocal 通过**避免共享**解决并发问题——没有共享，自然不存在竞争。在并发场景中使用非线程安全的工具类时，可采用以下两种方式：

| 实现方式 | 机制 | 优势 | 劣势 |
| --- | --- | --- | --- |
| **局部变量** | 每次调用创建新对象 | 实现简单 | 高并发下频繁创建对象，增加 GC 压力 |
| **ThreadLocal** | 每个线程持有一个实例 | 避免重复创建，性能更好 | 需手动管理，避免内存泄漏 |

#### (2) 使用场景

##### ① 原理

ThreadLocal 模式为每个线程提供独立的存储空间，通过**避免共享**解决并发问题，适用于以下场景：

<img src="/imgs/ccrcy-01-pattern/f9db291a155c092b3c794b9078d79912_MD5.jpg" style="display: block; width: 100%;" alt="ThreadLocal原理：为每个线程提供独立的存储空间">

##### ② 使用场景

| 应用场景 | 说明 | 典型示例 |
| --- | --- | --- |
| **保存上下文信息** | 为每个线程维护独立的执行上下文 | 用户会话信息、请求上下文、事务上下文 |
| **管理非线程安全对象** | 避免共享对象的同步开销，为每个线程创建独立实例 | **SimpleDateFormat**、**Random** 等非线程安全对象 |
| **实现线程特定行为** | 在每个线程中执行独立的业务逻辑，无需跨线程协调 | 日志跟踪（MDC）、性能统计、权限验证 |

##### ③ 内存泄漏问题

**ThreadLocal 在线程池场景下使用不当会导致内存泄漏和数据不一致**，问题的根源在于线程池的**线程复用机制**。

**问题原因：**

| 问题类型 | 原因分析 |
| --- | --- |
| **内存泄漏** | 线程复用时，**ThreadLocal** 变量在任务完成后未被清理，导致对象一直被引用无法回收 |
| **数据不一致** | 线程复用时，**ThreadLocal** 可能保留了上一次任务的数据，影响当前任务的正确性 |

**正确用法：**

为避免上述问题，在线程池场景下，应在 **finally** 块中手动调用 **remove()** 清理 **ThreadLocal** 变量：

```java
ExecutorService es;
ThreadLocal tl;

es.execute(() -> {
    // ThreadLocal增加变量
    tl.set(obj);
    try {
        // 省略业务逻辑代码
    } finally {
        // 手动清理ThreadLocal
        tl.remove();
    }
});
```

> **使用建议**：线程池场景下务必在 **finally** 块中调用 **remove()**，避免数据污染和内存泄漏。

## 4. 多线程版本的 if 模式

### 4.1 介绍

**守护挂起模式**和**避免执行模式**本质上是**多线程环境下的 if 控制结构**，通过判断守护条件决定后续执行流程：

| 模式 | 条件不满足时的处理 | 实践要点 |
| --- | --- | --- |
| **守护挂起模式** | **等待**至条件满足 | 需关注等待性能，避免长时间阻塞 |
| **避免执行模式** | **直接返回**，中断处理 | 需关注竞态条件，保证检查与执行的原子性 |

### 4.2 守护挂起（Guarded Suspension）模式——等我准备好哦

#### (1) 模式概述

**Guarded Suspension 模式**用于解决多线程间的**结果传递**问题：一个线程等待并获取另一个线程的计算结果。该模式通过检查**守护条件**来控制执行流程——条件不满足时线程**挂起等待**，条件满足后被**唤醒执行**。

<img src="/imgs/ccrcy-01-pattern/aaa226a36b73f4097e12dc0e842ec15c_MD5.jpg" style="display: block; width: 100%;" alt="Guarded Suspension模式：等待守护条件满足后执行">

**应用场景：**

| 场景类型 | 实现方式 |
| --- | --- |
| **单次结果传递** | 多个线程关联同一个 **GuardedObject** 实例 |
| **多次结果传递** | 使用 **消息队列**（如 BlockingQueue） |

**JDK 中的应用：**

| API | 典型应用 |
| --- | --- |
| **Thread.join()** | 等待目标线程执行完毕 |
| **Future** | 获取异步任务执行结果 |

由于该模式需要等待另一方的结果，因此归类为**同步模式**（Synchronization Pattern）。

#### (2) 等待唤醒机制

**守护挂起模式**的实现基础是 Java 的**等待唤醒机制**。该机制提供三种实现方式：

| 实现方式              | 核心方法                                             |
| ----------------- | ------------------------------------------------ |
| **synchronized**  | `wait()` / `notify()` / `notifyAll()`            |
| **ReentrantLock** | `Condition.await()` / `signal()` / `signalAll()` |
| **CAS**           | `LockSupport.park()` / `unpark()`                |

这些机制底层基于 Linux pthread 的 `pthread_mutex_lock/unlock` 和 `pthread_cond_wait/signal` 实现。

在实际开发中，等待唤醒机制主要用于协调线程间的执行顺序，解决线程协作问题。

<img src="/imgs/ccrcy-01-pattern/6802548c0c53100e22148f7898310912_MD5.jpg" style="display: block; width: 100%;" alt="Java等待唤醒机制三种实现方式：synchronized、ReentrantLock、CAS">

#### (3) 使用场景

Guarded Suspension 模式用于解决**跨线程结果传递**问题：一个线程需要等待另一个线程的计算结果后才能继续执行。

| 场景特征 | 说明 |
| --- | --- |
| **结果依赖** | 线程 B 需要获取线程 A 的计算结果才能继续执行 |
| **状态管理** | 资源对象管理自身状态，判断是否满足获取条件 |
| **等待唤醒** | 条件不满足时等待，条件满足后唤醒等待线程 |

#### (4) 模式实现

**GuardedObject** 通过 Java 内置的**等待唤醒机制**实现跨线程的结果传递。

**代码实现：**

```java
public class GuardedObject<T> {
    // 结果对象
    private T obj;

    // 获取结果
    public T get(){
        synchronized (this){
            // 通过 while 循环检查条件，防止虚假唤醒
            while (obj==null){
                try {
                    this.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return obj;
        }
    }

    // 设置结果
    public void complete(T obj){
        synchronized (this){
            // 设置结果对象
            this.obj = obj;
            // 唤醒所有等待线程
            this.notifyAll();
        }
    }
}
```

**方法说明：**

| 方法 | 行为描述 | 线程状态变化 |
| --- | --- | --- |
| **get()** | 通过 **while 循环**检查条件是否满足，未满足则调用 **wait()** 进入等待 | 从 **RUNNABLE** 转为 **WAITING** 状态 |
| **complete()** | 设置结果后调用 **notifyAll()** 通知所有等待线程 | 等待线程从 **WAITING** 恢复为 **RUNNABLE** 状态 |

### 4.3 避免执行（Balking）模式 —— 避免执行模式

#### (1) 模式介绍

**Balking 模式**通过**守护条件**判断决定是否执行操作：条件不满足时**直接返回**，避免不必要的执行。

**工作机制：**

多个线程可能执行同一操作时，若某操作已被其他线程完成，则当前线程无需重复执行，直接结束返回。这种模式适用于**执行顺序依赖于共享变量**的场景。

<img src="/imgs/ccrcy-01-pattern/1d592acb05c50cd9db6250d5797594bf_MD5.jpg" style="display: block; width: 100%;" alt="Balking模式：守护条件不满足时直接返回">

**与 Guarded Suspension 对比：**

<img src="/imgs/ccrcy-01-pattern/7eb4423143a26d88d98cdce547998654_MD5.jpg" style="display: block; width: 100%;" alt="Balking模式与Guarded Suspension模式对比">

| 模式 | 守护条件不满足时的行为 | 典型特征 |
| --- | --- | --- |
| **Balking** | **直接返回**，中断处理 | 快速失败，避免等待 |
| **Guarded Suspension** | **等待**至条件满足 | 阻塞等待，保证执行 |

#### (2) 使用场景

Balking 模式适用于**操作重复执行无意义**的场景：当守护条件不满足时，直接放弃当前操作。以下是几个典型应用：

| 应用场景 | 守护条件 | 放弃原因 |
| --- | --- | --- |
| **synchronized 轻量级锁膨胀** | 仅需一个线程执行膨胀 | 避免多个线程重复获取 monitor 对象 |
| **DCL 单例模式** | 单例对象已创建 | 防止重复初始化 |
| **服务组件初始化** | 组件已初始化 | 避免重复加载资源 |

这些场景的共同特点是：**多个线程同时请求操作，但只需要其中一个线程执行即可**。通过快速判断守护条件并直接返回，可以避免不必要的重复执行，减少资源消耗。

#### (3) 实现方式

Balking 模式的**关键点**在于保证**守护条件检查**与**操作执行**之间的**原子性**，防止多个线程同时通过检查而导致重复执行。Java 中常用的实现方式如下：

| 实现方式 | 机制 | 适用场景 | 性能特点 |
| --- | --- | --- | --- |
| **synchronized** | 对象监视器锁 | 通用场景，代码简单直观 | 中等，存在锁竞争开销 |
| **ReentrantLock** | 可重入锁 | 需要公平锁或超时控制 | 可配置，灵活性高 |
| **CAS** | 无锁原子操作 | 高并发场景，读多写少 | 高性能，无线程阻塞 |
| **volatile** | 内存可见性 | 仅需保证可见性，无需原子性 | 最高，但适用范围有限 |

**实现说明：**

| 方式 | 说明 |
| --- | --- |
| **锁机制** | 通过互斥锁确保检查与执行的原子性，适合大多数业务场景 |
| **CAS** | 基于硬件指令的原子操作，适合对性能要求高的场景 |
| **volatile** | 仅保证共享变量的可见性，适用于不需要原子性的场景（如单次初始化状态标识） |

#### (4) 应用示例：自动保存功能

**自动保存**是 Balking 模式的典型应用场景：编辑器按固定周期检查文件状态，仅在内容发生变化时才执行存盘操作，避免不必要的磁盘 I/O。

**实现逻辑：**

| 关键步骤 | 具体实现 |
| --- | --- |
| **定时触发** | 按设定周期自动调用存盘检查 |
| **状态检测** | 通过 `changed` 标志位判断文件是否被修改 |
| **条件放弃** | 文件未修改时直接返回，避免无效 I/O 操作 |
| **状态重置** | 执行存盘后重置标志位，等待下次修改 |

```java
boolean changed = false; // 自动存盘操作

void autoSave() {
    synchronized(this) {
        if (!changed) {
            return;
        }
        changed = false;
    } // 执行存盘操作
    // 省略具体实现
    this.execSave();
} // 编辑操作

void edit() {
    // 省略编辑逻辑
    ......
    change();
} // 改变状态

void change() {
    synchronized(this) {
        changed = true;
    }
}
```

**代码说明：** `autoSave()` 方法在 **synchronized 块内**检查 `changed` 标志位，若为 `false` 则直接返回；若为 `true` 则重置为 `false` 并在**锁外**执行实际的存盘操作，减少锁的持有时间。

#### (5) 单次初始化场景

**单次初始化**是 Balking 模式的常见应用：通过守护条件判断，确保初始化操作**仅执行一次**，避免重复加载资源或重复执行初始化逻辑。

实际开发中，很多资源只需要初始化一次，例如配置加载、连接池创建、缓存预热等。多个线程同时调用初始化方法时，只需要其中一个线程执行初始化，其他线程直接跳过即可。

**实现要点：**

| 关键点 | 说明 |
| --- | --- |
| **守护条件** | 通过 `inited` 标志位判断是否已初始化 |
| **原子性保证** | 使用 `synchronized` 确保检查与设置的原子性 |
| **快速返回** | 已初始化时直接返回，避免重复执行 |

**实现示例：**

```java
boolean inited = false;

synchronized void init() {
    if (inited) {
        return;
    }
    //省略doInit的实现
    doInit();
    inited = true;
}
```

上述代码通过 `synchronized` 关键字保证了 `if (inited)` 检查和 `inited = true` 设置之间的**原子性**，防止多个线程同时通过检查而重复执行初始化。

> **性能优化**：此示例适用于大多数单次初始化场景。若对性能有更高要求，可考虑使用基于 **CAS** 的实现方式（如 `AtomicBoolean`），避免锁机制带来的开销。

## 5. 多线程分工模式

### 5.1 概述

**Thread-Per-Message 模式**、**Worker Thread 模式**和**生产者-消费者模式**属于多线程分工模式，通过不同的线程组织方式提升并发处理效率。

| 模式 | 分工方式 | 核心关注点 |
| --- | --- | --- |
| **Thread-Per-Message** | 为每个任务分配独立线程 | 线程创建与销毁开销、OOM 风险 |
| **Worker Thread** | 固定数量线程重复处理任务 | 任务间依赖导致的死锁问题 |
| **生产者-消费者** | 通过队列解耦生产与消费 | 队列容量与速度匹配 |

三种模式的实践要点：

- **Thread-Per-Message 模式**：适用于**并发度不高**的场景，需注意线程创建、销毁开销以及是否会导致 OOM
- **Worker Thread 模式**：需注意**死锁问题**，提交的任务之间不要存在依赖性
- **生产者-消费者模式**：可直接使用**线程池**来实现，简化开发

### 5.2 Thread-Per-Message 模式

**Thread-Per-Message 模式**为每个任务分配独立线程，是**最简单直观**的分工方法。网络服务端是其典型应用场景：为每个客户端请求创建独立线程，处理完成后自动销毁。

<img src="/imgs/ccrcy-01-pattern/95d52b489c769503db8595b0b27ff444_MD5.jpg" style="display: block; width: 100%;" alt="Thread-Per-Message模式：为每个任务分配独立线程">

下面通过一个简单的服务端示例来演示：

```java
final ServerSocketChannel ssc = ServerSocketChannel.open().bind(new InetSocketAddress(8080)); // 处理请求

try {
    while (true) {
        // 接收请求
        SocketChannel sc = ssc.accept();
        // 每个请求都创建一个线程
        new Thread(() -> {
            try {
                // 读Socket
                ByteBuffer rb = ByteBuffer.allocateDirect(1024);
                sc.read(rb);
                // 模拟处理请求
                Thread.sleep(2000);
                // 写Socket
                ByteBuffer wb = (ByteBuffer) rb.flip();
                sc.write(wb);
                // 关闭Socket
                sc.close();
            } catch (Exception e) {
                throw new UncheckedIOException(e);
            }
        }).start();
    }
} finally {
    ssc.close();
}
```

**Java 环境下的局限性：**

| 局限维度 | 具体问题 | 影响 |
| --- | --- | --- |
| **创建开销大** | 线程属于重量级对象，创建过程耗时 | 增加响应延迟 |
| **内存占用高** | 每个线程需要分配独立的内存空间 | 限制并发规模 |

由于上述局限，Java 中为每个请求创建新线程**不适合高并发场景**。实际开发中可使用**线程池**等工具类来优化。

**其他语言实践：**

相比之下，Go 等语言基于**轻量级线程**（goroutine）实现，其创建开销和内存占用都远小于 Java 线程，因此更适合采用 Thread-Per-Message 模式。

**适用场景：**

该模式适用于**并发度要求不高**的异步场景，例如**定时任务**等。

### 5.3 Worker Thread 模式

#### (1) 模式介绍

**Worker Thread 模式**通过**线程复用**避免频繁创建和销毁线程的资源消耗。该模式借鉴车间的工作方式：预先创建固定数量的工作线程，任务到达时由空闲线程处理，无任务时线程处于等待状态。

#### (2) 模式实现

**Worker Thread 模式**的**核心思想**是**线程复用**：预先创建固定数量的工作线程，任务到达时由空闲线程处理，处理完成后线程不销毁而是等待下一个任务。这种方式避免了频繁创建和销毁线程的开销。

以前面的网络服务端为例，使用**线程池**改进后的实现如下：

```java
ExecutorService es = Executors.newFixedThreadPool(200);
final ServerSocketChannel ssc = ServerSocketChannel.open().bind(new InetSocketAddress(8080)); // 处理请求

try {
    while (true) {
        // 接收请求
        SocketChannel sc = ssc.accept();

        // 将请求处理任务提交给线程池
        es.execute(() -> {
            try {
                // 读 Socket
                ByteBuffer rb = ByteBuffer.allocateDirect(1024);
                sc.read(rb);

                // 模拟处理请求
                Thread.sleep(2000);

                // 写 Socket
                ByteBuffer wb = (ByteBuffer) rb.flip();
                sc.write(wb);

                // 关闭 Socket
                sc.close();
            } catch (Exception e) {
                throw new UncheckedIOException(e);
            }
        });
    }
} finally {
    ssc.close();
    es.shutdown();
}
```

**改进要点：**

| 改进点 | 具体措施 |
| --- | --- |
| **线程复用** | 使用固定大小线程池（**200个线程**）处理请求，避免频繁创建销毁 |
| **资源控制** | 通过线程池大小限制并发数量，防止资源耗尽 |
| **优雅关闭** | 在 **finally** 块中调用 **shutdown()**，确保线程池正确关闭 |

#### (3) 应用场景

Worker Thread 模式适用于需要**重复处理大量任务**的场景，其优势主要体现在**线程复用**和**并发控制**两个方面。Java 标准库中的**线程池**机制已内置了该模式，在实际开发中推荐直接使用：

| 价值维度 | 具体说明 |
| --- | --- |
| **性能优化** | 避免频繁创建和销毁线程，降低系统开销 |
| **资源管控** | 限制线程数量，防止资源耗尽和系统过载 |
| **编码规范** | 企业级应用推荐使用线程池，而非手动创建线程 |

### 5.4 生产者-消费者模式

#### (1) 模式介绍

**生产者-消费者模式**通过**共享队列**实现生产与消费的解耦：生产者线程负责将任务添加到队列，消费者线程负责从队列取出并执行任务。队列作为**缓冲区**，协调两者速度差异。

<img src="/imgs/ccrcy-01-pattern/398fe1d380d92574aafd0554e9721c2e_MD5.jpg" style="display: block; width: 100%;" alt="生产者-消费者模式：通过共享队列解耦生产与消费">

下面通过 **ArrayBlockingQueue** 演示该模式的实现：

```java
public class BlockingQueueExample {
    private static final int QUEUE_CAPACITY = 5;
    private static final int PRODUCER_DELAY_MS = 1000;
    private static final int CONSUMER_DELAY_MS = 2000;

    public static void main(String[] args) throws InterruptedException {
        // 创建一个容量为QUEUE_CAPACITY的阻塞队列
        BlockingQueue<String> queue = new ArrayBlockingQueue<>(QUEUE_CAPACITY);

        // 创建一个生产者线程
        Runnable producer = () -> {
            while (true) {
                try {
                    // 在队列满时阻塞
                    queue.put("producer");
                    System.out.println("生产了一个元素，队列中元素个数：" + queue.size());
                    Thread.sleep(PRODUCER_DELAY_MS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        new Thread(producer).start();

        // 创建一个消费者线程
        Runnable consumer = () -> {
            while (true) {
                try {
                    // 在队列为空时阻塞
                    String element = queue.take();
                    System.out.println("消费了一个元素，队列中元素个数：" + queue.size());
                    Thread.sleep(CONSUMER_DELAY_MS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        new Thread(consumer).start();
    }
}
```

**代码说明：**

- **ArrayBlockingQueue**：基于数组的**有界阻塞队列**，队列满时 `put()` 操作阻塞，队列空时 `take()` 操作阻塞
- **生产者线程**：每隔 **PRODUCER_DELAY_MS** 毫秒向队列添加一个元素
- **消费者线程**：每隔 **CONSUMER_DELAY_MS** 毫秒从队列取出一个元素
- **阻塞机制**：当队列满时，生产者**阻塞等待**；当队列空时，消费者**阻塞等待**

#### (2) 模式优点

##### ① 支持异步处理

**典型场景**：用户注册成功后，系统需要发送注册邮件和短信通知。

**传统实现方式**：

| 方式 | 执行特点 | 主要问题 |
| --- | --- | --- |
| **串行执行** | 按顺序依次完成邮件和短信发送 | 响应时间过长，用户体验差 |
| **并行执行** | 使用多线程同时处理两种通知 | 线程创建和管理开销大 |


<img src="/imgs/ccrcy-01-pattern/d5dde41d9077086b83ef64ced7825ce3_MD5.jpg" style="display: block; width: 100%;" alt="串行执行与并行执行对比">

引入消息队列后，将非必需的业务逻辑转为异步处理：

<img src="/imgs/ccrcy-01-pattern/7f0119cf1a5fa2048d4cb898bc960a96_MD5.jpg" style="display: block; width: 100%;" alt="异步处理：通过消息队列实现异步通知">

##### ② 解耦

**典型场景**：用户下单后，订单系统需要通知库存系统扣减库存。

<img src="/imgs/ccrcy-01-pattern/fa8dc767c48792d3d74ac87f5eac9f50_MD5.jpg" style="display: block; width: 100%;" alt="系统解耦：通过队列实现订单系统与库存系统解耦">

订单系统只需将订单消息写入队列，由库存系统自主消费，无需直接调用库存接口。

##### ③ 消除速度差异

<img src="/imgs/ccrcy-01-pattern/eb0cbdfaf789945095f15662a224463c_MD5.jpg" style="display: block; width: 100%;" alt="削峰填谷：队列缓冲平衡生产与消费速度差异">

**优化线程资源**

线程数量过多会增加 CPU 上下文切换的成本。生产者-消费者模式通过固定数量的消费者线程处理任务，避免了线程的过度创建。

| 模式对比 | 线程数量特征 | CPU 开销 |
| --- | --- | --- |
| **直接并发处理** | 线程数随任务数线性增长 | 高 |
| **生产者-消费者模式** | 消费者线程数量固定 | 低 |

**实现削峰填谷**

业务高峰期，生产者产生任务的速度快于消费者的处理速度，队列在二者之间起到缓冲作用，平衡了生产与消费的速度差异。

<img src="/imgs/ccrcy-01-pattern/e3e6b00d10794fffee18e0eac5c5d9bb_MD5.jpg" style="display: block; width: 100%;" alt="队列缓冲作用：平衡生产与消费速度差异">

#### (3) 过饱问题处理

生产环境中可能出现**生产速度持续高于消费速度**的情况，导致任务在阻塞队列中持续堆积，最终队列被填满。

那么，**消费者速度高于生产者**是否就能避免过饱？

<img src="/imgs/ccrcy-01-pattern/fd391546bf1d71c78ea5be73ebe86bf9_MD5.jpg" style="display: block; width: 100%;" alt="消费者速度高于生产者的过饱判断">

**判断标准**：系统能够在**业务容忍的最长响应时间**内处理完堆积任务，即不算过饱。

**业务容忍的最长响应时间**是指系统可接受的延迟上限。例如数据统计系统需要在前一天生成报表供次日查看，如果前一天的数据到次日还未处理完，就超出了业务容忍范围。这类系统需要保证消费者在 **24 小时内**的消费能力高于生产者。

针对不同场景的处理策略：

| 场景 | 问题特征 | 处理策略 | 原因分析 |
| --- | --- | --- | --- |
| **消费者能力不足** | 日消费量 < 日生产量（如生产 1 万条/日，消费 5 千条/日） | **消费者扩容** | 必须在当日处理完，无法通过限流解决，只能提升消费能力 |
| **高峰期队列满** | 日消费量 > 日生产量，但高峰期生产速度过快 | **加大队列容量** | 消费者日均能力已满足，只需缓冲高峰流量，避免队列瞬时填满 |
| **队列容量受限** | 日消费量 > 日生产量，但队列容量受限（资源或条件限制） | **生产者限流** | 消费能力足够但队列容量不足，只能限制生产速度，降低高峰填充速率 |
<img src="/imgs/ccrcy-01-pattern/292709543e0abfe57d0da8f1288f0172_MD5.jpg" style="display: block; width: 100%;" alt="过饱问题处理策略：消费者扩容、加大队列容量、生产者限流">

## 6. 总结

本文系统讲解 **Java 并发编程设计模式**，通过**线程终止**、**避免共享**、**线程协作**、**线程分工**四类模式，帮助读者：

| 学习层次 | 核心收获 |
| ---- | ---- |
| **理解并发设计思想** | 掌握**两阶段终止**、**不变性**、**写时复制**、**ThreadLocal**、**Guarded Suspension**、**Balking**、**Worker Thread**、**生产者-消费者**八种设计模式的适用场景与实现要点 |
| **提升并发编程能力** | 能够根据业务特点选择合适的设计模式，处理**线程优雅退出**、**资源竞争**、**线程间协作**、**并发任务处理**等常见并发问题 |
| **规避并发编程陷阱** | 理解**标志位与中断双重检查**、**final 属性不可变性边界**、**ThreadLocal 内存泄漏**、**队列过饱和**等典型问题及其处理方法 |
