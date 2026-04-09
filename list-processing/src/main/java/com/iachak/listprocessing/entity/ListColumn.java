package com.iachak.listprocessing.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name="list_columns")
@Getter
@Setter
@NoArgsConstructor
public class ListColumn {
    @Id
    @GeneratedValue(strategy=GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="list_id",nullable=false)
    private ListEntity list;

    @Column(nullable=false)
    private String columnName;

    @Column(nullable=false)
    private Integer columnIndex;

    @Column(nullable=false)
    private String columnType="TEXT";
}
