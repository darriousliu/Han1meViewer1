package io.github.daisukikaffuchino.han1meviewer.logic.platform

/**
 * 刷新签到桌面小组件。只有 Android 有小组件，其余平台是空实现。
 *
 * 打卡数据变了（签到、删记录、导入备份、设置页改配置）之后都该调一次。
 */
expect suspend fun updateCheckInWidget()
