package com.eldercare.service.schedule;

import com.eldercare.service.entity.MedicationSlotEntity;
import com.eldercare.service.repository.MedicationSlotRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class MedicationSlotScheduler {

    private static final Logger log = LogManager.getLogger(MedicationSlotScheduler.class);

    private final MedicationSlotRepository medicationSlotRepository;
    private final String slotMissedCron;

    public MedicationSlotScheduler(MedicationSlotRepository medicationSlotRepository,
                                   @Value("${app.slot.missed.cron}") String slotMissedCron) {
        this.medicationSlotRepository = medicationSlotRepository;
        this.slotMissedCron = slotMissedCron;
    }

    @Scheduled(cron = "${app.slot.missed.cron}")
    @Transactional
    public void markMissedSlots() {
        LocalDateTime now = LocalDateTime.now();
        List<MedicationSlotEntity> overdue = medicationSlotRepository.findByStatusAndScheduledTimeBefore("PENDING", now);
        if (overdue.isEmpty()) {
            return;
        }
        overdue.forEach(slot -> slot.setStatus("MISSED"));
        medicationSlotRepository.saveAll(overdue);
        log.info("Marked {} overdue medication slots as MISSED", overdue.size());
    }
}
