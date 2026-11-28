### Test failures for 27f5acb1b4275b05a8ab5a24a0000a63c12c136f
```
```
Gradle log tail:
```
Starting a Gradle Daemon (subsequent builds will be faster)
> Task :app:preBuild UP-TO-DATE
> Task :app:preDebugBuild UP-TO-DATE
> Task :app:mergeDebugNativeDebugMetadata NO-SOURCE
> Task :app:generateDebugBuildConfig
> Task :app:generateDebugResources
> Task :app:packageDebugResources
> Task :app:processDebugNavigationResources
> Task :app:generateDebugAssets UP-TO-DATE
> Task :app:javaPreCompileDebug
> Task :app:parseDebugLocalResources
> Task :app:mergeDebugAssets
> Task :app:desugarDebugFileDependencies
> Task :app:generateDebugRFile
> Task :app:checkDebugAarMetadata
> Task :app:compressDebugAssets
> Task :app:mapDebugSourceSetPaths
> Task :app:compileDebugNavigationResources
> Task :app:createDebugCompatibleScreenManifests
> Task :app:extractDeepLinksDebug
> Task :app:processDebugMainManifest
> Task :app:processDebugManifest
> Task :app:mergeDebugResources
> Task :app:processDebugManifestForPackage
> Task :app:checkDebugDuplicateClasses
> Task :app:processDebugResources
> Task :app:compileDebugKotlin
> Task :app:mergeExtDexDebug

> Task :app:compileDebugKotlin
e: file:///home/runner/work/Light/Light/app/src/main/java/com/example/MainActivity.kt:1191:53 'when' expression must be exhaustive. Add the 'CHAT' branch or an 'else' branch.

> Task :app:compileDebugKotlin FAILED
gradle/actions: Writing build results to /home/runner/work/_temp/.gradle-actions/build-results/__run_2-1795828822012.json

FAILURE: Build failed with an exception.

* What went wrong:
Execution failed for task ':app:compileDebugKotlin'.
> A failure occurred while executing org.jetbrains.kotlin.compilerRunner.GradleCompilerRunnerWithWorkers$GradleKotlinCompilerWorkAction
   > Compilation error. See log for more details

* Try:
> Run with --stacktrace option to get the stack trace.
> Run with --info or --debug option to get more log output.
> Run with --scan to get full insights from a Build Scan (powered by Develocity).
> Get more help at https://help.gradle.org.

BUILD FAILED in 2m 42s
23 actionable tasks: 23 executed
```
