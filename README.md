# DraggableScaffold

[![](https://jitpack.io/v/Tgo1014/DraggableScaffold.svg)](https://jitpack.io/#Tgo1014/DraggableScaffold)

DraggableScaffold is a library that helps stack one composable on top of another so it can be dragged to reveal the content under it.

***Current Compose Version: 1.0.0-beta08***

## How to use

1 - Add it in your root build.gradle at the end of repositories:
```gradle
allprojects {
    repositories {
      ...
      maven { url 'https://jitpack.io' }
      ...
  }
}
```

2 - Add the dependency:
```gradle
dependencies {
  ...
  implementation "com.github.Tgo1014:DraggableScaffold:1.0.0"
  ...
}
```

## Demo

To see the code for the examples in the gif you can check [here](https://github.com/Tgo1014/DraggableScaffold/blob/f1b7bd1a68e5c1b56f6cbf04afdd23cd9147fdcb/app/src/main/java/tgo1014/draggablescaffold/MainActivity.kt#L42).

![](https://github.com/Tgo1014/DraggableScaffold/raw/main/sources/demo.gif)