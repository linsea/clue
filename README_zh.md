# clue
一个高性能的Android日志库

## 功能
- Class名字作为默认的tag, 或者自定义tag
- 显示调用者的方法名
- 显示源代码行号
- 显示线程名
- 在Android Studio的日志窗口中点击日志中的文件名跳转到源代码相应的位置
- 无性能损耗地获取以上日志信息
- 可扩展的API接口设计

## 使用方法
1 在项目的顶级 `build.gradle` 文件中引用插件的classpath.
```
buildscript {
  repositories {
    maven {
      url "https://plugins.gradle.org/m2/"
    }
  }
  dependencies {
    classpath "gradle.plugin.com.github.linsea:clue-plugin:1.0.0"
  }
}
```
2 在项目的 `build.gradle` 中应用插件.
```
apply plugin: "com.github.linsea.clue-plugin"
```
3 在项目的 `build.gradle` 加入依赖库.
```
compile 'com.linsea:clue:1.0'
```
4 在 `Application` class中加入一个log实例.
```java
public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Clue.addLog(new ConsoleLog()); //初始化Clue log,默认使用系统的Logcat输出到控制台.
    }
}
```
5 调用 `Clue` 类的静态方法记录日志.
比如:
```java

public static void v(@NonNull String message, Object... args);

public static void vt(String tag, @NonNull String message, Object... args)

public static void v(Throwable t, @NonNull String message, Object... args)

public static void vt(String tag, Throwable t, @NonNull String message, Object... args)
```

更详细的使用方法请参考位于 `clue/clue-sample` 的示例项目.

## 为什么性能高
通常的Android日志库, 为了获取到class名, 方法名, 行号, 都是通过以下API实现的:
```java
StackTraceElement[] stacks = Thread.currentThread().getStackTrace(); //expensive cost
StackTraceElement element = stacks[4];
int lineNumber = element.getLineNumber();
String fileName = element.getFileName()
```
以上的方式损耗的性能是很高昂的, 线上代码中不应该出现.

显然, 如果代码写好, 在编译之前, 所有的class名, 方法名, 行号是固定的, 不会再变化,
不应该在运行时通过以上代价高昂的方式去动态获取.

**Clue** 日志没有调用以上API来获取class名, 方法名, 行号, 而是换了另外一种思路,
它通过在编译期操作class文件字节码, 从中获取这些信息. 这对于需要线上记录日志到文件的场景尤其有用.

## 扩展接口
你可以添加自己的`logger`实现来扩展`Clue`, 比如继承`BaseLog`实现一个把日志写入文件的Log Receiver,
然后调用`Clue.add(...)`把它添加进`Clue`中, 具体可以参考库中[ConsoleLog](https://github.com/linsea/clue/blob/master/clue/src/main/java/com/github/linsea/clue/ConsoleLog.java)的实现.

# License

    Copyright 2017 clue author

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.