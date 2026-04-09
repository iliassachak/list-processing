package com.iachak.listprocessing.repository;

import com.iachak.listprocessing.entity.ListEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ListRepository extends JpaRepository<ListEntity, UUID> {
}
