# SmartLog
log info localization and print info smart


Step 1. Add the JitPack repository to your build file
Add it in your root build.gradle at the end of repositories:

	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
  
Step 2. Add the dependency

	dependencies {
	        implementation 'com.github.mingway426:SmartLog:v1.0.0'
	}

Step 3. init in Application onCreate()

```
SLog.init(context,tag,logCallback);
```

you can replace **Log.d** with **SLog.d** (also: i,v,e,w) in your project;

find the local log files in: storage/android/packgeName/cache/logs

any questions,please create an issue to report it!

enjoy it!
