package io.github.daisukikaffuchino.han1meviewer.ui.screen.home.homepage

internal actual suspend fun saveImageToGallery(imageUrl: String) = saveImageViaFileKit(imageUrl)
