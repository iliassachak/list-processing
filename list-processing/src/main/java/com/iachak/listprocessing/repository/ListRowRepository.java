package com.iachak.listprocessing.repository;

import com.iachak.listprocessing.entity.ListRow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ListRowRepository extends JpaRepository<ListRow, UUID> {
    List<ListRow> findByListIdOrderByRowIndex(UUID listId);

    @Query("""
            SELECT r FROM ListRow r
                WHERE r.list.id=:listId
                    AND r.rowIndex BETWEEN :s AND :e
                ORDER BY r.rowIndex
            """)
    List<ListRow> findInRange(@Param("listId") UUID listId, @Param("s") int s, @Param("e") int e);

    @Query("""
            SELECT COALESCE(MAX(r.rowIndex),-1) FROM ListRow r
                        WHERE r.list.id=:listId
            """)
    Integer findMaxRowIndex(@Param("listId") UUID listId);
}
