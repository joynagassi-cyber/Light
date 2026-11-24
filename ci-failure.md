### Test failures for db08d86eedeb3c440dbad547cbe1b8b4e9c7d285
```
=== app/build/test-results/testDebugUnitTest/TEST-com.example.ExampleRobolectricTest.xml ===
<?xml version="1.0" encoding="UTF-8"?>
<testsuite name="com.example.ExampleRobolectricTest" tests="1" skipped="0" failures="1" errors="0" timestamp="2026-11-24T14:45:56.027Z" hostname="ip-10-0-131-16" time="0.79">
  <properties/>
  <testcase name="classMethod" classname="com.example.ExampleRobolectricTest" time="0.006">
    <failure message="java.lang.AssertionError: Failed to fetch maven artifact org.robolectric:android-all-instrumented:14-robolectric-10818077-i7" type="java.lang.AssertionError">java.lang.AssertionError: Failed to fetch maven artifact org.robolectric:android-all-instrumented:14-robolectric-10818077-i7
	at org.robolectric.internal.dependency.MavenArtifactFetcher.fetchArtifact(MavenArtifactFetcher.java:129)
	at org.robolectric.internal.dependency.MavenDependencyResolver.lambda$getLocalArtifactUrls$0(MavenDependencyResolver.java:87)
	at org.robolectric.internal.dependency.MavenDependencyResolver.whileLocked(MavenDependencyResolver.java:107)
	at org.robolectric.internal.dependency.MavenDependencyResolver.getLocalArtifactUrls(MavenDependencyResolver.java:82)
	at org.robolectric.internal.dependency.MavenDependencyResolver.getLocalArtifactUrl(MavenDependencyResolver.java:123)
	at org.robolectric.plugins.LegacyDependencyResolver.getLocalArtifactUrl(LegacyDependencyResolver.java:95)
	at org.robolectric.plugins.DefaultSdkProvider$DefaultSdk.getJarPath(DefaultSdkProvider.java:141)
	at org.robolectric.internal.AndroidSandbox$SdkSandboxClassLoader.&lt;init&gt;(AndroidSandbox.java:128)
	at java.base/jdk.internal.reflect.NativeConstructorAccessorImpl.newInstance0(Native Method)
	at java.base/jdk.internal.reflect.NativeConstructorAccessorImpl.newInstance(NativeConstructorAccessorImpl.java:77)
	at java.base/jdk.internal.reflect.DelegatingConstructorAccessorImpl.newInstance(DelegatingConstructorAccessorImpl.java:45)
	at java.base/java.lang.reflect.Constructor.newInstanceWithCaller(Constructor.java:500)
	at java.base/java.lang.reflect.Constructor.newInstance(Constructor.java:481)
	at org.robolectric.util.inject.Injector.inject(Injector.java:258)
	at org.robolectric.util.inject.Injector.lambda$memoized$1(Injector.java:239)
	at org.robolectric.util.inject.Injector$MemoizingProvider.get(Injector.java:506)
	at org.robolectric.util.inject.Injector.getInstanceInternal(Injector.java:230)
	at org.robolectric.util.inject.Injector.resolveDependencies(Injector.java:305)
	at org.robolectric.util.inject.Injector.inject(Injector.java:256)
	at org.robolectric.util.inject.Injector.lambda$memoized$1(Injector.java:239)
	at org.robolectric.util.inject.Injector$MemoizingProvider.get(Injector.java:506)
	at org.robolectric.util.inject.Injector.getInstanceInternal(Injector.java:230)
	at org.robolectric.util.inject.Injector.getInstance(Injector.java:214)
	at org.robolectric.util.inject.Injector.access$700(Injector.java:96)
	at org.robolectric.util.inject.Injector$ScopeBuilderProvider.create(Injector.java:575)
	at org.robolectric.util.inject.Injector$ScopeBuilderProvider.lambda$get$0(Injector.java:558)
	at jdk.proxy3/jdk.proxy3.$Proxy20.build(Unknown Source)
	at org.robolectric.internal.SandboxManager.getAndroidSandbox(SandboxManager.java:68)
	at org.robolectric.RobolectricTestRunner.getSandbox(RobolectricTestRunner.java:263)
	at org.robolectric.RobolectricTestRunner.getSandbox(RobolectricTestRunner.java:64)
	at org.robolectric.internal.SandboxTestRunner.sandboxGroupingClassBlock(SandboxTestRunner.java:140)
	at org.robolectric.internal.SandboxTestRunner.classBlock(SandboxTestRunner.java:119)
	at org.junit.runners.ParentRunner.run(ParentRunner.java:412)
	at org.robolectric.RobolectricTestRunner.run(RobolectricTestRunner.java:142)
	at org.junit.runner.JUnitCore.run(JUnitCore.java:137)
	at org.junit.runner.JUnitCore.run(JUnitCore.java:115)
	at org.gradle.api.internal.tasks.testing.junit.JUnitTestExecutor.runRequest(JUnitTestExecutor.java:175)
	at org.gradle.api.internal.tasks.testing.junit.JUnitTestExecutor.accept(JUnitTestExecutor.java:84)
	at org.gradle.api.internal.tasks.testing.junit.JUnitTestExecutor.accept(JUnitTestExecutor.java:47)
	at org.gradle.api.internal.tasks.testing.junit.AbstractJUnitTestDefinitionProcessor.processTestDefinition(AbstractJUnitTestDefinitionProcessor.java:65)
	at org.gradle.api.internal.tasks.testing.SuiteTestDefinitionProcessor.processTestDefinition(SuiteTestDefinitionProcessor.java:54)
	at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
	at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:77)
	at java.base/jdk.internal.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
	at java.base/java.lang.reflect.Method.invoke(Method.java:569)
	at org.gradle.internal.dispatch.MethodInvocation.invokeOn(MethodInvocation.java:77)
	at org.gradle.internal.dispatch.ReflectionDispatch.dispatch(ReflectionDispatch.java:28)
	at org.gradle.internal.dispatch.ReflectionDispatch.dispatch(ReflectionDispatch.java:19)
	at org.gradle.internal.dispatch.ContextClassLoaderDispatch.dispatch(ContextClassLoaderDispatch.java:33)
	at org.gradle.internal.dispatch.ProxyDispatchAdapter$DispatchingInvocationHandler.invoke(ProxyDispatchAdapter.java:88)
	at jdk.proxy1/jdk.proxy1.$Proxy4.processTestDefinition(Unknown Source)
	at org.gradle.api.internal.tasks.testing.worker.TestWorker$2.run(TestWorker.java:178)
	at org.gradle.api.internal.tasks.testing.worker.TestWorker.executeAndMaintainThreadName(TestWorker.java:126)
	at org.gradle.api.internal.tasks.testing.worker.TestWorker.execute(TestWorker.java:103)
	at org.gradle.api.internal.tasks.testing.worker.TestWorker.execute(TestWorker.java:63)
	at org.gradle.process.internal.worker.child.ActionExecutionWorker.execute(ActionExecutionWorker.java:56)
	at org.gradle.process.internal.worker.child.SystemApplicationClassLoaderWorker.call(SystemApplicationClassLoaderWorker.java:122)
	at org.gradle.process.internal.worker.child.SystemApplicationClassLoaderWorker.call(SystemApplicationClassLoaderWorker.java:72)
	at worker.org.gradle.process.internal.worker.GradleWorkerMain.run(GradleWorkerMain.java:69)
	at worker.org.gradle.process.internal.worker.GradleWorkerMain.main(GradleWorkerMain.java:74)
Caused by: java.util.concurrent.ExecutionException: java.io.IOException: Server returned HTTP response code: 429 for URL: https://repo1.maven.org/maven2/org/robolectric/android-all-instrumented/14-robolectric-10818077-i7/android-all-instrumented-14-robolectric-10818077-i7.jar
	at com.google.common.util.concurrent.AbstractFuture.getDoneValue(AbstractFuture.java:292)
	at com.google.common.util.concurrent.AbstractFutureState.blockingGet(AbstractFutureState.java:252)
	at com.google.common.util.concurrent.Platform.get(Platform.java:54)
	at com.google.common.util.concurrent.AbstractFuture.get(AbstractFuture.java:253)
	at com.google.common.util.concurrent.AbstractFuture$TrustedFuture.get(AbstractFuture.java:96)
	at org.robolectric.internal.dependency.MavenArtifactFetcher.fetchArtifact(MavenArtifactFetcher.java:121)
	... 59 more
Caused by: java.io.IOException: Server returned HTTP response code: 429 for URL: https://repo1.maven.org/maven2/org/robolectric/android-all-instrumented/14-robolectric-10818077-i7/android-all-instrumented-14-robolectric-10818077-i7.jar
	at java.base/sun.net.www.protocol.http.HttpURLConnection.getInputStream0(HttpURLConnection.java:2059)
	at java.base/sun.net.www.protocol.http.HttpURLConnection.getInputStream(HttpURLConnection.java:1644)
	at java.base/sun.net.www.protocol.https.HttpsURLConnectionImpl.getInputStream(HttpsURLConnectionImpl.java:224)
	at org.robolectric.internal.dependency.MavenArtifactFetcher$FetchToFileTask.call(MavenArtifactFetcher.java:251)
	at com.google.common.util.concurrent.TrustedListenableFutureTask$TrustedFutureInterruptibleAsyncTask.runInterruptibly(TrustedListenableFutureTask.java:164)
	at com.google.common.util.concurrent.TrustedListenableFutureTask$TrustedFutureInterruptibleAsyncTask.runInterruptibly(TrustedListenableFutureTask.java:147)
	at com.google.common.util.concurrent.InterruptibleTask.run(InterruptibleTask.java:74)
	at com.google.common.util.concurrent.TrustedListenableFutureTask.run(TrustedListenableFutureTask.java:80)
	at java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1136)
	at java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:635)
	at java.base/java.lang.Thread.run(Thread.java:840)
</failure>
  </testcase>
  <system-out><![CDATA[]]></system-out>
  <system-err><![CDATA[ERROR: Failed to fetch maven artifact org.robolectric:android-all-instrumented:14-robolectric-10818077-i7
java.util.concurrent.ExecutionException: java.io.IOException: Server returned HTTP response code: 429 for URL: https://repo1.maven.org/maven2/org/robolectric/android-all-instrumented/14-robolectric-10818077-i7/android-all-instrumented-14-robolectric-10818077-i7.jar
	at com.google.common.util.concurrent.AbstractFuture.getDoneValue(AbstractFuture.java:292)
	at com.google.common.util.concurrent.AbstractFutureState.blockingGet(AbstractFutureState.java:252)
	at com.google.common.util.concurrent.Platform.get(Platform.java:54)
	at com.google.common.util.concurrent.AbstractFuture.get(AbstractFuture.java:253)
	at com.google.common.util.concurrent.AbstractFuture$TrustedFuture.get(AbstractFuture.java:96)
	at org.robolectric.internal.dependency.MavenArtifactFetcher.fetchArtifact(MavenArtifactFetcher.java:121)
	at org.robolectric.internal.dependency.MavenDependencyResolver.lambda$getLocalArtifactUrls$0(MavenDependencyResolver.java:87)
	at org.robolectric.internal.dependency.MavenDependencyResolver.whileLocked(MavenDependencyResolver.java:107)
	at org.robolectric.internal.dependency.MavenDependencyResolver.getLocalArtifactUrls(MavenDependencyResolver.java:82)
	at org.robolectric.internal.dependency.MavenDependencyResolver.getLocalArtifactUrl(MavenDependencyResolver.java:123)
	at org.robolectric.plugins.LegacyDependencyResolver.getLocalArtifactUrl(LegacyDependencyResolver.java:95)
	at org.robolectric.plugins.DefaultSdkProvider$DefaultSdk.getJarPath(DefaultSdkProvider.java:141)
	at org.robolectric.internal.AndroidSandbox$SdkSandboxClassLoader.<init>(AndroidSandbox.java:128)
	at java.base/jdk.internal.reflect.NativeConstructorAccessorImpl.newInstance0(Native Method)
	at java.base/jdk.internal.reflect.NativeConstructorAccessorImpl.newInstance(NativeConstructorAccessorImpl.java:77)
	at java.base/jdk.internal.reflect.DelegatingConstructorAccessorImpl.newInstance(DelegatingConstructorAccessorImpl.java:45)
	at java.base/java.lang.reflect.Constructor.newInstanceWithCaller(Constructor.java:500)
	at java.base/java.lang.reflect.Constructor.newInstance(Constructor.java:481)
	at org.robolectric.util.inject.Injector.inject(Injector.java:258)
	at org.robolectric.util.inject.Injector.lambda$memoized$1(Injector.java:239)
	at org.robolectric.util.inject.Injector$MemoizingProvider.get(Injector.java:506)
	at org.robolectric.util.inject.Injector.getInstanceInternal(Injector.java:230)
	at org.robolectric.util.inject.Injector.resolveDependencies(Injector.java:305)
	at org.robolectric.util.inject.Injector.inject(Injector.java:256)
	at org.robolectric.util.inject.Injector.lambda$memoized$1(Injector.java:239)
	at org.robolectric.util.inject.Injector$MemoizingProvider.get(Injector.java:506)
	at org.robolectric.util.inject.Injector.getInstanceInternal(Injector.java:230)
	at org.robolectric.util.inject.Injector.getInstance(Injector.java:214)
	at org.robolectric.util.inject.Injector.access$700(Injector.java:96)
	at org.robolectric.util.inject.Injector$ScopeBuilderProvider.create(Injector.java:575)
	at org.robolectric.util.inject.Injector$ScopeBuilderProvider.lambda$get$0(Injector.java:558)
	at jdk.proxy3/jdk.proxy3.$Proxy20.build(Unknown Source)
	at org.robolectric.internal.SandboxManager.getAndroidSandbox(SandboxManager.java:68)
	at org.robolectric.RobolectricTestRunner.getSandbox(RobolectricTestRunner.java:263)
	at org.robolectric.RobolectricTestRunner.getSandbox(RobolectricTestRunner.java:64)
	at org.robolectric.internal.SandboxTestRunner.sandboxGroupingClassBlock(SandboxTestRunner.java:140)
	at org.robolectric.internal.SandboxTestRunner.classBlock(SandboxTestRunner.java:119)
	at org.junit.runners.ParentRunner.run(ParentRunner.java:412)
	at org.robolectric.RobolectricTestRunner.run(RobolectricTestRunner.java:142)
	at org.junit.runner.JUnitCore.run(JUnitCore.java:137)
	at org.junit.runner.JUnitCore.run(JUnitCore.java:115)
	at org.gradle.api.internal.tasks.testing.junit.JUnitTestExecutor.runRequest(JUnitTestExecutor.java:175)
	at org.gradle.api.internal.tasks.testing.junit.JUnitTestExecutor.accept(JUnitTestExecutor.java:84)
	at org.gradle.api.internal.tasks.testing.junit.JUnitTestExecutor.accept(JUnitTestExecutor.java:47)
	at org.gradle.api.internal.tasks.testing.junit.AbstractJUnitTestDefinitionProcessor.processTestDefinition(AbstractJUnitTestDefinitionProcessor.java:65)
	at org.gradle.api.internal.tasks.testing.SuiteTestDefinitionProcessor.processTestDefinition(SuiteTestDefinitionProcessor.java:54)
	at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
	at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:77)
	at java.base/jdk.internal.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
	at java.base/java.lang.reflect.Method.invoke(Method.java:569)
	at org.gradle.internal.dispatch.MethodInvocation.invokeOn(MethodInvocation.java:77)
	at org.gradle.internal.dispatch.ReflectionDispatch.dispatch(ReflectionDispatch.java:28)
	at org.gradle.internal.dispatch.ReflectionDispatch.dispatch(ReflectionDispatch.java:19)
	at org.gradle.internal.dispatch.ContextClassLoaderDispatch.dispatch(ContextClassLoaderDispatch.java:33)
	at org.gradle.internal.dispatch.ProxyDispatchAdapter$DispatchingInvocationHandler.invoke(ProxyDispatchAdapter.java:88)
	at jdk.proxy1/jdk.proxy1.$Proxy4.processTestDefinition(Unknown Source)
	at org.gradle.api.internal.tasks.testing.worker.TestWorker$2.run(TestWorker.java:178)
	at org.gradle.api.internal.tasks.testing.worker.TestWorker.executeAndMaintainThreadName(TestWorker.java:126)
	at org.gradle.api.internal.tasks.testing.worker.TestWorker.execute(TestWorker.java:103)
	at org.gradle.api.internal.tasks.testing.worker.TestWorker.execute(TestWorker.java:63)
	at org.gradle.process.internal.worker.child.ActionExecutionWorker.execute(ActionExecutionWorker.java:56)
	at org.gradle.process.internal.worker.child.SystemApplicationClassLoaderWorker.call(SystemApplicationClassLoaderWorker.java:122)
	at org.gradle.process.internal.worker.child.SystemApplicationClassLoaderWorker.call(SystemApplicationClassLoaderWorker.java:72)
	at worker.org.gradle.process.internal.worker.GradleWorkerMain.run(GradleWorkerMain.java:69)
	at worker.org.gradle.process.internal.worker.GradleWorkerMain.main(GradleWorkerMain.java:74)
Caused by: java.io.IOException: Server returned HTTP response code: 429 for URL: https://repo1.maven.org/maven2/org/robolectric/android-all-instrumented/14-robolectric-10818077-i7/android-all-instrumented-14-robolectric-10818077-i7.jar
	at java.base/sun.net.www.protocol.http.HttpURLConnection.getInputStream0(HttpURLConnection.java:2059)
	at java.base/sun.net.www.protocol.http.HttpURLConnection.getInputStream(HttpURLConnection.java:1644)
	at java.base/sun.net.www.protocol.https.HttpsURLConnectionImpl.getInputStream(HttpsURLConnectionImpl.java:224)
	at org.robolectric.internal.dependency.MavenArtifactFetcher$FetchToFileTask.call(MavenArtifactFetcher.java:251)
	at com.google.common.util.concurrent.TrustedListenableFutureTask$TrustedFutureInterruptibleAsyncTask.runInterruptibly(TrustedListenableFutureTask.java:164)
	at com.google.common.util.concurrent.TrustedListenableFutureTask$TrustedFutureInterruptibleAsyncTask.runInterruptibly(TrustedListenableFutureTask.java:147)
	at com.google.common.util.concurrent.InterruptibleTask.run(InterruptibleTask.java:74)
	at com.google.common.util.concurrent.TrustedListenableFutureTask.run(TrustedListenableFutureTask.java:80)
	at java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1136)
	at java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:635)
	at java.base/java.lang.Thread.run(Thread.java:840)
]]></system-err>
</testsuite>
---END app/build/test-results/testDebugUnitTest/TEST-com.example.ExampleRobolectricTest.xml---
=== app/build/test-results/testDebugUnitTest/TEST-com.example.data.document.LocalDocumentExtractorTest.xml ===
<?xml version="1.0" encoding="UTF-8"?>
<testsuite name="com.example.data.document.LocalDocumentExtractorTest" tests="1" skipped="0" failures="1" errors="0" timestamp="2026-11-24T14:45:56.934Z" hostname="ip-10-0-131-16" time="0.072">
  <properties/>
  <testcase name="classMethod" classname="com.example.data.document.LocalDocumentExtractorTest" time="0.001">
    <failure message="java.lang.AssertionError: Failed to fetch maven artifact org.robolectric:android-all-instrumented:14-robolectric-10818077-i7" type="java.lang.AssertionError">java.lang.AssertionError: Failed to fetch maven artifact org.robolectric:android-all-instrumented:14-robolectric-10818077-i7
	at org.robolectric.internal.dependency.MavenArtifactFetcher.fetchArtifact(MavenArtifactFetcher.java:129)
	at org.robolectric.internal.dependency.MavenDependencyResolver.lambda$getLocalArtifactUrls$0(MavenDependencyResolver.java:87)
	at org.robolectric.internal.dependency.MavenDependencyResolver.whileLocked(MavenDependencyResolver.java:107)
	at org.robolectric.internal.dependency.MavenDependencyResolver.getLocalArtifactUrls(MavenDependencyResolver.java:82)
	at org.robolectric.internal.dependency.MavenDependencyResolver.getLocalArtifactUrl(MavenDependencyResolver.java:123)
	at org.robolectric.plugins.LegacyDependencyResolver.getLocalArtifactUrl(LegacyDependencyResolver.java:95)
	at org.robolectric.plugins.DefaultSdkProvider$DefaultSdk.getJarPath(DefaultSdkProvider.java:141)
	at org.robolectric.internal.AndroidSandbox$SdkSandboxClassLoader.&lt;init&gt;(AndroidSandbox.java:128)
	at java.base/jdk.internal.reflect.NativeConstructorAccessorImpl.newInstance0(Native Method)
	at java.base/jdk.internal.reflect.NativeConstructorAccessorImpl.newInstance(NativeConstructorAccessorImpl.java:77)
	at java.base/jdk.internal.reflect.DelegatingConstructorAccessorImpl.newInstance(DelegatingConstructorAccessorImpl.java:45)
	at java.base/java.lang.reflect.Constructor.newInstanceWithCaller(Constructor.java:500)
	at java.base/java.lang.reflect.Constructor.newInstance(Constructor.java:481)
	at org.robolectric.util.inject.Injector.inject(Injector.java:258)
	at org.robolectric.util.inject.Injector.lambda$memoized$1(Injector.java:239)
	at org.robolectric.util.inject.Injector$MemoizingProvider.get(Injector.java:506)
	at org.robolectric.util.inject.Injector.getInstanceInternal(Injector.java:230)
	at org.robolectric.util.inject.Injector.resolveDependencies(Injector.java:305)
	at org.robolectric.util.inject.Injector.inject(Injector.java:256)
	at org.robolectric.util.inject.Injector.lambda$memoized$1(Injector.java:239)
	at org.robolectric.util.inject.Injector$MemoizingProvider.get(Injector.java:506)
	at org.robolectric.util.inject.Injector.getInstanceInternal(Injector.java:230)
	at org.robolectric.util.inject.Injector.getInstance(Injector.java:214)
	at org.robolectric.util.inject.Injector.access$700(Injector.java:96)
	at org.robolectric.util.inject.Injector$ScopeBuilderProvider.create(Injector.java:575)
	at org.robolectric.util.inject.Injector$ScopeBuilderProvider.lambda$get$0(Injector.java:558)
	at jdk.proxy3/jdk.proxy3.$Proxy20.build(Unknown Source)
	at org.robolectric.internal.SandboxManager.getAndroidSandbox(SandboxManager.java:68)
	at org.robolectric.RobolectricTestRunner.getSandbox(RobolectricTestRunner.java:263)
	at org.robolectric.RobolectricTestRunner.getSandbox(RobolectricTestRunner.java:64)
	at org.robolectric.internal.SandboxTestRunner.sandboxGroupingClassBlock(SandboxTestRunner.java:140)
	at org.robolectric.internal.SandboxTestRunner.classBlock(SandboxTestRunner.java:119)
	at org.junit.runners.ParentRunner.run(ParentRunner.java:412)
	at org.robolectric.RobolectricTestRunner.run(RobolectricTestRunner.java:142)
	at org.junit.runner.JUnitCore.run(JUnitCore.java:137)
	at org.junit.runner.JUnitCore.run(JUnitCore.java:115)
	at org.gradle.api.internal.tasks.testing.junit.JUnitTestExecutor.runRequest(JUnitTestExecutor.java:175)
	at org.gradle.api.internal.tasks.testing.junit.JUnitTestExecutor.accept(JUnitTestExecutor.java:84)
	at org.gradle.api.internal.tasks.testing.junit.JUnitTestExecutor.accept(JUnitTestExecutor.java:47)
	at org.gradle.api.internal.tasks.testing.junit.AbstractJUnitTestDefinitionProcessor.processTestDefinition(AbstractJUnitTestDefinitionProcessor.java:65)
	at org.gradle.api.internal.tasks.testing.SuiteTestDefinitionProcessor.processTestDefinition(SuiteTestDefinitionProcessor.java:54)
	at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
	at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:77)
	at java.base/jdk.internal.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
	at java.base/java.lang.reflect.Method.invoke(Method.java:569)
	at org.gradle.internal.dispatch.MethodInvocation.invokeOn(MethodInvocation.java:77)
	at org.gradle.internal.dispatch.ReflectionDispatch.dispatch(ReflectionDispatch.java:28)
	at org.gradle.internal.dispatch.ReflectionDispatch.dispatch(ReflectionDispatch.java:19)
	at org.gradle.internal.dispatch.ContextClassLoaderDispatch.dispatch(ContextClassLoaderDispatch.java:33)
	at org.gradle.internal.dispatch.ProxyDispatchAdapter$DispatchingInvocationHandler.invoke(ProxyDispatchAdapter.java:88)
	at jdk.proxy1/jdk.proxy1.$Proxy4.processTestDefinition(Unknown Source)
	at org.gradle.api.internal.tasks.testing.worker.TestWorker$2.run(TestWorker.java:178)
	at org.gradle.api.internal.tasks.testing.worker.TestWorker.executeAndMaintainThreadName(TestWorker.java:126)
	at org.gradle.api.internal.tasks.testing.worker.TestWorker.execute(TestWorker.java:103)
	at org.gradle.api.internal.tasks.testing.worker.TestWorker.execute(TestWorker.java:63)
	at org.gradle.process.internal.worker.child.ActionExecutionWorker.execute(ActionExecutionWorker.java:56)
	at org.gradle.process.internal.worker.child.SystemApplicationClassLoaderWorker.call(SystemApplicationClassLoaderWorker.java:122)
	at org.gradle.process.internal.worker.child.SystemApplicationClassLoaderWorker.call(SystemApplicationClassLoaderWorker.java:72)
	at worker.org.gradle.process.internal.worker.GradleWorkerMain.run(GradleWorkerMain.java:69)
	at worker.org.gradle.process.internal.worker.GradleWorkerMain.main(GradleWorkerMain.java:74)
Caused by: java.util.concurrent.ExecutionException: java.io.IOException: Server returned HTTP response code: 429 for URL: https://repo1.maven.org/maven2/org/robolectric/android-all-instrumented/14-robolectric-10818077-i7/android-all-instrumented-14-robolectric-10818077-i7.jar
	at com.google.common.util.concurrent.AbstractFuture.getDoneValue(AbstractFuture.java:292)
	at com.google.common.util.concurrent.AbstractFutureState.blockingGet(AbstractFutureState.java:252)
	at com.google.common.util.concurrent.Platform.get(Platform.java:54)
	at com.google.common.util.concurrent.AbstractFuture.get(AbstractFuture.java:253)
	at com.google.common.util.concurrent.AbstractFuture$TrustedFuture.get(AbstractFuture.java:96)
	at org.robolectric.internal.dependency.MavenArtifactFetcher.fetchArtifact(MavenArtifactFetcher.java:121)
	... 59 more
Caused by: java.io.IOException: Server returned HTTP response code: 429 for URL: https://repo1.maven.org/maven2/org/robolectric/android-all-instrumented/14-robolectric-10818077-i7/android-all-instrumented-14-robolectric-10818077-i7.jar
	at java.base/sun.net.www.protocol.http.HttpURLConnection.getInputStream0(HttpURLConnection.java:2059)
	at java.base/sun.net.www.protocol.http.HttpURLConnection.getInputStream(HttpURLConnection.java:1644)
	at java.base/sun.net.www.protocol.https.HttpsURLConnectionImpl.getInputStream(HttpsURLConnectionImpl.java:224)
	at org.robolectric.internal.dependency.MavenArtifactFetcher$FetchToFileTask.call(MavenArtifactFetcher.java:251)
	at com.google.common.util.concurrent.TrustedListenableFutureTask$TrustedFutureInterruptibleAsyncTask.runInterruptibly(TrustedListenableFutureTask.java:164)
	at com.google.common.util.concurrent.TrustedListenableFutureTask$TrustedFutureInterruptibleAsyncTask.runInterruptibly(TrustedListenableFutureTask.java:147)
	at com.google.common.util.concurrent.InterruptibleTask.run(InterruptibleTask.java:74)
	at com.google.common.util.concurrent.TrustedListenableFutureTask.run(TrustedListenableFutureTask.java:80)
	at java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1136)
	at java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:635)
	at java.base/java.lang.Thread.run(Thread.java:840)
</failure>
  </testcase>
  <system-out><![CDATA[]]></system-out>
  <system-err><![CDATA[ERROR: Failed to fetch maven artifact org.robolectric:android-all-instrumented:14-robolectric-10818077-i7
java.util.concurrent.ExecutionException: java.io.IOException: Server returned HTTP response code: 429 for URL: https://repo1.maven.org/maven2/org/robolectric/android-all-instrumented/14-robolectric-10818077-i7/android-all-instrumented-14-robolectric-10818077-i7.jar
	at com.google.common.util.concurrent.AbstractFuture.getDoneValue(AbstractFuture.java:292)
	at com.google.common.util.concurrent.AbstractFutureState.blockingGet(AbstractFutureState.java:252)
	at com.google.common.util.concurrent.Platform.get(Platform.java:54)
	at com.google.common.util.concurrent.AbstractFuture.get(AbstractFuture.java:253)
	at com.google.common.util.concurrent.AbstractFuture$TrustedFuture.get(AbstractFuture.java:96)
	at org.robolectric.internal.dependency.MavenArtifactFetcher.fetchArtifact(MavenArtifactFetcher.java:121)
	at org.robolectric.internal.dependency.MavenDependencyResolver.lambda$getLocalArtifactUrls$0(MavenDependencyResolver.java:87)
	at org.robolectric.internal.dependency.MavenDependencyResolver.whileLocked(MavenDependencyResolver.java:107)
	at org.robolectric.internal.dependency.MavenDependencyResolver.getLocalArtifactUrls(MavenDependencyResolver.java:82)
	at org.robolectric.internal.dependency.MavenDependencyResolver.getLocalArtifactUrl(MavenDependencyResolver.java:123)
	at org.robolectric.plugins.LegacyDependencyResolver.getLocalArtifactUrl(LegacyDependencyResolver.java:95)
	at org.robolectric.plugins.DefaultSdkProvider$DefaultSdk.getJarPath(DefaultSdkProvider.java:141)
	at org.robolectric.internal.AndroidSandbox$SdkSandboxClassLoader.<init>(AndroidSandbox.java:128)
	at java.base/jdk.internal.reflect.NativeConstructorAccessorImpl.newInstance0(Native Method)
	at java.base/jdk.internal.reflect.NativeConstructorAccessorImpl.newInstance(NativeConstructorAccessorImpl.java:77)
	at java.base/jdk.internal.reflect.DelegatingConstructorAccessorImpl.newInstance(DelegatingConstructorAccessorImpl.java:45)
	at java.base/java.lang.reflect.Constructor.newInstanceWithCaller(Constructor.java:500)
	at java.base/java.lang.reflect.Constructor.newInstance(Constructor.java:481)
	at org.robolectric.util.inject.Injector.inject(Injector.java:258)
	at org.robolectric.util.inject.Injector.lambda$memoized$1(Injector.java:239)
	at org.robolectric.util.inject.Injector$MemoizingProvider.get(Injector.java:506)
	at org.robolectric.util.inject.Injector.getInstanceInternal(Injector.java:230)
	at org.robolectric.util.inject.Injector.resolveDependencies(Injector.java:305)
	at org.robolectric.util.inject.Injector.inject(Injector.java:256)
	at org.robolectric.util.inject.Injector.lambda$memoized$1(Injector.java:239)
	at org.robolectric.util.inject.Injector$MemoizingProvider.get(Injector.java:506)
	at org.robolectric.util.inject.Injector.getInstanceInternal(Injector.java:230)
	at org.robolectric.util.inject.Injector.getInstance(Injector.java:214)
	at org.robolectric.util.inject.Injector.access$700(Injector.java:96)
	at org.robolectric.util.inject.Injector$ScopeBuilderProvider.create(Injector.java:575)
	at org.robolectric.util.inject.Injector$ScopeBuilderProvider.lambda$get$0(Injector.java:558)
	at jdk.proxy3/jdk.proxy3.$Proxy20.build(Unknown Source)
	at org.robolectric.internal.SandboxManager.getAndroidSandbox(SandboxManager.java:68)
	at org.robolectric.RobolectricTestRunner.getSandbox(RobolectricTestRunner.java:263)
	at org.robolectric.RobolectricTestRunner.getSandbox(RobolectricTestRunner.java:64)
	at org.robolectric.internal.SandboxTestRunner.sandboxGroupingClassBlock(SandboxTestRunner.java:140)
	at org.robolectric.internal.SandboxTestRunner.classBlock(SandboxTestRunner.java:119)
	at org.junit.runners.ParentRunner.run(ParentRunner.java:412)
	at org.robolectric.RobolectricTestRunner.run(RobolectricTestRunner.java:142)
	at org.junit.runner.JUnitCore.run(JUnitCore.java:137)
	at org.junit.runner.JUnitCore.run(JUnitCore.java:115)
	at org.gradle.api.internal.tasks.testing.junit.JUnitTestExecutor.runRequest(JUnitTestExecutor.java:175)
	at org.gradle.api.internal.tasks.testing.junit.JUnitTestExecutor.accept(JUnitTestExecutor.java:84)
	at org.gradle.api.internal.tasks.testing.junit.JUnitTestExecutor.accept(JUnitTestExecutor.java:47)
	at org.gradle.api.internal.tasks.testing.junit.AbstractJUnitTestDefinitionProcessor.processTestDefinition(AbstractJUnitTestDefinitionProcessor.java:65)
	at org.gradle.api.internal.tasks.testing.SuiteTestDefinitionProcessor.processTestDefinition(SuiteTestDefinitionProcessor.java:54)
	at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
	at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:77)
	at java.base/jdk.internal.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
	at java.base/java.lang.reflect.Method.invoke(Method.java:569)
	at org.gradle.internal.dispatch.MethodInvocation.invokeOn(MethodInvocation.java:77)
	at org.gradle.internal.dispatch.ReflectionDispatch.dispatch(ReflectionDispatch.java:28)
	at org.gradle.internal.dispatch.ReflectionDispatch.dispatch(ReflectionDispatch.java:19)
	at org.gradle.internal.dispatch.ContextClassLoaderDispatch.dispatch(ContextClassLoaderDispatch.java:33)
	at org.gradle.internal.dispatch.ProxyDispatchAdapter$DispatchingInvocationHandler.invoke(ProxyDispatchAdapter.java:88)
	at jdk.proxy1/jdk.proxy1.$Proxy4.processTestDefinition(Unknown Source)
	at org.gradle.api.internal.tasks.testing.worker.TestWorker$2.run(TestWorker.java:178)
	at org.gradle.api.internal.tasks.testing.worker.TestWorker.executeAndMaintainThreadName(TestWorker.java:126)
	at org.gradle.api.internal.tasks.testing.worker.TestWorker.execute(TestWorker.java:103)
	at org.gradle.api.internal.tasks.testing.worker.TestWorker.execute(TestWorker.java:63)
	at org.gradle.process.internal.worker.child.ActionExecutionWorker.execute(ActionExecutionWorker.java:56)
	at org.gradle.process.internal.worker.child.SystemApplicationClassLoaderWorker.call(SystemApplicationClassLoaderWorker.java:122)
	at org.gradle.process.internal.worker.child.SystemApplicationClassLoaderWorker.call(SystemApplicationClassLoaderWorker.java:72)
	at worker.org.gradle.process.internal.worker.GradleWorkerMain.run(GradleWorkerMain.java:69)
	at worker.org.gradle.process.internal.worker.GradleWorkerMain.main(GradleWorkerMain.java:74)
Caused by: java.io.IOException: Server returned HTTP response code: 429 for URL: https://repo1.maven.org/maven2/org/robolectric/android-all-instrumented/14-robolectric-10818077-i7/android-all-instrumented-14-robolectric-10818077-i7.jar
	at java.base/sun.net.www.protocol.http.HttpURLConnection.getInputStream0(HttpURLConnection.java:2059)
	at java.base/sun.net.www.protocol.http.HttpURLConnection.getInputStream(HttpURLConnection.java:1644)
	at java.base/sun.net.www.protocol.https.HttpsURLConnectionImpl.getInputStream(HttpsURLConnectionImpl.java:224)
	at org.robolectric.internal.dependency.MavenArtifactFetcher$FetchToFileTask.call(MavenArtifactFetcher.java:251)
	at com.google.common.util.concurrent.TrustedListenableFutureTask$TrustedFutureInterruptibleAsyncTask.runInterruptibly(TrustedListenableFutureTask.java:164)
	at com.google.common.util.concurrent.TrustedListenableFutureTask$TrustedFutureInterruptibleAsyncTask.runInterruptibly(TrustedListenableFutureTask.java:147)
	at com.google.common.util.concurrent.InterruptibleTask.run(InterruptibleTask.java:74)
	at com.google.common.util.concurrent.TrustedListenableFutureTask.run(TrustedListenableFutureTask.java:80)
	at java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1136)
	at java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:635)
	at java.base/java.lang.Thread.run(Thread.java:840)
]]></system-err>
</testsuite>
---END app/build/test-results/testDebugUnitTest/TEST-com.example.data.document.LocalDocumentExtractorTest.xml---
=== app/build/test-results/testDebugUnitTest/TEST-com.example.data.repository.AiStudyPlanResponseTest.xml ===
<?xml version="1.0" encoding="UTF-8"?>
<testsuite name="com.example.data.repository.AiStudyPlanResponseTest" tests="1" skipped="0" failures="1" errors="0" timestamp="2026-11-24T14:45:57.458Z" hostname="ip-10-0-131-16" time="0.071">
  <properties/>
  <testcase name="classMethod" classname="com.example.data.repository.AiStudyPlanResponseTest" time="0.004">
    <failure message="java.lang.AssertionError: Failed to fetch maven artifact org.robolectric:android-all-instrumented:14-robolectric-10818077-i7" type="java.lang.AssertionError">java.lang.AssertionError: Failed to fetch maven artifact org.robolectric:android-all-instrumented:14-robolectric-10818077-i7
	at org.robolectric.internal.dependency.MavenArtifactFetcher.fetchArtifact(MavenArtifactFetcher.java:129)
	at org.robolectric.internal.dependency.MavenDependencyResolver.lambda$getLocalArtifactUrls$0(MavenDependencyResolver.java:87)
	at org.robolectric.internal.dependency.MavenDependencyResolver.whileLocked(MavenDependencyResolver.java:107)
	at org.robolectric.internal.dependency.MavenDependencyResolver.getLocalArtifactUrls(MavenDependencyResolver.java:82)
	at org.robolectric.internal.dependency.MavenDependencyResolver.getLocalArtifactUrl(MavenDependencyResolver.java:123)
	at org.robolectric.plugins.LegacyDependencyResolver.getLocalArtifactUrl(LegacyDependencyResolver.java:95)
	at org.robolectric.plugins.DefaultSdkProvider$DefaultSdk.getJarPath(DefaultSdkProvider.java:141)
	at org.robolectric.internal.AndroidSandbox$SdkSandboxClassLoader.&lt;init&gt;(AndroidSandbox.java:128)
	at java.base/jdk.internal.reflect.NativeConstructorAccessorImpl.newInstance0(Native Method)
	at java.base/jdk.internal.reflect.NativeConstructorAccessorImpl.newInstance(NativeConstructorAccessorImpl.java:77)
	at java.base/jdk.internal.reflect.DelegatingConstructorAccessorImpl.newInstance(DelegatingConstructorAccessorImpl.java:45)
	at java.base/java.lang.reflect.Constructor.newInstanceWithCaller(Constructor.java:500)
	at java.base/java.lang.reflect.Constructor.newInstance(Constructor.java:481)
	at org.robolectric.util.inject.Injector.inject(Injector.java:258)
	at org.robolectric.util.inject.Injector.lambda$memoized$1(Injector.java:239)
	at org.robolectric.util.inject.Injector$MemoizingProvider.get(Injector.java:506)
	at org.robolectric.util.inject.Injector.getInstanceInternal(Injector.java:230)
	at org.robolectric.util.inject.Injector.resolveDependencies(Injector.java:305)
	at org.robolectric.util.inject.Injector.inject(Injector.java:256)
	at org.robolectric.util.inject.Injector.lambda$memoized$1(Injector.java:239)
	at org.robolectric.util.inject.Injector$MemoizingProvider.get(Injector.java:506)
	at org.robolectric.util.inject.Injector.getInstanceInternal(Injector.java:230)
	at org.robolectric.util.inject.Injector.getInstance(Injector.java:214)
	at org.robolectric.util.inject.Injector.access$700(Injector.java:96)
	at org.robolectric.util.inject.Injector$ScopeBuilderProvider.create(Injector.java:575)
	at org.robolectric.util.inject.Injector$ScopeBuilderProvider.lambda$get$0(Injector.java:558)
	at jdk.proxy3/jdk.proxy3.$Proxy20.build(Unknown Source)
	at org.robolectric.internal.SandboxManager.getAndroidSandbox(SandboxManager.java:68)
	at org.robolectric.RobolectricTestRunner.getSandbox(RobolectricTestRunner.java:263)
	at org.robolectric.RobolectricTestRunner.getSandbox(RobolectricTestRunner.java:64)
	at org.robolectric.internal.SandboxTestRunner.sandboxGroupingClassBlock(SandboxTestRunner.java:140)
	at org.robolectric.internal.SandboxTestRunner.classBlock(SandboxTestRunner.java:119)
	at org.junit.runners.ParentRunner.run(ParentRunner.java:412)
	at org.robolectric.RobolectricTestRunner.run(RobolectricTestRunner.java:142)
	at org.junit.runner.JUnitCore.run(JUnitCore.java:137)
	at org.junit.runner.JUnitCore.run(JUnitCore.java:115)
	at org.gradle.api.internal.tasks.testing.junit.JUnitTestExecutor.runRequest(JUnitTestExecutor.java:175)
	at org.gradle.api.internal.tasks.testing.junit.JUnitTestExecutor.accept(JUnitTestExecutor.java:84)
	at org.gradle.api.internal.tasks.testing.junit.JUnitTestExecutor.accept(JUnitTestExecutor.java:47)
	at org.gradle.api.internal.tasks.testing.junit.AbstractJUnitTestDefinitionProcessor.processTestDefinition(AbstractJUnitTestDefinitionProcessor.java:65)
	at org.gradle.api.internal.tasks.testing.SuiteTestDefinitionProcessor.processTestDefinition(SuiteTestDefinitionProcessor.java:54)
	at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
	at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:77)
	at java.base/jdk.internal.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
	at java.base/java.lang.reflect.Method.invoke(Method.java:569)
	at org.gradle.internal.dispatch.MethodInvocation.invokeOn(MethodInvocation.java:77)
	at org.gradle.internal.dispatch.ReflectionDispatch.dispatch(ReflectionDispatch.java:28)
	at org.gradle.internal.dispatch.ReflectionDispatch.dispatch(ReflectionDispatch.java:19)
	at org.gradle.internal.dispatch.ContextClassLoaderDispatch.dispatch(ContextClassLoaderDispatch.java:33)
	at org.gradle.internal.dispatch.ProxyDispatchAdapter$DispatchingInvocationHandler.invoke(ProxyDispatchAdapter.java:88)
	at jdk.proxy1/jdk.proxy1.$Proxy4.processTestDefinition(Unknown Source)
	at org.gradle.api.internal.tasks.testing.worker.TestWorker$2.run(TestWorker.java:178)
	at org.gradle.api.internal.tasks.testing.worker.TestWorker.executeAndMaintainThreadName(TestWorker.java:126)
	at org.gradle.api.internal.tasks.testing.worker.TestWorker.execute(TestWorker.java:103)
	at org.gradle.api.internal.tasks.testing.worker.TestWorker.execute(TestWorker.java:63)
	at org.gradle.process.internal.worker.child.ActionExecutionWorker.execute(ActionExecutionWorker.java:56)
	at org.gradle.process.internal.worker.child.SystemApplicationClassLoaderWorker.call(SystemApplicationClassLoaderWorker.java:122)
	at org.gradle.process.internal.worker.child.SystemApplicationClassLoaderWorker.call(SystemApplicationClassLoaderWorker.java:72)
	at worker.org.gradle.process.internal.worker.GradleWorkerMain.run(GradleWorkerMain.java:69)
	at worker.org.gradle.process.internal.worker.GradleWorkerMain.main(GradleWorkerMain.java:74)
Caused by: java.util.concurrent.ExecutionException: java.io.IOException: Server returned HTTP response code: 429 for URL: https://repo1.maven.org/maven2/org/robolectric/android-all-instrumented/14-robolectric-10818077-i7/android-all-instrumented-14-robolectric-10818077-i7.jar
	at com.google.common.util.concurrent.AbstractFuture.getDoneValue(AbstractFuture.java:292)
	at com.google.common.util.concurrent.AbstractFutureState.blockingGet(AbstractFutureState.java:252)
	at com.google.common.util.concurrent.Platform.get(Platform.java:54)
	at com.google.common.util.concurrent.AbstractFuture.get(AbstractFuture.java:253)
	at com.google.common.util.concurrent.AbstractFuture$TrustedFuture.get(AbstractFuture.java:96)
	at org.robolectric.internal.dependency.MavenArtifactFetcher.fetchArtifact(MavenArtifactFetcher.java:121)
	... 59 more
Caused by: java.io.IOException: Server returned HTTP response code: 429 for URL: https://repo1.maven.org/maven2/org/robolectric/android-all-instrumented/14-robolectric-10818077-i7/android-all-instrumented-14-robolectric-10818077-i7.jar
	at java.base/sun.net.www.protocol.http.HttpURLConnection.getInputStream0(HttpURLConnection.java:2059)
	at java.base/sun.net.www.protocol.http.HttpURLConnection.getInputStream(HttpURLConnection.java:1644)
	at java.base/sun.net.www.protocol.https.HttpsURLConnectionImpl.getInputStream(HttpsURLConnectionImpl.java:224)
	at org.robolectric.internal.dependency.MavenArtifactFetcher$FetchToFileTask.call(MavenArtifactFetcher.java:251)
	at com.google.common.util.concurrent.TrustedListenableFutureTask$TrustedFutureInterruptibleAsyncTask.runInterruptibly(TrustedListenableFutureTask.java:164)
	at com.google.common.util.concurrent.TrustedListenableFutureTask$TrustedFutureInterruptibleAsyncTask.runInterruptibly(TrustedListenableFutureTask.java:147)
	at com.google.common.util.concurrent.InterruptibleTask.run(InterruptibleTask.java:74)
	at com.google.common.util.concurrent.TrustedListenableFutureTask.run(TrustedListenableFutureTask.java:80)
	at java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1136)
	at java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:635)
	at java.base/java.lang.Thread.run(Thread.java:840)
</failure>
  </testcase>
  <system-out><![CDATA[]]></system-out>
  <system-err><![CDATA[ERROR: Failed to fetch maven artifact org.robolectric:android-all-instrumented:14-robolectric-10818077-i7
java.util.concurrent.ExecutionException: java.io.IOException: Server returned HTTP response code: 429 for URL: https://repo1.maven.org/maven2/org/robolectric/android-all-instrumented/14-robolectric-10818077-i7/android-all-instrumented-14-robolectric-10818077-i7.jar
	at com.google.common.util.concurrent.AbstractFuture.getDoneValue(AbstractFuture.java:292)
	at com.google.common.util.concurrent.AbstractFutureState.blockingGet(AbstractFutureState.java:252)
	at com.google.common.util.concurrent.Platform.get(Platform.java:54)
	at com.google.common.util.concurrent.AbstractFuture.get(AbstractFuture.java:253)
	at com.google.common.util.concurrent.AbstractFuture$TrustedFuture.get(AbstractFuture.java:96)
	at org.robolectric.internal.dependency.MavenArtifactFetcher.fetchArtifact(MavenArtifactFetcher.java:121)
	at org.robolectric.internal.dependency.MavenDependencyResolver.lambda$getLocalArtifactUrls$0(MavenDependencyResolver.java:87)
	at org.robolectric.internal.dependency.MavenDependencyResolver.whileLocked(MavenDependencyResolver.java:107)
	at org.robolectric.internal.dependency.MavenDependencyResolver.getLocalArtifactUrls(MavenDependencyResolver.java:82)
	at org.robolectric.internal.dependency.MavenDependencyResolver.getLocalArtifactUrl(MavenDependencyResolver.java:123)
	at org.robolectric.plugins.LegacyDependencyResolver.getLocalArtifactUrl(LegacyDependencyResolver.java:95)
	at org.robolectric.plugins.DefaultSdkProvider$DefaultSdk.getJarPath(DefaultSdkProvider.java:141)
	at org.robolectric.internal.AndroidSandbox$SdkSandboxClassLoader.<init>(AndroidSandbox.java:128)
	at java.base/jdk.internal.reflect.NativeConstructorAccessorImpl.newInstance0(Native Method)
	at java.base/jdk.internal.reflect.NativeConstructorAccessorImpl.newInstance(NativeConstructorAccessorImpl.java:77)
	at java.base/jdk.internal.reflect.DelegatingConstructorAccessorImpl.newInstance(DelegatingConstructorAccessorImpl.java:45)
	at java.base/java.lang.reflect.Constructor.newInstanceWithCaller(Constructor.java:500)
	at java.base/java.lang.reflect.Constructor.newInstance(Constructor.java:481)
	at org.robolectric.util.inject.Injector.inject(Injector.java:258)
	at org.robolectric.util.inject.Injector.lambda$memoized$1(Injector.java:239)
	at org.robolectric.util.inject.Injector$MemoizingProvider.get(Injector.java:506)
	at org.robolectric.util.inject.Injector.getInstanceInternal(Injector.java:230)
	at org.robolectric.util.inject.Injector.resolveDependencies(Injector.java:305)
	at org.robolectric.util.inject.Injector.inject(Injector.java:256)
	at org.robolectric.util.inject.Injector.lambda$memoized$1(Injector.java:239)
	at org.robolectric.util.inject.Injector$MemoizingProvider.get(Injector.java:506)
	at org.robolectric.util.inject.Injector.getInstanceInternal(Injector.java:230)
	at org.robolectric.util.inject.Injector.getInstance(Injector.java:214)
	at org.robolectric.util.inject.Injector.access$700(Injector.java:96)
	at org.robolectric.util.inject.Injector$ScopeBuilderProvider.create(Injector.java:575)
	at org.robolectric.util.inject.Injector$ScopeBuilderProvider.lambda$get$0(Injector.java:558)
	at jdk.proxy3/jdk.proxy3.$Proxy20.build(Unknown Source)
	at org.robolectric.internal.SandboxManager.getAndroidSandbox(SandboxManager.java:68)
	at org.robolectric.RobolectricTestRunner.getSandbox(RobolectricTestRunner.java:263)
	at org.robolectric.RobolectricTestRunner.getSandbox(RobolectricTestRunner.java:64)
	at org.robolectric.internal.SandboxTestRunner.sandboxGroupingClassBlock(SandboxTestRunner.java:140)
	at org.robolectric.internal.SandboxTestRunner.classBlock(SandboxTestRunner.java:119)
	at org.junit.runners.ParentRunner.run(ParentRunner.java:412)
	at org.robolectric.RobolectricTestRunner.run(RobolectricTestRunner.java:142)
	at org.junit.runner.JUnitCore.run(JUnitCore.java:137)
	at org.junit.runner.JUnitCore.run(JUnitCore.java:115)
	at org.gradle.api.internal.tasks.testing.junit.JUnitTestExecutor.runRequest(JUnitTestExecutor.java:175)
	at org.gradle.api.internal.tasks.testing.junit.JUnitTestExecutor.accept(JUnitTestExecutor.java:84)
	at org.gradle.api.internal.tasks.testing.junit.JUnitTestExecutor.accept(JUnitTestExecutor.java:47)
	at org.gradle.api.internal.tasks.testing.junit.AbstractJUnitTestDefinitionProcessor.processTestDefinition(AbstractJUnitTestDefinitionProcessor.java:65)
	at org.gradle.api.internal.tasks.testing.SuiteTestDefinitionProcessor.processTestDefinition(SuiteTestDefinitionProcessor.java:54)
	at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
	at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:77)
	at java.base/jdk.internal.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
	at java.base/java.lang.reflect.Method.invoke(Method.java:569)
	at org.gradle.internal.dispatch.MethodInvocation.invokeOn(MethodInvocation.java:77)
	at org.gradle.internal.dispatch.ReflectionDispatch.dispatch(ReflectionDispatch.java:28)
	at org.gradle.internal.dispatch.ReflectionDispatch.dispatch(ReflectionDispatch.java:19)
	at org.gradle.internal.dispatch.ContextClassLoaderDispatch.dispatch(ContextClassLoaderDispatch.java:33)
	at org.gradle.internal.dispatch.ProxyDispatchAdapter$DispatchingInvocationHandler.invoke(ProxyDispatchAdapter.java:88)
	at jdk.proxy1/jdk.proxy1.$Proxy4.processTestDefinition(Unknown Source)
	at org.gradle.api.internal.tasks.testing.worker.TestWorker$2.run(TestWorker.java:178)
	at org.gradle.api.internal.tasks.testing.worker.TestWorker.executeAndMaintainThreadName(TestWorker.java:126)
	at org.gradle.api.internal.tasks.testing.worker.TestWorker.execute(TestWorker.java:103)
	at org.gradle.api.internal.tasks.testing.worker.TestWorker.execute(TestWorker.java:63)
	at org.gradle.process.internal.worker.child.ActionExecutionWorker.execute(ActionExecutionWorker.java:56)
	at org.gradle.process.internal.worker.child.SystemApplicationClassLoaderWorker.call(SystemApplicationClassLoaderWorker.java:122)
	at org.gradle.process.internal.worker.child.SystemApplicationClassLoaderWorker.call(SystemApplicationClassLoaderWorker.java:72)
	at worker.org.gradle.process.internal.worker.GradleWorkerMain.run(GradleWorkerMain.java:69)
	at worker.org.gradle.process.internal.worker.GradleWorkerMain.main(GradleWorkerMain.java:74)
Caused by: java.io.IOException: Server returned HTTP response code: 429 for URL: https://repo1.maven.org/maven2/org/robolectric/android-all-instrumented/14-robolectric-10818077-i7/android-all-instrumented-14-robolectric-10818077-i7.jar
	at java.base/sun.net.www.protocol.http.HttpURLConnection.getInputStream0(HttpURLConnection.java:2059)
	at java.base/sun.net.www.protocol.http.HttpURLConnection.getInputStream(HttpURLConnection.java:1644)
	at java.base/sun.net.www.protocol.https.HttpsURLConnectionImpl.getInputStream(HttpsURLConnectionImpl.java:224)
	at org.robolectric.internal.dependency.MavenArtifactFetcher$FetchToFileTask.call(MavenArtifactFetcher.java:251)
	at com.google.common.util.concurrent.TrustedListenableFutureTask$TrustedFutureInterruptibleAsyncTask.runInterruptibly(TrustedListenableFutureTask.java:164)
	at com.google.common.util.concurrent.TrustedListenableFutureTask$TrustedFutureInterruptibleAsyncTask.runInterruptibly(TrustedListenableFutureTask.java:147)
	at com.google.common.util.concurrent.InterruptibleTask.run(InterruptibleTask.java:74)
	at com.google.common.util.concurrent.TrustedListenableFutureTask.run(TrustedListenableFutureTask.java:80)
	at java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1136)
	at java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:635)
	at java.base/java.lang.Thread.run(Thread.java:840)
]]></system-err>
</testsuite>
---END app/build/test-results/testDebugUnitTest/TEST-com.example.data.repository.AiStudyPlanResponseTest.xml---
=== app/build/test-results/testDebugUnitTest/TEST-com.example.ui.screens.AgentChatAndSettingsUiTest.xml ===
<?xml version="1.0" encoding="UTF-8"?>
<testsuite name="com.example.ui.screens.AgentChatAndSettingsUiTest" tests="2" skipped="0" failures="1" errors="0" timestamp="2026-11-24T14:46:17.377Z" hostname="ip-10-0-131-16" time="2.257">
  <properties/>
  <testcase name="agentChat_menuUsesSkillForOneMessage_andDrawerListsThreads" classname="com.example.ui.screens.AgentChatAndSettingsUiTest" time="1.334"/>
  <testcase name="settingsScreen_themesAndGradientsAreCollapsedUntilExpanded" classname="com.example.ui.screens.AgentChatAndSettingsUiTest" time="0.92">
    <failure message="java.lang.AssertionError: No node found that matches TestTag = 'settings_expand_themes' in scrollable container&#10;Semantics of the node:&#10;Node #413 at (l=0.0, t=0.0, r=320.0, b=470.0)px, Tag: 'settings_list'&#10;IsTraversalGroup = 'true'&#10;VerticalScrollAxisRange = 'ScrollAxisRange(value=3051.0, maxValue=3051.0, reverseScrolling=false)'&#10;CollectionInfo = 'androidx.compose.ui.semantics.CollectionInfo@6677c951'&#10;Actions = [ScrollBy, ScrollByOffset, IndexForKey, ScrollToIndex, GetScrollViewportLength]&#10;Has 4 children&#10;Selector used: (TestTag = 'settings_list')&#10;" type="java.lang.AssertionError">java.lang.AssertionError: No node found that matches TestTag = 'settings_expand_themes' in scrollable container
Semantics of the node:
Node #413 at (l=0.0, t=0.0, r=320.0, b=470.0)px, Tag: 'settings_list'
IsTraversalGroup = 'true'
VerticalScrollAxisRange = 'ScrollAxisRange(value=3051.0, maxValue=3051.0, reverseScrolling=false)'
CollectionInfo = 'androidx.compose.ui.semantics.CollectionInfo@6677c951'
Actions = [ScrollBy, ScrollByOffset, IndexForKey, ScrollToIndex, GetScrollViewportLength]
Has 4 children
Selector used: (TestTag = 'settings_list')

	at androidx.compose.ui.test.ActionsKt.performScrollToNode(Actions.kt:264)
	at com.example.ui.screens.AgentChatAndSettingsUiTest.settingsScreen_themesAndGradientsAreCollapsedUntilExpanded(AgentChatAndSettingsUiTest.kt:161)
	at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
	at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:77)
	at java.base/jdk.internal.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
	at java.base/java.lang.reflect.Method.invoke(Method.java:569)
	at org.junit.runners.model.FrameworkMethod$1.runReflectiveCall(FrameworkMethod.java:59)
	at org.junit.internal.runners.model.ReflectiveCallable.run(ReflectiveCallable.java:12)
	at org.junit.runners.model.FrameworkMethod.invokeExplosively(FrameworkMethod.java:56)
	at org.junit.internal.runners.statements.InvokeMethod.evaluate(InvokeMethod.java:17)
	at org.junit.rules.ExternalResource$1.evaluate(ExternalResource.java:54)
	at androidx.compose.ui.test.junit4.AndroidComposeTestRule$apply$1$evaluate$1.invoke(AndroidComposeTestRule.android.kt:272)
	at androidx.compose.ui.test.junit4.AndroidComposeTestRule$apply$1$evaluate$1.invoke(AndroidComposeTestRule.android.kt:271)
	at androidx.compose.ui.test.AndroidComposeUiTestEnvironment$AndroidComposeUiTestImpl.withDisposableContent(ComposeUiTest.android.kt:505)
	at androidx.compose.ui.test.AndroidComposeUiTestEnvironment$runTest$1$1$1$1$1$1.invoke(ComposeUiTest.android.kt:333)
	at androidx.compose.ui.test.AndroidComposeUiTestEnvironment.withComposeIdlingResource(ComposeUiTest.android.kt:385)
	at androidx.compose.ui.test.AndroidComposeUiTestEnvironment.access$withComposeIdlingResource(ComposeUiTest.android.kt:219)
	at androidx.compose.ui.test.AndroidComposeUiTestEnvironment$runTest$1$1$1$1$1.invoke(ComposeUiTest.android.kt:332)
	at androidx.compose.ui.test.AndroidComposeUiTestEnvironment.withWindowRecomposer(ComposeUiTest.android.kt:359)
	at androidx.compose.ui.test.AndroidComposeUiTestEnvironment.access$withWindowRecomposer(ComposeUiTest.android.kt:219)
	at androidx.compose.ui.test.AndroidComposeUiTestEnvironment$runTest$1$1$1$1.invoke(ComposeUiTest.android.kt:331)
	at androidx.compose.ui.test.AndroidComposeUiTestEnvironment.withTestCoroutines(ComposeUiTest.android.kt:372)
	at androidx.compose.ui.test.AndroidComposeUiTestEnvironment.access$withTestCoroutines(ComposeUiTest.android.kt:219)
	at androidx.compose.ui.test.AndroidComposeUiTestEnvironment$runTest$1$1$1.invoke(ComposeUiTest.android.kt:330)
	at androidx.compose.ui.test.IdlingStrategy.withStrategy(IdlingStrategy.android.kt:52)
	at androidx.compose.ui.test.AndroidComposeUiTestEnvironment$runTest$1$1.invoke(ComposeUiTest.android.kt:329)
	at androidx.compose.ui.test.IdlingResourceRegistry.withRegistry(IdlingResourceRegistry.jvm.kt:155)
	at androidx.compose.ui.test.AndroidComposeUiTestEnvironment$runTest$1.invoke(ComposeUiTest.android.kt:328)
	at androidx.compose.ui.test.ComposeRootRegistry.withRegistry(ComposeRootRegistry.android.kt:146)
	at androidx.compose.ui.test.AndroidComposeUiTestEnvironment.runTest(ComposeUiTest.android.kt:327)
	at androidx.compose.ui.test.junit4.AndroidComposeTestRule$apply$1.evaluate(AndroidComposeTestRule.android.kt:271)
	at org.junit.runners.ParentRunner$3.evaluate(ParentRunner.java:306)
	at org.robolectric.RobolectricTestRunner$HelperTestRunner$1.evaluate(RobolectricTestRunner.java:524)
	at org.robolectric.internal.SandboxTestRunner.executeInSandbox(SandboxTestRunner.java:494)
	at org.robolectric.internal.SandboxTestRunner.access$900(SandboxTestRunner.java:67)
	at org.robolectric.internal.SandboxTestRunner$7.evaluate(SandboxTestRunner.java:442)
	at org.junit.runners.BlockJUnit4ClassRunner$1.evaluate(BlockJUnit4ClassRunner.java:100)
	at org.junit.runners.ParentRunner.runLeaf(ParentRunner.java:366)
	at org.junit.runners.BlockJUnit4ClassRunner.runChild(BlockJUnit4ClassRunner.java:103)
	at org.robolectric.internal.SandboxTestRunner.access$600(SandboxTestRunner.java:67)
	at org.robolectric.internal.SandboxTestRunner$6.evaluate(SandboxTestRunner.java:333)
	at org.robolectric.internal.SandboxTestRunner$3.evaluate(SandboxTestRunner.java:233)
	at org.robolectric.internal.SandboxTestRunner$5.lambda$evaluate$0(SandboxTestRunner.java:317)
	at org.robolectric.internal.bytecode.Sandbox.lambda$runOnMainThread$0(Sandbox.java:101)
	at java.base/java.util.concurrent.FutureTask.run(FutureTask.java:264)
	at java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1136)
	at java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:635)
	at java.base/java.lang.Thread.run(Thread.java:840)
</failure>
  </testcase>
  <system-out><![CDATA[]]></system-out>
  <system-err><![CDATA[]]></system-err>
</testsuite>
---END app/build/test-results/testDebugUnitTest/TEST-com.example.ui.screens.AgentChatAndSettingsUiTest.xml---
=== app/build/test-results/testDebugUnitTest/TEST-com.example.ui.screens.FocusAnalyticsAndSettingsUiTest.xml ===
<?xml version="1.0" encoding="UTF-8"?>
<testsuite name="com.example.ui.screens.FocusAnalyticsAndSettingsUiTest" tests="8" skipped="0" failures="1" errors="0" timestamp="2026-11-24T14:46:19.646Z" hostname="ip-10-0-131-16" time="2.596">
  <properties/>
  <testcase name="progressScreen_preservesExistingChartsAndAddsHeatmapRadarVelocityAndMatrix" classname="com.example.ui.screens.FocusAnalyticsAndSettingsUiTest" time="0.578"/>
  <testcase name="focusAnalyticsScreen_displaysLineChartKpisAndSwitchesTimeWindows" classname="com.example.ui.screens.FocusAnalyticsAndSettingsUiTest" time="0.405"/>
  <testcase name="redesignedSettingsScreen_displaysGroupedCardsAndOpensDetailSheets" classname="com.example.ui.screens.FocusAnalyticsAndSettingsUiTest" time="0.408">
    <failure message="java.lang.AssertionError: Assert failed: The component is not displayed!" type="java.lang.AssertionError">java.lang.AssertionError: Assert failed: The component is not displayed!
	at androidx.compose.ui.test.AssertionsKt.assertIsDisplayed(Assertions.kt:34)
	at com.example.ui.screens.FocusAnalyticsAndSettingsUiTest.redesignedSettingsScreen_displaysGroupedCardsAndOpensDetailSheets(FocusAnalyticsAndSettingsUiTest.kt:178)
	at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
	at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:77)
	at java.base/jdk.internal.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
	at java.base/java.lang.reflect.Method.invoke(Method.java:569)
	at org.junit.runners.model.FrameworkMethod$1.runReflectiveCall(FrameworkMethod.java:59)
	at org.junit.internal.runners.model.ReflectiveCallable.run(ReflectiveCallable.java:12)
	at org.junit.runners.model.FrameworkMethod.invokeExplosively(FrameworkMethod.java:56)
	at org.junit.internal.runners.statements.InvokeMethod.evaluate(InvokeMethod.java:17)
	at org.junit.rules.ExternalResource$1.evaluate(ExternalResource.java:54)
	at androidx.compose.ui.test.junit4.AndroidComposeTestRule$apply$1$evaluate$1.invoke(AndroidComposeTestRule.android.kt:272)
	at androidx.compose.ui.test.junit4.AndroidComposeTestRule$apply$1$evaluate$1.invoke(AndroidComposeTestRule.android.kt:271)
	at androidx.compose.ui.test.AndroidComposeUiTestEnvironment$AndroidComposeUiTestImpl.withDisposableContent(ComposeUiTest.android.kt:505)
	at androidx.compose.ui.test.AndroidComposeUiTestEnvironment$runTest$1$1$1$1$1$1.invoke(ComposeUiTest.android.kt:333)
	at androidx.compose.ui.test.AndroidComposeUiTestEnvironment.withComposeIdlingResource(ComposeUiTest.android.kt:385)
	at androidx.compose.ui.test.AndroidComposeUiTestEnvironment.access$withComposeIdlingResource(ComposeUiTest.android.kt:219)
	at androidx.compose.ui.test.AndroidComposeUiTestEnvironment$runTest$1$1$1$1$1.invoke(ComposeUiTest.android.kt:332)
	at androidx.compose.ui.test.AndroidComposeUiTestEnvironment.withWindowRecomposer(ComposeUiTest.android.kt:359)
	at androidx.compose.ui.test.AndroidComposeUiTestEnvironment.access$withWindowRecomposer(ComposeUiTest.android.kt:219)
	at androidx.compose.ui.test.AndroidComposeUiTestEnvironment$runTest$1$1$1$1.invoke(ComposeUiTest.android.kt:331)
	at androidx.compose.ui.test.AndroidComposeUiTestEnvironment.withTestCoroutines(ComposeUiTest.android.kt:372)
	at androidx.compose.ui.test.AndroidComposeUiTestEnvironment.access$withTestCoroutines(ComposeUiTest.android.kt:219)
	at androidx.compose.ui.test.AndroidComposeUiTestEnvironment$runTest$1$1$1.invoke(ComposeUiTest.android.kt:330)
	at androidx.compose.ui.test.IdlingStrategy.withStrategy(IdlingStrategy.android.kt:52)
	at androidx.compose.ui.test.AndroidComposeUiTestEnvironment$runTest$1$1.invoke(ComposeUiTest.android.kt:329)
	at androidx.compose.ui.test.IdlingResourceRegistry.withRegistry(IdlingResourceRegistry.jvm.kt:155)
	at androidx.compose.ui.test.AndroidComposeUiTestEnvironment$runTest$1.invoke(ComposeUiTest.android.kt:328)
	at androidx.compose.ui.test.ComposeRootRegistry.withRegistry(ComposeRootRegistry.android.kt:146)
	at androidx.compose.ui.test.AndroidComposeUiTestEnvironment.runTest(ComposeUiTest.android.kt:327)
	at androidx.compose.ui.test.junit4.AndroidComposeTestRule$apply$1.evaluate(AndroidComposeTestRule.android.kt:271)
	at org.junit.runners.ParentRunner$3.evaluate(ParentRunner.java:306)
	at org.robolectric.RobolectricTestRunner$HelperTestRunner$1.evaluate(RobolectricTestRunner.java:524)
	at org.robolectric.internal.SandboxTestRunner.executeInSandbox(SandboxTestRunner.java:494)
	at org.robolectric.internal.SandboxTestRunner.access$900(SandboxTestRunner.java:67)
	at org.robolectric.internal.SandboxTestRunner$7.evaluate(SandboxTestRunner.java:442)
	at org.junit.runners.BlockJUnit4ClassRunner$1.evaluate(BlockJUnit4ClassRunner.java:100)
	at org.junit.runners.ParentRunner.runLeaf(ParentRunner.java:366)
	at org.junit.runners.BlockJUnit4ClassRunner.runChild(BlockJUnit4ClassRunner.java:103)
	at org.robolectric.internal.SandboxTestRunner.access$600(SandboxTestRunner.java:67)
	at org.robolectric.internal.SandboxTestRunner$6.evaluate(SandboxTestRunner.java:333)
	at org.robolectric.internal.SandboxTestRunner$3.evaluate(SandboxTestRunner.java:233)
	at org.robolectric.internal.SandboxTestRunner$5.lambda$evaluate$0(SandboxTestRunner.java:317)
	at org.robolectric.internal.bytecode.Sandbox.lambda$runOnMainThread$0(Sandbox.java:101)
	at java.base/java.util.concurrent.FutureTask.run(FutureTask.java:264)
	at java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1136)
	at java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:635)
	at java.base/java.lang.Thread.run(Thread.java:840)
</failure>
  </testcase>
  <testcase name="moreHubScreen_exposesFocusAnalyticsNavigationEntry" classname="com.example.ui.screens.FocusAnalyticsAndSettingsUiTest" time="0.165"/>
  <testcase name="premiumChartAggregations_computeHeatmapStackedModesRadarAndMatrixAccurately" classname="com.example.ui.screens.FocusAnalyticsAndSettingsUiTest" time="0.092"/>
  <testcase name="settingsScreen_rendersCleanlyInOledAndGlassDesignSystem" classname="com.example.ui.screens.FocusAnalyticsAndSettingsUiTest" time="0.476"/>
  <testcase name="focusAnalyticsScreen_displaysPremiumHeatmapStackedRadarAndDepthCharts" classname="com.example.ui.screens.FocusAnalyticsAndSettingsUiTest" time="0.372"/>
  <testcase name="aggregateDailyFocusPoints_andComputeStreak_excludeBreaksAndSumCorrectly" classname="com.example.ui.screens.FocusAnalyticsAndSettingsUiTest" time="0.088"/>
  <system-out><![CDATA[]]></system-out>
  <system-err><![CDATA[]]></system-err>
</testsuite>
---END app/build/test-results/testDebugUnitTest/TEST-com.example.ui.screens.FocusAnalyticsAndSettingsUiTest.xml---
=== app/build/test-results/testDebugUnitTest/TEST-com.example.ui.screens.FocusAndDrawerUiTest.xml ===
<?xml version="1.0" encoding="UTF-8"?>
<testsuite name="com.example.ui.screens.FocusAndDrawerUiTest" tests="3" skipped="0" failures="1" errors="0" timestamp="2026-11-24T14:46:22.267Z" hostname="ip-10-0-131-16" time="5.208">
  <properties/>
  <testcase name="settingsScreen_containsCollapsibleFocusUniverseAndAudioSection" classname="com.example.ui.screens.FocusAndDrawerUiTest" time="0.398">
    <failure message="java.lang.AssertionError: No node found that matches TestTag = 'settings_expand_focus_universe' in scrollable container&#10;Semantics of the node:&#10;Node #1774 at (l=0.0, t=0.0, r=1233.0, b=2745.0)px, Tag: 'settings_list'&#10;IsTraversalGroup = 'true'&#10;VerticalScrollAxisRange = 'ScrollAxisRange(value=2497.0, maxValue=2497.0, reverseScrolling=false)'&#10;CollectionInfo = 'androidx.compose.ui.semantics.CollectionInfo@4464a7fb'&#10;Actions = [ScrollBy, ScrollByOffset, IndexForKey, ScrollToIndex, GetScrollViewportLength]&#10;Has 6 children&#10;Selector used: (TestTag = 'settings_list')&#10;" type="java.lang.AssertionError">java.lang.AssertionError: No node found that matches TestTag = 'settings_expand_focus_universe' in scrollable container
Semantics of the node:
Node #1774 at (l=0.0, t=0.0, r=1233.0, b=2745.0)px, Tag: 'settings_list'
IsTraversalGroup = 'true'
VerticalScrollAxisRange = 'ScrollAxisRange(value=2497.0, maxValue=2497.0, reverseScrolling=false)'
CollectionInfo = 'androidx.compose.ui.semantics.CollectionInfo@4464a7fb'
Actions = [ScrollBy, ScrollByOffset, IndexForKey, ScrollToIndex, GetScrollViewportLength]
Has 6 children
Selector used: (TestTag = 'settings_list')

	at androidx.compose.ui.test.ActionsKt.performScrollToNode(Actions.kt:264)
	at com.example.ui.screens.FocusAndDrawerUiTest.settingsScreen_containsCollapsibleFocusUniverseAndAudioSection(FocusAndDrawerUiTest.kt:267)
	at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
	at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:77)
	at java.base/jdk.internal.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
	at java.base/java.lang.reflect.Method.invoke(Method.java:569)
	at org.junit.runners.model.FrameworkMethod$1.runReflectiveCall(FrameworkMethod.java:59)
	at org.junit.internal.runners.model.ReflectiveCallable.run(ReflectiveCallable.java:12)
	at org.junit.runners.model.FrameworkMethod.invokeExplosively(FrameworkMethod.java:56)
	at org.junit.internal.runners.statements.InvokeMethod.evaluate(InvokeMethod.java:17)
	at org.junit.rules.ExternalResource$1.evaluate(ExternalResource.java:54)
	at androidx.compose.ui.test.junit4.AndroidComposeTestRule$apply$1$evaluate$1.invoke(AndroidComposeTestRule.android.kt:272)
	at androidx.compose.ui.test.junit4.AndroidComposeTestRule$apply$1$evaluate$1.invoke(AndroidComposeTestRule.android.kt:271)
	at androidx.compose.ui.test.AndroidComposeUiTestEnvironment$AndroidComposeUiTestImpl.withDisposableContent(ComposeUiTest.android.kt:505)
	at androidx.compose.ui.test.AndroidComposeUiTestEnvironment$runTest$1$1$1$1$1$1.invoke(ComposeUiTest.android.kt:333)
	at androidx.compose.ui.test.AndroidComposeUiTestEnvironment.withComposeIdlingResource(ComposeUiTest.android.kt:385)
	at androidx.compose.ui.test.AndroidComposeUiTestEnvironment.access$withComposeIdlingResource(ComposeUiTest.android.kt:219)
	at androidx.compose.ui.test.AndroidComposeUiTestEnvironment$runTest$1$1$1$1$1.invoke(ComposeUiTest.android.kt:332)
	at androidx.compose.ui.test.AndroidComposeUiTestEnvironment.withWindowRecomposer(ComposeUiTest.android.kt:359)
	at androidx.compose.ui.test.AndroidComposeUiTestEnvironment.access$withWindowRecomposer(ComposeUiTest.android.kt:219)
	at androidx.compose.ui.test.AndroidComposeUiTestEnvironment$runTest$1$1$1$1.invoke(ComposeUiTest.android.kt:331)
	at androidx.compose.ui.test.AndroidComposeUiTestEnvironment.withTestCoroutines(ComposeUiTest.android.kt:372)
	at androidx.compose.ui.test.AndroidComposeUiTestEnvironment.access$withTestCoroutines(ComposeUiTest.android.kt:219)
	at androidx.compose.ui.test.AndroidComposeUiTestEnvironment$runTest$1$1$1.invoke(ComposeUiTest.android.kt:330)
	at androidx.compose.ui.test.IdlingStrategy.withStrategy(IdlingStrategy.android.kt:52)
	at androidx.compose.ui.test.AndroidComposeUiTestEnvironment$runTest$1$1.invoke(ComposeUiTest.android.kt:329)
	at androidx.compose.ui.test.IdlingResourceRegistry.withRegistry(IdlingResourceRegistry.jvm.kt:155)
	at androidx.compose.ui.test.AndroidComposeUiTestEnvironment$runTest$1.invoke(ComposeUiTest.android.kt:328)
	at androidx.compose.ui.test.ComposeRootRegistry.withRegistry(ComposeRootRegistry.android.kt:146)
	at androidx.compose.ui.test.AndroidComposeUiTestEnvironment.runTest(ComposeUiTest.android.kt:327)
	at androidx.compose.ui.test.junit4.AndroidComposeTestRule$apply$1.evaluate(AndroidComposeTestRule.android.kt:271)
	at org.junit.runners.ParentRunner$3.evaluate(ParentRunner.java:306)
	at org.robolectric.RobolectricTestRunner$HelperTestRunner$1.evaluate(RobolectricTestRunner.java:524)
	at org.robolectric.internal.SandboxTestRunner.executeInSandbox(SandboxTestRunner.java:494)
	at org.robolectric.internal.SandboxTestRunner.access$900(SandboxTestRunner.java:67)
	at org.robolectric.internal.SandboxTestRunner$7.evaluate(SandboxTestRunner.java:442)
	at org.junit.runners.BlockJUnit4ClassRunner$1.evaluate(BlockJUnit4ClassRunner.java:100)
	at org.junit.runners.ParentRunner.runLeaf(ParentRunner.java:366)
	at org.junit.runners.BlockJUnit4ClassRunner.runChild(BlockJUnit4ClassRunner.java:103)
	at org.robolectric.internal.SandboxTestRunner.access$600(SandboxTestRunner.java:67)
	at org.robolectric.internal.SandboxTestRunner$6.evaluate(SandboxTestRunner.java:333)
	at org.robolectric.internal.SandboxTestRunner$3.evaluate(SandboxTestRunner.java:233)
	at org.robolectric.internal.SandboxTestRunner$5.lambda$evaluate$0(SandboxTestRunner.java:317)
	at org.robolectric.internal.bytecode.Sandbox.lambda$runOnMainThread$0(Sandbox.java:101)
	at java.base/java.util.concurrent.FutureTask.run(FutureTask.java:264)
	at java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1136)
	at java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:635)
	at java.base/java.lang.Thread.run(Thread.java:840)
</failure>
  </testcase>
  <testcase name="focusScreen_heroCardClean_andCustomizationSeparatedInOverflowMenu" classname="com.example.ui.screens.FocusAndDrawerUiTest" time="4.532"/>
  <testcase name="scaffoldTopBarAndLeftNavigationDrawer_switchCreateAndDeleteThreads" classname="com.example.ui.screens.FocusAndDrawerUiTest" time="0.25"/>
  <system-out><![CDATA[]]></system-out>
  <system-err><![CDATA[]]></system-err>
</testsuite>
---END app/build/test-results/testDebugUnitTest/TEST-com.example.ui.screens.FocusAndDrawerUiTest.xml---
```
Gradle log tail:
```
To suppress the warning do one of the following:
- Annotate the data class with the '@ConsistentCopyVisibility' annotation.
- Use the '-Xconsistent-data-class-copy-visibility' compiler flag.
- Annotate the data class with the '@ExposedCopyVisibility' annotation 
  (Discouraged, but can be used to keep binary compatibility).

To learn more, see the documentation of the '@ConsistentCopyVisibility' and '@ExposedCopyVisibility' annotations.

This will become an error in language version 2.3.
w: file:///home/runner/work/Light/Light/app/src/main/java/com/example/ui/screens/FocusAnalyticsScreen.kt:419:46 'val Icons.Rounded.TrendingUp: ImageVector' is deprecated. Use the AutoMirrored version at Icons.AutoMirrored.Rounded.TrendingUp.
w: file:///home/runner/work/Light/Light/app/src/main/java/com/example/ui/screens/GanttScreen.kt:253:36 'val Icons.Rounded.ArrowForwardIos: ImageVector' is deprecated. Use the AutoMirrored version at Icons.AutoMirrored.Rounded.ArrowForwardIos.
w: file:///home/runner/work/Light/Light/app/src/main/java/com/example/ui/screens/LibraryScreen.kt:621:45 'fun Modifier.menuAnchor(): Modifier' is deprecated. Use overload that takes MenuAnchorType and enabled parameters.
w: file:///home/runner/work/Light/Light/app/src/main/java/com/example/ui/screens/LibraryScreen.kt:641:45 'fun Modifier.menuAnchor(): Modifier' is deprecated. Use overload that takes MenuAnchorType and enabled parameters.
w: file:///home/runner/work/Light/Light/app/src/main/java/com/example/ui/screens/LibraryScreen.kt:657:45 'fun Modifier.menuAnchor(): Modifier' is deprecated. Use overload that takes MenuAnchorType and enabled parameters.

> Task :app:compileDebugJavaWithJavac

> Task :app:compileReleaseKotlin
w: file:///home/runner/work/Light/Light/app/src/main/java/com/example/MainActivity.kt:1474:25 'when' is exhaustive so 'else' is redundant here.
w: file:///home/runner/work/Light/Light/app/src/main/java/com/example/data/repository/AgentTurnActivity.kt:14:30 Non-public primary constructor is exposed via the generated 'copy()' method of the 'data' class.

The generated 'copy()' will change its visibility in future releases.

To suppress the warning do one of the following:
- Annotate the data class with the '@ConsistentCopyVisibility' annotation.
- Use the '-Xconsistent-data-class-copy-visibility' compiler flag.
- Annotate the data class with the '@ExposedCopyVisibility' annotation 
  (Discouraged, but can be used to keep binary compatibility).

To learn more, see the documentation of the '@ConsistentCopyVisibility' and '@ExposedCopyVisibility' annotations.

This will become an error in language version 2.3.
w: file:///home/runner/work/Light/Light/app/src/main/java/com/example/ui/screens/FocusAnalyticsScreen.kt:419:46 'val Icons.Rounded.TrendingUp: ImageVector' is deprecated. Use the AutoMirrored version at Icons.AutoMirrored.Rounded.TrendingUp.
w: file:///home/runner/work/Light/Light/app/src/main/java/com/example/ui/screens/GanttScreen.kt:253:36 'val Icons.Rounded.ArrowForwardIos: ImageVector' is deprecated. Use the AutoMirrored version at Icons.AutoMirrored.Rounded.ArrowForwardIos.
w: file:///home/runner/work/Light/Light/app/src/main/java/com/example/ui/screens/LibraryScreen.kt:621:45 'fun Modifier.menuAnchor(): Modifier' is deprecated. Use overload that takes MenuAnchorType and enabled parameters.
w: file:///home/runner/work/Light/Light/app/src/main/java/com/example/ui/screens/LibraryScreen.kt:641:45 'fun Modifier.menuAnchor(): Modifier' is deprecated. Use overload that takes MenuAnchorType and enabled parameters.
w: file:///home/runner/work/Light/Light/app/src/main/java/com/example/ui/screens/LibraryScreen.kt:657:45 'fun Modifier.menuAnchor(): Modifier' is deprecated. Use overload that takes MenuAnchorType and enabled parameters.

> Task :app:dexBuilderDebug
> Task :app:processDebugJavaRes
> Task :app:compileReleaseJavaWithJavac
> Task :app:mergeDebugGlobalSynthetics
> Task :app:mergeProjectDexDebug
> Task :app:mergeDebugJavaResource
> Task :app:dexBuilderRelease
> Task :app:mergeReleaseGlobalSynthetics FROM-CACHE
> Task :app:generateReleaseLintVitalReportModel
> Task :app:processReleaseJavaRes
> Task :app:packageDebug
> Task :app:createDebugApkListingFileRedirect
> Task :app:assembleDebug
> Task :app:mergeReleaseJavaResource
> Task :app:bundleDebugClassesToRuntimeJar
> Task :app:bundleDebugClassesToCompileJar
> Task :app:mergeDexRelease
> Task :app:compileReleaseArtProfile

> Task :app:compileDebugUnitTestKotlin
w: file:///home/runner/work/Light/Light/app/src/test/java/com/example/ExampleRobolectricTest.kt:29:47 'val googleAuthPausedForTesting: Flow<Boolean>' is deprecated. Test mode removed.

> Task :app:compileDebugUnitTestJavaWithJavac NO-SOURCE
> Task :app:processDebugUnitTestJavaRes
> Task :app:lintVitalAnalyzeRelease

> Task :app:testDebugUnitTest

ExampleRobolectricTest > classMethod FAILED
    java.lang.AssertionError at MavenArtifactFetcher.java:129
        Caused by: java.util.concurrent.ExecutionException at AbstractFuture.java:292
            Caused by: java.io.IOException at HttpURLConnection.java:2059

ExampleUnitTest > addition_isCorrect PASSED

AgentMarkdownDocumentTest > pageBreaksPreserveUnicodeAndExportPreservesEveryByte PASSED

AgentMarkdownDocumentTest > shortMessagesStayInlineButWideOrTallMessagesBecomeDocuments PASSED

LocalDocumentExtractorTest > classMethod FAILED
    java.lang.AssertionError at MavenArtifactFetcher.java:129
        Caused by: java.util.concurrent.ExecutionException at AbstractFuture.java:292
            Caused by: java.io.IOException at HttpURLConnection.java:2059

FirestoreDiagnosticPathTest > preservesCollectionAndMasksDocumentIds PASSED

AgentHistoryTest > unknownOrFutureDatesAreVisibleOnlyInAllAndDoNotFloatAboveTodaysThreads PASSED

AgentHistoryTest > filtersUseLocalCalendarBoundariesAndRespectInclusiveEdges PASSED

ProfileGateTest > successNullWithInitErrorIsError PASSED

ProfileGateTest > errorStateIsErrorEvenWithProfileNull PASSED

ProfileGateTest > needsOnboardingWhenFlagFalse PASSED

ProfileGateTest > readyWhenOnboardingCompleted PASSED

ProfileGateTest > errorTakesPrecedenceOverInitializing PASSED

ProfileGateTest > successNullWithoutErrorIsInitializing PASSED

ProfileGateTest > successNullDuringOfflineCreationShowsInitializingNotHome PASSED

ProfileGateTest > loadingIsInitializing PASSED

ProfileGateTest > successNullWithCreatingStillInitializing PASSED

AgentThreadsStoreTest > saveAndLoadThreads_preservesMessagesActionsAndPlanPreview PASSED

AgentThreadsStoreTest > store_sanitizesUserId_isolatesUsers_andEnforcesCaps PASSED

AgentToolContractTest > aValidScenarioForEveryToolReachesOnlyItsTypedExecutor PASSED

AgentToolContractTest > malformedDisabledAndRejectedCallsNeverWrite PASSED

AgentToolResultTest > explicit unsuccessful result is not logged as success PASSED

AgentToolResultTest > explicit successful result is logged as success PASSED

AgentToolResultTest > malformed success field is conservatively treated as failure PASSED

AgentToolResultTest > read result without success field remains successful PASSED

AgentToolResultTest > error or cancellation without success field is not logged as success PASSED

AgentTurnActivityTest > unknownArgumentsAndDeniedWritesAreNotReportedAsRunning PASSED

AgentTurnActivityTest > progressBelongsOnlyToItsTurnAndReflectionKeepsLegacyPreferences PASSED

AgentTurnActivityTest > aMetadataOnlyLastChunkDoesNotTruncateAnswerOrDropCalls PASSED

AgentTurnInstructionsTest > catalogExcludesDisabledEntriesAndSeparatesCustomIdsFromBuiltIns PASSED

AgentTurnInstructionsTest > httpUsesSelectedMethodForTheToolRoundButNeverGrantsOrPersistsIt PASSED

AiStudyPlanResponseTest > classMethod FAILED
    java.lang.AssertionError at MavenArtifactFetcher.java:129
        Caused by: java.util.concurrent.ExecutionException at AbstractFuture.java:292
            Caused by: java.io.IOException at HttpURLConnection.java:2059

LocalLibraryTest > createFolderAndItemIsolatedByAccount PASSED

LocalLibraryTest > htmlAndPdfStoredAsBoundedText PASSED

LocalLibraryTest > rejectsInvalidFolderAndMime PASSED

LocalTestStudyRepositoryTest > localProjects_supportCrudAndDetachLinkedTasksOnDelete PASSED

LocalTestStudyRepositoryTest > diskPersistence_survivesRestart_andSyncsIdempotentlyToCloud PASSED

LocalTestStudyRepositoryTest > taskOrderDependenciesAndMoveSurviveOpeningTheRepositoryAgain PASSED
```
