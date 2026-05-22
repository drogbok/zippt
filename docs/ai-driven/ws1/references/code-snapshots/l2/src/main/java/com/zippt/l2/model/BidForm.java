package com.zippt.l2.model;

/**
 * [L2] Value Object : 입찰서 양식 (Description 6단계).
 * <p>
 * 공인중개사가 작성하는 제안 내용.
 * Alternative A3-1 : 취소 시 {@link #discard()} 로 임시 데이터 파기.
 */
public class BidForm {

    private double commissionRate;        // 제안 수수료율
    private String marketingStrategy;     // 마케팅 전략
    private int    expectedSalePeriod;    // 예상 매각 기간 (일)
    private String serviceTerms;          // 서비스 조건

    public BidForm(double commissionRate, String marketingStrategy,
                   int expectedSalePeriod, String serviceTerms) {
        this.commissionRate     = commissionRate;
        this.marketingStrategy  = marketingStrategy;
        this.expectedSalePeriod = expectedSalePeriod;
        this.serviceTerms       = serviceTerms;
    }

    /** Alternative A3-1 : 임시 데이터 파기. */
    public void discard() {
        this.commissionRate     = 0.0;
        this.marketingStrategy  = null;
        this.expectedSalePeriod = 0;
        this.serviceTerms       = null;
    }

    public double getCommissionRate()    { return commissionRate; }
    public String getMarketingStrategy() { return marketingStrategy; }
    public int    getExpectedSalePeriod(){ return expectedSalePeriod; }
    public String getServiceTerms()      { return serviceTerms; }
}
