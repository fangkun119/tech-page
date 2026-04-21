---
title: Java并发编程 03：Completable Future
author: fangkun119
date: 2025-10-06 12:00:00 +0800
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


## 1. 文档概要

本文系统讲解 **Java 异步编程技术演进**，聚焦以下**核心内容**：

| 知识模块 | 说明 |
| ---- | ---- |
| **接口对比** | **Runnable** 与 **Callable** 的**功能差异**及**使用场景** |
| **Future 接口** | **任务管理能力**：**取消**、**状态查询**、**结果获取** |
| **FutureTask 实践** | **基础用法**与**商品信息查询案例** |
| **Future 局限性** | **阻塞获取**、**缺乏链式调用**、**无法组合任务** |
| **CompletableFuture 进阶** | **异步任务编排**：**依赖关系**、**AND 聚合**、**OR 聚合**、**并行执行** |
| **实战案例** | **烧水泡茶程序**对比 **Future** 与 **CompletableFuture** 实现 |

> 本文从**接口对比**到**实战应用**，**系统阐述** **Java 异步编程**从 **Future** 到 **CompletableFuture** 的**技术演进**，帮助读者掌握**异步任务编排**的**方法论**。

## 2. Runnable 与 Callable

### 2.1 接口定义

**Java** 提供了多种**创建线程**的方式，其中直接继承 **Thread** 或实现 **Runnable 接口**是最常见的两种方法。这两种方式存在一个**共同限制**：**无法获取任务执行结果**。

针对这一需求，**Java 1.5** 引入了 **Callable 接口**。**Callable** 与 **Future**、**FutureTask** 配合使用，能够支持**带返回值的异步任务执行**。

**接口定义对比：**

```java
@FunctionalInterface
public interface Runnable {
    public abstract void run();
}

@FunctionalInterface
public interface Callable<V> {
    V call() throws Exception;
}
```

### 2.2 对比

**Runnable** 和 **Callable** 在功能设计上存在明显差异。下面从**接口能力**和**使用场景**两个维度进行对比。

| 对比维度     | Runnable               | Callable                    |
| -------- | ---------------------- | --------------------------- |
| **返回值**  | **无返回值**                   | **支持返回值**                       |
| **异常处理** | **不能抛出** checked Exception | **可以声明抛出** checked Exception    |
| **方法签名** | `void run()`           | `V call() throws Exception` |

从功能角度看，**Callable** 相比 **Runnable** 具有以下**增强能力**：

| 能力维度 | 说明 |
|---------|------|
| **返回值支持** | `call()` 方法可以**返回执行结果**，便于**获取任务处理数据** |
| **异常处理** | 能够**声明抛出受检异常**，**调用方**可以**捕获并处理任务执行中的异常** |
| **任务管理** | 配合 **Future 接口**，可以**查询任务执行状态**、**取消任务执行**或**获取执行结果** |

```java
new Thread(new Runnable() {
    @Override
    public void run() {
        System.out.println("通过Runnable方式执行任务");
    }
}).start();

FutureTask task = new FutureTask(new Callable() {
    @Override
    public Object call() throws Exception {
        System.out.println("通过Callable方式执行任务");
        Thread.sleep(3000);
        return "返回任务结果";
    }
});

new Thread(task).start();
System.out.println(task.get());
```

## 3. Future 接口详解

### 3.1 核心功能

**Future 接口**用于管理 **Runnable** 或 **Callable** 任务的**执行过程**，提供了**三项基本能力**：

| 功能 | 说明 |
|------|------|
| **取消任务** | **中止正在执行的任务** |
| **查询状态** | **检查任务是否完成或取消** |
| **获取结果** | 通过 **`get()`** 方法**阻塞等待任务完成并获取结果** |

### 3.2 API 方法

#### (1) 介绍

**Future 接口**提供了 **5 个方法**，可以分为**任务控制方法**和**结果获取方法**两类。

<img src="/imgs/ccrcy-03-completable-future/c8aee4be2a26c5c14ab3550cbc063369_MD5.jpg" style="display: block; width: 600px;" alt="Future API 方法">

<!-- Future接口的5个方法分类图示：左侧显示任务控制方法（cancel取消任务、isCancelled判断是否取消、isDone判断是否完成），右侧显示结果获取方法（get()阻塞获取结果、get(timeout, unit)带超时的获取结果）。图中通过颜色和图标区分两类方法的功能，展示了Future接口用于异步任务控制与结果获取的核心API设计 -->

#### (2) 任务取消与状态查询

| 方法 | 说明 |
|------|------|
| **`boolean cancel(boolean mayInterruptIfRunning)`** | **取消任务执行**。参数 **`mayInterruptIfRunning`** 决定是否**中断正在执行的任务** |
| **`boolean isCancelled()`** | **判断任务是否已被取消**。任务正常完成前被取消则返回 `true` |
| **`boolean isDone()`** | **判断任务是否已完成**。任务正常结束、抛出异常或被取消时均返回 `true` |

#### (3) 结果获取

| 方法 | 说明 |
|------|------|
| **`V get()`** | **阻塞等待任务完成并获取结果**。可能抛出 **`InterruptedException`**（**线程中断**）、**`ExecutionException`**（**任务异常**）、**`CancellationException`**（**任务被取消**） |
| **`V get(long timeout, TimeUnit unit)`** | **带超时的阻塞等待**。**`timeout`** 指定超时时长，**`unit`** 指定时间单位。超时后抛出 **`TimeoutException`** |

## 4. FutureTask 使用详解

### 4.1 核心机制

**Future 接口**的实际实现类是 **FutureTask**。从使用角度看，**FutureTask** 连接了**任务的执行**和**结果的获取**：

| 角色 | 说明 |
|------|------|
| **执行端**（**生产者**） | **FutureTask** 存储 **Callable** 任务的处理结果，并维护**任务状态**（**未开始**、**正在处理**、**已完成**） |
| **获取端**（**消费者**） | 通过 **Future 接口**可以**非阻塞查询任务状态**，或**阻塞等待任务完成**并获取结果 |

**FutureTask** 的一个关键特性是**双重身份**：它既可以作为 **Runnable** 传递给 **Thread** 或**线程池**执行任务，又可以作为 **Future** 获取 **Callable** 的**返回结果**。

<img src="/imgs/ccrcy-03-completable-future/5f30429e814b89dda004de43825e7ab3_MD5.jpg" style="display: block; width: 100%;" alt="FutureTask 双重身份">

<!-- FutureTask具有双重角色：既是Runnable可提交给线程池执行（生产者端），又是Future可供其他线程调用获取结果（消费者端）。它实现了异步任务的"提交-执行-结果获取"闭环，通过实现Runnable接口被Thread或ExecutorService执行，同时实现Future接口提供get()、isDone()等方法供调用者获取结果与阻塞等待。 -->

将 **`Callable` 实例**作为**构造参数**创建 **`FutureTask` 对象**，将其作为 **`Runnable`** 提交到**线程池**或**线程**执行，最后通过 **`FutureTask`** 获取**执行结果**。

### 4.2 基础示例

本示例演示 **FutureTask** 的**基本用法**，计算 **0 到 99 的整数和**，展示 **三个关键步骤**：

1. **创建 FutureTask**：将 **Callable 实例**作为**构造参数**
2. **启动任务**：将 **FutureTask** 作为 **Runnable** 传递给 **Thread** 执行
3. **获取结果**：通过 **get()** 方法**阻塞等待**任务完成并获取**返回值**

```java
public class FutureTaskDemo {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        Task task = new Task();
        // 构建FutureTask：包裹Task对象，并通过RunnableFuture同时实现Runnable和Future接口
        FutureTask<Integer> futureTask = new FutureTask<>(task);
        // 作为Runnable对象入参：创建线程并启动
        new Thread(futureTask).start();
        // 作为Future对象，获取线程执行结果
        System.out.println("task运行结果："+futureTask.get());
    }

    static class Task implements Callable<Integer> {
        @Override
        public Integer call() throws Exception {
            System.out.println("子线程正在计算");
            int sum = 0;
            for (int i = 0; i < 100; i++) {
                sum += i;
            }
            return sum;
        }
    }
}
```

代码执行时，**主线程**会调用 `futureTask.get()` **阻塞等待**，直到 **子线程** 完成计算并返回 **执行结果**。

### 4.3 实战案例：商品信息查询

**促销活动**维护中需要查询**商品信息**，包括**商品基本信息**、**价格**、**库存**、**图片**和**销售状态**等。这些信息分布在不同的**业务中心**，由**独立系统**提供服务。

采用**同步方式**时，假设单个接口响应时间为 **50ms**，完成全部查询需要 **200-300ms**。通过 **Future** **并行执行**多个查询任务，**总耗时**降低至单个服务的响应时间，约 **50ms**。

<img src="/imgs/ccrcy-03-completable-future/bf4f94049dcee154da78a6e4e62b4c07_MD5.jpg" style="display: block; width: 100%;" alt="商品信息查询并行执行">

<!-- 该图片展示了同步串行与并行执行的性能对比。左侧同步串行方式按顺序依次执行5个查询任务（商品基本信息、价格、库存、图片、销售状态），每个任务50ms，总耗时约250ms；右侧并行执行通过Future将5个查询任务同时执行，所有任务并行无需等待，总耗时降低至约50ms（单个任务的响应时间），性能提升5倍，体现了Future在商品信息查询场景中的并发优化价值 -->

```java
public class FutureTaskDemo2 {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        FutureTask<String> ft1 = new FutureTask<>(new T1Task());
        FutureTask<String> ft2 = new FutureTask<>(new T2Task());
        FutureTask<String> ft3 = new FutureTask<>(new T3Task());
        FutureTask<String> ft4 = new FutureTask<>(new T4Task());
        FutureTask<String> ft5 = new FutureTask<>(new T5Task());
        //构建线程池
        ExecutorService executorService = Executors.newFixedThreadPool(5);
        executorService.submit(ft1);
        executorService.submit(ft2);
        executorService.submit(ft3);
        executorService.submit(ft4);
        executorService.submit(ft5);
        //获取执行结果
        System.out.println(ft1.get());
        System.out.println(ft2.get());
        System.out.println(ft3.get());
        System.out.println(ft4.get());
        System.out.println(ft5.get());
        executorService.shutdown();
    }
    static class T1Task implements Callable<String> {
        @Override
        public String call() throws Exception {
            System.out.println("T1:查询商品基本信息...");
            TimeUnit.MILLISECONDS.sleep(50);
            return "商品基本信息查询成功";
        }
    }
    static class T2Task implements Callable<String> {
        @Override
        public String call() throws Exception {
            System.out.println("T2:查询商品价格...");
            TimeUnit.MILLISECONDS.sleep(50);
            return "商品价格查询成功";
        }
    }
    static class T3Task implements Callable<String> {
        @Override
        public String call() throws Exception {
            System.out.println("T3:查询商品库存...");
            TimeUnit.MILLISECONDS.sleep(50);
            return "商品库存查询成功";
        }
    }
    static class T4Task implements Callable<String> {
        @Override
        public String call() throws Exception {
            System.out.println("T4:查询商品图片...");
            TimeUnit.MILLISECONDS.sleep(50);
            return "商品图片查询成功";
        }
    }
    static class T5Task implements Callable<String> {
        @Override
        public String call() throws Exception {
            System.out.println("T5:查询商品销售状态...");
            TimeUnit.MILLISECONDS.sleep(50);
            return "商品销售状态查询成功";
        }
    }
}
```

## 5. Future 的局限性

**Future** 表示**异步计算**的结果，提供了 **`isDone()`** 检测计算状态、**`get()`** 获取计算结果等能力。处理**单个异步任务**时，**Future** 可以满足需求。但在**实际业务场景**中，**多个异步任务**之间往往存在**依赖**、**并行**或**聚合关系**，**Future** 在这些场景下存在**以下局限**：

<img src="/imgs/ccrcy-03-completable-future/350d9bc6b458c9ed5d92b2ca83822866_MD5.jpg" style="display: block; width: 100%;" alt="Future 的局限性">

<!-- 该图片展示了Java Future接口的四大核心局限性：1) 阻塞式获取结果 - get()方法会阻塞线程直到任务完成；2) 缺乏链式调用能力 - 无法在任务完成后自动触发后续操作；3) 无法组合多个任务 - 不支持多个Future之间的依赖、并行或聚合关系；4) 异常处理复杂 - 异常被包装在ExecutionException中，需要通过getCause()获取原始异常。这些局限性在实际业务场景中限制了Future的实用性，特别是在需要处理多个异步任务依赖关系时。 -->

**详细说明如下：**

| 局限性 | 说明 |
| --- | --- |
| **阻塞式获取结果** | **`get()`** 方法会**阻塞当前线程**直到任务完成。**并发执行多个任务**时，线程在**等待期间**无法执行其他操作 |
| **缺乏链式调用能力** | 无法在任务完成后**自动触发后续动作**。例如：计算任务完成后发送邮件，需要**手动编码**实现**回调逻辑** |
| **无法组合多个任务** | 缺少**组合多个任务**的能力。例如：等待 **10 个任务**全部完成后执行特定动作，**Future** 无法直接支持 |
| **异常处理机制复杂** | **`get()`** 方法将异常包装在 **`ExecutionException`** 中，需要调用 **`e.getCause()`** 才能获取**原始异常**，增加了**错误处理**的复杂度 |

**异常处理示例：**

```java
try {
    String result = future.get();  // **阻塞获取结果**
} catch (InterruptedException e) {
    Thread.currentThread().interrupt();  // **恢复中断状态**
} catch (ExecutionException e) {
    // 需要手动解包才能获取**原始异常**
    Throwable cause = e.getCause();
    if (cause instanceof IOException) {
        // 处理**原始异常**
    }
}
```

后续介绍 **CompletableFuture** 时，将展示更清晰简洁的**异常处理方式**。

## 6. CompletableFuture使用详解

### 6.1 功能增强

处理**单个异步任务**时，**Future** 可以满足**基本需求**。**实际业务**中，**多个异步任务**之间往往存在**依赖关系**、**并行或聚合关系**，手动使用 **Future** 实现**这些逻辑**会比较**复杂**。

**CompletableFuture** 在 **Future 接口**基础上进行了**扩展**，主要增强了以下**三大核心能力**：

<img src="/imgs/ccrcy-03-completable-future/13ce7ddbc9b37c8dc40f183c0394d200_MD5.jpg" style="display: block; width: 100%;" alt="CompletableFuture 能力增强">

<!-- CompletableFuture组件三大核心能力增强:1)声明式任务编排-支持AND/OR逻辑编排及组合式任务编排,解决传统Future只能处理单个任务的限制;2)链式流式调用-提供流式API支持任务结果传递与组合,实现更自然的异步调用链;3)内聚异常处理-通过统一异常处理策略,实现异常集中拦截与恢复,提升代码可维护性 -->

<!--
| 能力维度     | 说明                     |
| -------- | ---------------------- |
| **任务编排** | 支持声明式地组织多个任务的执行顺序和依赖关系 |
| **链式调用** | 提供流畅的 API，支持任务之间的串联和组合 |
| **异常处理** | 内置异常处理机制，无需额外编码        |

相比传统的 **CountDownLatch** 等工具类，**CompletableFuture** 通过**链式调用**简化了**任务编排**逻辑，**代码更易于维护**。
-->

### 6.2 任务编排

#### (1) 方法概览

**CompletableFuture** 根据任务间的依赖和聚合关系，提供了丰富的方法来组织异步任务的执行顺序：

| 关系类型         | 方法               | 说明                      |
| ------------ | ---------------- | ----------------------- |
| **依赖关系**     | **thenApply()**      | 将前一个任务的结果传递给后续处理函数      |
|              | **thenCompose()**    | 连接两个有依赖关系的任务，返回第二个任务的结果 |
| **AND 聚合关系** | **thenCombine()**    | 合并两个任务的结果，有返回值          |
|              | **thenAcceptBoth()** | 消费两个任务的结果，无返回值          |
|              | **runAfterBoth()**   | 两个任务完成后执行指定操作           |
| **OR 聚合关系**  | **applyToEither()**  | 使用最先完成的任务结果进行转换         |
|              | **acceptEither()**   | 消费最先完成的任务结果             |
|              | **runAfterEither()** | 任意任务完成后执行指定操作           |
| **并行执行**     | **anyOf()**          | 等待多个任务中任意一个完成           |
|              | **allOf()**          | 等待多个任务全部完成              |
#### (2) 依赖关系

当一个任务的输出作为另一个任务的输入时，两个任务形成链式依赖关系。**`thenApply`** 和 **`thenCompose`** 用于编排这种依赖关系，实现任务间的结果传递。

<img src="/imgs/ccrcy-03-completable-future/4a2a57fbd5f54582c30aa8e03395e89e_MD5.jpg" style="display: block; width: 100%;" alt="依赖关系编排">

<!-- CompletableFuture依赖关系编排对比示意图：左侧展示thenApply（同步转换），接收Function参数，将CompletableFuture<T>的结果同步映射为CompletableFuture<U>，适用于内部计算无需异步的场景；右侧展示thenCompose（链式异步展开），接收返回CompletionStage的Function参数，将CompletableFuture<T>的结果异步映射为新的CompletableFuture<U>，实现真正的异步依赖关系编排（类似FlatMap，支持异步任务的链式调用） -->

**使用对比：**

```java
// 注意：未指定 Executor 时使用 ForkJoinPool.commonPool()
// thenApply：**同步转换**（函数返回普通值）
CompletableFuture<Integer> result1 = CompletableFuture
    .supplyAsync(() -> 100)
    .thenApply(x -> x * 2);  // 100 -> 200

// thenCompose：**异步任务链**（函数返回 CompletableFuture）
CompletableFuture<Integer> result2 = CompletableFuture
    .supplyAsync(() -> 100)
    .thenCompose(x ->        // 避免**嵌套结构**
        CompletableFuture.supplyAsync(() -> x * 2)  // 100 -> 200
    );
```

**核心区别**：**`thenApply`** 适用于同步转换，**`thenCompose`** 适用于异步任务链，后者能避免 **`CompletableFuture<CompletableFuture<T>>`** 嵌套结构。

#### (3) AND 聚合

当需要等待两个独立任务全部完成后再进行后续操作时，形成 **AND 聚合关系**。**`thenCombine`**、**`thenAcceptBoth`**、**`runAfterBoth`** 用于编排这种聚合关系，实现任务间的结果聚合。

<img src="/imgs/ccrcy-03-completable-future/a401e452fc39e03296dfe034fc918697_MD5.jpg" style="display: block; width: 100%;" alt="AND 聚合关系编排">

<!-- AND 聚合关系编排：展示 CompletableFuture 中如何实现两个并行任务的 AND 聚合关系。图中显示两个任务（task1 和 task2）并行执行，需要等待两者全部完成后再进行后续操作。提供了三种方法：thenCombine（合并两个任务的结果并返回新值）、thenAcceptBoth（消费两个任务的结果但无返回值）、runAfterBoth（两个任务完成后执行操作，不访问结果）。 -->

**使用对比：**

```java
// 注意：未指定 Executor 时使用 ForkJoinPool.commonPool()
CompletableFuture<Integer> task1 = CompletableFuture.supplyAsync(() -> 100);
CompletableFuture<Integer> task2 = CompletableFuture.supplyAsync(() -> 200);

// thenCombine：**合并两个任务的结果**，有返回值
CompletableFuture<Integer> result1 = task1
    .thenCombine(task2, (x, y) -> x + y);  // 返回 300

// thenAcceptBoth：**消费两个任务的结果**，无返回值
CompletableFuture<Void> result2 = task1
    .thenAcceptBoth(task2, (x, y) -> {
        System.out.println("求和：" + (x + y));  // 打印但不返回
    });

// runAfterBoth：**两个任务完成后执行指定操作**，不关心结果
CompletableFuture<Void> result3 = task1
    .runAfterBoth(task2, () -> {
        System.out.println("两个任务都完成了");  // 无法访问结果
    });
```

**核心区别**：**`thenCombine`** 需要两个结果并返回新值，**`thenAcceptBoth`** 消费结果但无返回值，**`runAfterBoth`** 仅在完成后触发操作，不访问结果。

#### (4) OR 聚合

当两个任务并行执行，只需要任意一个完成即可继续时，形成 **OR 聚合关系**。**`applyToEither`**、**`acceptEither`**、**`runAfterEither`** 用于编排这种聚合关系，使用最先完成的任务结果。

<img src="/imgs/ccrcy-03-completable-future/051949bbf2988d2e3839f8d99f2fdf51_MD5.jpg" style="display: block; width: 100%;" alt="OR 关系编排">

<!-- 展示 CompletableFuture 的 OR 聚合关系编排：两个并行任务 task1 和 task2 同时执行，只需任意一个完成即可继续，使用最先完成任务的结果进行后续操作。适用于竞速场景，如从多个数据源获取数据，使用最快返回的结果。 -->

**使用对比：**

```java
// 注意：未指定 Executor 时使用 ForkJoinPool.commonPool()
CompletableFuture<Integer> task1 = CompletableFuture.supplyAsync(() -> {
    try {
        TimeUnit.SECONDS.sleep(3);  // 模拟耗时操作
    } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
    }
    return 100;
});

CompletableFuture<Integer> task2 = CompletableFuture.supplyAsync(() -> {
    try {
        TimeUnit.SECONDS.sleep(1);  // 更快完成
    } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
    }
    return 200;
});

// applyToEither：**使用最快结果进行转换**，有返回值
CompletableFuture<Integer> result1 = task1
    .applyToEither(task2, x -> x * 2);  // 使用 task2 的 200，返回 400

// acceptEither：**消费最快结果**，无返回值
CompletableFuture<Void> result2 = task1
    .acceptEither(task2, x -> {
        System.out.println("最快结果：" + x);  // 打印 200
    });

// runAfterEither：**任意任务完成后执行操作**，不访问结果
CompletableFuture<Void> result3 = task1
    .runAfterEither(task2, () -> {
        System.out.println("有一个任务完成了");  // 不知道是哪个结果
    });
```

**核心区别**：**`applyToEither`** 使用最快结果并返回新值，**`acceptEither`** 消费最快结果但无返回值，**`runAfterEither`** 仅在任意完成后触发操作，不访问结果。

#### (5) 并行等待

当需要等待多个任务满足特定完成条件（任意一个或全部完成）时，形成并行关系。**`anyOf`** 和 **`allOf`** 用于编排这种并行关系，实现任务的批量等待。

<img src="/imgs/ccrcy-03-completable-future/a0811721de5453a470437ab5a77389de_MD5.jpg" style="display: block; width: 100%;" alt="并行关系">

<!-- CompletableFuture并行关系对比图：左侧展示allOf()方法，需要等待所有任务（任务1、任务2、任务3）全部完成才能继续执行；右侧展示anyOf()方法，只要有任意一个任务完成就可以继续执行。allOf适用于需要聚合多个异步结果的场景，anyOf适用于竞速场景或只需要任一结果的情况。 -->

**使用对比：**

```java
// 注意：未指定 Executor 时使用 ForkJoinPool.commonPool()
CompletableFuture<String> task1 = CompletableFuture.supplyAsync(() -> {
    try {
        TimeUnit.SECONDS.sleep(2);
    } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
    }
    return "任务1完成";
});

CompletableFuture<String> task2 = CompletableFuture.supplyAsync(() -> {
    try {
        TimeUnit.SECONDS.sleep(1);
    } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
    }
    return "任务2完成";
});

CompletableFuture<Integer> task3 = CompletableFuture.supplyAsync(() -> {
    try {
        TimeUnit.SECONDS.sleep(3);
    } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
    }
    return 100;
});

// anyOf：**等待多个任务中任意一个完成**，返回最先完成的结果
// 注意：由于多个任务的结果类型可能不同（String、Integer等），返回类型为 CompletableFuture<Object>
CompletableFuture<Object> result1 = CompletableFuture.anyOf(task1, task2, task3);
// result1.get() 返回 "任务2完成"（最快完成者），需要类型判断或转换

// allOf：**等待多个任务全部完成**，返回 CompletableFuture<Void>
CompletableFuture<Void> result2 = CompletableFuture.allOf(task1, task2, task3);
result2.join();  // 阻塞直到所有任务完成
// 此时可访问各个任务的结果：task1.get(), task2.get(), task3.get()
```

**核心区别**：**`anyOf`** 等待任意一个完成并返回该结果（适用于竞速场景），**`allOf`** 等待全部完成但无返回值（适用于批量任务同步）。

### 6.3 创建异步任务

**CompletableFuture** 提供了**四个静态方法**来创建**异步任务**：

#### (1) 方法签名

| 方法 | 返回值 | 说明 |
|------|--------|------|
| **`runAsync()`** | `CompletableFuture<Void>` | 执行**无返回值**的异步任务 |
| **`runAsync()`** | `CompletableFuture<Void>` | 执行**无返回值**的异步任务（**指定线程池**） |
| **`supplyAsync()`** | `CompletableFuture<U>` | 执行**有返回值**的异步任务 |
| **`supplyAsync()`** | `CompletableFuture<U>` | 执行**有返回值**的异步任务（**指定线程池**） |

```java
public static CompletableFuture<Void> runAsync(Runnable runnable)
public static CompletableFuture<Void> runAsync(Runnable runnable, Executor executor)
public static <U> CompletableFuture<U> supplyAsync(Supplier<U> supplier)
public static <U> CompletableFuture<U> supplyAsync(Supplier<U> supplier, Executor executor)
```

#### (2) 方法特点

这四个方法的**核心差异**主要体现在以下**两个方面**：

| 对比维度 | **`runAsync()`** | **`supplyAsync()`** |
|---------|----------|-------------|
| **参数类型** | `Runnable`（**无返回值**） | `Supplier<U>`（**有返回值**） |
| **返回类型** | `CompletableFuture<Void>` | `CompletableFuture<U>` |

**线程池选择：**

- 未指定 `Executor` 时，使用 **`ForkJoinPool.commonPool()`** 作为**默认线程池**
- 指定 `Executor` 时，使用**自定义线程池**执行任务

**线程池配置建议：**

默认的 **`ForkJoinPool.commonPool()`** 线程数等于 **CPU 核数**（可通过 **JVM 参数** `-Djava.util.concurrent.ForkJoinPool.common.parallelism` 调整）。如果**多个任务共享**该线程池，一旦某任务执行**耗时的 I/O 操作**，可能导致**线程阻塞**并**影响其他任务执行**。因此，建议根据**业务类型**创建**独立的线程池**，**避免不同任务相互干扰**。

#### (3) 使用示例

```java
Runnable runnable = () -> System.out.println("执行无返回结果的异步任务");
CompletableFuture.runAsync(runnable);

CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
    System.out.println("执行有返回值的异步任务");
    try {
        Thread.sleep(5000);
    } catch (InterruptedException e) {
        e.printStackTrace();
    }
    return "Hello World";
});
String result = future.get();
System.out.println(result);
```

**执行结果：**

```bash
执行无返回结果的异步任务

执行有返回值的异步任务
```

上述示例展示了**两种创建异步任务的方式**：**`runAsync()`** 用于执行**无返回值**的任务，**`supplyAsync()`** 用于执行**有返回值**的任务。通过 **`future.get()`** 可以**阻塞获取**异步任务的执行结果。

### 6.4 结果获取

**`join()`** 和 **`get()`** 方法用于获取 **CompletableFuture 异步任务**的**执行结果**。两者在**异常处理方式**上存在**核心差异**：

<img src="/imgs/ccrcy-03-completable-future/0b563f8281c2079861aaf1bc6dc59124_MD5.jpg" style="display: block; width: 100%;" alt="join() 与 get() 对比">

<!-- 本图对比了 CompletableFuture 中获取任务结果的两种方法：join() 和 get()。核心区别：1. **join() 方法**：抛出未检查异常（unchecked exception），编译器不强制要求显式处理异常，使用更简便。2. **get() 方法**：抛出已检查异常（checked exception，如 ExecutionException、InterruptedException），必须显式处理异常（捕获或声明抛出），异常处理更严格。选择建议：如果不需要对异常进行细粒度控制，使用 join() 代码更为简洁。 -->

| 方法 | 异常类型 | 是否强制处理 | 说明 |
|------|---------|-------------|------|
| **`join()`** | **unchecked 异常** | **否** | 抛出**未经检查的异常**，**编译器不强制要求处理** |
| **`get()`** | **checked 异常** | **是** | 抛出 **`ExecutionException`**、**`InterruptedException`**，**需要显式处理** |

**实际开发**中，如果**不需要对异常进行细粒度控制**，使用 **`join()`** **代码更为简洁**。

### 6.5 结果处理

#### (1) **回调方法**

**任务完成**或**抛出异常**时，可以通过**回调方法**进行处理。**`whenComplete`** 用于处理**正常结果**和**异常**，**`exceptionally`** 专门用于**异常恢复**。

<img src="/imgs/ccrcy-03-completable-future/0a627df5c0745e5173cb1c13e474d65a_MD5.jpg" style="display: block; width: 100%;" alt="结果处理回调方法">

<!-- 该图片展示了一个Java程序的运行结果，演示了CompletableFuture的结果处理回调方法。程序输出了两行结果：第一行显示"当前线程名称：main"，第二行显示"上抛异常信息：java.lang.NullPointerException"。这展示了whenComplete方法的使用，该方法可以处理任务完成后的结果和异常，能够获取到任务执行过程中抛出的异常信息，体现了CompletableFuture的异常处理机制。 -->

**方法签名：**

```java
public CompletableFuture<T> whenComplete(BiConsumer<? super T,? super Throwable> action)
public CompletableFuture<T> whenCompleteAsync(BiConsumer<? super T,? super Throwable> action)
public CompletableFuture<T> whenCompleteAsync(BiConsumer<? super T,? super Throwable> action, Executor executor)
public CompletableFuture<T> exceptionally(Function<Throwable,? extends T> fn)
```

**执行特点：**

| 方法 | 执行线程 | 说明 |
|------|---------|------|
| **whenComplete** | **当前线程** | 使用完成任务的线程执行回调 |
| **whenCompleteAsync** | **异步线程** | 使用线程池中的其他线程执行回调 |
| **exceptionally** | **当前线程** | **仅在发生异常时触发**，可返回**替代值** |

**返回值**：方法返回 **`CompletableFuture`**，**回调执行**后**继续传递原始结果**或**异常**。

#### (2) **使用示例**

```java
CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
    try {
        TimeUnit.SECONDS.sleep(1);
    } catch (InterruptedException e) {
    }
    if (new Random().nextInt(10) % 2 == 0) {
        int i = 12 / 0;
    }
    System.out.println("执行结束！");
    return "test";
});
future.whenComplete(new BiConsumer<String, Throwable>() {
    @Override
    public void accept(String t, Throwable action) {
        System.out.println(t+" 执行完成！");
    }
});
future.exceptionally(new Function<Throwable, String>() {
    @Override
    public String apply(Throwable t) {
        System.out.println("执行失败：" + t.getMessage());
        return "异常xxxx";
    }
}).join();
```

**代码输出**

```bash
执行结束！

test 执行完成！

或者

执行失败：java.lang.ArithmeticException: / by zero

null 执行完成！
```

### 6.6 结果转换

#### (1) 转换说明

**结果转换**将上一阶段任务的执行结果作为下一阶段任务的输入，进行计算并产生新的结果。

#### (2) thenApply 方法

**thenApply** 接收一个函数作为参数，使用该函数处理上一个 **CompletableFuture** 的结果，并返回包含新结果的 **CompletableFuture** 对象。

```java
public <U> CompletableFuture<U> thenApply(Function<? super T,? extends U> fn)
public <U> CompletableFuture<U> thenApplyAsync(Function<? super T,? extends U> fn)
public <U> CompletableFuture<U> thenApplyAsync(Function<? super T,? extends U> fn, Executor executor)
```

```java
CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
    int result = 100;
    System.out.println("一阶段：" + result);
    return result;
}).thenApply(number -> {
    int result = number * 3;
    System.out.println("二阶段：" + result);
    return result;
});
System.out.println("最终结果：" + future.get());
```

代码输出

```bash
一阶段：100
二阶段：300
最终结果：300
```

#### (3) thenCompose 方法

**thenCompose** 的参数是一个返回 **CompletableFuture** 实例的函数，该函数以先前计算步骤的结果作为输入参数。

```java
public <U> CompletableFuture<U> thenCompose(Function<? super T, ? extends CompletionStage<U>> fn);
public <U> CompletableFuture<U> thenComposeAsync(Function<? super T, ? extends CompletionStage<U>> fn) ;
public <U> CompletableFuture<U> thenComposeAsync(Function<? super T, ? extends CompletionStage<U>> fn, Executor executor) ;
```

```java
CompletableFuture<Integer> future = CompletableFuture
    .supplyAsync(new Supplier<Integer>() {
        @Override
        public Integer get() {
            int number = new Random().nextInt(30);
            System.out.println("第一阶段：" + number);
            return number;
        }
    })
    .thenCompose(new Function<Integer, CompletionStage<Integer>>() {
        @Override
        public CompletionStage<Integer> apply(Integer param) {
            return CompletableFuture.supplyAsync(new Supplier<Integer>() {
                @Override
                public Integer get() {
                    int number = param * 2;
                    System.out.println("第二阶段：" + number);
                    return number;
                }
            });
        }
    });
System.out.println("最终结果: " + future.get());
```

代码输出

```bash
第一阶段：10

第二阶段：20

最终结果: 20
```

#### (4) 两者对比

**thenApply** 和 **thenCompose** 都用于结果转换，但处理方式不同：

**thenApply**：转换泛型类型，返回封装了转换结果的新 **CompletableFuture** 实例

```java
CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> "Hello");
CompletableFuture<String> result = future.thenApply(param -> param + " World");
```

**thenCompose**：将内部的 **CompletableFuture** 调用展开，使用上一个 **CompletableFuture** 的结果在下一步调用中进行运算，生成扁平化的新 **CompletableFuture**

```java
CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> "Hello");
CompletableFuture<String> result = future
    .thenCompose(param -> CompletableFuture.supplyAsync(() -> param + " World"));
```

**关键区别**：

| 对比维度 | thenApply | thenCompose |
|---------|-----------|-------------|
| **返回值结构** | 可能产生**嵌套**的 `CompletableFuture<CompletableFuture<T>>` | 始终返回**扁平化**的 `CompletableFuture<T>` |
| **适用场景** | **同步转换**，无需异步操作 | 需要**链式调用多个异步任务** |
| **函数参数** | 返回**普通值** | 返回 **CompletableFuture** 实例 |

下面通过示例对比两种方法的使用方式和结果：

```java
CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> "Hello");
// thenApply: 直接拼接字符串
CompletableFuture<String> result1 = future.thenApply(param -> param + " World");
// thenCompose: 通过异步任务拼接字符串
CompletableFuture<String> result2 = future
    .thenCompose(param -> CompletableFuture.supplyAsync(() -> param + " World"));
System.out.println(result1.get());
System.out.println(result2.get());
```

代码输出：

```bash
Hello World
Hello World
```

### 6.7 结果消费

#### (1) 消费说明

与**结果处理**和**结果转换**方法不同，**结果消费**方法只**执行操作**而**不返回新的计算值**。

根据对结果的处理方式，**结果消费**方法分为三类：

| 方法分类 | 说明 |
|---------|------|
| **thenAccept 系列** | **消费单个**异步任务的结果 |
| **thenAcceptBoth 系列** | **同时消费两个**异步任务的结果 |
| **thenRun 系列** | **不依赖结果**，仅**执行指定的操作** |

图形演示如下

#### (2) **thenAccept** 方法

**thenAccept** 方法接收 **Consumer** 函数式接口，对**异步任务的结果**进行**消费处理**。由于 **Consumer** 只有**输入参数**而**无返回值**，因此该方法返回 `CompletableFuture<Void>`。

```java
public CompletionStage<Void> thenAccept(Consumer<? super T> action);
public CompletionStage<Void> thenAcceptAsync(Consumer<? super T> action);
public CompletionStage<Void> thenAcceptAsync(Consumer<? super T> action,Executor executor);
```

**示例代码**：

```java
CompletableFuture<Void> future = CompletableFuture
    .supplyAsync(() -> {
        int number = new Random().nextInt(10);
        System.out.println("第一阶段：" + number);
        return number;
    })
    .thenAccept(number -> System.out.println("第二阶段：" + number * 5));
System.out.println("最终结果：" + future.get());
```

**代码输出**

```bash
第一阶段：8

第二阶段：40

最终结果：null
```

#### (3) **thenAcceptBoth** 方法

**thenAcceptBoth** 方法用于**等待两个异步任务全部完成后**，**同时消费它们的结果**。

```java
public <U> CompletionStage<Void> thenAcceptBoth(CompletionStage<? extends U> other,BiConsumer<? super T, ? super U> action);
public <U> CompletionStage<Void> thenAcceptBothAsync(CompletionStage<? extends U> other,BiConsumer<? super T, ? super U> action);
public <U> CompletionStage<Void> thenAcceptBothAsync(CompletionStage<? extends U> other,BiConsumer<? super T, ? super U> action, Executor executor);
```

**示例代码**：

```java
CompletableFuture<Integer> futrue1 = CompletableFuture.supplyAsync(new Supplier<Integer>() {
    @Override
    public Integer get() {
        int number = new Random().nextInt(3) + 1;
        try {
            TimeUnit.SECONDS.sleep(number);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("第一阶段：" + number);
        return number;
    }
});

CompletableFuture<Integer> future2 = CompletableFuture.supplyAsync(new Supplier<Integer>() {
    @Override
    public Integer get() {
        int number = new Random().nextInt(3) + 1;
        try {
            TimeUnit.SECONDS.sleep(number);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("第二阶段：" + number);
        return number;
    }
});

futrue1.thenAcceptBoth(future2, new BiConsumer<Integer, Integer>() {
    @Override
    public void accept(Integer x, Integer y) {
        System.out.println("最终结果：" + (x + y));
    }
}).join();
```

**代码输出**

```bash
第二阶段：1

第一阶段：2

最终结果：3
```

#### (4) **thenRun** 方法

**thenRun** 在**上一阶段异步任务完成后**执行**指定的 Runnable 操作**，**不依赖任务的结果**。

```java
public CompletionStage<Void> thenRun(Runnable action);
public CompletionStage<Void> thenRunAsync(Runnable action);
public CompletionStage<Void> thenRunAsync(Runnable action, Executor executor);
```

**示例代码**：

```java
CompletableFuture<Void> future = CompletableFuture.supplyAsync(() -> {
    int number = new Random().nextInt(10);
    System.out.println("第一阶段：" + number);
    return number;
}).thenRun(() -> System.out.println("thenRun 执行"));
System.out.println("最终结果：" + future.get());
```

**代码输出**

```bash
第一阶段：2

thenRun 执行

最终结果：null
```

### 6.8 结果组合

**thenCombine** 用于组合**两个独立任务**的结果。两个任务都完成后，它们的结果会作为参数传递给**指定函数**进行处理，最终返回**新结果**。

```java
public <U,V> CompletionStage<V> **thenCombine**(CompletionStage<? extends U> other,BiFunction<? super T,? super U,? extends V> fn);
public <U,V> CompletionStage<V> **thenCombineAsync**(CompletionStage<? extends U> other,BiFunction<? super T,? super U,? extends V> fn);
public <U,V> CompletionStage<V> **thenCombineAsync**(CompletionStage<? extends U> other,BiFunction<? super T,? super U,? extends V> fn,Executor executor);
```

```java
CompletableFuture<Integer> future1 = CompletableFuture
    .supplyAsync(new Supplier<Integer>() {
        @Override
        public Integer get() {
            int number = new Random().nextInt(10);
            System.out.println("第一阶段：" + number);
            return number;
        }
    });

CompletableFuture<Integer> future2 = CompletableFuture
    .supplyAsync(new Supplier<Integer>() {
        @Override
        public Integer get() {
            int number = new Random().nextInt(10);
            System.out.println("第二阶段：" + number);
            return number;
        }
    });

CompletableFuture<Integer> result = future1
    .thenCombine(future2, new BiFunction<Integer, Integer, Integer>() {
        @Override
        public Integer apply(Integer x, Integer y) {
            return x + y;
        }
    });

System.out.println("最终结果：" + result.get());
```

**代码输出**

```bash
第一阶段：9

第二阶段：5

最终结果：14
```

### 6.9 任务交互

#### (1) 交互说明

实际开发中，**多个任务**的执行时长各不相同。**任务交互**是指根据**任务完成**的先后顺序，选择相应的**处理策略**。

#### (2) **OR 聚合**：**任意完成**

某些场景下，只需要**多个任务**中的**任意一个**完成即可继续执行，无需等待**所有任务**。

<img src="/imgs/ccrcy-03-completable-future/12c276e259b56354ca59d81a7a6b8ff5_MD5.jpg" style="display: block; width: 100%;" alt="OR 聚合关系：任意完成">

<!-- CompletableFuture 的 OR 聚合关系示意图：展示了两个并行任务（Task 1 和 Task 2）竞争执行的场景。图中用蓝色箭头表示两个任务，绿色箭头表示"最快输出"，即任意一个任务先完成就立即使用其结果继续执行。右侧逻辑规则框说明了三种 OR 聚合方法：applyToEither（取最快结果进入转换函数）、acceptEither（取最快结果进入消费节点）、anyOf（N个任务竞争，绝对最快的决定最终Object结果）。这种机制适用于只需要任意一个任务完成即可继续执行的场景，无需等待所有任务完成。 -->

##### ① **applyToEither** 方法

当**两个任务**并行执行时，使用**先完成任务**的结果进行**转换**。

```java
public <U> CompletionStage<U> applyToEither(CompletionStage<? extends T> other,Function<? super T, U> fn);
public <U> CompletionStage<U> applyToEitherAsync(CompletionStage<? extends T> other,Function<? super T, U> fn);
public <U> CompletionStage<U> applyToEitherAsync(CompletionStage<? extends T> other,Function<? super T, U> fn,Executor executor);
```

```java
CompletableFuture<Integer> future1 = CompletableFuture
    .supplyAsync(new Supplier<Integer>() {
        @Override
        public Integer get() {
            int number = new Random().nextInt(10);
            System.out.println("第一阶段start：" + number);
            try {
                TimeUnit.SECONDS.sleep(number);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("第一阶段end：" + number);
            return number;
        }
    });

CompletableFuture<Integer> future2 = CompletableFuture
    .supplyAsync(new Supplier<Integer>() {
        @Override
        public Integer get() {
            int number = new Random().nextInt(10);
            System.out.println("第二阶段start：" + number);
            try {
                TimeUnit.SECONDS.sleep(number);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("第二阶段end：" + number);
            return number;
        }
    });

future1.applyToEither(future2, new Function<Integer, Integer>() {
    @Override
    public Integer apply(Integer number) {
        System.out.println("最快结果：" + number);
        return number * 2;
    }
}).join();
```

代码输出

```bash
第一阶段start：6

第二阶段start：5

第二阶段end：5

最快结果：5
```

##### ② **acceptEither** 方法

**两个任务**并行执行，优先使用**先完成者**的结果。

**方法签名**

```java
public CompletionStage<Void> acceptEither(CompletionStage<? extends T> other, Consumer<? super T> action);
public CompletionStage<Void> acceptEitherAsync(CompletionStage<? extends T> other, Consumer<? super T> action);
public CompletionStage<Void> acceptEitherAsync(CompletionStage<? extends T> other, Consumer<? super T> action, Executor executor);
```

**使用示例**

```java
CompletableFuture<Integer> future1 = CompletableFuture
    .supplyAsync(new Supplier<Integer>() {
        @Override
        public Integer get() {
            int number = new Random().nextInt(10) + 1;
            try {
                TimeUnit.SECONDS.sleep(number);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("第一阶段：" + number);
            return number;
        }
    });
CompletableFuture<Integer> future2 = CompletableFuture
    .supplyAsync(new Supplier<Integer>() {
        @Override
        public Integer get() {
            int number = new Random().nextInt(10) + 1;
            try {
                TimeUnit.SECONDS.sleep(number);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("第二阶段：" + number);
            return number;
        }
    });
future1.acceptEither(future2, new Consumer<Integer>() {
    @Override
    public void accept(Integer number) {
        System.out.println("最快结果：" + number);
    }
}).join();
```

**输出结果**

```bash
第二阶段：3

最快结果：3
```

**说明**

- **future2** 先完成（休眠 3 秒），因此其结果被 **Consumer** 消费
- **future1** 虽然也在执行，但其结果被忽略
- **无返回值**，仅用于消费**最先完成**的任务结果

##### ③ **runAfterEither** 方法

当**两个任务**中的**任意一个**完成时，执行**指定操作**，无需获取**任务结果**。

**方法签名**

```java
public CompletionStage<Void> runAfterEither(CompletionStage<?> other, Runnable action);
public CompletionStage<Void> runAfterEitherAsync(CompletionStage<?> other, Runnable action);
public CompletionStage<Void> runAfterEitherAsync(CompletionStage<?> other, Runnable action, Executor executor);
```

**使用示例**

```java
CompletableFuture<Integer> future1 = CompletableFuture
    .supplyAsync(new Supplier<Integer>() {
        @Override
        public Integer get() {
            int number = new Random().nextInt(5);
            try {
                TimeUnit.SECONDS.sleep(number);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("第一阶段：" + number);
            return number;
        }
    });
CompletableFuture<Integer> future2 = CompletableFuture
    .supplyAsync(new Supplier<Integer>() {
        @Override
        public Integer get() {
            int number = new Random().nextInt(5);
            try {
                TimeUnit.SECONDS.sleep(number);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("第二阶段：" + number);
            return number;
        }
    });
future1.runAfterEither(future2, new Runnable() {
    @Override
    public void run() {
        System.out.println("已经有一个任务完成了");
    }
}).join();
```

**输出结果**

```bash
第一阶段：3

已经有一个任务完成了
```

**说明**

- `runAfterEither` 等待**两个任务**中**最先完成**的那个
- **回调动作** `Runnable` 不接收任何参数，也无法获取已完成任务的结果
- 适用于仅需知道"**有任务完成了**"的场景，如：**触发后续流程**、**记录日志**等

##### ④ **anyOf** 方法

接收**多个 CompletableFuture**，返回**最先完成**的结果。

**方法签名**

```java
public static CompletableFuture<Object> anyOf(CompletableFuture<?>... cfs)
```

**使用示例**

```java
Random random = new Random();
CompletableFuture<String> future1 = CompletableFuture
    .supplyAsync(() -> {
        try {
            TimeUnit.SECONDS.sleep(random.nextInt(5));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "hello";
    });
CompletableFuture<String> future2 = CompletableFuture
    .supplyAsync(() -> {
        try {
            TimeUnit.SECONDS.sleep(random.nextInt(1));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "world";
    });
CompletableFuture<Object> result = CompletableFuture.anyOf(future1, future2);
System.out.println(result.get());
```

**运行结果**

```bash
world
```

**特点说明**

- 返回类型为 `CompletableFuture<Object>`，因为**多个任务**的结果类型可能不同
- 只要有一个任务完成，**anyOf** 就会完成并返回该任务的结果
- 适用于**竞速场景**，如从**多个服务**中获取**最快响应**的那个

#### (3) **AND 聚合**：**全部完成**

与 **OR 关系**不同，某些场景需要等待**所有任务全部完成**后才能继续执行。


<img src="/imgs/ccrcy-03-completable-future/2ea0696dfbe07d4cafff03175b4f66ac_MD5.jpg" style="display: block; width: 100%;" alt="AND 聚合关系：全部完成">

<!-- AND 聚合关系示意图：展示了三个任务（Task 1、Task 2、Task 3）通过 allOf() 方法进行 AND 聚合的场景。图中显示三个并行任务分别在不同时间点完成（t1、t2、t3），最终在所有任务都完成后才触发后续操作。这种聚合关系要求所有任务都必须成功完成，只有当最后一个任务在 t3 时刻完成时，整个聚合操作才算完成，体现了 CompletableFuture 中 allOf() 方法等待所有任务完成的特性。 -->

##### ⑤ **runAfterBoth** 方法

等待**两个任务全部执行完成**后，执行**下一步操作**，**不关心运行结果**。

**方法签名：**

```java
public CompletionStage<Void> runAfterBoth(CompletionStage<?> other, Runnable action);
public CompletionStage<Void> runAfterBothAsync(CompletionStage<?> other, Runnable action);
public CompletionStage<Void> runAfterBothAsync(CompletionStage<?> other, Runnable action, Executor executor);
```

**使用示例：**

```java
CompletableFuture<Integer> future1 = CompletableFuture
    .supplyAsync(new Supplier<Integer>() {
        @Override
        public Integer get() {
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("第一阶段：1");
            return 1;
        }
    });

CompletableFuture<Integer> future2 = CompletableFuture
    .supplyAsync(new Supplier<Integer>() {
        @Override
        public Integer get() {
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("第二阶段：2");
            return 2;
        }
    });

future1.runAfterBoth(future2, new Runnable() {
    @Override
    public void run() {
        System.out.println("上面两个任务都执行完成了。");
    }
}).get();
```

**执行结果：**

```bash
第一阶段：1

第二阶段：2

上面两个任务都执行完成了。
```

**特点说明：**

| 特性 | 说明 |
| ---- | ---- |
| **执行时机** | **两个任务全部完成**后才执行**后续操作** |
| **结果处理** | **不接收两个任务的运行结果**，仅执行**回调** |
| **异步支持** | 提供 `async` 变体，支持在**线程池**中**异步执行** |
| **适用场景** | **任务完成后**需要执行**清理或通知操作**，但**不需要任务结果** |

##### ⑥ **allOf** 方法

`allOf` 等待**多个 CompletableFuture 全部完成**后才会**继续执行**。

```java
public static CompletableFuture<Void> allOf(CompletableFuture<?>... cfs)
```

使用示例：创建**两个异步任务**，等待它们**全部完成**。

```java
CompletableFuture<String> future1 = CompletableFuture
    .supplyAsync(() -> {
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("future1完成！");
        return "future1完成！";
    });
CompletableFuture<String> future2 = CompletableFuture
    .supplyAsync(() -> {
        System.out.println("future2完成！");
        return "future2完成！";
    });
CompletableFuture<Void> combindFuture = CompletableFuture
    .allOf(future1, future2);
try {
    combindFuture.get();
} catch (InterruptedException e) {
    e.printStackTrace();
} catch (ExecutionException e) {
    e.printStackTrace();
}
System.out.println("future1: " + future1.isDone() + "，future2: " + future2.isDone());
```

执行结果：

```bash
future2完成！

future1完成！

future1: true，future2: true
```

从输出可以看到：

- **future2 先完成**（**无延迟**）
- **future1 在 2 秒后完成**
- `allOf` 返回的 **CompletableFuture** 在**两个任务都完成后**才完成
- 最终**两个任务的状态**都为 `true`（**已完成**）

### 6.10 实战案例：烧水泡茶

#### (1) 场景描述

华罗庚在《统筹方法》中用烧水泡茶说明**任务统筹**的价值。下图展示了经过优化的工序流程：

<img src="/imgs/ccrcy-03-completable-future/2e90f11e43cb254a19b6bc446e2451ba_MD5.jpg" style="display: block; width: 100%;" alt="烧水泡茶工序流程">

<!-- 该图展示了基于Future实现的并发任务协调机制：T1线程（烧水流程：洗水壶、烧开水、泡茶）和T2线程（洗茶流程：洗茶壶、洗茶杯、拿茶叶）并行执行，T1在"泡茶"步骤通过t2.get()阻塞等待T2的FutureTask完成，体现了Future模式下线程间依赖关系和阻塞等待的特点 -->

实现这一场景可以采用**双线程分工**：**T1** 负责洗水壶、烧开水、泡茶，**T2** 负责洗茶壶、洗茶杯、拿茶叶。**T1** 执行泡茶时需要等待 **T2** 完成拿茶叶，体现了**任务间的依赖关系**。

#### (2) Future 实现方案

下面使用 **Future** 和 **FutureTask** 实现**任务编排**。这个场景中，**T1** 在泡茶前需要等待 **T2** 完成拿茶叶，体现了**任务间的依赖关系**。

**任务划分：**

| 线程 | 任务步骤 | 依赖关系 |
| --- | --- | --- |
| **T1** | 洗水壶 → 烧开水 → 泡茶 | 泡茶需要等待 **T2** 的结果 |
| **T2** | 洗茶壶 → 洗茶杯 → 拿茶叶 | 无依赖，可独立执行 |

**实现方式：**

- **T1** 和 **T2** 分别创建独立的 **FutureTask**
- **T1** 持有 **T2** 的 **FutureTask** 引用，在需要时调用 `get()` 方法**阻塞等待**
- **两个任务并行执行**，**T1** 在需要 **T2** 结果时会自动等待

代码实现：

```java
public class FutureTaskDemo3 {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        // 创建任务T2的FutureTask
        FutureTask<String> ft2 = new FutureTask<>(new T2Task());
        // 创建任务T1的FutureTask
        FutureTask<String> ft1 = new FutureTask<>(new T1Task(ft2));
        // 线程T1执行任务ft1
        Thread T1 = new Thread(ft1);
        T1.start();
        // 线程T2执行任务ft2
        Thread T2 = new Thread(ft2);
        T2.start();
        // 等待线程T1执行结果
        System.out.println(ft1.get());
    }
}

// T1Task需要执行的任务：
// 洗水壶、烧开水、泡茶
class T1Task implements Callable<String> {
    FutureTask<String> ft2;

    // T1任务需要T2任务的FutureTask
    T1Task(FutureTask<String> ft2) {
        this.ft2 = ft2;
    }

    @Override
    public String call() throws Exception {
        System.out.println("T1:洗水壶...");
        TimeUnit.SECONDS.sleep(1);
        System.out.println("T1:烧开水...");
        TimeUnit.SECONDS.sleep(15);
        // 获取T2线程的茶叶
        String tf = ft2.get();
        System.out.println("T1:拿到茶叶:" + tf);
        System.out.println("T1:泡茶...");
        return "上茶:" + tf;
    }
}

// T2Task需要执行的任务:
// 洗茶壶、洗茶杯、拿茶叶
class T2Task implements Callable<String> {
    @Override
    public String call() throws Exception {
        System.out.println("T2:洗茶壶...");
        TimeUnit.SECONDS.sleep(1);
        System.out.println("T2:洗茶杯...");
        TimeUnit.SECONDS.sleep(2);
        System.out.println("T2:拿茶叶...");
        TimeUnit.SECONDS.sleep(1);
        return "龙井";
    }
}
```

#### (3) CompletableFuture 实现方案

使用 **CompletableFuture** 可以更优雅地实现上述**任务编排**，**无需手动管理线程和阻塞等待**。

**实现思路：**

- **任务 1**（洗水壶、烧开水）：使用 `runAsync()` 执行**无返回值的异步任务**
- **任务 2**（洗茶壶、洗茶杯、拿茶叶）：使用 `supplyAsync()` 执行**有返回值的异步任务**，返回茶叶名称
- **任务 3**（泡茶）：使用 `thenCombine()` **等待任务 1 和任务 2 全部完成后**，获取结果并执行泡茶操作

代码实现如下：

```java
public class CompletableFutureDemo2 {
    public static void main(String[] args) {
        //任务1：洗水壶->烧开水
        CompletableFuture<Void> f1 = CompletableFuture
            .runAsync(() -> {
                System.out.println("T1:洗水壶...");
                sleep(1, TimeUnit.SECONDS);
                System.out.println("T1:烧开水...");
                sleep(15, TimeUnit.SECONDS);
            });
        //任务2：洗茶壶->洗茶杯->拿茶叶
        CompletableFuture<String> f2 = CompletableFuture
            .supplyAsync(() -> {
                System.out.println("T2:洗茶壶...");
                sleep(1, TimeUnit.SECONDS);
                System.out.println("T2:洗茶杯...");
                sleep(2, TimeUnit.SECONDS);
                System.out.println("T2:拿茶叶...");
                sleep(1, TimeUnit.SECONDS);
                return "龙井";
            });
        //任务3：任务1和任务2完成后执行：泡茶
        CompletableFuture<String> f3 = f1.thenCombine(f2, (__, tf) -> {
            System.out.println("T1:拿到茶叶:" + tf);
            System.out.println("T1:泡茶...");
            return "上茶:" + tf;
        });
        //等待任务3执行结果
        System.out.println(f3.join());
    }
    static void sleep(int t, TimeUnit u){
        try {
            u.sleep(t);
        } catch (InterruptedException e) {
        }
    }
}
```

上述代码思路汇总如下图

<img src="/imgs/ccrcy-03-completable-future/c39f8ca4bf8aa78c36cd85e470c66ebe_MD5.jpg" style="display: block; width: 100%;" alt="CompletableFuture 实现思路">

<!-- CompletableFuture 实现思路流程图：展示了异步任务的并行执行与组合模式。图中包含两个并行分支 T1 (Async) 和 T2 (ApplyTnc)，T1 分支处理洗水壶(1m)和烧开水(15m)两个步骤，T2 分支顺序执行洗茶壶(1m)、洗茶杯(2m)和拿茶叶(1m)三个步骤。两个分支通过 AND-Gate (thenCombine) 进行同步等待，只有当两个分支都完成后才能进入最终的泡茶阶段。该图形象地说明了 CompletableFuture 如何实现异步任务的并行编排和依赖组合，体现了并行处理提升效率、同步机制确保依赖关系的核心思想。 -->

与 **Future** 实现方式相比，**CompletableFuture** 具有以下**核心差异**：

| 特性 | 说明 |
|------|------|
| **代码量少** | 无需定义 **Callable** 类和手动管理线程 |
| **依赖关系清晰** | 通过 `thenCombine()` 方法表达任务间的依赖 |
| **自动等待** | 自动处理任务间的等待和结果传递 |

## 7. 文档总结

本文系统阐述 **Java 异步编程技术演进**，通过**接口对比分析**、**Future 机制讲解**、**FutureTask 实践演示**、**CompletableFuture 进阶应用**四个维度，帮助读者：

| 学习层次 | 核心收获 |
| ---- | ---- |
| **理解技术演进** | 掌握 **Runnable** → **Callable** → **Future** → **CompletableFuture** 的演进路径，理解各阶段的技术特点与局限性 |
| **掌握基础能力** | 熟练使用 **Future** 和 **FutureTask** 进行异步任务管理，理解阻塞获取、状态查询等基本操作 |
| **提升实践水平** | 掌握 **CompletableFuture** 任务编排方法，能够处理依赖关系、组合聚合、并行执行等复杂异步场景 |
| **自动等待** | 自动处理任务间的等待和结果传递，无需手动调用 `get()` 阻塞 |
