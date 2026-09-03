package com.example.maimaibackend.service;

import com.example.maimaibackend.mapper.HomeMapper;
import com.example.maimaibackend.vo.home.HomeRecommendItemVO;
import com.example.maimaibackend.vo.home.HomePageVO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class HomeService {
    private final HomeMapper homeMapper;

    public HomeService(HomeMapper homeMapper) {
        this.homeMapper = homeMapper;
    }

    public HomePageVO getHomeIndex(String cityName, Integer limit, Integer offset) {
        int realLimit = limit == null || limit <= 0 ? 20 : Math.min(limit, 50);
        int realOffset = offset == null || offset < 0 ? 0 : offset;
        List<HomeRecommendItemVO> fetched = homeMapper.selectHomeRecommendList(realLimit + 1, realOffset);
        boolean hasMore = fetched.size() > realLimit;
        List<HomeRecommendItemVO> recommends = hasMore
                ? new ArrayList<>(fetched.subList(0, realLimit))
                : fetched;

        HomePageVO vo = new HomePageVO();
        vo.setBanners(realOffset == 0 ? homeMapper.selectActiveBanners() : Collections.emptyList());
        vo.setCategories(realOffset == 0 ? homeMapper.selectHomeCategories() : Collections.emptyList());
        vo.setRecommends(recommends);
        vo.setHasMore(hasMore);
        vo.setLimit(realLimit);
        vo.setOffset(realOffset);
        return vo;
    }
}
