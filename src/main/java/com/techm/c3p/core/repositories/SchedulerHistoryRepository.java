package com.techm.c3p.core.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.techm.c3p.core.entitybeans.SchedulerHistoryEntity;
@Repository
public interface SchedulerHistoryRepository extends JpaRepository<SchedulerHistoryEntity, Long> {

}
