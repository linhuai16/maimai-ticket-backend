package com.example.maimaibackend.service;

import com.example.maimaibackend.common.BusinessException;
import com.example.maimaibackend.dto.audience.AudienceSaveDTO;
import com.example.maimaibackend.dto.audience.CreateAudienceRequest;
import com.example.maimaibackend.dto.audience.UpdateAudienceRequest;
import com.example.maimaibackend.mapper.AudienceMapper;
import com.example.maimaibackend.util.HashUtil;
import com.example.maimaibackend.util.MaskUtil;
import com.example.maimaibackend.util.ValidateUtil;
import com.example.maimaibackend.vo.audience.AudienceDetailVO;
import com.example.maimaibackend.vo.audience.AudienceItemVO;
import com.example.maimaibackend.vo.audience.AudienceListPageVO;
import com.example.maimaibackend.vo.audience.AudienceOperateResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class AudienceService {

    private final AudienceMapper audienceMapper;
    private final UserService userService;

    public AudienceService(AudienceMapper audienceMapper, UserService userService) {
        this.audienceMapper = audienceMapper;
        this.userService = userService;
    }

    public AudienceListPageVO getAudienceList(Long userId) {
        userService.requireUser(userId);
        List<AudienceDetailVO> records = audienceMapper.selectAudienceList(userId);
        List<AudienceItemVO> items = new ArrayList<>();
        for (AudienceDetailVO record : records) {
            AudienceItemVO item = new AudienceItemVO();
            item.setAudienceId(record.getAudienceId());
            item.setRealName(record.getRealName());
            item.setCertificateType(record.getCertificateType());
            item.setMaskedCertificateNo(MaskUtil.maskCertificateNo(record.getCertificateNo()));
            item.setMaskedPhone(MaskUtil.maskPhone(record.getPhone()));
            item.setIsDefault(record.getIsDefault());
            items.add(item);
        }
        AudienceListPageVO vo = new AudienceListPageVO();
        vo.setAudiences(items);
        return vo;
    }

    public AudienceDetailVO getAudienceDetail(Long userId, Long audienceId) {
        userService.requireUser(userId);
        ValidateUtil.requirePositiveId(audienceId, "观演人ID");
        AudienceDetailVO detail = audienceMapper.selectAudienceDetail(userId, audienceId);
        if (detail == null) {
            throw new BusinessException("观演人不存在或不属于当前用户");
        }
        return detail;
    }

    @Transactional(rollbackFor = Exception.class)
    public AudienceDetailVO createAudience(Long userId, CreateAudienceRequest request) {
        userService.requireUser(userId);
        if (request == null) {
            throw new BusinessException("请求参数不能为空");
        }
        AudienceSaveDTO save = buildSaveDTO(userId, null, request.getRealName(), request.getCertificateType(),
                request.getCertificateNo(), request.getPhone(), request.getIsDefault());

        if (audienceMapper.countDuplicateCertificate(userId, save.getCertificateNoHash()) > 0) {
            throw new BusinessException("当前用户下已存在相同证件号的观演人");
        }

        int existingCount = audienceMapper.countByUserId(userId);
        boolean shouldDefault = existingCount == 0 || Boolean.TRUE.equals(save.getIsDefault());
        save.setIsDefault(shouldDefault);
        if (shouldDefault) {
            audienceMapper.clearDefault(userId);
        }

        audienceMapper.insertAudience(save);
        return getAudienceDetail(userId, save.getAudienceId());
    }

    @Transactional(rollbackFor = Exception.class)
    public AudienceDetailVO updateAudience(Long userId, Long audienceId, UpdateAudienceRequest request) {
        userService.requireUser(userId);
        ValidateUtil.requirePositiveId(audienceId, "观演人ID");
        if (request == null) {
            throw new BusinessException("请求参数不能为空");
        }
        AudienceDetailVO old = getAudienceDetail(userId, audienceId);
        AudienceSaveDTO save = buildSaveDTO(userId, audienceId, request.getRealName(), request.getCertificateType(),
                request.getCertificateNo(), request.getPhone(), request.getIsDefault());

        if (audienceMapper.countDuplicateCertificateExcludeSelf(userId, audienceId, save.getCertificateNoHash()) > 0) {
            throw new BusinessException("当前用户下已存在其他相同证件号的观演人");
        }

        if (Boolean.TRUE.equals(save.getIsDefault())) {
            audienceMapper.clearDefault(userId);
        }
        audienceMapper.updateAudience(save);

        if (Boolean.TRUE.equals(old.getIsDefault()) && !Boolean.TRUE.equals(save.getIsDefault())) {
            ensureOneDefault(userId);
        }
        return getAudienceDetail(userId, audienceId);
    }

    @Transactional(rollbackFor = Exception.class)
    public AudienceOperateResponse deleteAudience(Long userId, Long audienceId) {
        userService.requireUser(userId);
        AudienceDetailVO old = getAudienceDetail(userId, audienceId);
        int rows = audienceMapper.deleteAudience(userId, audienceId);
        if (rows > 0 && Boolean.TRUE.equals(old.getIsDefault())) {
            ensureOneDefault(userId);
        }
        return new AudienceOperateResponse(rows > 0);
    }

    @Transactional(rollbackFor = Exception.class)
    public AudienceOperateResponse setDefaultAudience(Long userId, Long audienceId) {
        userService.requireUser(userId);
        getAudienceDetail(userId, audienceId);
        audienceMapper.clearDefault(userId);
        audienceMapper.setDefault(userId, audienceId);
        return new AudienceOperateResponse(true);
    }

    private AudienceSaveDTO buildSaveDTO(Long userId, Long audienceId, String realName, String certificateType,
                                         String certificateNo, String phone, Boolean isDefault) {
        AudienceSaveDTO save = new AudienceSaveDTO();
        save.setUserId(userId);
        save.setAudienceId(audienceId);
        save.setRealName(ValidateUtil.requireText(realName, "观演人姓名", 30));
        save.setCertificateType(ValidateUtil.requireText(certificateType, "证件类型", 32));
        save.setCertificateNo(ValidateUtil.requireText(certificateNo, "证件号", 255));
        save.setCertificateNoHash(HashUtil.sha256(save.getCertificateNo()));
        save.setPhone(ValidateUtil.requirePhone(phone));
        save.setIsDefault(Boolean.TRUE.equals(isDefault));
        return save;
    }

    private void ensureOneDefault(Long userId) {
        if (audienceMapper.countByUserId(userId) > 0 && audienceMapper.countDefaultByUserId(userId) == 0) {
            Long firstId = audienceMapper.selectFirstAudienceId(userId);
            if (firstId != null) {
                audienceMapper.setDefault(userId, firstId);
            }
        }
    }
}
