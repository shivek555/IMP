package com.Athenaeum.repository;

import com.Athenaeum.entity.Document;
import com.Athenaeum.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {

    List<Document> findByUser(User user);
    

    List<Document> findByUserAndFolder(User user, String folder);

    @Query("SELECT d FROM Document d WHERE d.user = :user AND " +
           "(LOWER(d.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(d.content) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(d.keywords) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Document> searchByKeyword(@Param("user") User user, @Param("keyword") String keyword);

    @Query("SELECT DISTINCT d.folder FROM Document d WHERE d.user = :user AND d.folder IS NOT NULL")
    List<String> findDistinctFoldersByUser(@Param("user") User user);

    @Query("SELECT d FROM Document d WHERE d.user = :user AND d.fileType = :fileType")
    List<Document> findByUserAndFileType(@Param("user") User user, @Param("fileType") String fileType);
    

    
}
