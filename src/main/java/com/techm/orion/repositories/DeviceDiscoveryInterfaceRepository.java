package com.techm.orion.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.techm.orion.entitybeans.DeviceDiscoveryEntity;
import com.techm.orion.entitybeans.DeviceDiscoveryInterfaceEntity;
import com.techm.orion.entitybeans.DiscoveryResultDeviceDetailsEntity;
import com.techm.orion.entitybeans.DiscoveryResultDeviceInterfaceEntity;
import com.techm.orion.entitybeans.SiteInfoEntity;

@Repository
public interface DeviceDiscoveryInterfaceRepository extends JpaRepository<DeviceDiscoveryInterfaceEntity, Long> {

	List<DeviceDiscoveryInterfaceEntity>findByDevice(DeviceDiscoveryEntity device);

}