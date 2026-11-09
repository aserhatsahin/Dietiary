package com.dietary.measurement.service;

import com.dietary.client.domain.Client;
import com.dietary.client.repository.ClientRepository;
import com.dietary.common.exception.ResourceNotFoundException;
import com.dietary.measurement.controller.dto.MeasurementDTO;
import com.dietary.measurement.controller.dto.MeasurementRequest;
import com.dietary.measurement.domain.Measurement;
import com.dietary.measurement.repository.MeasurementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MeasurementService {

    private final MeasurementRepository measurementRepository;
    private final ClientRepository clientRepository;

    @Transactional(readOnly = true)
    public List<MeasurementDTO> getAllMeasurements(UUID clientId, UUID dietitianId) {
        validateClientBelongsToDietitian(clientId, dietitianId);

        // Newest first, so each measurement's previous one is the next element.
        List<Measurement> measurements = measurementRepository.findAllByClientIdAndDietitianId(clientId, dietitianId);
        List<MeasurementDTO> result = new ArrayList<>(measurements.size());
        for (int i = 0; i < measurements.size(); i++) {
            Measurement previous = i + 1 < measurements.size() ? measurements.get(i + 1) : null;
            result.add(MeasurementDTO.fromEntityWithDelta(measurements.get(i), previous));
        }
        return result;
    }

    @Transactional(readOnly = true)
    public MeasurementDTO getLatestMeasurement(UUID clientId, UUID dietitianId) {
        validateClientBelongsToDietitian(clientId, dietitianId);

        Measurement latest = measurementRepository.findLatestByClientId(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Measurement", "clientId", clientId));
        return withDelta(latest);
    }

    @Transactional
    public MeasurementDTO createMeasurement(UUID clientId, MeasurementRequest request, UUID dietitianId) {
        Client client = clientRepository.findByIdAndDietitianId(clientId, dietitianId)
                .orElseThrow(() -> new ResourceNotFoundException("Client", "id", clientId));

        Measurement measurement = Measurement.builder().client(client).build();
        applyRequest(measurement, request, client);

        measurement = measurementRepository.saveAndFlush(measurement);
        log.info("Created measurement for client '{}' with BMI: {}", client.getId(), measurement.getBmi());
        return withDelta(measurement);
    }

    @Transactional
    public MeasurementDTO updateMeasurement(UUID clientId, UUID measurementId, MeasurementRequest request,
                                            UUID dietitianId) {
        Measurement measurement = findOwnedMeasurement(clientId, measurementId, dietitianId);
        applyRequest(measurement, request, measurement.getClient());

        measurement = measurementRepository.saveAndFlush(measurement);
        log.info("Updated measurement '{}' for client '{}'", measurementId, clientId);
        return withDelta(measurement);
    }

    @Transactional
    public void deleteMeasurement(UUID clientId, UUID measurementId, UUID dietitianId) {
        Measurement measurement = findOwnedMeasurement(clientId, measurementId, dietitianId);
        measurementRepository.delete(measurement);
        log.info("Deleted measurement '{}' for client '{}'", measurementId, clientId);
    }

    private void applyRequest(Measurement measurement, MeasurementRequest request, Client client) {
        measurement.setMeasurementDate(request.getMeasurementDate());
        measurement.setWeightKg(request.getWeightKg());
        measurement.setBodyFatPercentage(request.getBodyFatPercentage());
        measurement.setMuscleMassKg(request.getMuscleMassKg());
        measurement.setWaterPercentage(request.getWaterPercentage());
        measurement.setBoneMassKg(request.getBoneMassKg());
        measurement.setVisceralFat(request.getVisceralFat());
        measurement.setBasalMetabolicRate(request.getBasalMetabolicRate());
        measurement.setMetabolicAge(request.getMetabolicAge());
        measurement.setNotes(request.getNotes());
        measurement.setEntryMethod(request.getEntryMethod());
        // BMI = weight(kg) / height(m)^2, recalculated whenever the weight changes
        measurement.setBmi(calculateBmi(request.getWeightKg(), client.getHeightCm()));
    }

    private MeasurementDTO withDelta(Measurement measurement) {
        Measurement previous = measurementRepository.findPrevious(measurement.getClient().getId(), measurement.getId(),
                measurement.getMeasurementDate(), measurement.getCreatedAt()).orElse(null);
        return MeasurementDTO.fromEntityWithDelta(measurement, previous);
    }

    private Measurement findOwnedMeasurement(UUID clientId, UUID measurementId, UUID dietitianId) {
        return measurementRepository.findByIdAndClientIdAndDietitianId(measurementId, clientId, dietitianId)
                .orElseThrow(() -> new ResourceNotFoundException("Measurement", "id", measurementId));
    }

    private BigDecimal calculateBmi(BigDecimal weightKg, BigDecimal heightCm) {
        if (weightKg == null || heightCm == null || heightCm.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        // Convert height from cm to meters
        BigDecimal heightM = heightCm.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
        // BMI = weight / height^2
        BigDecimal heightSquared = heightM.multiply(heightM);
        return weightKg.divide(heightSquared, 2, RoundingMode.HALF_UP);
    }

    private void validateClientBelongsToDietitian(UUID clientId, UUID dietitianId) {
        if (!clientRepository.findByIdAndDietitianId(clientId, dietitianId).isPresent()) {
            throw new ResourceNotFoundException("Client", "id", clientId);
        }
    }
}
