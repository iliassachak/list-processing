package com.iachak.listprocessing.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name="column_permissions",
        uniqueConstraints=@UniqueConstraint(columnNames={"list_id","user_id","column_name"})
)
@Getter
@Setter
@NoArgsConstructor
public class ColumnPermission {
    @Id
    @GeneratedValue(strategy=GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="list_id",nullable=false)
    private ListEntity list;

    @ManyToOne(fetch=FetchType.EAGER)
    @JoinColumn(name="user_id",nullable=false)
    private User user;

    @Column(name="column_name",nullable=false)
    private String columnName;

    private boolean canEdit=false;

    private boolean canView=true;
}
