# RoboPing
![License: GPL](https://img.shields.io/badge/License-GPL-blue) [![API](https://img.shields.io/badge/API-19%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=19) 

A small library wrapper for calling `/system/bin/ping` and get the result as RxJava's `Completable` or `Observable<String>` classes

### Installation:

Add this to your project level `build.gradle` file:

```groovy
repositories {
  maven { url "https://jitpack.io" }
}
```

And then add this to your module level `build.gradle`:

```groovy
dependencies {
  implementation 'com.github.sizeofanton:RoboPing:1.0'
}
```

### Usage:

Building an instance:

```kotlin
val pingWrapper = RoboPing.Builder()
	.setCount(10)
	.setInterval(2)
	.build()
```

All available `Builder` methods:

| Method                       | Description                                                  |
| ---------------------------- | ------------------------------------------------------------ |
| `enableBroadcast()`          | Allows pinging a broadcast address                           |
| `setCount(count: Int)`       | Set the number of times to send the ping request             |
| `enableSoDebug()`            | Set the SO-DEBUG option on the socket being used             |
| `enableFlood()`              | Flood the network by sending hundred or more packets per second |
| `setInterval(interval: Int)` | Specify an interval between successive packet transmissions  |
| `setTtl(ttl: Int)`           | Set the Time To Live (number of hops)                        |
| `setDeadline(deadline: Int)` | Specify a timeout, in seconds, before ping exits             |
| `setTimeout(timeout: Int)`   | Set the time(seconds) to wait for a response                 |
| `build()`                    | Creates RoboPing class instance with specified parameters    |

RoboPing contains two methods, one that returns a `Completable` class without any additional information and the one that returns an `Observable<String>` with a ping command's `stdout` and `stderr` , that will be transmited to observers.

Silent ping example:

```kotlin
val pingWrapper = RoboPing.Builder().build()
pingWrapper.ping(host)
	.subscribeOn(Schedulers.io())
	.observeOn(AndroidSchedulers.mainThread())
	.subscribe({
    Toast.makeText(this, "Host reachable", Toast.LENGTH_LONG).show()
  },{
    Toast.makeText(this, "Host unreachable", Toast.LENGTH_LONG).show()
  })
```

Verbose ping example:

```kotlin
val pingWrapper = RoboPing.Builder().build()
pingWrapper.ping(host)
	.subscribeOn(Schedulers.io())
	.observeOn(AndroidSchedulers.mainThread())
	.subscribe({
    log.append(it)
  },{
    log.append(errorMsg)
  })
```

![](./readme/verbose.gif)