package io.github.daisukikaffuchino.han1meviewer.ui.preview

// 只放 commonMain 的 @Preview 用得到的假数据，其余仍在 androidMain 的 ComposePreviewDataSource

val fakeTagList2 = listOf(
    "新番",
    "预告",
    "校园",
    "妹妹",
    "姐系",
    "正太",
    "萝莉",
    "伪娘",
    "NTR",
    "SM",
    "暴力",
    "GURO",
    "血腥",
    "人妻"
)

const val longText =
    "这是一段用于预览的简介文本，包含一个链接 https://hanime1.me/watch?v=101573 ，用于验证展开和收" +
            "起功能是否正常。为了触发折叠，这里再补充一些额外内容。超长文本超长文本超长文本超长文本超长文本超长文本超长文本" +
            "超长文本超长文本超长文本超长文本超长文本超长文本超长文本超长文本超长文本超长文本超长文本超长文本超长文本"
