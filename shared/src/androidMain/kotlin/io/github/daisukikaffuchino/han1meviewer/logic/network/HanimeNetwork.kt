package io.github.daisukikaffuchino.han1meviewer.logic.network

import de.jensklingenberg.ktorfit.Ktorfit
import io.github.daisukikaffuchino.han1meviewer.GETCHU_BASE_URL
import io.github.daisukikaffuchino.han1meviewer.HANIME_BASE_URL
import io.github.daisukikaffuchino.han1meviewer.logic.network.service.GetchuService
import io.github.daisukikaffuchino.han1meviewer.logic.network.service.HanimeBaseService
import io.github.daisukikaffuchino.han1meviewer.logic.network.service.HanimeCommentService
import io.github.daisukikaffuchino.han1meviewer.logic.network.service.HanimeMyListService
import io.github.daisukikaffuchino.han1meviewer.logic.network.service.HanimeSubscriptionService
import io.github.daisukikaffuchino.han1meviewer.logic.network.service.createGetchuService
import io.github.daisukikaffuchino.han1meviewer.logic.network.service.createHanimeBaseService
import io.github.daisukikaffuchino.han1meviewer.logic.network.service.createHanimeCommentService
import io.github.daisukikaffuchino.han1meviewer.logic.network.service.createHanimeMyListService
import io.github.daisukikaffuchino.han1meviewer.logic.network.service.createHanimeSubscriptionService

object HanimeNetwork {
    var hanimeService = _hanimeService
        private set
    var getchuService = _getchuService
        private set
    var commentService = _commentService
        private set
    var myListService = _myListService
        private set
    var subscriptionService = _subscriptionService
        private set

    private val hanimeKtorfit
        get() = Ktorfit.Builder()
            .baseUrl(HANIME_BASE_URL)
            .httpClient(ServiceCreator.hClient)
            .build()

    private val getchuKtorfit
        get() = Ktorfit.Builder()
            .baseUrl(GETCHU_BASE_URL)
            .httpClient(ServiceCreator.getchuClient)
            .build()

    private val _hanimeService: HanimeBaseService
        get() = hanimeKtorfit.createHanimeBaseService()

    private val _getchuService: GetchuService
        get() = getchuKtorfit.createGetchuService()

    private val _commentService: HanimeCommentService
        get() = hanimeKtorfit.createHanimeCommentService()

    private val _myListService: HanimeMyListService
        get() = hanimeKtorfit.createHanimeMyListService()

    private val _subscriptionService: HanimeSubscriptionService
        get() = hanimeKtorfit.createHanimeSubscriptionService()

    fun rebuildNetwork() {
        ServiceCreator.rebuildHttpClient()
        hanimeService = _hanimeService
        getchuService = _getchuService
        commentService = _commentService
        myListService = _myListService
        subscriptionService = _subscriptionService
    }
}
