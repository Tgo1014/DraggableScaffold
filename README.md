# DraggableScaffold

[![](https://jitpack.io/v/Tgo1014/DraggableScaffold.svg)](https://jitpack.io/#Tgo1014/DraggableScaffold)

DraggableScaffold is a library that helps stack one composable on top of another so it can be dragged to reveal the content under it.

## Demo

To see the code for the examples in the gif you can check [here](https://github.com/Tgo1014/DraggableScaffold/blob/e20c54ea365336cf62307b95d0b9ef011aef6a07/app/src/main/java/tgo1014/sample/MainActivity.kt#L62).

![](https://github.com/Tgo1014/DraggableScaffold/raw/main/sources/demo.gif)

## Basic use

```kotlin
DraggableScaffold(
  contentUnderRight = { Text(text = "Hello 😃", Modifier.padding(4.dp)) },
  contentOnTop = {
    Card(
      modifier = Modifier.padding(4.dp).fillMaxWidth(),
      elevation = 4.dp
     { Text(text = "Drag this to show content on the right", Modifier.padding(16.dp)) }
  }
)
```

## Adding to your porject

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
  implementation "com.github.Tgo1014:DraggableScaffold:1.3.0"
  ...
}
```
