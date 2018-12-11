package com.zdd.risk.service;

import com.alibaba.fastjson.JSONObject;
import com.sun.org.apache.xpath.internal.operations.Equals;
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

    //获取用户授权信息taskID
    public JSONObject selectAccreditTaskId(String uid, String productId) {

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("getDeviceInfo", "1722B-000O8460453-52468711");
        //查询用户的授权信息
        List<Accredit> list = jingdongdao.selectTaskids(uid);
        log.info("获取用户的授权信息集合参数 list= " + JSONObject.toJSONString(list));
//        System.out.println(productId);
//        System.out.println(list.size());
        if (list.size() == 0) {
            jsonObject.put("getTaobaoInfo", "");
            jsonObject.put("getTaobaoReport", "");
            jsonObject.put("getCarrierInfo", "");
        } else {
            if (productId.equals("1")) {
                for (int i = 0; i < list.size(); i++) {
                    Accredit accredit = (Accredit) list.get(i);
//                    System.out.println(accredit.getType());
                    if (accredit.getType().equals("1")) {
                        jsonObject.put("getEducationInfo", accredit.getTaskId());

                    } else if (accredit.getType().equals("2")) {
                        jsonObject.put("getTaobaoInfo", accredit.getTaskId());
                        jsonObject.put("getTaobaoReport", accredit.getTaskId());
                    }
                }
            }
            if (productId.equals("2")) {
                for (int i = 0; i < list.size(); i++) {
                    Accredit accredit = (Accredit) list.get(i);
//                    System.out.println(accredit.getType());
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

    //获取风控返回结果
    public String selectByExample1(String uid) {
        JSONObject jsonObject = new JSONObject();
        //回调业务部门的接口传入参数信息
        List<ApproveResult> list = jingdongdao.selectByExample(uid);
        if (list.size() == 0) {
            jsonObject.put("userId", uid);
            jsonObject.put("approveResult", "");
            jsonObject.put("approveCredit", "");
            jsonObject.put("listresult", "查询数据为空！");
        } else {
            ApproveResult approveResult = (ApproveResult) list.get(0);
            jsonObject.put("userId", uid);
            if (approveResult.getApproveResult().equals("reject")) {
                jsonObject.put("approveResult", "E");
            } else if (approveResult.getApproveResult().equals("pass")) {
                jsonObject.put("approveResult", "A");
            } else if (approveResult.getApproveResult().equals("postscreen")) {
                jsonObject.put("approveResult", "D");
            } else {
                jsonObject.put("approveResult", approveResult.getApproveResult());
            }
            jsonObject.put("approveCredit", approveResult.getApproveCredit());
        }
        return jsonObject.toString();
    }


    public JSONObject selectBizNo(String uid) {

        JSONObject jsonObject = new JSONObject();
        //回调业务部门的接口传入参数信息
        List<ApproveResult> list = jingdongdao.selectBizNo(uid);
        if (list.size() == 0) {
            jsonObject.put("bizNo", "");
        } else {
            for (int i = 0; i < list.size(); i++) {
                ApproveResult accredit = (ApproveResult) list.get(i);
                jsonObject.put("bizNo", accredit.getBizNo());
            }
        }
//        System.out.println(list.size());
        return jsonObject;


    }


}
