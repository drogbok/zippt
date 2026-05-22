package com.zippt.l2.port;

import com.zippt.l2.model.Auction;
import java.util.List;

/**
 * [L2] Port : 경매 저장소.
 */
public interface AuctionRepository {

    /** Description 2~3단계 : 활성 경매 목록 조회. */
    List<Auction> findActive();

    /** Description 4단계 : 경매 id 단건 조회. */
    Auction findById(String auctionId);
}
