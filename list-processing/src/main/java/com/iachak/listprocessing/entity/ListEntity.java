package com.iachak.listprocessing.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;
import java.util.List;

@Entity
@Table(name="lists")
@Getter
@Setter
@NoArgsConstructor
public class ListEntity {
    @Id
    @GeneratedValue(strategy=GenerationType.UUID)
    private UUID id;

    @Column(nullable=false)
    private String name;

    private String description;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="created_by")
    private User createdBy;

    @Column(nullable=false)
    private LocalDateTime createdAt= LocalDateTime.now();

    private Integer totalRows=0;

    @OneToMany(mappedBy="list",cascade=CascadeType.ALL,orphanRemoval=true)
    @OrderBy("columnIndex ASC")
    private List<ListColumn> columns=new ArrayList<>();
}
