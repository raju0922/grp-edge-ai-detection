package com.edgeguard.app.personalization;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "personal_objects")
public class PersonalObject {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public String name;
    public String notes;
    public long createdAt;
}
