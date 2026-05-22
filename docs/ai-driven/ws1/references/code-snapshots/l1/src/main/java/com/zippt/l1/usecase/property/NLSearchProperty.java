package com.zippt.l1.usecase.property;

/**
 * [L1] Concrete UC : 자연어 매물 검색 (NL Search Property).
 * <p>
 * project_proposal.md 3.1 : "역세권 학원가 근처 10억대 매물" 과 같은 자연어 입력을
 * LLM 이 분석하여 검색 조건으로 자동 변환.
 * Actors : Buyer
 * Extends : SearchProperty (Base)
 */
public class NLSearchProperty {
    private SearchProperty baseUseCase;   // <<extend>> Base
    public void execute() { /* TODO: LLM 통합 로직은 Diagram 에 없음 */ }
}
