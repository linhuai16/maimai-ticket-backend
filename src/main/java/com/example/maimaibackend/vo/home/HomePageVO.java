package com.example.maimaibackend.vo.home;

import java.util.List;

public class HomePageVO {

    private List<BannerItemVO> banners;
    private List<CategoryItemVO> categories;
    private List<HomeRecommendItemVO> recommends;
    private boolean hasMore;
    private int limit;
    private int offset;

    public List<BannerItemVO> getBanners() {
        return banners;
    }

    public void setBanners(List<BannerItemVO> banners) {
        this.banners = banners;
    }

    public List<CategoryItemVO> getCategories() {
        return categories;
    }

    public void setCategories(List<CategoryItemVO> categories) {
        this.categories = categories;
    }

    public List<HomeRecommendItemVO> getRecommends() {
        return recommends;
    }

    public void setRecommends(List<HomeRecommendItemVO> recommends) {
        this.recommends = recommends;
    }

    public boolean getHasMore() {
        return hasMore;
    }

    public void setHasMore(boolean hasMore) {
        this.hasMore = hasMore;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }
}
