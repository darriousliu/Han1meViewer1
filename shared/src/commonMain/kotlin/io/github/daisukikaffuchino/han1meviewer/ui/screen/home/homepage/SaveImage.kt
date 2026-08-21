package io.github.daisukikaffuchino.han1meviewer.ui.screen.home.homepage

/** 下载远程图片并保存到系统相册。 */
internal expect suspend fun saveImageToGallery(imageUrl: String)
