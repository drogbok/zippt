package com.zippt.l3l4;

import com.zippt.l3l4.client.control.AuctionClientControl;
import com.zippt.l3l4.common.command.Commands.AuctionConditionInput;
import com.zippt.l3l4.common.command.Commands.BidProposalInput;
import com.zippt.l3l4.common.command.Commands.CriteriaInput;
import com.zippt.l3l4.common.enums.CredentialStatus;
import com.zippt.l3l4.common.enums.PropertyType;
import com.zippt.l3l4.common.enums.WinnerPriorityType;
import com.zippt.l3l4.common.result.Results.AuctionRegistrationResult;
import com.zippt.l3l4.common.result.Results.BidSubmissionResult;
import com.zippt.l3l4.common.result.Results.WinnerSelectionResult;
import com.zippt.l3l4.server.control.AuctionLifecycleControl;
import com.zippt.l3l4.server.domain.Agent;
import com.zippt.l3l4.server.domain.AgentCredential;
import com.zippt.l3l4.server.domain.Auction;
import com.zippt.l3l4.server.domain.Property;
import com.zippt.l3l4.server.domain.Seller;
import com.zippt.l3l4.server.service.DataStore;
import com.zippt.l3l4.server.service.ZIPPTTransactionServer;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Main {
    public static void main(String[] args) {
        DataStore store = new DataStore();
        seed(store);

        ZIPPTTransactionServer server = new ZIPPTTransactionServer(store);
        AuctionClientControl clientControl = new AuctionClientControl(server);

        clientControl.startRegisterAuction("seller-1");
        clientControl.propertySelected("property-1");
        clientControl.auctionConditionEntered(new AuctionConditionInput(
                LocalDateTime.now().plusDays(7),
                "weekly report",
                "verified agent only"
        ));
        clientControl.criteriaEntered(new CriteriaInput(
                WinnerPriorityType.BALANCED,
                BigDecimal.valueOf(0.6),
                BigDecimal.valueOf(0.4)
        ));
        AuctionRegistrationResult auctionResult = clientControl.confirmAuctionRegistration();

        clientControl.startSubmitBid("agent-1");
        clientControl.auctionSelected(auctionResult.auctionId());
        clientControl.bidProposalEntered(new BidProposalInput(
                BigDecimal.valueOf(2.5),
                "local marketing and premium listing",
                30,
                "daily seller feedback"
        ));
        BidSubmissionResult bidResult = clientControl.confirmBidSubmission();

        Auction auction = store.findAuction(auctionResult.auctionId());
        new AuctionLifecycleControl().closeAuction(auction);

        clientControl.startSelectWinner("seller-1");
        clientControl.winnerSelected(auctionResult.auctionId(), bidResult.bidId());
        WinnerSelectionResult winnerResult = clientControl.confirmWinnerSelection();

        System.out.println("[ZIP-PT L3+L4] demo completed");
        System.out.println("  auctionId=" + auctionResult.auctionId());
        System.out.println("  bidId=" + bidResult.bidId());
        System.out.println("  winningBidId=" + winnerResult.winningBidId());
        System.out.println("  notifications=" + store.notifications().size());
    }

    private static void seed(DataStore store) {
        Seller seller = new Seller("seller-1", "Seller", "seller@zippt.test", "hash", "A");
        seller.login();
        seller.addOwnedProperty("property-1");
        store.saveUser(seller);

        Agent agent = new Agent("agent-1", "Agent", "agent@zippt.test", "hash", "ZIP Realty", "Seoul");
        agent.login();
        agent.verifyCredential();
        store.saveUser(agent);

        AgentCredential credential = new AgentCredential("credential-1", "agent-1", "LIC-1", "OFFICE-1");
        credential.verify();
        store.saveCredential(credential);

        Property property = new Property(
                "property-1",
                "seller-1",
                "Seoul Mapo",
                "Seoul",
                BigDecimal.valueOf(84),
                BigDecimal.valueOf(900_000_000L),
                PropertyType.APARTMENT,
                "river view apartment"
        );
        store.saveProperty(property);
    }
}

