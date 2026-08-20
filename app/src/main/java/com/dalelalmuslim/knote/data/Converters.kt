/* Copyright (C) 2026 Tom Frischmuth — GPLv3. Modified by Yosef, 2026. */

package com.dalelalmuslim.knote.data

import androidx.room.TypeConverter

class Converters {
    @TypeConverter fun fromTaskStatus(s: TaskStatus): String = s.name
    @TypeConverter fun toTaskStatus(s: String): TaskStatus = TaskStatus.valueOf(s)
    @TypeConverter fun fromNoteType(t: NoteType): String = t.name
    @TypeConverter fun toNoteType(t: String): NoteType = NoteType.valueOf(t)
    @TypeConverter fun fromTaskCategory(c: TaskCategory): String = c.name
    @TypeConverter fun toTaskCategory(s: String): TaskCategory = TaskCategory.valueOf(s)
}
