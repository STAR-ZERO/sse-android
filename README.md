SSE Android
===

Server-sent events (SSE) client for Android

## Usage

#### Connect

```java
EventSource eventSource = new EventSource(URL, new EventHandler() {
    @Override
    public void onOpen() {
        // run on worker thread
        Log.d(TAG, "Open");
    }

    @Override
    public void onMessage(MessageEvent messageEvent) {
        // run on worker thread
        Log.d(TAG, "Message");
    }

    @Override
    public void onError(Exception e) {
        // run on worker thread
        Log.w(TAG, e);
    }
});

eventSource.connect();
```

#### Close

```java
eventSource.close();
```

#### Configure

```
// timeout (milli second)
// default value = 0 (infinity)
eventSource.setReadTimeout(5000);
```

## Download

Add dependencies to Gradle.

Latest Version: [ ![Download](https://api.bintray.com/packages/star-zero/maven/sse/images/download.svg) ](https://bintray.com/star-zero/maven/sse/_latestVersion)

```groovy
repositories {
    jcenter()
}

compile 'com.star_zero:sse:<latest_version>'
// Gradle Plugin 3.0+
implementation 'com.star_zero:sse:<latest_version>'
```

## License

    Copyright 2017 Kenji Abe
    
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
    
        http://www.apache.org/licenses/LICENSE-2.0
    
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.