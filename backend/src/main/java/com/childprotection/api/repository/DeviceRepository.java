package com.childprotection.api.repository;

import com.childprotection.api.model.Device;
import com.childprotection.api.model.enums.DeviceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface DeviceRepository extends JpaRepository<Device, UUID> {
    List<Device> findByChildId(UUID childId);
    List<Device> findByFamilyId(UUID familyId);
    List<Device> findByFamilyIdAndStatus(UUID familyId, DeviceStatus status);
}
