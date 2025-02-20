package com.jg.android_recyclerview

import android.view.View
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.jg.android_recyclerview.ui.activity.MainActivity
import com.jg.android_recyclerview.ui.adapter.MainAdapter
import org.hamcrest.Matcher
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityTest {
    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun testTrashAndRestore() {
        var decorView: View? = null
        activityRule.scenario.onActivity {
            decorView = it.window.decorView
        }

        // 일반 목록 확인
        onView(withId(R.id.recyclerView))
            .check(matches(isDisplayed()))

        // 첫번째 아이템의 휴지통 아이콘 클릭
        onView(withId(R.id.recyclerView))
            .perform(
                RecyclerViewActions.actionOnItemAtPosition<MainAdapter.NormalViewHolder>(
                    0,
                    clickChildViewWithId(R.id.ivTrash)
                )
            )

        // 휴지통 목록으로 이동
        onView(withId(R.id.btnToggle))
            .perform(click())

        // 1초 대기
        Thread.sleep(1000)

        // 휴지통에 있는 아이템의 복구 아이콘 클릭
        onView(withId(R.id.recyclerView))
            .perform(
                RecyclerViewActions.actionOnItemAtPosition<MainAdapter.TrashViewHolder>(
                    0,
                    clickChildViewWithId(R.id.ivTrash)
                )
            )

        // 1초 대기
        Thread.sleep(1000)
    }
}

// Custom ViewAction
fun clickChildViewWithId(id: Int) = object : ViewAction {
    override fun getConstraints(): Matcher<View>? = null

    override fun getDescription(): String = "Click child view with id $id"

    override fun perform(uiController: UiController, view: View) {
        val childView = view.findViewById<View>(id)
        childView.performClick()
    }
}