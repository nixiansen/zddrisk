package com.zdd.risk.service;

import com.alibaba.fastjson.JSONObject;
import com.zdd.risk.api.JingdongController;
import com.zdd.risk.bean.Accredit;
import com.zdd.risk.bean.ApproveResult;
import com.zdd.risk.dao.JingDongDao;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Created by ZWYnqj on 2018/11/21.
 */

@Service
public class JingDongService {

    @Autowired
    private JingDongDao jingdongdao;
    private static final Logger log = LoggerFactory.getLogger(JingDongService.class);

    public JSONObject selectAccreditTaskId(String uid, String productId) {

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("getDeviceInfo","1722B-000O8460453-52468711");
        //查询用户的授权信息
        List<Accredit> list = jingdongdao.selectTaskids(uid);
        log.info("获取用户的授权信息集合参数 list= " + JSONObject.toJSONString(list));
        System.out.println(productId);
        System.out.println(list.size());
        if (list.size() == 0) {
            jsonObject.put("getTaobaoInfo", "");
            jsonObject.put("getTaobaoReport", "");

        } else {
            if (productId.equals("1")) {
                for (int i=0;i<list.size();i++) {
                    Accredit accredit=(Accredit)list.get(i);
                  System.out.println(accredit.getType());
                    if (accredit.getType().equals("1")) {
                        jsonObject.put("getEducationInfo", accredit.getTaskId());
                    } else if (accredit.getType().equals("2")) {
                        jsonObject.put("getTaobaoInfo", accredit.getTaskId());
                        jsonObject.put("getTaobaoReport", accredit.getTaskId());
                    }
                }
            } if (productId.equals("2")) {
                for (int i=0;i<list.size();i++) {
                    Accredit accredit=(Accredit)list.get(i);
                    System.out.println(accredit.getType());
                    if (accredit.getType().equals("3")) {
                        jsonObject.put("getCarrierInfo", accredit.getTaskId());
                        jsonObject.put("getCarrierReport", accredit.getTaskId());
                    } else if (accredit.getType().equals("2")) {
                        jsonObject.put("getTaobaoInfo", accredit.getTaskId());
                        jsonObject.put("getTaobaoReport", accredit.getTaskId());
                    }
                }
            }

        }
        log.info("返回用户的授权信息json数据" + jsonObject.toString());
        return jsonObject;
    }


    public List<ApproveResult> selectByExample(String uid) {

        JSONObject jsonObject = new JSONObject();
        //回调业务部门的接口传入参数信息
        List<ApproveResult> list = jingdongdao.selectByExample(uid);
//        System.out.println(list.size());
        return list;
    }




}
