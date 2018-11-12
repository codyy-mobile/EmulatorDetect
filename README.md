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
	        compile 'com.github.CodyyAndroid:EmulatorDetect:1.1.0'
	}
```
## Usage

```
 @Override
    protected void onStart() {
        super.onStart();
        EmulatorDetector.getDefault().bind(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Unbind from the service
        EmulatorDetector.getDefault().unbind(this);
    }

```
```
if (EmulatorDetector.getDefault().isEmulator()) {
      textView.setText("This device is emulator\n" + EmulatorDetector.getDefault().getEmulatorName());
   } else {
      textView.setText("This device is not emulator\n");
}
```

## [API DOC](https://jitpack.io/com/github/CodyyAndroid/EmulatorDetect/1.0.8/javadoc/)
