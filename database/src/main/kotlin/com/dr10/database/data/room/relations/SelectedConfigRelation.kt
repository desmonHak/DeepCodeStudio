package com.dr10.database.data.room.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.dr10.database.data.room.entities.ColorSchemesEntity
import com.dr10.database.data.room.entities.SelectedConfigHistoryEntity
import com.dr10.database.data.room.entities.SyntaxAndSuggestionsEntity

data class SelectedConfigRelation(
    @Embedded val syntaxAndSuggestionsEntity: SyntaxAndSuggestionsEntity,
    @Relation(
        parentColumn = "unique_id",
        entityColumn = "unique_id"
    )
    val selectedConfigHistoryEntity: SelectedConfigHistoryEntity,
    @Relation(
        parentColumn = "unique_id",
        entityColumn = "unique_id"
    )
    val colorSchemesEntity: ColorSchemesEntity
)
