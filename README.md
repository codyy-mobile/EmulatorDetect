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
	        compile 'com.github.CodyyAndroid:EmulatorDetect:v1.0.1'
	}
```
## Usage

模拟器检测器
```
RxPermissions rxPermissions = new RxPermissions(getSupportFragmentManager());
        rxPermissions.request(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_PHONE_STATE)
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean aBoolean) {
                        if (aBoolean) {
                            mEmulatorDetector = new EmulatorDetector().with(MainActivity.this);
                            mEmulatorDetector
                                    .setCheckTelephony(true)
                                    .setDebug(true)
                                    .detect(new EmulatorDetector.OnEmulatorDetectorListener() {
                                        @Override
                                        public void onResult(final boolean isEmulator) {
                                            place.setVisibility(View.GONE);
                                            if (isEmulator) {
                                                textView.setText("This device is emulator" + getCheckInfo());
                                            } else {
                                                textView.setText("This device is not emulator" + getCheckInfo());
                                            }
                                            /*SendAsyncTask asyncTask = new SendAsyncTask();
                                            asyncTask.execute("设备信息", getCheckInfo());*///send email
                                            Log.d(getClass().getName(), "Running on emulator --> " + isEmulator);
                                        }
                                    });
                        } else {
                            //OpenSettings
                        }
                    }
                });
```
