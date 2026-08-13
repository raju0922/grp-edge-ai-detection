package com.edgeguard.app.personalization;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface PersonalObjectDao {
    @Insert long insert(PersonalObject object);

    @Delete void delete(PersonalObject object);

    @Query("SELECT * FROM personal_objects ORDER BY createdAt DESC")
    List<PersonalObject> getAll();

    @Query("DELETE FROM personal_objects")
    void deleteAll();
}
