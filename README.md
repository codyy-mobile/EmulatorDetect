# EmulatorDetect

[![](https://jitpack.io/v/CodyyAndroid/EmulatorDetect.svg)](https://jitpack.io/#CodyyAndroid/EmulatorDetect)

## How to
**Step 1. Add the JitPack repository to your build file**

Add it in your root build.gradle at the end of repositories:
```
allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
```
**Step 2. Add the dependency**
```
dependencies {
	        compile 'com.github.CodyyAndroid:EmulatorDetect:v1.0.0'
	}
```
## Usage

模拟器检测器
```
DeviceInfo info = EmulatorDetect.emulatorDetect(Context);
```
