package dev.jx.ec04.entity

import android.os.Parcelable
import com.google.firebase.database.IgnoreExtraProperties
import kotlinx.parcelize.Parcelize

@Parcelize
@IgnoreExtraProperties
data class Post(
    var id: String? = null,
    val authorId: String? = null,
    val petName: String? = null,
    val petAnimal: String? = null,
    val petAge: Int? = null,
    val petSex: String? = null,
    val petStage: String? = null,
    val petWeight: Float? = null,
    val petStory: String? = null,
    val state: String? = null,
    val city: String? = null,
    val contact: String? = null,
    var imageUrl: String? = null,
    val createdAt: String? = null
) : Parcelable