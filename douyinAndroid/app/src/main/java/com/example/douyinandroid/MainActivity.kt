package com.example.douyinandroid

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.douyinandroid.core.core_auth.AuthPreferences
import com.example.douyinandroid.core.core_video.video.VideoPlayerManager
import com.example.douyinandroid.feature.feature_auth.ui.LoginActivity
import com.example.douyinandroid.feature.feature_main.ui.MainFragment
import com.example.douyinandroid.feature.feature_main.ui.SimpleTabFragment
import com.example.douyinandroid.feature.feature_publish.ui.PublishActivity
import com.google.android.material.button.MaterialButton

class MainActivity : AppCompatActivity() {

    private lateinit var authPreferences: AuthPreferences
    private lateinit var tabs: Map<MainTab, TextView>
    private lateinit var publishTab: MaterialButton
    private val tabFragments = mutableMapOf<MainTab, Fragment>()
    private var currentTab = MainTab.HOME

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Initialize video player manager
        VideoPlayerManager.instance.initialize(this)

        authPreferences = AuthPreferences.getInstance(this)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        if (!authPreferences.isLoggedIn) {
            navigateToLogin()
            return
        }

        setupBottomNavigation()

        if (savedInstanceState == null) {
            showTab(MainTab.HOME)
        } else {
            restoreTabFragments()
            updateSelectedTab(currentTab)
        }
    }

    private fun setupBottomNavigation() {
        tabs = mapOf(
            MainTab.HOME to findViewById(R.id.tabHome),
            MainTab.FRIENDS to findViewById(R.id.tabFriends),
            MainTab.MESSAGE to findViewById(R.id.tabMessage),
            MainTab.ME to findViewById(R.id.tabMe)
        )
        publishTab = findViewById(R.id.tabPublish)

        tabs.forEach { (tab, view) ->
            view.setOnClickListener {
                showTab(tab)
            }
        }
        publishTab.setOnClickListener {
            navigateToPublish()
        }

        updateSelectedTab(MainTab.HOME)
    }

    private fun restoreTabFragments() {
        MainTab.entries.forEach { tab ->
            supportFragmentManager.findFragmentByTag(tab.name)?.let { fragment ->
                tabFragments[tab] = fragment
            }
        }
    }

    private fun showTab(tab: MainTab) {
        if (currentTab == tab && tabFragments[tab]?.isAdded == true) {
            updateSelectedTab(tab)
            return
        }

        currentTab = tab
        if (tab != MainTab.HOME) {
            VideoPlayerManager.instance.pause()
        }

        val transaction = supportFragmentManager.beginTransaction()
        tabFragments.forEach { (_, fragment) ->
            if (fragment.isAdded) {
                transaction.hide(fragment)
            }
        }

        val targetFragment = tabFragments.getOrPut(tab) { createFragment(tab) }
        if (targetFragment.isAdded) {
            transaction.show(targetFragment)
        } else {
            transaction.add(R.id.fragmentContainer, targetFragment, tab.name)
        }

        transaction.commit()
        updateSelectedTab(tab)
    }

    private fun createFragment(tab: MainTab): Fragment {
        return when (tab) {
            MainTab.HOME -> MainFragment.newInstance()
            MainTab.FRIENDS -> SimpleTabFragment.newInstance("朋友", "好友动态正在建设中")
            MainTab.MESSAGE -> SimpleTabFragment.newInstance("消息", "消息列表正在建设中")
            MainTab.ME -> SimpleTabFragment.newInstance("我", "个人主页正在建设中")
        }
    }

    private fun updateSelectedTab(selectedTab: MainTab) {
        if (!::tabs.isInitialized) return
        tabs.forEach { (tab, view) ->
            view.setTextColor(if (tab == selectedTab) Color.WHITE else Color.parseColor("#B3FFFFFF"))
        }
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivityForResult(intent, REQUEST_LOGIN)
    }

    private fun navigateToPublish() {
        VideoPlayerManager.instance.pause()
        startActivity(Intent(this, PublishActivity::class.java))
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_LOGIN) {
            if (resultCode == RESULT_OK) {
                setupBottomNavigation()
                showTab(MainTab.HOME)
            } else {
                finish()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        VideoPlayerManager.instance.release()
    }

    companion object {
        private const val REQUEST_LOGIN = 1001
    }

    private enum class MainTab {
        HOME,
        FRIENDS,
        MESSAGE,
        ME
    }
}
