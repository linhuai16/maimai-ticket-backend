package com.example.maimaibackend.controller.admin;

import com.example.maimaibackend.common.Result;
import com.example.maimaibackend.dto.admin.AdminSaveVenueRequest;
import com.example.maimaibackend.service.admin.AdminVenueService;
import com.example.maimaibackend.vo.admin.AdminOperateResponse;
import com.example.maimaibackend.vo.admin.AdminVenueListPageVO;
import com.example.maimaibackend.vo.admin.AdminVenueOptionListVO;
import com.example.maimaibackend.vo.admin.AdminVenueVO;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/venues")
public class AdminVenueController {

    private final AdminVenueService adminVenueService;

    public AdminVenueController(AdminVenueService adminVenueService) {
        this.adminVenueService = adminVenueService;
    }

    @GetMapping
    public Result<AdminVenueListPageVO> getVenueList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String cityName,
            @RequestParam(required = false) Integer pageNo,
            @RequestParam(required = false) Integer pageSize
    ) {
        return Result.success(adminVenueService.getVenueList(
                keyword, cityName, pageNo, pageSize
        ));
    }

    @GetMapping("/options")
    public Result<AdminVenueOptionListVO> getVenueOptions(
            @RequestParam(required = false) String cityName,
            @RequestParam(required = false) String stationName,
            @RequestParam(required = false) Integer limit
    ) {
        return Result.success(adminVenueService.getVenueOptions(
                cityName, stationName, limit
        ));
    }

    @GetMapping("/{venueId}")
    public Result<AdminVenueVO> getVenueDetail(@PathVariable Long venueId) {
        return Result.success(adminVenueService.getVenueDetail(venueId));
    }

    @PostMapping
    public Result<AdminVenueVO> createVenue(@RequestBody AdminSaveVenueRequest request) {
        return Result.success(adminVenueService.createVenue(request));
    }

    @PutMapping("/{venueId}")
    public Result<AdminVenueVO> updateVenue(
            @PathVariable Long venueId,
            @RequestBody AdminSaveVenueRequest request
    ) {
        return Result.success(adminVenueService.updateVenue(venueId, request));
    }

    @DeleteMapping("/{venueId}")
    public Result<AdminOperateResponse> deleteVenue(@PathVariable Long venueId) {
        return Result.success(adminVenueService.deleteVenue(venueId));
    }
}
