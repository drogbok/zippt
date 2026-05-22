package com.zippt.l2.port;

import com.zippt.l2.model.Bid;
import com.zippt.l2.model.User;
import com.zippt.l2.exception.StorageException;

/**
 * [L2] Port : 입찰 저장소.
 */
public interface BidRepository {

    /** Description 9단계 : 입찰서 영속화. */
    void save(Bid bid) throws StorageException;

    /** Alternative A4-1 : 트랜잭션 롤백. */
    void rollback();

    /** Precondition P4 : 해당 경매에 이미 활성 입찰서가 존재하는가. */
    boolean hasActiveBid(User agent, String auctionId);

    /** Precondition P4 : 해당 경매가 재제출을 허용하는가. */
    boolean isResubmitAllowed(String auctionId);
}
