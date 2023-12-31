package com.techm.c3p.core.repositories;

import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.techm.c3p.core.entitybeans.DiscoveryDashboardEntity;

public interface DiscoveryDashboardRepository extends JpaRepository<DiscoveryDashboardEntity, Integer> {

	DiscoveryDashboardEntity findByDisDashId(String disDashId);

	DiscoveryDashboardEntity findByDisId(int disId);

	Set<DiscoveryDashboardEntity> findByDisCreatedByIgnoreCaseOrderByDisCreatedDateDesc(String user);

	Set<DiscoveryDashboardEntity> findByDisStatusIgnoreCaseAndDisCreatedByIgnoreCase(String status, String user);

	@Query(value = "select count(dis_status) from c3p_t_discovery_dashboard where dis_status=:status and dis_created_by like :creatorName ", nativeQuery = true)
	int getRequestStatusCount(@Param("status") String status, @Param("creatorName") String creatorName);

	Set<DiscoveryDashboardEntity> findByDisStatusIgnoreCaseAndDisCreatedByIgnoreCaseOrderByDisCreatedDateDesc(
			String type, String user);
	
	List<DiscoveryDashboardEntity> findAllByOrderByDisCreatedDateDesc();

	Set<DiscoveryDashboardEntity> findAllByDisStatusIgnoreCaseOrderByDisCreatedDateDesc(String type);

	List<DiscoveryDashboardEntity> findByDisCreatedByOrderByDisCreatedDateDesc(String userName, Pageable pageable);
}
