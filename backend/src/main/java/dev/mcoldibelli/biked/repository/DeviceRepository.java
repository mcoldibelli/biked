package dev.mcoldibelli.biked.repository;

import dev.mcoldibelli.biked.model.Device;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeviceRepository extends JpaRepository<Device, UUID> {

  Optional<Device> findByMacAddress(String macAddress);

  Page<Device> findAllByUserId(UUID userId, Pageable pageable);

  boolean existsByMacAddress(String macAddress);
}
