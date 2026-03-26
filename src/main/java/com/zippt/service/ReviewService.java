package com.zippt.service;

import com.zippt.enums.ReservationStatus;
import com.zippt.model.Reservation;
import com.zippt.model.Review;
import com.zippt.repository.ReservationRepository;
import com.zippt.repository.ReviewRepository;

import java.util.List;
import java.util.Optional;

public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final ReservationRepository reservationRepository;

    public ReviewService(ReviewRepository reviewRepository, ReservationRepository reservationRepository) {
        this.reviewRepository = reviewRepository;
        this.reservationRepository = reservationRepository;
    }

    public Review create(long reservationId, long buyerId, int rating, String content) {
        Optional<Reservation> opt = reservationRepository.findById(reservationId);
        if (opt.isEmpty()) {
            throw new IllegalArgumentException("존재하지 않는 예약입니다.");
        }

        Reservation reservation = opt.get();
        if (reservation.getBuyerId() != buyerId) {
            throw new IllegalArgumentException("본인의 예약 건에만 후기를 작성할 수 있습니다.");
        }
        if (reservation.getStatus() != ReservationStatus.VISITED) {
            throw new IllegalStateException(
                    "방문 완료(Visited) 상태의 예약만 후기 작성이 가능합니다. 현재 상태: "
                            + reservation.getStatus().getDisplayName());
        }
        if (reviewRepository.existsByReservationId(reservationId)) {
            throw new IllegalStateException("이미 후기가 작성된 예약입니다.");
        }
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("별점은 1~5 사이로 입력해주세요.");
        }

        Review review = new Review();
        review.setReservationId(reservationId);
        review.setBuyerId(buyerId);
        review.setAgentId(reservation.getAgentId());
        review.setPropertyId(reservation.getPropertyId());
        review.setRating(rating);
        review.setContent(content);

        Review saved = reviewRepository.save(review);

        reservation.setStatus(ReservationStatus.REVIEWED);
        reservationRepository.save(reservation);

        return saved;
    }

    public List<Review> findByAgentId(long agentId) {
        return reviewRepository.findByAgentId(agentId);
    }

    public List<Review> findByPropertyId(long propertyId) {
        return reviewRepository.findByPropertyId(propertyId);
    }

    public List<Review> findByBuyerId(long buyerId) {
        return reviewRepository.findByBuyerId(buyerId);
    }
}
