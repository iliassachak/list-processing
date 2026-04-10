package com.iachak.listprocessing.repository;

import com.iachak.listprocessing.entity.ListEntity;
import com.iachak.listprocessing.entity.RowAssignment;
import com.iachak.listprocessing.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface RowAssignmentRepository extends JpaRepository<RowAssignment,UUID> {
    List<RowAssignment> findByListId(UUID listId);
    List<RowAssignment> findByListIdAndUserId(UUID listId, UUID userId);

    @Query("SELECT CASE WHEN COUNT(ra)>0 THEN true ELSE false END " +
            "FROM RowAssignment ra JOIN ListRow lr ON lr.list.id=ra.list.id AND lr.id=:rowId " +
            "WHERE ra.list.id=:listId AND ra.user.id=:userId AND lr.rowIndex BETWEEN ra.startRow AND ra.endRow")
    boolean isRowInUserRange(@Param("listId") UUID listId,
                             @Param("rowId") UUID rowId,
                             @Param("userId") UUID userId);

    @Query("""
        SELECT DISTINCT ra.list
        FROM RowAssignment ra
        WHERE ra.user = :user
    """)
    List<ListEntity> findListsByUser(User user);
}
