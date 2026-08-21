package io.github.daisukikaffuchino.han1meviewer.logic.entity

import han1meviewer.shared.generated.resources.Res
import han1meviewer.shared.generated.resources.checkin_type_masturbation
import han1meviewer.shared.generated.resources.checkin_type_oral
import han1meviewer.shared.generated.resources.checkin_type_other
import han1meviewer.shared.generated.resources.checkin_type_sex
import han1meviewer.shared.generated.resources.checkin_type_wet_dream
import org.jetbrains.compose.resources.StringResource

enum class CheckInType(val displayNameRes: StringResource, val storeName: String) {
    MASTURBATION(Res.string.checkin_type_masturbation, "自慰"),
    WET_DREAM(Res.string.checkin_type_wet_dream, "梦遗"),
    SEX(Res.string.checkin_type_sex, "做爱"),
    ORAL(Res.string.checkin_type_oral, "口交"),
    OTHER(Res.string.checkin_type_other, "其它");

    companion object {
        fun fromDisplayName(name: String): CheckInType =
            entries.firstOrNull { it.storeName == name } ?: MASTURBATION
    }
}