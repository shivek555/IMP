package com.Athenaeum.repository;

import com.Athenaeum.entity.Note;
import com.Athenaeum.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NoteRepository extends JpaRepository<Note, Long> {

    List<Note> findByUser(User user);

    List<Note> findByUserAndFolder(User user, String folder);

    @Query("SELECT n FROM Note n WHERE n.user = :user AND " +
           "(LOWER(n.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(n.content) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(n.keywords) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Note> searchByKeyword(@Param("user") User user, @Param("keyword") String keyword);

    @Query("SELECT DISTINCT n.folder FROM Note n WHERE n.user = :user AND n.folder IS NOT NULL")
    List<String> findDistinctFoldersByUser(@Param("user") User user);
}