## [中文版说明](README_zh.md)

# clue
a extremely high-performance logger for android

## Features
- Class name tag(default) or custom tag
- Caller method name
- Source line number information
- Thread name information
- Link to source in IDE
- High performance to intercept above information
- Extensible API

## Usage
1 include plugin in top level `build.gradle` file.
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
2 apply plugin to project `build.gradle`
```
apply plugin: "com.github.linsea.clue-plugin"
```
3 add library dependency to project `build.gradle` file.
```
compile 'com.linsea:clue:1.0'
```
4 add a log receiver in the `Application` class
```java
public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Clue.addLog(new ConsoleLog()); //init Clue log with default logcat
    }
}
```
5 call `Clue`'s static methods, e.g.
```java

public static void v(@NonNull String message, Object... args);

public static void vt(String tag, @NonNull String message, Object... args)

public static void v(Throwable t, @NonNull String message, Object... args)

public static void vt(String tag, Throwable t, @NonNull String message, Object... args)
```

see sample application in `clue/clue-sample`

## Why High-Performance
In common logger library, in order to get caller class name(for TAG usage),
method name and source line number, they make use of below APIs:
```java
StackTraceElement[] stacks = Thread.currentThread().getStackTrace(); //expensive cost
StackTraceElement element = stacks[4];
int lineNumber = element.getLineNumber();
String fileName = element.getFileName()
```
which cost is extremely expensive, and should never happen in production.

It is obviously when source code is finished and ready to compile, the class name,
method name and source line number is fixed and never change, should not dynamically
be obtained at runtime to degrade application performances.

**Clue** don't call this expensive cost API, it leverages bytecode manipulate power to get
these information at compile time to speed up the application performances, this is useful
in the scenario which would write logs for production APP.

## Extension
You are able to add your own `logger` implementation to `Clue` (e.g. a logger to write logs
to files), which only needs to extends `BaseLog` and call `Clue.add(...)` to add to Clue,
more details references [ConsoleLog](https://github.com/linsea/clue/blob/master/clue/src/main/java/com/github/linsea/clue/ConsoleLog.java) implementation.

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