SSE Android
===

Server-sent events (SSE) client for Android

## Usage

#### Connect

```java
EventSource eventSource = new EventSource(URL, new EventHandler() {
    @Override
    public void onOpen() {
        Log.d(TAG, "Open");
    }

    @Override
    public void onMessage(MessageEvent messageEvent) {
        Log.d(TAG, "Message");
    }

    @Override
    public void onError(Exception e) {
        Log.w(TAG, e);
    }
});

eventSource.connect();
```

#### Close

```java
eventSource.close();
```

## Download

Add dependencies to Gradle.

```groovy
repositories {
    jcenter()
}

compile 'com.star_zero:sse:1.0.0'
// Gradle Plugin 3.0+
implementation 'com.star_zero:sse:1.0.0'
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