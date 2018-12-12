package com.zdd.risk.api;

import com.alibaba.fastjson.JSONObject;
import com.zdd.risk.bean.*;
import com.zdd.risk.dao.*;
import com.zdd.risk.service.JingDongService;
import com.zdd.risk.utils.*;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang.StringUtils;
import org.apache.http.message.BasicHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JingDong Service
 *
 * @author 租无忧科技有限公司
 * @date 2018-11-01.
 */
@RestController
@RequestMapping("/risk/moxie")
@PropertySource(value = "classpath:sanfang/moxie.properties", ignoreResourceNotFound = true)
public class MoXieController {


    @Value("${apiKen}")
    String apiKen;
    @Value("${token}")
    String token;
    @Value("${Secret}")
    String Secret;

    @Autowired
    private ICertificationUserInfoDAO certificationUserInfoDAO;
    @Autowired
    private ICertificationDAO certificationDAO;

    HttpUtils httpUtil = new HttpUtils();
    Tools tools = new Tools();
    private static final Logger log = LoggerFactory.getLogger(MoXieController.class);



    @ApiOperation("1.1查询淘宝信息V6接口")
    @RequestMapping(value = "/getTaobaoInfo")
    public void getTaobaoInfo(@RequestBody String param) {
        log.info("获取查询淘宝信息V的6接口入参 param= " + param);
        JSONObject params = JSONObject.parseObject(param);
        BasicHeader[] headers = new BasicHeader[]{
                new BasicHeader("content-type", "application/json;charset=UTF-8"),
                new BasicHeader("Authorization", "token " + token)};
        String result = "";
        CertificationUserInfoExample example = new CertificationUserInfoExample();
        CertificationUserInfoExample.Criteria criteria = example.createCriteria();
        criteria.andUserIdEqualTo(params.getString("userId"));
//        criteria.andUserIdEqualTo("1");
        List<CertificationUserInfo> certificationuserinfolist = certificationUserInfoDAO.selectByExample(example);
        CertificationUserInfo cuil = (CertificationUserInfo) certificationuserinfolist.get(0);
        String idCard = cuil.getIdCard();
        String mobile = cuil.getMobile();
        try {
            //taobao原始数据
            result = httpUtil.sendGet(
                    "https://api.51datakey.com/gateway/taobao/v6/data/" + params.getString("taskId"),
                    headers);
        } catch (Exception e) {
            e.printStackTrace();
        }
        log.info("调用查询淘宝信息V6接口返回信息 result= " + result);
        params.put("idCard", idCard);
        params.put("mobile", mobile);
        params.put("certificationItem", params.getString("taskId"));
        params.put("certificationResult", result);
        params.put("certificationLimit", tools.getTime());
        params.put("creatTime", new Date());
        params.put("certificationType", 6);
        params.put("flag",0);
        Certification record = params.toJavaObject(Certification.class);
        int a = certificationDAO.insert(record);
        if (a != 1) {
            log.info("插入淘宝信息失败");
        }
    }



    @ApiOperation("1.2查询淘宝报告信息V4接口")
    @RequestMapping(value = "/getTaobaoReport")
    public void getTaobaoReport(@RequestBody String param) {
        log.info("获取1.2查询淘宝报告信息V4接口入参 param= " + param);
        JSONObject params = JSONObject.parseObject(param);
        BasicHeader[] headers = new BasicHeader[]{
                new BasicHeader("content-type", "application/json;charset=UTF-8"),
                new BasicHeader("Authorization", "token " + token)};
        String result = "";
        CertificationUserInfoExample example = new CertificationUserInfoExample();
        CertificationUserInfoExample.Criteria criteria = example.createCriteria();
        criteria.andUserIdEqualTo(params.getString("userId"));
//        criteria.andUserIdEqualTo("1");
        List<CertificationUserInfo> certificationuserinfolist = certificationUserInfoDAO.selectByExample(example);
        CertificationUserInfo cuil = (CertificationUserInfo) certificationuserinfolist.get(0);
        String idCard = cuil.getIdCard();
        String mobile = cuil.getMobile();
        try {
            log.info("调用查询淘宝报告信息V4接口入参信息 https://api.51datakey.com/gateway/taobao/v4/report/ " + params.getString("taskId"));
            //淘宝报告原始数据
            result = httpUtil.sendGet(
                    "https://api.51datakey.com/gateway/taobao/v4/report/" + params.getString("taskId"),
                    headers);
        } catch (Exception e) {
            e.printStackTrace();
        }
        log.info("调用查询淘宝报告信息V4接口返回信息 result= " + result);
        params.put("idCard", idCard);
        params.put("mobile", mobile);
        params.put("certificationItem", params.getString("taskId"));
        params.put("certificationResult", result);
        params.put("certificationLimit", tools.getTime());
        params.put("creatTime", new Date());
        params.put("certificationType", 9);
        params.put("flag",0);
        Certification record = params.toJavaObject(Certification.class);
        int a = certificationDAO.insert(record);
        if (a != 1) {
            log.info("插入淘宝报告信息失败");
        }
    }




    @ApiOperation("1.3查询学信网信息V2接口")
    @RequestMapping(value = "/getCarrierInfo")
    public void getCarrierInfo(@RequestBody String param) {
        log.info("获取1.3查询学信网信息V2接口入参 param= " + param);
        JSONObject params = JSONObject.parseObject(param);
        BasicHeader[] headers = new BasicHeader[]{
                new BasicHeader("content-type", "application/json;charset=UTF-8"),
                new BasicHeader("Authorization", "token " + token)};
        String result = "";
        CertificationUserInfoExample example = new CertificationUserInfoExample();
        CertificationUserInfoExample.Criteria criteria = example.createCriteria();
        criteria.andUserIdEqualTo(params.getString("userId"));
//        criteria.andUserIdEqualTo("1");
        List<CertificationUserInfo> certificationuserinfolist = certificationUserInfoDAO.selectByExample(example);
        CertificationUserInfo cuil = (CertificationUserInfo) certificationuserinfolist.get(0);
        String idCard = cuil.getIdCard();
        String mobile = cuil.getMobile();
        try {
            //taobao原始数据
            result = httpUtil.sendGet(
                    "https://api.51datakey.com/chsi/v2/students-educations/" + params.getString("taskId"),
                    headers);
        } catch (Exception e) {
            e.printStackTrace();
        }
        log.info("调用1.3查询学信网信息V2接口返回信息 result= " + result);
        params.put("idCard", idCard);
        params.put("mobile", mobile);
        params.put("certificationItem", params.getString("taskId"));
        params.put("certificationResult", result);
        params.put("certificationLimit", tools.getTime());
        params.put("creatTime", new Date());
        params.put("certificationType", 7);
        params.put("flag",0);
        Certification record = params.toJavaObject(Certification.class);
        int a = certificationDAO.insert(record);
        if (a != 1) {
            log.info("插入1.3查询学信网信息V2接口信息失败");
        }
    }




    @ApiOperation("1.4查询运营商报告信息接口")
    @RequestMapping(value = "/getCarrierReport")
    public void getCarrierReport(@RequestBody String param) {
        log.info("获取1.4查询运营商报告信息接口入参 param= " + param);
        JSONObject params = JSONObject.parseObject(param);
        BasicHeader[] headers = new BasicHeader[]{
                new BasicHeader("content-type", "application/json;charset=UTF-8"),
                new BasicHeader("Authorization", "token " + token)};
        String result = "";
        CertificationUserInfoExample example = new CertificationUserInfoExample();
        CertificationUserInfoExample.Criteria criteria = example.createCriteria();
        criteria.andUserIdEqualTo(params.getString("userId"));
//        criteria.andUserIdEqualTo("1");
        List<CertificationUserInfo> certificationuserinfolist = certificationUserInfoDAO.selectByExample(example);
        CertificationUserInfo cuil = (CertificationUserInfo) certificationuserinfolist.get(0);
        String idCard = cuil.getIdCard();
        String mobile = cuil.getMobile();
        try {
            //taobao原始数据
            result = httpUtil.sendGet(
                    "https://api.51datakey.com/carrier/v3/mobiles/"+mobile+"/mxreport",
                    headers);
        } catch (Exception e) {
            e.printStackTrace();
        }
        log.info("调用1.4查询运营商报告信息接口返回信息 result= " + result);
        params.put("idCard", idCard);
        params.put("mobile", mobile);
        params.put("certificationItem", params.getString("taskId"));
        params.put("certificationResult", result);
        params.put("certificationLimit", tools.getTime());
        params.put("creatTime", new Date());
        params.put("certificationType", 10);
        params.put("flag",0);
        Certification record = params.toJavaObject(Certification.class);
        int a = certificationDAO.insert(record);
        if (a != 1) {
            log.info("插入1.4查询运营商报告信息接口信息失败");
        }
    }






    @ApiOperation("1.5查询运营商原始数据信息接口")
    @RequestMapping(value = "/getEducationInfo")
    public void getEducationInfo(@RequestBody String param) {
        log.info("获取1.5查询运营商原始数据信息接口入参 param= " + param);
        JSONObject params = JSONObject.parseObject(param);
        BasicHeader[] headers = new BasicHeader[]{
                new BasicHeader("content-type", "application/json;charset=UTF-8"),
                new BasicHeader("Authorization", "token " + token)};
        String result = "";
        CertificationUserInfoExample example = new CertificationUserInfoExample();
        CertificationUserInfoExample.Criteria criteria = example.createCriteria();
        criteria.andUserIdEqualTo(params.getString("userId"));
//        criteria.andUserIdEqualTo("1");
        List<CertificationUserInfo> certificationuserinfolist = certificationUserInfoDAO.selectByExample(example);
        CertificationUserInfo cuil = (CertificationUserInfo) certificationuserinfolist.get(0);
        String idCard = cuil.getIdCard();
        String mobile = cuil.getMobile();
        try {
            //taobao原始数据
            result = httpUtil.sendGet(
                    "https://api.51datakey.com/carrier/v3/mobiles/"+mobile+"/mxdata-ex?task_id="+params.getString("taskId"),
                    headers);
        } catch (Exception e) {
            e.printStackTrace();
        }
        log.info("调用1.5查询运营商原始数据信息接口返回信息 result= " + result);
        params.put("idCard", idCard);
        params.put("mobile", mobile);
        params.put("certificationItem", params.getString("taskId"));
        params.put("certificationResult", result);
        params.put("certificationLimit", tools.getTime());
        params.put("creatTime", new Date());
        params.put("certificationType", 8);
        params.put("flag",0);
        Certification record = params.toJavaObject(Certification.class);
        int a = certificationDAO.insert(record);
        if (a != 1) {
            log.info("插入1.5查询运营商原始数据信息接口信息失败");
        }
    }

}
