package com.edgeguard.app.personalization;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {PersonalObject.class}, version = 1, exportSchema = false)
public abstract class EdgeGuardDatabase extends RoomDatabase {
    public abstract PersonalObjectDao personalObjectDao();
}
