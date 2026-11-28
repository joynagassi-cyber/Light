### Test failures for be568311cf394e8dbcdc35339ea56647bf5d4938
```
=== app/build/test-results/testDebugUnitTest/TEST-com.example.ExampleRobolectricTest.xml ===
<?xml version="1.0" encoding="UTF-8"?>
<testsuite name="com.example.ExampleRobolectricTest" tests="3" skipped="0" failures="1" errors="0" timestamp="2026-11-28T02:22:53.017Z" hostname="runnervm0j63q" time="11.12">
  <properties/>
  <testcase name="focus illustration and background selections persist" classname="com.example.ExampleRobolectricTest" time="4.697"/>
  <testcase name="read string from context" classname="com.example.ExampleRobolectricTest" time="0.083"/>
  <testcase name="Google sign in is paused by default for local testing" classname="com.example.ExampleRobolectricTest" time="0.123">
    <failure message="java.lang.AssertionError" type="java.lang.AssertionError">java.lang.AssertionError
	at org.junit.Assert.fail(Assert.java:87)
	at org.junit.Assert.assertTrue(Assert.java:42)
	at org.junit.Assert.assertTrue(Assert.java:53)
	at com.example.ExampleRobolectricTest$Google sign in is paused by default for local testing$1.invokeSuspend(ExampleRobolectricTest.kt:29)
	at kotlin.coroutines.jvm.internal.BaseContinuationImpl.resumeWith(ContinuationImpl.kt:34)
	at kotlinx.coroutines.DispatchedTask.run(DispatchedTask.kt:100)
	at kotlinx.coroutines.EventLoopImplBase.processNextEvent(EventLoop.common.kt:263)
	at kotlinx.coroutines.BlockingCoroutine.joinBlocking(Builders.kt:94)
	at kotlinx.coroutines.BuildersKt__BuildersKt.runBlocking(Builders.kt:70)
	at kotlinx.coroutines.BuildersKt.runBlocking(Unknown Source)
	at kotlinx.coroutines.BuildersKt__BuildersKt.runBlocking$default(Builders.kt:48)
	at kotlinx.coroutines.BuildersKt.runBlocking$default(Unknown Source)
	at com.example.ExampleRobolectricTest.Google sign in is paused by default for local testing(ExampleRobolectricTest.kt:27)
	at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
	at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:77)
	at java.base/jdk.internal.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
	at java.base/java.lang.reflect.Method.invoke(Method.java:569)
	at org.junit.runners.model.FrameworkMethod$1.runReflectiveCall(FrameworkMethod.java:59)
	at org.junit.internal.runners.model.ReflectiveCallable.run(ReflectiveCallable.java:12)
	at org.junit.runners.model.FrameworkMethod.invokeExplosively(FrameworkMethod.java:56)
	at org.junit.internal.runners.statements.InvokeMethod.evaluate(InvokeMethod.java:17)
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
---END app/build/test-results/testDebugUnitTest/TEST-com.example.ExampleRobolectricTest.xml---
=== app/build/test-results/testDebugUnitTest/TEST-com.example.ui.screens.AgentChatAndSettingsUiTest.xml ===
<?xml version="1.0" encoding="UTF-8"?>
<testsuite name="com.example.ui.screens.AgentChatAndSettingsUiTest" tests="2" skipped="0" failures="1" errors="0" timestamp="2026-11-28T02:23:22.813Z" hostname="runnervm0j63q" time="2.997">
  <properties/>
  <testcase name="agentChat_menuUsesSkillForOneMessage_andDrawerListsThreads" classname="com.example.ui.screens.AgentChatAndSettingsUiTest" time="1.547"/>
  <testcase name="settingsScreen_themesAndGradientsAreCollapsedUntilExpanded" classname="com.example.ui.screens.AgentChatAndSettingsUiTest" time="1.442">
    <failure message="java.lang.AssertionError: No node found that matches TestTag = 'settings_expand_themes' in scrollable container&#10;Semantics of the node:&#10;Node #413 at (l=0.0, t=0.0, r=320.0, b=470.0)px, Tag: 'settings_list'&#10;CollectionInfo = 'androidx.compose.ui.semantics.CollectionInfo@e8d8914'&#10;IsTraversalGroup = 'true'&#10;VerticalScrollAxisRange = 'ScrollAxisRange(value=3051.0, maxValue=3051.0, reverseScrolling=false)'&#10;Actions = [GetScrollViewportLength, IndexForKey, ScrollBy, ScrollByOffset, ScrollToIndex]&#10;Has 4 children&#10;Selector used: (TestTag = 'settings_list')&#10;" type="java.lang.AssertionError">java.lang.AssertionError: No node found that matches TestTag = 'settings_expand_themes' in scrollable container
Semantics of the node:
Node #413 at (l=0.0, t=0.0, r=320.0, b=470.0)px, Tag: 'settings_list'
CollectionInfo = 'androidx.compose.ui.semantics.CollectionInfo@e8d8914'
IsTraversalGroup = 'true'
VerticalScrollAxisRange = 'ScrollAxisRange(value=3051.0, maxValue=3051.0, reverseScrolling=false)'
Actions = [GetScrollViewportLength, IndexForKey, ScrollBy, ScrollByOffset, ScrollToIndex]
Has 4 children
Selector used: (TestTag = 'settings_list')

	at androidx.compose.ui.test.ActionsKt.performScrollToNode(Actions.kt:257)
	at com.example.ui.screens.AgentChatAndSettingsUiTest.settingsScreen_themesAndGradientsAreCollapsedUntilExpanded(AgentChatAndSettingsUiTest.kt:161)
	at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
	at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:77)
	at java.base/jdk.internal.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
	at java.base/java.lang.reflect.Method.invoke(Method.java:569)
	at org.junit.runners.model.FrameworkMethod$1.runReflectiveCall(FrameworkMethod.java:59)
	at org.junit.internal.runners.model.ReflectiveCallable.run(ReflectiveCallable.java:12)
	at org.junit.runners.model.FrameworkMethod.invokeExplosively(FrameworkMethod.java:56)
	at org.junit.internal.runners.statements.InvokeMethod.evaluate(InvokeMethod.java:17)
	at androidx.compose.ui.test.junit4.AndroidComposeTestRule$apply$testWithDisposal$1.evaluate(AndroidComposeTestRule.android.kt:306)
	at org.junit.rules.ExternalResource$1.evaluate(ExternalResource.java:54)
	at androidx.compose.ui.test.junit4.AndroidComposeTestRule$apply$1$evaluate$1.invokeSuspend(AndroidComposeTestRule.android.kt:318)
	at androidx.compose.ui.test.junit4.AndroidComposeTestRule$apply$1$evaluate$1.invoke(AndroidComposeTestRule.android.kt)
	at androidx.compose.ui.test.junit4.AndroidComposeTestRule$apply$1$evaluate$1.invoke(AndroidComposeTestRule.android.kt)
	at androidx.compose.ui.test.AndroidComposeUiTestEnvironment$runTest$1$1$1$1$1.invokeSuspend(ComposeUiTest.android.kt:569)
	at androidx.compose.ui.test.AndroidComposeUiTestEnvironment$runTest$1$1$1$1$1.invoke(ComposeUiTest.android.kt)
	at androidx.compose.ui.test.AndroidComposeUiTestEnvironment$runTest$1$1$1$1$1.invoke(ComposeUiTest.android.kt)
	at androidx.compose.ui.test.IdlingStrategy.withStrategy$suspendImpl(IdlingStrategy.android.kt:55)
	at androidx.compose.ui.test.IdlingStrategy.withStrategy(IdlingStrategy.android.kt)
	at androidx.compose.ui.test.AndroidComposeUiTestEnvironment$runTest$1$1.invokeSuspend(ComposeUiTest.android.kt:566)
	at androidx.compose.ui.test.AndroidComposeUiTestEnvironment$runTest$1$1.invoke(ComposeUiTest.android.kt)
	at androidx.compose.ui.test.AndroidComposeUiTestEnvironment$runTest$1$1.invoke(ComposeUiTest.android.kt)
	at kotlinx.coroutines.test.TestBuildersKt__TestBuildersKt$runTest$2$1$1.invokeSuspend(TestBuilders.kt:317)
	at kotlin.coroutines.jvm.internal.BaseContinuationImpl.resumeWith(ContinuationImpl.kt:34)
	at kotlinx.coroutines.DispatchedTask.run(DispatchedTask.kt:100)
	at kotlinx.coroutines.test.TestDispatcher.processEvent$kotlinx_coroutines_test(TestDispatcher.kt:24)
	at kotlinx.coroutines.test.TestCoroutineScheduler.tryRunNextTaskUnless$kotlinx_coroutines_test(TestCoroutineScheduler.kt:99)
	at kotlinx.coroutines.test.TestBuildersKt__TestBuildersKt$runTest$2$1$workRunner$1.invokeSuspend(TestBuilders.kt:326)
	at kotlin.coroutines.jvm.internal.BaseContinuationImpl.resumeWith(ContinuationImpl.kt:34)
	at kotlinx.coroutines.DispatchedTask.run(DispatchedTask.kt:100)
	at kotlinx.coroutines.EventLoopImplBase.processNextEvent(EventLoop.common.kt:263)
	at kotlinx.coroutines.BlockingCoroutine.joinBlocking(Builders.kt:94)
	at kotlinx.coroutines.BuildersKt__BuildersKt.runBlocking(Builders.kt:70)
	at kotlinx.coroutines.BuildersKt.runBlocking(Unknown Source)
	at kotlinx.coroutines.BuildersKt__BuildersKt.runBlocking$default(Builders.kt:48)
	at kotlinx.coroutines.BuildersKt.runBlocking$default(Unknown Source)
	at kotlinx.coroutines.test.TestBuildersJvmKt.createTestResult(TestBuildersJvm.kt:10)
	at kotlinx.coroutines.test.TestBuildersKt__TestBuildersKt.runTest-8Mi8wO0(TestBuilders.kt:309)
	at kotlinx.coroutines.test.TestBuildersKt.runTest-8Mi8wO0(Unknown Source)
	at kotlinx.coroutines.test.TestBuildersKt__TestBuildersKt.runTest-8Mi8wO0(TestBuilders.kt:167)
	at kotlinx.coroutines.test.TestBuildersKt.runTest-8Mi8wO0(Unknown Source)
	at androidx.compose.ui.test.AndroidComposeUiTestEnvironment.runTest(ComposeUiTest.android.kt:549)
	at androidx.compose.ui.test.junit4.AndroidComposeTestRule$apply$1.evaluate(AndroidComposeTestRule.android.kt:318)
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
<testsuite name="com.example.ui.screens.FocusAnalyticsAndSettingsUiTest" tests="8" skipped="0" failures="1" errors="0" timestamp="2026-11-28T02:23:25.822Z" hostname="runnervm0j63q" time="3.087">
  <properties/>
  <testcase name="progressScreen_preservesExistingChartsAndAddsHeatmapRadarVelocityAndMatrix" classname="com.example.ui.screens.FocusAnalyticsAndSettingsUiTest" time="0.741"/>
  <testcase name="focusAnalyticsScreen_displaysLineChartKpisAndSwitchesTimeWindows" classname="com.example.ui.screens.FocusAnalyticsAndSettingsUiTest" time="0.627"/>
  <testcase name="redesignedSettingsScreen_displaysGroupedCardsAndOpensDetailSheets" classname="com.example.ui.screens.FocusAnalyticsAndSettingsUiTest" time="0.512">
    <failure message="java.lang.AssertionError: Assert failed: The component with TestTag = 'appearance_mode_oled' is not displayed!" type="java.lang.AssertionError">java.lang.AssertionError: Assert failed: The component with TestTag = 'appearance_mode_oled' is not displayed!
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
	at androidx.compose.ui.test.junit4.AndroidComposeTestRule$apply$testWithDisposal$1.evaluate(AndroidComposeTestRule.android.kt:306)
	at org.junit.rules.ExternalResource$1.evaluate(ExternalResource.java:54)
	at androidx.compose.ui.test.junit4.AndroidComposeTestRule$apply$1$evaluate$1.invokeSuspend(AndroidComposeTestRule.android.kt:318)
	at androidx.compose.ui.test.junit4.AndroidComposeTestRule$apply$1$evaluate$1.invoke(AndroidComposeTestRule.android.kt)
	at androidx.compose.ui.test.junit4.AndroidComposeTestRule$apply$1$evaluate$1.invoke(AndroidComposeTestRule.android.kt)
	at androidx.compose.ui.test.AndroidComposeUiTestEnvironment$runTest$1$1$1$1$1.invokeSuspend(ComposeUiTest.android.kt:569)
	at androidx.compose.ui.test.AndroidComposeUiTestEnvironment$runTest$1$1$1$1$1.invoke(ComposeUiTest.android.kt)
	at androidx.compose.ui.test.AndroidComposeUiTestEnvironment$runTest$1$1$1$1$1.invoke(ComposeUiTest.android.kt)
	at androidx.compose.ui.test.IdlingStrategy.withStrategy$suspendImpl(IdlingStrategy.android.kt:55)
	at androidx.compose.ui.test.IdlingStrategy.withStrategy(IdlingStrategy.android.kt)
	at androidx.compose.ui.test.AndroidComposeUiTestEnvironment$runTest$1$1.invokeSuspend(ComposeUiTest.android.kt:566)
	at androidx.compose.ui.test.AndroidComposeUiTestEnvironment$runTest$1$1.invoke(ComposeUiTest.android.kt)
	at androidx.compose.ui.test.AndroidComposeUiTestEnvironment$runTest$1$1.invoke(ComposeUiTest.android.kt)
	at kotlinx.coroutines.test.TestBuildersKt__TestBuildersKt$runTest$2$1$1.invokeSuspend(TestBuilders.kt:317)
	at kotlin.coroutines.jvm.internal.BaseContinuationImpl.resumeWith(ContinuationImpl.kt:34)
	at kotlinx.coroutines.DispatchedTask.run(DispatchedTask.kt:100)
	at kotlinx.coroutines.test.TestDispatcher.processEvent$kotlinx_coroutines_test(TestDispatcher.kt:24)
	at kotlinx.coroutines.test.TestCoroutineScheduler.tryRunNextTaskUnless$kotlinx_coroutines_test(TestCoroutineScheduler.kt:99)
	at kotlinx.coroutines.test.TestBuildersKt__TestBuildersKt$runTest$2$1$workRunner$1.invokeSuspend(TestBuilders.kt:326)
	at kotlin.coroutines.jvm.internal.BaseContinuationImpl.resumeWith(ContinuationImpl.kt:34)
	at kotlinx.coroutines.DispatchedTask.run(DispatchedTask.kt:100)
	at kotlinx.coroutines.EventLoopImplBase.processNextEvent(EventLoop.common.kt:263)
	at kotlinx.coroutines.BlockingCoroutine.joinBlocking(Builders.kt:94)
	at kotlinx.coroutines.BuildersKt__BuildersKt.runBlocking(Builders.kt:70)
	at kotlinx.coroutines.BuildersKt.runBlocking(Unknown Source)
	at kotlinx.coroutines.BuildersKt__BuildersKt.runBlocking$default(Builders.kt:48)
	at kotlinx.coroutines.BuildersKt.runBlocking$default(Unknown Source)
	at kotlinx.coroutines.test.TestBuildersJvmKt.createTestResult(TestBuildersJvm.kt:10)
	at kotlinx.coroutines.test.TestBuildersKt__TestBuildersKt.runTest-8Mi8wO0(TestBuilders.kt:309)
	at kotlinx.coroutines.test.TestBuildersKt.runTest-8Mi8wO0(Unknown Source)
	at kotlinx.coroutines.test.TestBuildersKt__TestBuildersKt.runTest-8Mi8wO0(TestBuilders.kt:167)
	at kotlinx.coroutines.test.TestBuildersKt.runTest-8Mi8wO0(Unknown Source)
	at androidx.compose.ui.test.AndroidComposeUiTestEnvironment.runTest(ComposeUiTest.android.kt:549)
	at androidx.compose.ui.test.junit4.AndroidComposeTestRule$apply$1.evaluate(AndroidComposeTestRule.android.kt:318)
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
  <testcase name="moreHubScreen_exposesFocusAnalyticsNavigationEntry" classname="com.example.ui.screens.FocusAnalyticsAndSettingsUiTest" time="0.16"/>
  <testcase name="premiumChartAggregations_computeHeatmapStackedModesRadarAndMatrixAccurately" classname="com.example.ui.screens.FocusAnalyticsAndSettingsUiTest" time="0.106"/>
  <testcase name="settingsScreen_rendersCleanlyInOledAndGlassDesignSystem" classname="com.example.ui.screens.FocusAnalyticsAndSettingsUiTest" time="0.477"/>
  <testcase name="focusAnalyticsScreen_displaysPremiumHeatmapStackedRadarAndDepthCharts" classname="com.example.ui.screens.FocusAnalyticsAndSettingsUiTest" time="0.369"/>
  <testcase name="aggregateDailyFocusPoints_andComputeStreak_excludeBreaksAndSumCorrectly" classname="com.example.ui.screens.FocusAnalyticsAndSettingsUiTest" time="0.079"/>
  <system-out><![CDATA[]]></system-out>
  <system-err><![CDATA[]]></system-err>
</testsuite>
---END app/build/test-results/testDebugUnitTest/TEST-com.example.ui.screens.FocusAnalyticsAndSettingsUiTest.xml---
=== app/build/test-results/testDebugUnitTest/TEST-com.example.ui.screens.FocusAndDrawerUiTest.xml ===
<?xml version="1.0" encoding="UTF-8"?>
<testsuite name="com.example.ui.screens.FocusAndDrawerUiTest" tests="3" skipped="0" failures="1" errors="0" timestamp="2026-11-28T02:23:28.929Z" hostname="runnervm0j63q" time="7.299">
  <properties/>
  <testcase name="settingsScreen_containsCollapsibleFocusUniverseAndAudioSection" classname="com.example.ui.screens.FocusAndDrawerUiTest" time="0.573">
    <failure message="java.lang.AssertionError: No node found that matches TestTag = 'settings_expand_focus_universe' in scrollable container&#10;Semantics of the node:&#10;Node #1784 at (l=0.0, t=0.0, r=1233.0, b=2745.0)px, Tag: 'settings_list'&#10;CollectionInfo = 'androidx.compose.ui.semantics.CollectionInfo@6b1f431e'&#10;IsTraversalGroup = 'true'&#10;VerticalScrollAxisRange = 'ScrollAxisRange(value=2497.0, maxValue=2497.0, reverseScrolling=false)'&#10;Actions = [GetScrollViewportLength, IndexForKey, ScrollBy, ScrollByOffset, ScrollToIndex]&#10;Has 6 children&#10;Selector used: (TestTag = 'settings_list')&#10;" type="java.lang.AssertionError">java.lang.AssertionError: No node found that matches TestTag = 'settings_expand_focus_universe' in scrollable container
Semantics of the node:
Node #1784 at (l=0.0, t=0.0, r=1233.0, b=2745.0)px, Tag: 'settings_list'
CollectionInfo = 'androidx.compose.ui.semantics.CollectionInfo@6b1f431e'
IsTraversalGroup = 'true'
VerticalScrollAxisRange = 'ScrollAxisRange(value=2497.0, maxValue=2497.0, reverseScrolling=false)'
Actions = [GetScrollViewportLength, IndexForKey, ScrollBy, ScrollByOffset, ScrollToIndex]
Has 6 children
Selector used: (TestTag = 'settings_list')

	at androidx.compose.ui.test.ActionsKt.performScrollToNode(Actions.kt:257)
	at com.example.ui.screens.FocusAndDrawerUiTest.settingsScreen_containsCollapsibleFocusUniverseAndAudioSection(FocusAndDrawerUiTest.kt:267)
	at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
	at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:77)
	at java.base/jdk.internal.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
	at java.base/java.lang.reflect.Method.invoke(Method.java:569)
	at org.junit.runners.model.FrameworkMethod$1.runReflectiveCall(FrameworkMethod.java:59)
	at org.junit.internal.runners.model.ReflectiveCallable.run(ReflectiveCallable.java:12)
	at org.junit.runners.model.FrameworkMethod.invokeExplosively(FrameworkMethod.java:56)
	at org.junit.internal.runners.statements.InvokeMethod.evaluate(InvokeMethod.java:17)
	at androidx.compose.ui.test.junit4.AndroidComposeTestRule$apply$testWithDisposal$1.evaluate(AndroidComposeTestRule.android.kt:306)
	at org.junit.rules.ExternalResource$1.evaluate(ExternalResource.java:54)
	at androidx.compose.ui.test.junit4.AndroidComposeTestRule$apply$1$evaluate$1.invokeSuspend(AndroidComposeTestRule.android.kt:318)
	at androidx.compose.ui.test.junit4.AndroidComposeTestRule$apply$1$evaluate$1.invoke(AndroidComposeTestRule.android.kt)
	at androidx.compose.ui.test.junit4.AndroidComposeTestRule$apply$1$evaluate$1.invoke(AndroidComposeTestRule.android.kt)
	at androidx.compose.ui.test.AndroidComposeUiTestEnvironment$runTest$1$1$1$1$1.invokeSuspend(ComposeUiTest.android.kt:569)
	at androidx.compose.ui.test.AndroidComposeUiTestEnvironment$runTest$1$1$1$1$1.invoke(ComposeUiTest.android.kt)
	at androidx.compose.ui.test.AndroidComposeUiTestEnvironment$runTest$1$1$1$1$1.invoke(ComposeUiTest.android.kt)
	at androidx.compose.ui.test.IdlingStrategy.withStrategy$suspendImpl(IdlingStrategy.android.kt:55)
	at androidx.compose.ui.test.IdlingStrategy.withStrategy(IdlingStrategy.android.kt)
	at androidx.compose.ui.test.AndroidComposeUiTestEnvironment$runTest$1$1.invokeSuspend(ComposeUiTest.android.kt:566)
	at androidx.compose.ui.test.AndroidComposeUiTestEnvironment$runTest$1$1.invoke(ComposeUiTest.android.kt)
	at androidx.compose.ui.test.AndroidComposeUiTestEnvironment$runTest$1$1.invoke(ComposeUiTest.android.kt)
	at kotlinx.coroutines.test.TestBuildersKt__TestBuildersKt$runTest$2$1$1.invokeSuspend(TestBuilders.kt:317)
	at kotlin.coroutines.jvm.internal.BaseContinuationImpl.resumeWith(ContinuationImpl.kt:34)
	at kotlinx.coroutines.DispatchedTask.run(DispatchedTask.kt:100)
	at kotlinx.coroutines.test.TestDispatcher.processEvent$kotlinx_coroutines_test(TestDispatcher.kt:24)
	at kotlinx.coroutines.test.TestCoroutineScheduler.tryRunNextTaskUnless$kotlinx_coroutines_test(TestCoroutineScheduler.kt:99)
	at kotlinx.coroutines.test.TestBuildersKt__TestBuildersKt$runTest$2$1$workRunner$1.invokeSuspend(TestBuilders.kt:326)
	at kotlin.coroutines.jvm.internal.BaseContinuationImpl.resumeWith(ContinuationImpl.kt:34)
	at kotlinx.coroutines.DispatchedTask.run(DispatchedTask.kt:100)
	at kotlinx.coroutines.EventLoopImplBase.processNextEvent(EventLoop.common.kt:263)
	at kotlinx.coroutines.BlockingCoroutine.joinBlocking(Builders.kt:94)
	at kotlinx.coroutines.BuildersKt__BuildersKt.runBlocking(Builders.kt:70)
	at kotlinx.coroutines.BuildersKt.runBlocking(Unknown Source)
	at kotlinx.coroutines.BuildersKt__BuildersKt.runBlocking$default(Builders.kt:48)
	at kotlinx.coroutines.BuildersKt.runBlocking$default(Unknown Source)
	at kotlinx.coroutines.test.TestBuildersJvmKt.createTestResult(TestBuildersJvm.kt:10)
	at kotlinx.coroutines.test.TestBuildersKt__TestBuildersKt.runTest-8Mi8wO0(TestBuilders.kt:309)
	at kotlinx.coroutines.test.TestBuildersKt.runTest-8Mi8wO0(Unknown Source)
	at kotlinx.coroutines.test.TestBuildersKt__TestBuildersKt.runTest-8Mi8wO0(TestBuilders.kt:167)
	at kotlinx.coroutines.test.TestBuildersKt.runTest-8Mi8wO0(Unknown Source)
	at androidx.compose.ui.test.AndroidComposeUiTestEnvironment.runTest(ComposeUiTest.android.kt:549)
	at androidx.compose.ui.test.junit4.AndroidComposeTestRule$apply$1.evaluate(AndroidComposeTestRule.android.kt:318)
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
  <testcase name="focusScreen_heroCardClean_andCustomizationSeparatedInOverflowMenu" classname="com.example.ui.screens.FocusAndDrawerUiTest" time="6.265"/>
  <testcase name="scaffoldTopBarAndLeftNavigationDrawer_switchCreateAndDeleteThreads" classname="com.example.ui.screens.FocusAndDrawerUiTest" time="0.454"/>
  <system-out><![CDATA[]]></system-out>
  <system-err><![CDATA[]]></system-err>
</testsuite>
---END app/build/test-results/testDebugUnitTest/TEST-com.example.ui.screens.FocusAndDrawerUiTest.xml---
```
Gradle log tail:
```
> Task :app:mergeReleaseArtProfile
> Task :app:generateReleaseAssets UP-TO-DATE
> Task :app:mergeReleaseAssets
> Task :app:processReleaseResources
> Task :app:extractReleaseVersionControlInfo
> Task :app:extractProguardFiles
> Task :app:compressReleaseAssets
> Task :app:writeReleaseAppMetadata
> Task :app:writeReleaseSigningConfigVersions
> Task :app:bundleDebugClassesToRuntimeJar
> Task :app:optimizeReleaseResources
> Task :app:preDebugUnitTestBuild UP-TO-DATE
> Task :app:javaPreCompileDebugUnitTest FROM-CACHE
> Task :app:bundleDebugClassesToCompileJar
> Task :app:packageDebugUnitTestForUnitTest
> Task :app:processDebugUnitTestManifest
> Task :app:generateDebugUnitTestConfig

> Task :app:compileReleaseKotlin
w: file:///home/runner/work/Light/Light/app/src/main/java/com/example/MainActivity.kt:1487:25 'when' is exhaustive so 'else' is redundant here.
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

> Task :app:compileDebugUnitTestKotlin
> Task :app:compileReleaseJavaWithJavac
> Task :app:dexBuilderRelease
> Task :app:mergeReleaseGlobalSynthetics
> Task :app:generateReleaseLintVitalReportModel

> Task :app:compileDebugUnitTestKotlin
w: file:///home/runner/work/Light/Light/app/src/test/java/com/example/ExampleRobolectricTest.kt:29:47 'val googleAuthPausedForTesting: Flow<Boolean>' is deprecated. Test mode removed.

> Task :app:processReleaseJavaRes
> Task :app:compileDebugUnitTestJavaWithJavac NO-SOURCE
> Task :app:processDebugUnitTestJavaRes
> Task :app:mergeReleaseJavaResource
> Task :app:mergeDexRelease

> Task :app:testDebugUnitTest

ExampleRobolectricTest > focus illustration and background selections persist PASSED

ExampleRobolectricTest > read string from context PASSED

ExampleRobolectricTest > Google sign in is paused by default for local testing FAILED
    java.lang.AssertionError at ExampleRobolectricTest.kt:27

ExampleUnitTest > addition_isCorrect PASSED

AgentMarkdownDocumentTest > pageBreaksPreserveUnicodeAndExportPreservesEveryByte PASSED

AgentMarkdownDocumentTest > shortMessagesStayInlineButWideOrTallMessagesBecomeDocuments PASSED

LocalDocumentExtractorTest > Excel shared strings cell references formulas and dates are retained PASSED

LocalDocumentExtractorTest > Excel date system and leap year compatibility are explicit PASSED

LocalDocumentExtractorTest > positioned PDF text keeps table columns and rows distinct PASSED

LocalDocumentExtractorTest > Word tables retain row and cell boundaries PASSED

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

AiStudyPlanResponseTest > configuredExternalTransportGetsThePlannerPromptWithoutFirebase PASSED

AiStudyPlanResponseTest > responseFieldsAreNormalizedBeforePreview PASSED

AiStudyPlanResponseTest > unreadablePayloadIsNotEchoedInError PASSED

LocalLibraryTest > createFolderAndItemIsolatedByAccount PASSED

LocalLibraryTest > htmlAndPdfStoredAsBoundedText PASSED

LocalLibraryTest > rejectsInvalidFolderAndMime PASSED

LocalTestStudyRepositoryTest > localProjects_supportCrudAndDetachLinkedTasksOnDelete PASSED

LocalTestStudyRepositoryTest > diskPersistence_survivesRestart_andSyncsIdempotentlyToCloud PASSED

LocalTestStudyRepositoryTest > taskOrderDependenciesAndMoveSurviveOpeningTheRepositoryAgain PASSED

LocalTestStudyRepositoryTest > courses_storeSelectedIconKeyAndSeedRecognizableSubjectIcons PASSED

LocalTestStudyRepositoryTest > navigationLayoutSurvivesRestartAndDropsUnexpectedRoutes PASSED
```
