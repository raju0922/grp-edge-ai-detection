package com.edgeguard.app.personalization;

import android.content.Context;
import androidx.room.Room;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PersonalObjectManager {
    private final EdgeGuardDatabase db;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public PersonalObjectManager(Context context) {
        db = Room.databaseBuilder(context.getApplicationContext(),
                EdgeGuardDatabase.class, "edgeguard.db").build();
    }

    public void add(String name, String notes) {
        PersonalObject o = new PersonalObject();
        o.name = name;
        o.notes = notes;
        o.createdAt = System.currentTimeMillis();
        executor.execute(() -> db.personalObjectDao().insert(o));
    }

    public void clear() {
        executor.execute(() -> db.personalObjectDao().deleteAll());
    }

    public void shutdown() {
        executor.shutdown();
        db.close();
    }
}
