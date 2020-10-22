package com.shevaalex.android.rickmortydatabase.models.episode

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import com.shevaalex.android.rickmortydatabase.models.ApiObjectModel
import com.shevaalex.android.rickmortydatabase.utils.networking.ApiConstants

@Entity
class EpisodeModel(

        @PrimaryKey
        override val id: Int,

        @ColumnInfo(collate = ColumnInfo.LOCALIZED)
        override var name: String,

        override var timeStamp: Int,

        @SerializedName(ApiConstants.ApiCallEpisodeKeys.EPISODE_AIR_DATE)
        var airDate: String,

        @SerializedName(ApiConstants.ApiCallEpisodeKeys.EPISODE_CODE)
        val code: String,

        @SerializedName(ApiConstants.ApiCallEpisodeKeys.EPISODE_CHARACTERS)
        val charactersList: Array<String>

): ApiObjectModel {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is EpisodeModel) return false

        if (id != other.id) return false
        if (name != other.name) return false
        if (airDate != other.airDate) return false
        if (code != other.code) return false
        if (!charactersList.contentEquals(other.charactersList)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + name.hashCode()
        result = 31 * result + airDate.hashCode()
        result = 31 * result + code.hashCode()
        result = 31 * result + charactersList.contentHashCode()
        return result
    }

    override fun toString(): String {
        return "EpisodeModel(id=$id, name='$name', airDate='$airDate', code='$code')"
    }


}