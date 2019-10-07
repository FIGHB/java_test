package com.cskaoyan.mall.service.wechat;


import com.cskaoyan.mall.bean.*;
import com.cskaoyan.mall.bean.wechat.UserCouponBean;
import com.cskaoyan.mall.mapper.CategoryMapper;
import com.cskaoyan.mall.mapper.GoodsMapper;
import com.cskaoyan.mall.mapper.KeywordMapper;
import com.cskaoyan.mall.mapper.wechat.LRWXMallMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author 李锐
 */
@Service
public class LRWXMallServiceImpl implements LRWXMallService {

    @Autowired
    LRWXMallMapper lrwxMallMapper;


    @Autowired
    CategoryMapper categoryMapper;

    @Autowired
    KeywordMapper keywordMapper;

    @Autowired
    GoodsMapper goodsMapper;

    @Override
    public Map queryCatalogIndex() {
        Map data = new HashMap();
        data.put("categoryList", lrwxMallMapper.queryCategoryList());
        Category category = lrwxMallMapper.queryFirstLevel1Category();
        data.put("currentCategory", category);
        data.put("currentSubCategory", lrwxMallMapper.queryCurrentSubCategory(category.getId()));
        return data;
    }

    @Override
    public Integer queryGoodsCount() {
        return lrwxMallMapper.queryGoodsCount();
    }

    @Override
    public Map querySearchIndex(String username) {
        HashMap<Object, Object> data = new HashMap<>();
        data.put("defaultKeyword", lrwxMallMapper.queryDefaultKeyword());
        if(username == null || "".equals(username)) {
            data.put("historyKeywordList", new ArrayList<>());
        } else {
            int userId = lrwxMallMapper.selectUserIdByUserName(username);
            data.put("historyKeywordList", lrwxMallMapper.queryHistoryKeywordList(userId));
        }
        data.put("hotKeywordList", lrwxMallMapper.queryHotKeywordList());
        return data;
    }

    @Override
    public List<String> querySearchHelper(String keyword) {
        String keyword2 = "%" + keyword + "%";
        return lrwxMallMapper.querySearchHelper(keyword2);
    }

    @Override
    public boolean deleteSearchHistory(String username) {
        int userId = lrwxMallMapper.queryUserIdByUserName(username);
        int flag = lrwxMallMapper.deleteSearchHistory(userId);
        return flag > 0;
    }

    /**
     * 查询当前登录用户的各订单状态的总数
     * @param username 当前登录用户的用户名
     * @return
     */
    @Override
    public Map queryUserIndex(String username) {
        Map<Object, Object> map = new HashMap<>();
        int userId = lrwxMallMapper.queryUserIdByUserName(username);
        //查询订单状态是已收货的id
        List<Order> orders = lrwxMallMapper.queryOrdersByUserAndStatus(userId, 402);
        int uncomment = 0;
        for (Order order : orders) {
            int commentStatus = lrwxMallMapper.queryCommentStatusByOrderId(order.getId());
            if(commentStatus == 0) {
                uncomment++;
            }
        }
        map.put("uncomment", uncomment);
        map.put("unpaid", lrwxMallMapper.queryOrdersByUserAndStatus(userId, 101).size());
        map.put("unrecv", lrwxMallMapper.queryOrdersByUserAndStatus(userId, 301).size());
        map.put("unship", lrwxMallMapper.queryOrdersByUserAndStatus(userId, 201).size());
        HashMap<Object, Object> order = new HashMap<>();
        order.put("order", map);
        return order;
    }

    @Override
    public Map queryMyCouponList(String username, int status, int page, int size) {
        //获取用户id
        int userId = lrwxMallMapper.queryUserIdByUserName(username);
        //开启分页
        PageHelper.startPage(page, size);
        List<UserCouponBean> userCouponlists = lrwxMallMapper.queryMyCouponListByStatus(userId, status);
        //获取总数
        PageInfo<UserCouponBean> pageInfo = new PageInfo<>(userCouponlists);
        HashMap<Object, Object> map = new HashMap<>();
        map.put("data", userCouponlists);
        map.put("count", pageInfo.getTotal());
        return map;
    }

    @Override
    public String receiveCoupon(String username, Integer couponId) {
        int userId = getUserIdByUsername(username);
        //查询当前用户是否拥有该优惠券
        int count = lrwxMallMapper.queryCouponehaded(couponId, userId);
        if(count > 0) {
            return "优惠券已经领取过了";
        }
        //查询优惠券是否被逻辑删除，如果删除的表明库存为0
        int count1 = lrwxMallMapper.queryCoupondeleted(couponId);
        if(count1 == 0) {
            return "优惠券已领完";
        }

        Coupon coupon = lrwxMallMapper.queryCouponById(couponId);
        Date date = new Date();
        //为当前用户添加优惠券
        int update = lrwxMallMapper.addCouponForUser(
                userId, couponId, coupon.getStartTime(),coupon.getEndTime(),date);
        if(update < 1) {
            return "系统错误，添加失败";
        } else {
            int total = coupon.getTotal();
            //修改优惠券的库存
            //如果只有1张了 则 逻辑删除
            if(total == 1) {
                lrwxMallMapper.deleteCouponById(couponId);
            } else {
                --total;
                lrwxMallMapper.updateCouponTotal(couponId, total);
            }
            //返回null表示添加成功
            return null;
        }
    }

    @Override
    public Map queryCouponList(int page, int size) {
        PageHelper.startPage(page, size);
        List<Coupon> coupons = lrwxMallMapper.queryAllCouponList();
        PageInfo<Coupon> couponPageInfo = new PageInfo<>(coupons);
        HashMap<String, Object> map = new HashMap<>();
        map.put("count", couponPageInfo.getTotal());
        map.put("data", coupons);
        return map;
    }


    @Override
    public Integer getGoodsCount(String username) {
        if(username == null) return 0;
        int userId = getUserIdByUsername(username);
        return lrwxMallMapper.querGoodsCount(userId);
    }

    @Override
    public String fastAddCart(Cart cart, String username) {
        if(username == null) return "请登录";
        int userId = getUserIdByUsername(username);
        Goods goods = goodsMapper.selectByPrimaryKey(cart.getGoodsId());
        GoodsProduct goodsProduct = lrwxMallMapper.queryGoodsProductById(cart.getGoodsId());
        Date date = new Date();
        cart.setUserId(userId);
        cart.setGoodsSn(goods.getGoodsSn());
        cart.setGoodsName(goods.getName());
        cart.setPrice(goodsProduct.getPrice());
        cart.setSpecifications(goodsProduct.getSpecifications());
        cart.setPicUrl(goodsProduct.getUrl());
        cart.setAddTime(date);
        cart.setUpdateTime(date);
        int update = lrwxMallMapper.insertCart(cart);
        return null;
    }

    @Override
    public int queryCartId(int userId) {
        return lrwxMallMapper.queryCartId(userId);
    }

    @Override
    public int getUserIdByUsername(String username) {
        return lrwxMallMapper.queryUserIdByUserName(username);
    }

    @Override
    public Map checkoutCart(int userId, Integer cartId, Integer addressId, Integer couponId, Integer grouponRulesId) {
        HashMap<Object, Object> map = new HashMap<>();
        List<Object> array = new ArrayList<>();
        if(cartId != 0) {
            Cart cart = lrwxMallMapper.queryCartById(cartId);
            GoodsProduct goodsProduct = lrwxMallMapper.queryGoodsProductById(Integer.parseInt(cart.getGoodsSn()));
            map.put("actualPrice", goodsProduct.getPrice());
            map.put("orderTotalPrice", goodsProduct.getPrice());
            map.put("couponPrice", goodsProduct.getPrice());
            array.add(cart);
        } else {
            array.addAll(lrwxMallMapper.queryCartByUserId(userId));
            map.put("actualPrice", 0);
            map.put("orderTotalPrice", 0);
            map.put("couponPrice", 0);
        }
        map.put("grouponPrice", 0);
        map.put("grouponRulesId", 0);
        //如果 addressId为 0 则查询默认地址，反之根据id 查询地址
        if(addressId == 0) {
            map.put("checkedAddress", lrwxMallMapper.queryDefaultAddressByUserId(userId));
        } else {
            map.put("checkedAddress", lrwxMallMapper.queryAddressByAddressId(addressId));
        }
        map.put("availableCouponLength", 0);
        map.put("couponId", couponId);
        map.put("freightPrice", 0);

        map.put("checkedGoodsList", array);

        map.put("goodsTotalPrice", 286);
        map.put("addressId", addressId);
        return map;
    }
}
