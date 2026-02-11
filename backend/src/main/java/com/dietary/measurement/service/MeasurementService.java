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
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MeasurementService {

    private final MeasurementRepository measurementRepository;
    private final ClientRepository clientRepository;

    @Transactional(readOnly = true)
    public List<MeasurementDTO> getAllMeasurements(UUID clientId, UUID dietitianId) {
        validateClientBelongsToDietitian(clientId, dietitianId);

        List<Measurement> measurements = measurementRepository.findAllByClientIdAndDietitianId(clientId, dietitianId);

        // Add delta for each measurement compared to previous
        return measurements.stream()
                .map(m -> {
                    Optional<Measurement> previous = measurementRepository.findPreviousByClientId(clientId, m.getId());
                    return MeasurementDTO.fromEntityWithDelta(m, previous.orElse(null));
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public MeasurementDTO getLatestMeasurement(UUID clientId, UUID dietitianId) {
        validateClientBelongsToDietitian(clientId, dietitianId);

        Measurement latest = measurementRepository.findLatestByClientId(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Measurement", "clientId", clientId));

        Optional<Measurement> previous = measurementRepository.findPreviousByClientId(clientId, latest.getId());
        return MeasurementDTO.fromEntityWithDelta(latest, previous.orElse(null));
    }

    @Transactional
    public MeasurementDTO createMeasurement(UUID clientId, MeasurementRequest request, UUID dietitianId) {
        Client client = clientRepository.findByIdAndDietitianId(clientId, dietitianId)
                .orElseThrow(() -> new ResourceNotFoundException("Client", "id", clientId));

        // Calculate BMI: weight(kg) / height(m)^2
        BigDecimal bmi = calculateBmi(request.getWeightKg(), client.getHeightCm());

        Measurement measurement = Measurement.builder()
                .client(client)
                .measurementDate(request.getMeasurementDate())
                .weightKg(request.getWeightKg())
                .bodyFatPercentage(request.getBodyFatPercentage())
                .muscleMassKg(request.getMuscleMassKg())
                .waterPercentage(request.getWaterPercentage())
                .boneMassKg(request.getBoneMassKg())
                .visceralFat(request.getVisceralFat())
                .basalMetabolicRate(request.getBasalMetabolicRate())
                .metabolicAge(request.getMetabolicAge())
                .bmi(bmi)
                .notes(request.getNotes())
                .entryMethod(request.getEntryMethod())
                .build();

        measurement = measurementRepository.save(measurement);
        log.info("Created measurement for client '{}' with BMI: {}", client.getFullName(), bmi);

        // Get previous measurement for delta calculation
        Optional<Measurement> previous = measurementRepository.findPreviousByClientId(clientId, measurement.getId());
        return MeasurementDTO.fromEntityWithDelta(measurement, previous.orElse(null));
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
