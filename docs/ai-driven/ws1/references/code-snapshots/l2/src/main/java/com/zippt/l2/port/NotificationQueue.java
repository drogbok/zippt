package com.zippt.l2.port;

import com.zippt.l2.model.Bid;
import com.zippt.l2.model.User;

/**
 * [L2] Port : 알림 큐.
 * <p>
 * Description 12단계 : 매도자의 알림 큐에 신규 입찰 알림을 등재.
 */
public interface NotificationQueue {
    void enqueue(User seller, Bid bid);
}
