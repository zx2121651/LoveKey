package com.example.lovekey_clone

import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import java.util.concurrent.atomic.AtomicReference

/**
 * 快速设置快捷磁贴：通知栏一键开关悬浮球服务（LoveKey 浮球快捷唤起入口）。
 *
 * 商业增强：
 *  - 实时同步状态（开启/关闭）与 FloatingBallService 共享
 *  - 无悬浮窗权限时自动引导用户去系统设置页面授权
 *  - 点击开关直接启停服务，无需跳转 APP 设置页，操作效率高
 */
class QuickSettingsTileService : TileService() {

    companion object {
        /** 状态缓存（服务单例 + TileService 多实例同步） */
        val lastState = AtomicReference<Boolean>(false)
    }

    override fun onStartListening() {
        super.onStartListening()
        syncState()
    }

    override fun onTileAdded() {
        super.onTileAdded()
        syncState()
    }

    override fun onClick() {
        super.onClick()
        val current = FloatingBallService.isRunning.get()
        if (current) {
            stopService(Intent(this, FloatingBallService::class.java))
        } else {
            if (!Settings.canDrawOverlays(this)) {
                // 无悬浮窗权限：打开系统设置让用户授权，不开服务
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    android.net.Uri.parse("package:$packageName")
                ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                launchPermissionPage(intent)
                syncState()
                return
            }
            FloatingBallService.start(this)
        }
        syncState()
    }

    /** API 28+ 用 collapse 版直接收起面板；旧版本回退普通启动 */
    private fun launchPermissionPage(intent: Intent) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            startActivityAndCollapse(intent)
        } else {
            startActivity(intent)
        }
    }

    private fun syncState() {
        val running = FloatingBallService.isRunning.get()
        lastState.set(running)
        qsTile?.state = if (running) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        qsTile?.updateTile()
    }
}
