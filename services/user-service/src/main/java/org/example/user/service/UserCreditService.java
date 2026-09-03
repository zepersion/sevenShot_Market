package org.example.user.service;


import org.example.common.vo.PageVO;
import org.example.user.entity.UserCredit;

public interface UserCreditService {
    PageVO<UserCredit> getCreditRecords(Integer page, Integer size, Long id);

    void updateCredit(Long id, Integer score);
}
