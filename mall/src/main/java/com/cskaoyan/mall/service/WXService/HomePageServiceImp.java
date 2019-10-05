package com.cskaoyan.mall.service.WXService;
import com.cskaoyan.mall.bean.Brand;
import com.cskaoyan.mall.bean.Category;
import com.cskaoyan.mall.bean.wxfbean.*;
import com.cskaoyan.mall.mapper.WXMapper.HomePageMapper;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class HomePageServiceImp implements HomePageService {
    @Autowired
    HomePageMapper homePageMapper;

    @Override
    public HomePageVO queryHomePage() {
        HomePageVO homePageVO = new HomePageVO();
        homePageVO.setBanner(homePageMapper.queryAD());
        homePageVO.setBrandList(homePageMapper.queryBrandList(homePageMapper.querySystemByKeyName("cskaoyan_mall_wx_index_brand")));
        homePageVO.setChannel(homePageMapper.queryChannel());
        homePageVO.setCouponList(homePageMapper.queryCouponList());
        List<FloorGoodList> floorGoodList = homePageMapper.queryFloorGoodList(homePageMapper.querySystemByKeyName("cskaoyan_mall_wx_catlog_list"));
        for (FloorGoodList floorGood : floorGoodList) {
            int l2Id = homePageMapper.queryL2IdByPid(floorGood.getId());
            floorGood.setGoodsList(homePageMapper.queryGoodsListByL2Id(l2Id,homePageMapper.querySystemByKeyName("cskaoyan_mall_wx_catlog_goods")));
        }
        homePageVO.setFloorGoodsList(floorGoodList);
        List<GroupOn> groupOnList = homePageMapper.queryGroupOnList();
        for (GroupOn groupOn:groupOnList) {
           groupOn.setGoods(homePageMapper.queryGoodById(groupOn.getGoods_id()));
           BigDecimal groupPrice = groupOn.getGoods().getRetailPrice().subtract(groupOn.getDiscount());
           groupOn.setGroupon_price(groupPrice);
        }
        homePageVO.setGrouponList(groupOnList);
        homePageVO.setHotGoodsList(homePageMapper.queryHotGoodsList(homePageMapper.querySystemByKeyName("cskaoyan_mall_wx_index_hot")));
        homePageVO.setNewGoodsList(homePageMapper.queryNewGoodsList(homePageMapper.querySystemByKeyName("cskaoyan_mall_wx_index_new")));
        homePageVO.setTopicList(homePageMapper.queryTopicList(homePageMapper.querySystemByKeyName("cskaoyan_mall_wx_index_topic")));
        return homePageVO;
    }

    @Override
    public CurrentCategoryBean queryCurrentCategory(int id) {
        CurrentCategoryBean currentCategoryBean = new CurrentCategoryBean();
        Category parentCategory = homePageMapper.queryCategoryById(id);
        currentCategoryBean.setCurrentCategory(parentCategory);
        currentCategoryBean.setCurrentSubCategory(homePageMapper.queryCategoryByPid(parentCategory.getId()));
        return currentCategoryBean;
    }

    @Override
    public BrandListBean queryBrandList(int page, int size) {
        BrandListBean brandListBean = new BrandListBean();
        PageHelper.startPage(page,size);
        List<Brand> brandList = homePageMapper.queryBrands();
        int total = homePageMapper.queryTotolOfBrand();
        brandListBean.setBrandList(brandList);
        brandListBean.setTotalPages((int) Math.ceil(total / size));
        return brandListBean;
    }

    @Override
    public BrandDetailBean queryBrandDetail(int id) {
        BrandDetailBean brandDetailBean = new BrandDetailBean();
        brandDetailBean.setBrand(homePageMapper.queryBrandById(id));
        return brandDetailBean;
    }
}
