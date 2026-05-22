package com.zippt.l2;

import com.zippt.l2.enums.AuctionStatus;
import com.zippt.l2.exception.StorageException;
import com.zippt.l2.model.Auction;
import com.zippt.l2.model.Bid;
import com.zippt.l2.model.BidForm;
import com.zippt.l2.model.BidReceipt;
import com.zippt.l2.model.User;
import com.zippt.l2.port.AuctionRepository;
import com.zippt.l2.port.BidRepository;
import com.zippt.l2.port.NotificationQueue;
import com.zippt.l2.usecase.SubmitBidUseCase;
import com.zippt.l2.usecase.ValidateUser;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        User seller = new User("seller-1", "Seller", true, true);
        User agent = new User("agent-1", "Certified Agent", true, true);

        InMemoryAuctionRepository auctionRepository = new InMemoryAuctionRepository();
        InMemoryBidRepository bidRepository = new InMemoryBidRepository();
        ConsoleNotificationQueue notificationQueue = new ConsoleNotificationQueue();

        Auction auction = new Auction(
                "auction-1",
                AuctionStatus.OPEN,
                LocalDateTime.now().plusDays(7),
                seller
        );
        auctionRepository.save(auction);

        SubmitBidUseCase submitBidUseCase = new SubmitBidUseCase(
                new ValidateUser(),
                auctionRepository,
                bidRepository,
                notificationQueue
        );

        BidForm form = new BidForm(
                2.5,
                "Online listing plus local buyer network",
                30,
                "Weekly seller report and dedicated showing schedule"
        );

        try {
            BidReceipt receipt = submitBidUseCase.execute(agent, auction.getId(), form);

            System.out.println("[ZIP-PT L2] SubmitBid demo completed.");
            System.out.println("  Receipt: " + receipt);
            System.out.println("  Auction status: " + auction.getStatus());
            System.out.println("  Bid count: " + auction.getBidCount());
            System.out.println("  Stored bids: " + bidRepository.count());
            System.out.println("  Notifications: " + notificationQueue.count());
        } catch (Exception e) {
            System.out.println("[ZIP-PT L2] SubmitBid demo failed.");
            e.printStackTrace(System.out);
        }
    }

    private static final class InMemoryAuctionRepository implements AuctionRepository {
        private final Map<String, Auction> auctions = new LinkedHashMap<>();

        void save(Auction auction) {
            auctions.put(auction.getId(), auction);
        }

        @Override
        public List<Auction> findActive() {
            List<Auction> activeAuctions = new ArrayList<>();
            for (Auction auction : auctions.values()) {
                if (auction.acceptsBids()) {
                    activeAuctions.add(auction);
                }
            }
            return activeAuctions;
        }

        @Override
        public Auction findById(String auctionId) {
            return auctions.get(auctionId);
        }
    }

    private static final class InMemoryBidRepository implements BidRepository {
        private final List<Bid> bids = new ArrayList<>();

        @Override
        public void save(Bid bid) throws StorageException {
            bids.add(bid);
        }

        @Override
        public void rollback() {
            if (!bids.isEmpty()) {
                bids.remove(bids.size() - 1);
            }
        }

        @Override
        public boolean hasActiveBid(User agent, String auctionId) {
            for (Bid bid : bids) {
                if (bid.getAgent().getId().equals(agent.getId())
                        && bid.getAuction().getId().equals(auctionId)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean isResubmitAllowed(String auctionId) {
            return false;
        }

        int count() {
            return bids.size();
        }
    }

    private static final class ConsoleNotificationQueue implements NotificationQueue {
        private final List<Bid> notifications = new ArrayList<>();

        @Override
        public void enqueue(User seller, Bid bid) {
            notifications.add(bid);
            System.out.println("  Notification queued for seller: " + seller.getName());
        }

        int count() {
            return notifications.size();
        }
    }
}
