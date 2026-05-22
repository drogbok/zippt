package com.zippt.l3l4sa.server.service;

import com.zippt.l3l4sa.common.command.Commands.RegisterAuctionCommand;
import com.zippt.l3l4sa.common.enums.NotificationType;
import com.zippt.l3l4sa.common.enums.UserRole;
import com.zippt.l3l4sa.common.result.Results.AuctionRegistrationResult;
import com.zippt.l3l4sa.server.control.AuctionLifecycleControl;
import com.zippt.l3l4sa.server.domain.*;
import java.util.UUID;

public class RegisterAuctionManager {
    private final DataStore store;
    private final AuthenticationManager authenticationManager;
    private final AuctionLifecycleControl lifecycleControl;

    public RegisterAuctionManager(DataStore store, AuthenticationManager authenticationManager,
                                  AuctionLifecycleControl lifecycleControl) {
        this.store = store;
        this.authenticationManager = authenticationManager;
        this.lifecycleControl = lifecycleControl;
    }

    public AuctionRegistrationResult registerAuction(RegisterAuctionCommand command) {
        Seller seller = authenticationManager.validateSeller(command.sellerId());
        Property property = store.findProperty(command.propertyId());
        if (property == null || !property.isOwnedBy(seller.getUserId())) {
            throw new IllegalArgumentException("Seller does not own property: " + command.propertyId());
        }

        synchronized (store.lockFor("property:" + command.propertyId())) {
            if (!validateNoActiveAuction(command.propertyId())) {
                throw new IllegalStateException("Active auction already exists for property.");
            }
            Auction auction = createAuction(command);
            lifecycleControl.openAuction(auction);
            property.markOnAuction();
            store.saveAuction(auction);
            notifyQualifiedAgents(auction);
            return new AuctionRegistrationResult(auction.getAuctionId(), "auction opened");
        }
    }

    public boolean validateNoActiveAuction(String propertyId) {
        return store.auctions().stream()
                .noneMatch(auction -> auction.getPropertyId().equals(propertyId) && auction.hasActiveStatus());
    }

    public boolean validateAuctionCondition(AuctionCondition condition) {
        return condition.validateRequiredFields()
                && condition.validateDeadlineRange(java.time.LocalDateTime.now());
    }

    public Auction createAuction(RegisterAuctionCommand command) {
        String auctionId = "auction-" + UUID.randomUUID();
        AuctionCondition condition = new AuctionCondition(
                "condition-" + UUID.randomUUID(),
                auctionId,
                command.conditionInput().serviceCondition(),
                command.conditionInput().minQualification(),
                command.conditionInput().bidDeadline()
        );
        WinnerSelectionCriteria criteria = new WinnerSelectionCriteria(
                "criteria-" + UUID.randomUUID(),
                auctionId,
                command.criteriaInput().priorityType(),
                command.criteriaInput().commissionRateWeight(),
                command.criteriaInput().marketingStrategyWeight()
        );
        if (!validateAuctionCondition(condition) || !criteria.validateWeights()) {
            throw new IllegalArgumentException("Invalid auction condition or winner criteria.");
        }
        return new Auction(auctionId, command.propertyId(), command.sellerId(), condition, criteria);
    }

    public void notifyQualifiedAgents(Auction auction) {
        for (User user : storeUsers()) {
            if (user.hasRole(UserRole.AGENT)) {
                store.addNotification(new Notification(
                        "noti-" + UUID.randomUUID(),
                        user.getUserId(),
                        NotificationType.NEW_AUCTION,
                        "New auction opened: " + auction.getAuctionId()
                ));
            }
        }
    }

    private Iterable<User> storeUsers() {
        return store.users();
    }
}

