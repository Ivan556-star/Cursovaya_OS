package com.example.testlauncher2

import android.content.Intent
import android.content.pm.ResolveInfo
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GestureDetectorCompat
import com.example.testlauncher2.databinding.ActivityMainBinding
import kotlin.math.abs

class MainActivity : AppCompatActivity() {
    private lateinit var mainBinding: ActivityMainBinding
    private lateinit var detector: GestureDetectorCompat


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mainBinding.root)
        PREF_FAVORITE_APPS = getSharedPreferences(FAVORITE_APPS, MODE_PRIVATE)
        PREF_SWIPE_APPS = getSharedPreferences(SWIPE_APPS, MODE_PRIVATE)

        detector = GestureDetectorCompat(this, DiaryGestureListener())


//        val editor = pref?.edit()
//        val packages = mutableSetOf<String>("2")
//        packages.add("awda")
//        pref.getStringSet("wdad", null)
//        editor.putStringSet("dawd", packages)

        /**Приведенная инструкция возвращает список всех приложений,
        доступных для запуска пользователем, в виде [ResolveInfo].
        Этот класс содержит множество информации, которая нам не нужна,
        поэтому мы создаем наш собственный класс данных под названием
        [AppBlock] для облегчения обработки данных.*/

        val resolvedApplist: List<ResolveInfo> = packageManager
            .queryIntentActivities(
                Intent(Intent.ACTION_MAIN, null)
                    .addCategory(Intent.CATEGORY_LAUNCHER), 0
            )


        /**поверяем приложения. Если пакет приложения не равен лончеру
        который мы создали, то обавляем объект [AppBlock]
        в [appList]*/

        val appList = ArrayList<AppBlock>()

        for (ri in resolvedApplist) {
            if (ri.activityInfo.packageName != this.packageName) {
                val app = AppBlock(
                    ri.loadLabel(packageManager).toString(), // получаем название
                    ri.activityInfo.loadIcon(packageManager), // получаем иконку
                    ri.activityInfo.packageName, // получаем имя пакета приложения
                    isFavoriteApp(ri.activityInfo.packageName) // проверяем изначально, приложение является избранным или нет.
                )
                appList.add(app)
            }
        }

        // Затем Adapter инициализируется, и список приложений передается ему с помощью функции passAppList().
        // сортируем приложения в алфовитном порядке.


        mainBinding.appRV.adapter = Adapter(this).also {
            it.passAppList(sortedApps(appList))
        }
    }


    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return if (detector.onTouchEvent(event))
            true
        else
            super.onTouchEvent(event)

    }

    inner class DiaryGestureListener : GestureDetector.SimpleOnGestureListener() {

        private val SWIPE_THRESHOLD = 100 // ПОРОГ РАЗМАХА
        private val SWIPE_VELOCITY_THRESHOLD = 100 // ПОРОГ СКОРОСТИ СВАЙПА

        override fun onFling(
            downEvent: MotionEvent?,
            moveEvent: MotionEvent?,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            val diffX = moveEvent?.x?.minus(downEvent!!.x) ?: 0.0F
            val diffY = moveEvent?.y?.minus(downEvent!!.y) ?: 0.0F

            return if (Math.abs(diffX) > Math.abs(diffY)) {
                // this is a left or right swipe
                if (abs(diffX) > SWIPE_THRESHOLD && abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffX > 0) {
                        // right swipe
                        if (!this@MainActivity.onSwipeRight())
                            Toast.makeText(
                                this@MainActivity,
                                "Вы ещё не добавили приложение для свайпа справа",
                                Toast.LENGTH_SHORT
                            ).show()
                    } else {
                        // left swipe.
                        if(!this@MainActivity.onLeftSwipe())
                            Toast.makeText(
                                this@MainActivity,
                                "Вы ещё не добавили приложение для свайпа слева",
                                Toast.LENGTH_SHORT
                            ).show()
                    }
                    true
                } else
                    super.onFling(downEvent, moveEvent, velocityX, velocityY)

            } else {
                // this is either a bottom or top swipe.
                if (abs(diffY) > SWIPE_THRESHOLD && abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffY > 0)
                        this@MainActivity.onSwipeTop()
                    else
                        this@MainActivity.onSwipeBottom()

                    true
                } else
                    super.onFling(downEvent, moveEvent, velocityX, velocityY)

            }


        }
    }


    private fun onSwipeBottom() {
        Toast.makeText(this, "Bottom Swipe", Toast.LENGTH_SHORT).show()
    }

    private fun onSwipeTop() {
        Toast.makeText(this, "Top Swipe", Toast.LENGTH_SHORT).show()
    }

    private fun onLeftSwipe(): Boolean {
        val tmpStr = PREF_SWIPE_APPS.getString(SWIPE_LEFT, "")
        if (tmpStr?.isEmpty() == true)
            return false
        startActivity(
            packageManager.getLaunchIntentForPackage(
                tmpStr ?: "com.krsolutions.yetanotherlauncher"
            )
        )
        return true
    }

    private fun onSwipeRight(): Boolean {
        val tmpStr = PREF_SWIPE_APPS.getString(SWIPE_RIGHT, "")
        if (tmpStr?.isEmpty() == true)
            return false
        startActivity(
            packageManager.getLaunchIntentForPackage(
                tmpStr ?: "com.krsolutions.yetanotherlauncher"
            )
        )
        return true
    }


    override fun onBackPressed() {
        // чтобы ничего не происходило
    }
}




