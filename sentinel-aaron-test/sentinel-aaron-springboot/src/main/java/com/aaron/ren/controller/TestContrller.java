package com.aaron.ren.controller;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.context.ContextUtil;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestContrller {


    @GetMapping("/echo")
    public String echo() {
        getUserInfo("application",1L);
        return "ok";
    }



    public static void getUserInfo(String application, Long accountId) {
        ContextUtil.enter("user-center", application);
        Entry entry = null;
        try {
            entry = SphU.entry("getUserInfo", EntryType.IN);
            OrderInfoDto orderInfoDto = getOrderInfo(accountId);
            return;

        } catch (BlockException e) {
            e.printStackTrace();
        } finally {
            if (entry != null) {
                entry.exit();
            }
        }

    }

    private static OrderInfoDto getOrderInfo(Long accountId) {
        Entry entry = null;
        try {
            entry = SphU.entry("getOrderInfo");
            return new OrderInfoDto();
            //查询订单信息
        } catch (BlockException e) {
            e.printStackTrace();
            return null;
        } finally {
            if (entry != null) {
                entry.exit();
            }
        }

    }

    static  class OrderInfoDto {
        private String order;
    }
}
