package cafe.service;

import cafe.dao.CafeDAO;

/**
 * 회원 관련 비즈니스 로직을 담당합니다.
 * - 회원 조회 / 등록
 * - 현재 세션의 회원 상태 관리 (전화번호, 스탬프)
 */
public class MemberService {
    private final CafeDAO dao;

    private String currentPhone = null;
    private int currentStamp   = 0;

    public MemberService(CafeDAO dao) {
        this.dao = dao;
    }

    /**
     * 전화번호로 회원을 조회합니다.
     * @return 스탬프 수(회원), -1(미등록)
     */
    public int checkMember(String phone) {
        return dao.checkMemberStamp(phone);
    }

    /**
     * 신규 회원을 등록하고 세션에 설정합니다.
     * @return 등록 성공 여부
     */
    public boolean registerMember(String phone, String name) {
        boolean success = dao.registerMember(phone, name);
        if (success) setCurrentMember(phone, 0);
        return success;
    }

    /** 세션에 현재 회원 정보를 저장합니다. */
    public void setCurrentMember(String phone, int stamp) {
        this.currentPhone = phone;
        this.currentStamp = stamp;
    }

    /** 세션의 회원 정보를 초기화합니다. */
    public void clearCurrentMember() {
        this.currentPhone = null;
        this.currentStamp = 0;
    }

    public String getCurrentPhone() { return currentPhone; }
    public int    getCurrentStamp() { return currentStamp; }
    public boolean isLoggedIn()     { return currentPhone != null; }

    /** 스탬프 10개 이상 보유 여부 */
    public boolean canUseStampDiscount() {
        return isLoggedIn() && currentStamp >= 10;
    }
}
