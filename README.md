# Android Firebase Performance Network Metrics

Firebase Performance network metrics with improved support for OkHTTP and Apollo GraphQL

![](https://img.shields.io/jitpack/version/com.github.ikurek/android-firebase-perf-network-metrics)
![](https://img.shields.io/github/v/release/ikurek/android-firebase-perf-network-metrics?label=github)
## Introduction

AFPNM library is mostly meant to make Firebase Performance network metrics usable with Apollo
GraphQL, but it also includes a few extensions for classic REST API requests. Firebase Performance
support for GraphQL requests is not the best, since there is no out-of-the-box way to determine
traces for specific GraphQL operations and measure their response time. AFPNM solves that by
providing a way to filter and analyze both REST and GraphQL operations.

## Supported Frameworks

AFPNM works with any networking library based on [OkHTTP](https://github.com/square/okhttp), since
the [FirebasePerformanceInterceptor](afpnm/src/main/kotlin/com/ikurek/android/afpnm/FirebasePerformanceInterceptor.kt)
is an implementation of OkHTTP interceptor interface.

For GraphQL, only [Apollo-Kotlin](https://github.com/apollographql/apollo-kotlin) is supported, but
since the interceptor is based on top of OkHTTP, Kotlin-Multiplatform projects are not supported. A
Kotlin-Multiplatform solution would be possible to build, with the use of Apollo Interceptors but is
not implemented at the moment.

## Features

### Append Apollo GraphQL operation names to request paths

By default, Firebase Performance does not provide a way to filter GraphQL operations the same way
REST operations can be filtered. For REST, the request path and type can be used, but for most
GraphQL APIs the URL and HTTP method will be the same. Because of this, the performance metrics for
all GraphQL operations look like this:

```
POST mygraphqlhost.com/graphql
```

With AFPNM, GraphQL operation names can be extracted from requests and appended to path like this:

```
POST mygraphqlhost.com/graphql/MyGraphQlOperation
```

So they can be filtered and observed just like classic REST operations

### Add custom trace attributes to network requests

With a simple interceptor configuration, AFPNM can attach custom metrics and attributes to request
traces, so different parameters like language, build version, etc. can be attached to request
metrics.
The [TraceAttribute](afpnm/src/main/kotlin/com/ikurek/android/afpnm/model/TraceAttribute.kt) clas
offers both predefined parameters implemented inside the interceptor, and custom key-value
attributes. Max number of attributes is limited to 5 (limitation from the Firebase Performance
system). An example configuration may look like this:

```kotlin
FirebasePerformanceInterceptor.Builder(
    customAttributes = listOf(
        TraceAttribute.OperationName(),
        TraceAttribute.Custom("My Custom Attribute", "Attribute Value")
    )
).build()
```

### Enable and disable traced parameters

By default, Firebase Performance traces all possible metrics for each request. With AFPNM each
metric can be enabled or disabled from the interceptor builder:

```kotlin
FirebasePerformanceInterceptor.Builder(
    appendGraphQlOperationToUrl = false,
    setRequestPayloadSize = false,
    setResponseContentType = false,
    setResponseHttpCode = false,
    setResponsePayloadSize = false
).build()
```

## Installation

### Requirements

#### Firebase Performance configuration

Since the project is based on top of Firebase Performance, before it can be implemented, a proper
configuration of Firebase Services and Firebase Performance is required. The
official [Firebase Performance Getting Started](https://firebase.google.com/docs/perf-mon/get-started-android)
section covers that in detail

#### Disabling default Firebase Performance network monitoring

Firebase Performance for Android comes with a pre-defined network tracing for all basic HTTP
requests. This does not interfere with AFPNM traces, but if both are enabled, the requests will
appear duplicated in the analytics.

To disable default network with Gradle configuration, add the following line to `gradle.properties`:

```
firebasePerformanceInstrumentationEnabled=false
```

Or, to do the same from the `build.gradle` files:

Groovy:

```groovy
buildTypes {
    release {
        FirebasePerformance {
            instrumentationEnabled false
        }
    }
    debug {
        FirebasePerformance {
            instrumentationEnabled false
        }
    }
}
```

Kotlin:

```kotlin
buildTypes {
    getByName("release") {
        withGroovyBuilder {
            "FirebasePerformance" {
                invokeMethod("setInstrumentationEnabled", false)
            }
        }
    }
    getByName("debug") {
        withGroovyBuilder {
            "FirebasePerformance" {
                invokeMethod("setInstrumentationEnabled", false)
            }
        }
    }
}
```

More Information can be found
on [Disable Firebase Performance Monitoring](https://firebase.google.com/docs/perf-mon/disable-sdk?platform=android)
page

### Getting the package

Add [jitpack.io](https://jitpack.io) repository to the project-level `settings.gradle`

Groovy:
```groovy
repositories {
    maven { url 'https://jitpack.io' }
}
```


Kotlin:
```kotlin
repositories {
    maven(url = "https://jitpack.io")
}
```

And add the package dependency in `build.gradle`:

Groovy:
```groovy
implementation 'com.github.ikurek:android-firebase-perf-network-metrics:1.0.0'
```


Kotlin:
```kotlin
implementation("com.github.ikurek:android-firebase-perf-network-metrics:1.0.0")
```

### Enabling the interceptor

When configuring the OkHTTP engine, simply pass the Interceptor as a network layer interceptor:

```kotlin
OkHttpClient.Builder().addNetworkInterceptor(
    FirebasePerformanceInterceptor.Builder().build()
).build()
```