package com.iachak.listprocessing.repository;

import com.iachak.listprocessing.entity.ColumnPermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ColumnPermissionRepository extends JpaRepository<ColumnPermission,UUID> {

    List<ColumnPermission> findByListId(UUID listId);
    List<ColumnPermission> findByListIdAndUserId(UUID listId, UUID userId);
    void deleteByListId(UUID listId);

    @Query("DELETE FROM ColumnPermission cp WHERE cp.list.id=:lid AND cp.user.id=:uid AND cp.columnName=:col")
    @org.springframework.data.jpa.repository.Modifying
    void deleteByKey(@Param("lid") UUID lid, @Param("uid") UUID uid, @Param("col") String col);

    @Query("SELECT CASE WHEN COUNT(cp)>0 THEN true ELSE false END FROM ColumnPermission cp " +
            "WHERE cp.list.id=:lid AND cp.user.id=:uid AND cp.columnName=:col AND cp.canEdit=true")
    boolean canUserEditColumn(@Param("lid") UUID lid, @Param("uid") UUID uid, @Param("col") String col);
}
