package com.cskaoyan.mall.controller.wechat;

import com.cskaoyan.mall.bean.Order;
import com.cskaoyan.mall.bean.OrderGoods;
import com.cskaoyan.mall.mapper.wechat.OrderDeleteMapper;
import com.cskaoyan.mall.service.wechat.OrderDeleteService;
import com.cskaoyan.mall.vo.BaseRespVo;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author 赵亚云
 */
@RestController
public class OrderDeleteController {
    @Autowired
    OrderDeleteService orderDeleteService;
    @Autowired
    OrderDeleteMapper orderDeleteMapper;
    @RequestMapping("wx/order/delete")
    public BaseRespVo orderDelete(@RequestBody OrderGoods orderGoods){
        if(orderDeleteService.orderDelete(orderGoods.getOrderId())){
            return BaseRespVo.ok(null);
        }
        return BaseRespVo.fail(700,"删除订单失败");
    }

}