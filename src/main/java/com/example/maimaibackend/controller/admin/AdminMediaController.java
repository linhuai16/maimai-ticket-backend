package com.example.maimaibackend.controller.admin;

import com.example.maimaibackend.common.Result;
import com.example.maimaibackend.service.admin.AdminMediaService;
import com.example.maimaibackend.vo.admin.AdminMediaItemVO;
import com.example.maimaibackend.vo.admin.AdminMediaPageVO;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/admin/media")
public class AdminMediaController {

    private final AdminMediaService adminMediaService;

    public AdminMediaController(AdminMediaService adminMediaService) {
        this.adminMediaService = adminMediaService;
    }

    @GetMapping
    public Result<AdminMediaPageVO> getMediaList(
            @RequestParam String businessType,
            @RequestParam(required = false) String mediaType,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer pageNo,
            @RequestParam(required = false) Integer pageSize
    ) {
        return Result.success(adminMediaService.getMediaList(
                businessType, mediaType, keyword, pageNo, pageSize
        ));
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<AdminMediaItemVO> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam String businessType
    ) {
        return Result.success(adminMediaService.upload(file, businessType));
    }
}
