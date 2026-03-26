package com.zippt.service;

import com.zippt.enums.ReservationStatus;
import com.zippt.model.Reservation;
import com.zippt.repository.ReservationRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class ReservationService {
    private static final long SCHEDULE_BLOCK_MINUTES = 60;

    private final ReservationRepository reservationRepository;

    public ReservationService(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    public Reservation create(long buyerId, long agentId, long propertyId,
                              LocalDateTime dateTime) {
        if (hasScheduleConflict(agentId, dateTime)) {
            throw new IllegalStateException(
                    "해당 시간대에 중개사의 기존 일정이 존재합니다. 다른 시간을 선택해주세요.");
        }

        Reservation reservation = new Reservation();
        reservation.setBuyerId(buyerId);
        reservation.setAgentId(agentId);
        reservation.setPropertyId(propertyId);
        reservation.setReservationDateTime(dateTime);
        return reservationRepository.save(reservation);
    }

    public boolean confirm(long reservationId, long agentId) {
        Optional<Reservation> opt = reservationRepository.findById(reservationId);
        if (opt.isEmpty()) return false;

        Reservation r = opt.get();
        if (r.getAgentId() != agentId) {
            throw new IllegalArgumentException("본인에게 배정된 예약만 처리할 수 있습니다.");
        }
        if (r.getStatus() != ReservationStatus.PENDING) {
            throw new IllegalStateException("대기 상태의 예약만 확정할 수 있습니다. 현재 상태: " + r.getStatus().getDisplayName());
        }

        if (hasScheduleConflict(agentId, r.getReservationDateTime())) {
            throw new IllegalStateException("해당 시간대에 이미 확정된 일정이 있습니다.");
        }

        r.setStatus(ReservationStatus.CONFIRMED);
        reservationRepository.save(r);
        return true;
    }

    public boolean reject(long reservationId, long agentId) {
        Optional<Reservation> opt = reservationRepository.findById(reservationId);
        if (opt.isEmpty()) return false;

        Reservation r = opt.get();
        if (r.getAgentId() != agentId) {
            throw new IllegalArgumentException("본인에게 배정된 예약만 처리할 수 있습니다.");
        }
        if (r.getStatus() != ReservationStatus.PENDING) {
            throw new IllegalStateException("대기 상태의 예약만 거절할 수 있습니다.");
        }

        r.setStatus(ReservationStatus.REJECTED);
        reservationRepository.save(r);
        return true;
    }

    public boolean markVisited(long reservationId, long agentId) {
        Optional<Reservation> opt = reservationRepository.findById(reservationId);
        if (opt.isEmpty()) return false;

        Reservation r = opt.get();
        if (r.getAgentId() != agentId) {
            throw new IllegalArgumentException("본인에게 배정된 예약만 처리할 수 있습니다.");
        }
        if (r.getStatus() != ReservationStatus.CONFIRMED) {
            throw new IllegalStateException("확정 상태의 예약만 방문 완료 처리할 수 있습니다.");
        }

        r.setStatus(ReservationStatus.VISITED);
        reservationRepository.save(r);
        return true;
    }

    public boolean markReviewed(long reservationId) {
        Optional<Reservation> opt = reservationRepository.findById(reservationId);
        if (opt.isEmpty()) return false;

        Reservation r = opt.get();
        if (r.getStatus() != ReservationStatus.VISITED) {
            throw new IllegalStateException("방문 완료 상태의 예약만 후기를 작성할 수 있습니다.");
        }

        r.setStatus(ReservationStatus.REVIEWED);
        reservationRepository.save(r);
        return true;
    }

    public boolean hasScheduleConflict(long agentId, LocalDateTime dateTime) {
        List<Reservation> confirmed = reservationRepository.findByAgentIdAndStatus(
                agentId, ReservationStatus.CONFIRMED);

        return confirmed.stream().anyMatch(r -> {
            LocalDateTime existing = r.getReservationDateTime();
            long diffMinutes = Math.abs(
                    java.time.Duration.between(existing, dateTime).toMinutes());
            return diffMinutes < SCHEDULE_BLOCK_MINUTES;
        });
    }

    public List<Reservation> findByBuyerId(long buyerId) {
        return reservationRepository.findByBuyerId(buyerId);
    }

    public List<Reservation> findByAgentId(long agentId) {
        return reservationRepository.findByAgentId(agentId);
    }

    public Optional<Reservation> findById(long id) {
        return reservationRepository.findById(id);
    }
}
