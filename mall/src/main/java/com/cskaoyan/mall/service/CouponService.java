package com.cskaoyan.mall.service;

import com.cskaoyan.mall.bean.Coupon;
import com.cskaoyan.mall.bean.CouponUser;
import com.cskaoyan.mall.vo.BaseRespVo;

public interface CouponService {
    BaseRespVo queryAllCoupon(int start, int limit);

    BaseRespVo queryCouponByCondition(int page, int limit, Coupon coupon);

    BaseRespVo delete(Coupon coupon);

    BaseRespVo read(int id);

    BaseRespVo queryCouponUser(int page, int limit,int couponId);
    BaseRespVo queryCouponUserByCondition(int page, int limit,CouponUser couponUser);

    BaseRespVo insert(Coupon coupon);
}