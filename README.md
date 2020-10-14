# CircularProgressBar

![img](https://github.com/yavuzyagiz/CircularProgressBar/blob/main/img/CircularProgressBar.PNG?raw=true)
## How to use
  - ### Kotlin
```
  progress_circular.apply {
            colorMode = CircularProgress.ColorMode.RED
            animationDuration = 10000L
            progressTextSize = 100f
            progress = 0f
  }          
```
  - ### XML
```
  <com.example.circularprogressbar.CircularProgress
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        app:progressTextSize="100dp"
        app:progress="10"
        app:colorMode="red"
        />        
```

  - ### Add Listener
```
    circularProgressBar.setCircularProgressListener(object: CircularProgress.CircularProgressListener{
            override fun onProgressValue(progress: Float) {}
            override fun onProgressStart() {}
            override fun onProgressPause() {}
            override fun onProgressResume() {}
            override fun onProgressEnd() {}
        })

```
