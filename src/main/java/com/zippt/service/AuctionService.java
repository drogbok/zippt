package com.zippt.service;

import com.zippt.enums.AuctionStatus;
import com.zippt.model.Auction;
import com.zippt.model.Bid;
import com.zippt.repository.AuctionRepository;

import java.util.List;
import java.util.Optional;

public class AuctionService {
    private final AuctionRepository auctionRepository;
    private final Object awardLock = new Object();

    public AuctionService(AuctionRepository auctionRepository) {
        this.auctionRepository = auctionRepository;
    }

    public Auction create(long propertyId, long sellerId, String requirements) {
        Auction auction = new Auction();
        auction.setPropertyId(propertyId);
        auction.setSellerId(sellerId);
        auction.setRequirements(requirements);
        auction.setStatus(AuctionStatus.OPEN);
        return auctionRepository.saveAuction(auction);
    }

    public Bid placeBid(long auctionId, long agentId, double commissionRate, String conditions) {
        Optional<Auction> opt = auctionRepository.findAuctionById(auctionId);
        if (opt.isEmpty()) {
            throw new IllegalArgumentException("존재하지 않는 경매입니다.");
        }

        Auction auction = opt.get();
        if (auction.getStatus() != AuctionStatus.OPEN && auction.getStatus() != AuctionStatus.ACTIVE) {
            throw new IllegalStateException("입찰 가능한 상태가 아닙니다. 현재 상태: " + auction.getStatus().getDisplayName());
        }

        boolean alreadyBid = auctionRepository.findBidsByAuctionId(auctionId).stream()
                .anyMatch(b -> b.getAgentId() == agentId);
        if (alreadyBid) {
            throw new IllegalStateException("이미 이 경매에 입찰하셨습니다.");
        }

        if (auction.getStatus() == AuctionStatus.OPEN) {
            auction.setStatus(AuctionStatus.ACTIVE);
            auctionRepository.saveAuction(auction);
        }

        Bid bid = new Bid();
        bid.setAuctionId(auctionId);
        bid.setAgentId(agentId);
        bid.setCommissionRate(commissionRate);
        bid.setConditions(conditions);
        return auctionRepository.saveBid(bid);
    }

    /**
     * synchronized로 동시성 제어: 중복 낙찰 방지
     */
    public boolean awardBid(long auctionId, long bidId, long sellerId) {
        synchronized (awardLock) {
            Optional<Auction> auctionOpt = auctionRepository.findAuctionById(auctionId);
            if (auctionOpt.isEmpty()) return false;

            Auction auction = auctionOpt.get();
            if (auction.getSellerId() != sellerId) {
                throw new IllegalArgumentException("본인의 경매만 낙찰 처리할 수 있습니다.");
            }
            if (auction.getStatus() == AuctionStatus.AWARDED) {
                throw new IllegalStateException("이미 낙찰이 완료된 경매입니다.");
            }
            if (auction.getStatus() != AuctionStatus.ACTIVE && auction.getStatus() != AuctionStatus.OPEN) {
                throw new IllegalStateException("낙찰 가능한 상태가 아닙니다.");
            }

            Optional<Bid> bidOpt = auctionRepository.findBidById(bidId);
            if (bidOpt.isEmpty() || bidOpt.get().getAuctionId() != auctionId) {
                throw new IllegalArgumentException("해당 경매에 속한 유효한 입찰이 아닙니다.");
            }

            auction.setStatus(AuctionStatus.AWARDED);
            auction.setAwardedBidId(bidId);
            auctionRepository.saveAuction(auction);
            return true;
        }
    }

    public boolean complete(long auctionId, long sellerId) {
        Optional<Auction> opt = auctionRepository.findAuctionById(auctionId);
        if (opt.isEmpty()) return false;

        Auction auction = opt.get();
        if (auction.getSellerId() != sellerId) {
            throw new IllegalArgumentException("본인의 경매만 처리할 수 있습니다.");
        }
        if (auction.getStatus() != AuctionStatus.AWARDED) {
            throw new IllegalStateException("낙찰 상태의 경매만 거래 완료 처리할 수 있습니다.");
        }

        auction.setStatus(AuctionStatus.COMPLETED);
        auctionRepository.saveAuction(auction);
        return true;
    }

    public boolean cancel(long auctionId, long sellerId) {
        Optional<Auction> opt = auctionRepository.findAuctionById(auctionId);
        if (opt.isEmpty()) return false;

        Auction auction = opt.get();
        if (auction.getSellerId() != sellerId) {
            throw new IllegalArgumentException("본인의 경매만 처리할 수 있습니다.");
        }
        if (auction.getStatus() == AuctionStatus.COMPLETED) {
            throw new IllegalStateException("이미 완료된 경매는 취소할 수 없습니다.");
        }

        auction.setStatus(AuctionStatus.CANCELLED);
        auctionRepository.saveAuction(auction);
        return true;
    }

    public List<Auction> findBySellerId(long sellerId) {
        return auctionRepository.findAuctionsBySellerId(sellerId);
    }

    public List<Auction> findOpenOrActiveAuctions() {
        return auctionRepository.findOpenOrActiveAuctions();
    }

    public List<Bid> findBidsByAuctionId(long auctionId) {
        return auctionRepository.findBidsByAuctionId(auctionId);
    }

    public List<Bid> findBidsByAgentId(long agentId) {
        return auctionRepository.findBidsByAgentId(agentId);
    }

    public Optional<Auction> findAuctionById(long id) {
        return auctionRepository.findAuctionById(id);
    }
}
