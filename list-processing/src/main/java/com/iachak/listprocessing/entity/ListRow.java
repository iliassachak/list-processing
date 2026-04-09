package com.iachak.listprocessing.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name="list_rows")
@Getter
@Setter
@NoArgsConstructor
public class ListRow {
    @Id
    @GeneratedValue(strategy=GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="list_id",nullable=false)
    private ListEntity list;

    @Column(nullable=false)
    private Integer rowIndex;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition="jsonb",nullable=false)
    private Map<String,Object> data=new LinkedHashMap<>();

    private LocalDateTime lastModifiedAt;

    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="last_modified_by")
    private User lastModifiedBy;
}
