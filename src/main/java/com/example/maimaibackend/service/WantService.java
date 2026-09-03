package com.example.maimaibackend.service;

import com.example.maimaibackend.common.BusinessException;
import com.example.maimaibackend.mapper.WantMapper;
import com.example.maimaibackend.util.ValidateUtil;
import com.example.maimaibackend.vo.want.WantActionResponse;
import com.example.maimaibackend.vo.want.WantListPageVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WantService {

    private final WantMapper wantMapper;
    private final UserService userService;

    public WantService(WantMapper wantMapper, UserService userService) {
        this.wantMapper = wantMapper;
        this.userService = userService;
    }

    public WantListPageVO getWantList(Long userId) {
        userService.requireUser(userId);
        WantListPageVO vo = new WantListPageVO();
        vo.setWants(wantMapper.selectWantList(userId));
        return vo;
    }

    @Transactional(rollbackFor = Exception.class)
    public WantActionResponse addWant(Long userId, Long projectId) {
        userService.requireUser(userId);
        ValidateUtil.requirePositiveId(projectId, "演出项目ID");
        if (wantMapper.countProjectCanShow(projectId) == 0) {
            throw new BusinessException("演出项目不存在或不可展示");
        }
        int rows = wantMapper.insertWantIfAbsent(userId, projectId);
        if (rows > 0) {
            wantMapper.increaseWantCount(projectId);
        }
        return new WantActionResponse(true, true);
    }

    @Transactional(rollbackFor = Exception.class)
    public WantActionResponse cancelWant(Long userId, Long projectId) {
        userService.requireUser(userId);
        ValidateUtil.requirePositiveId(projectId, "演出项目ID");
        int rows = wantMapper.deleteWant(userId, projectId);
        if (rows > 0) {
            wantMapper.decreaseWantCount(projectId);
        }
        return new WantActionResponse(rows > 0, false);
    }
}
