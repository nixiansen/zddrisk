package com.zdd.risk.api;

import com.alibaba.fastjson.JSONObject;
import com.zdd.risk.bean.*;
import com.zdd.risk.dao.*;
import com.zdd.risk.service.JingDongService;
import com.zdd.risk.utils.*;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang.StringUtils;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * JingDong Service
 *
 * @author 租无忧科技有限公司
 * @date 2018-11-01.
 */
@RestController
@RequestMapping("/risk/jingdong")
@PropertySource(value = "classpath:sanfang/jingdong.properties", ignoreResourceNotFound = true)
public class JingdongController {


    @Value("${getApplyAmount}")
    String getApplyAmount;

    @Value("${getBaseInfo}")
    String getBaseInfo;
    @Value("${approveResult}")
    String approveResult;

    @Value("${orgCode}")
    String orgCode;

    @Value("${appId}")
    String appId;

    @Value("${version}")
    String version;

    @Value("${appKey}")
    String appKey;

    @Value("${appSecret}")
    String appSecret;

    @Value("${APIurl}")
    String APIurl;

    @Value("${tobizurl}")
    String tobizurl;

    @Value("${fqzurl}")
    String fqzurl;

    @Value("${fqzfhurl}")
    String fqzfhurl;


    private static final Logger log = LoggerFactory.getLogger(JingdongController.class);

    @Autowired
    private IAccreditDAO accreditDAO;
    @Autowired
    private IApplyAmountDAO applyAmountDAO;
    @Autowired
    private IApproveResultDAO approveResultDAO;
    @Autowired
    private ICertificationUserInfoDAO certificationUserInfoDAO;
    @Autowired
    private IApproveStrategyResultDAO approveStrategyResultDAO;

    @Autowired
    private OrderSequence orderSequence;

    @Autowired
    private RSAUtils rsaUtils;

    @Autowired
    private Base64Utils base64Utils;

    @Autowired
    private JingDongService jingDongService;

    @Autowired
    private ComputeAge computeAge;
    @Autowired
    private MoXieController moXieController;
    @Autowired
    JingdongController jingdongControl;

    @ApiOperation("3.1请求ZRobot风控服务接口")
    @RequestMapping(value = "/approve")
    public JSONObject approve(@RequestBody String param) {
        log.info("获取请求ZRobot风控服务接口接口入参 param= " + param);
        Map reMap = new HashMap();
        reMap.put("code", "100000");
        reMap.put("codeMsg", "操作成功！");

//        JSONObject result = new JSONObject();
        JSONObject params = JSONObject.parseObject(param);
        params.put("uid", params.getString("userId"));
        params.put("orgCode", orgCode);
        params.put("appId", appId);
        params.put("version", version);
        params.put("appKey", appKey);
        params.put("approveResult", approveResult);

        //查询applyId
        long applyId1 = orderSequence.getOrderNo1();
        String applyId = applyId1 + "";
        params.put("applyId", applyId);


        //查询bizNo
        long bizno1 = orderSequence.getOrderNo2();
        String bizNo = bizno1 + "";
        params.put("bizNo", bizNo);

//        把bizNo数据插入risk_applyamount表
        JSONObject bizjson = new JSONObject();
        bizjson.put("bizNo", bizNo);
        bizjson.put("userId", params.getString("userId"));
        ApplyAmount record = bizjson.toJavaObject(ApplyAmount.class);
        ApplyAmountExample example = new ApplyAmountExample();
        ApplyAmountExample.Criteria criteria = example.createCriteria();
        criteria.andUserIdEqualTo(bizjson.getString("userId"));
        List<ApplyAmount> applyAmountlist = applyAmountDAO.selectByExample(example);
        int a = 1;
        System.out.println("集合大小=================" + applyAmountlist.size());
        if (applyAmountlist.size() == 0) {
            record.setCreateTime(new Date());
            log.info("往risk_applyamount表插入bizNo数据参数：" + JSONObject.toJSONString(record));
            a = applyAmountDAO.insert(record);
            System.out.println(a);
        } else {
            record.setUpdateTime(new Date());
            log.info("往risk_applyamount表更新bizNo数据参数：" + JSONObject.toJSONString(record));
            a = applyAmountDAO.updateByExampleSelective(record, example);
            System.out.println(a);
        }

        String productId = params.getString("productId");
        if (productId.equals("1")) {
            params.put("productId", "creditRentForCollege");

        } else if (productId.equals("2")) {
            params.put("productId", "creditRentForSalaryman");
        } else {
            reMap.put("code", "2");
            reMap.put("codeMsg", "请选择productId（1,2）");
//需要加一个异常类
            log.info("请选择productId（1,2）");
            return new JSONObject(reMap);
        }

        JSONObject database1 = new JSONObject();
        database1.put("getApplyAmount", getApplyAmount);
        database1.put("getBaseInfo", getBaseInfo);
        params.put("database", database1.toJSONString());

        //查询taskId
        JSONObject resultat = jingDongService.selectAccreditTaskId(params.getString("userId"), productId);
        params.put("dataTaskId", resultat.toJSONString());
        log.info("AccreditTaskId查询结果：" + resultat);
        log.info("调用京东风控策略接口传入参数信息 ：" + params);

//        Tools tools = new Tools();
//        String gettype = tools.getRandomInfo();
//
//        System.out.println(gettype);

//        if (gettype.equals("0")) {
        HttpUtils http = new HttpUtils();
        String result = http.post(fqzurl, params.getString("userId"));
        log.info("调用反欺诈风控策略接口返回信息 result= " + result);
        JSONObject reparam = JSONObject.parseObject(result);
        reparam.put("uid", params.getString("userId"));

//            JSONObject reparam=new JSONObject();
//            reparam.put("uid","jirhat17f9us");
//            reparam.put("approveResult","E");
//            reparam.put("approveCredit",0);

        ExecutorService executorService = Executors.newFixedThreadPool(20);
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                HttpUtils http1 = new HttpUtils();
                String result1 = http1.post(fqzfhurl, reparam.toJSONString());
                log.info("调用京东反欺诈策略接口返回信息 result1= " + result1);
            }
        });
//        } else {
        //调用京东风控策略接口
        HttpUtils http2 = new HttpUtils();
        String result2 = http2.post(APIurl, params.toJSONString());
        log.info("调用京东风控策略接口返回信息 result= " + result2);
//        }
        //TODO
        //回调业务系统
        return new JSONObject(reMap);
    }

    @ApiOperation("3.2ZRobot风控审批结果回调接口")
    @RequestMapping(value = "/approveResultFromZRobot")
    public JSONObject approveResultFromZRobot(@RequestBody String param) {
        log.info("获取ZRobot风控审批结果回调接口入参 param= " + param);
        Map reMap = new HashMap();
        reMap.put("success", "true");
        JSONObject params = JSONObject.parseObject(param);
        params.put("approveCredit", params.getInteger("approveCredit") * 100);
        params.put("userId", params.getString("uid"));
        //insert DB
        ApproveResult record = params.toJavaObject(ApproveResult.class);
        record.setCreateTime(new Date());
        log.info("往risk_approveResult表插入数据参数 param1= " + JSONObject.toJSONString(record));
        int a = approveResultDAO.insert(record);
        System.out.println("==========================================================");
        if (a != 1) {
            reMap.put("success", "false");
            log.info("往risk_approveResult表插入数据失败" + JSONObject.toJSONString(record));
        }
        //TODO
        //回调业务系统
        return new JSONObject(reMap);
    }

    @ApiOperation("3.5ZRobot风控审批结果回调接口")
    @RequestMapping(value = "/approveResultFromZRobottwo")
    public JSONObject approveResultFromZRobottwo(@RequestBody String param) {
        log.info("获取反欺诈风控审批结果回调接口入参 param= " + param);
        Map reMap = new HashMap();
        reMap.put("success", "true");
        JSONObject params = JSONObject.parseObject(param);
        params.put("approveCredit", params.getInteger("approveCredit") * 100);
        params.put("userId", params.getString("uid"));
        //insert DB
        ApproveResult record = params.toJavaObject(ApproveResult.class);
        record.setCreateTime(new Date());
        log.info("往risk_approveResult表插入数据参数 param1= " + JSONObject.toJSONString(record));
        int a = approveResultDAO.insert(record);
        System.out.println("==========================================================");
        if (a != 1) {
            reMap.put("success", "false");
            log.info("往risk_approveResult表插入数据失败" + JSONObject.toJSONString(record));
        } else {
            resultApproveToZRobot(params.getString("userId"));
        }
        //TODO
        //回调业务系统
        return new JSONObject(reMap);
    }


    @ApiOperation("3.3返回ZRobot风控服务回调接口")
    @RequestMapping(value = "/resultApproveToZRobot")
    public void resultApproveToZRobot(@RequestBody String userId) {
        log.info("返回ZRobot风控服务回调接口入参 userId= " + userId);

//        JSONObject jsonObject1 = new JSONObject();
//        //测试数据
//        jsonObject1.put("userId", userId);
//        jsonObject1.put("approveResult", "pass");
//        jsonObject1.put("approveCredit", "5000");
        //通过userID查询数据
        String approveResute = jingDongService.selectByExample1(userId);
        Map<String, JSONObject> map = new HashMap<String, JSONObject>();
        JSONObject jsonObject = JSONObject.parseObject(approveResute);
        map.put("result", jsonObject);
        log.info("回调业务部门的接口传入参数信息 ：" + map.toString());
        //回调业务部门的接口
        HttpUtils http = new HttpUtils();
        String result1 = http.doPostHttp1(tobizurl, map);
        log.info("回调业务部门的接口返回信息 result= " + result1);
    }


    @ApiOperation("3.4京东风控策略详细返回信息")
    @RequestMapping(value = "/approveStrategyResult")
    public JSONObject approveStrategyResult(@RequestBody String param) {

        log.info("京东风控策略详细返回信息 param= " + param);
        Map reMap = new HashMap();
        reMap.put("success", "true");
        JSONObject params = JSONObject.parseObject(param);
        params.put("zrobotCredit", params.getString("zrobotCredit"));
        params.put("zrobotBlack", params.getString("zrobotBlack"));
        params.put("financalBehavior", params.getString("financalBehavior"));
        params.put("antiFraud", params.getString("antiFraud"));
        params.put("userId", params.getString("uid"));
        //insert DB
        log.info("往risk_approveStrategyResult表插入数据参数 param1= " + params);
        ApproveStrategyResult record = params.toJavaObject(ApproveStrategyResult.class);
        record.setCreateTime(new Date());
        log.info("往risk_approveStrategyResult表插入数据参数 param1= " + JSONObject.toJSONString(record));
        int a = approveStrategyResultDAO.insert(record);
        if (a != 1) {
            reMap.put("success", "false");
        }
        //TODO
        //回调业务系统
        return new JSONObject(reMap);
    }

    @ApiOperation("4.1获取用户申请额度接口")
    @RequestMapping(value = "/getApplyAmountFromZDD", method = RequestMethod.GET)
    public String getApplyAmountFromZDD(@RequestParam("uid") String uid) {
        log.info("获取用户授权信息数据接口入参条件 uid= " + uid);
        JSONObject params = new JSONObject();
        //初始化返回参数
        JSONObject data = new JSONObject();
        data.put("uid", uid);
        data.put("applyAmount", "1000");
        data.put("applyDays", "30");
        data.put("applyMonths", "12");
        data.put("contractAmount", "5000");
        data.put("modelNo", "iPhone 7s 银色/128G");
//        JSONObject resultat = jingDongService.selectBizNo(uid);
//        data.put("bizNo", resultat.getString("bizNo"));
        data.put("bizNo", "100003445460110851003989");


        log.info("获取用户授权信息数据接口入参 data:" + data);

        //select DB
/*        ApplyAmountExample example = new ApplyAmountExample();
        ApplyAmountExample.Criteria criteria = example.createCriteria();
        criteria.andUserIdEqualTo(uid);*/
        //通过userID查询申请额度信息
//        List<ApplyAmount> applyamountlist = applyAmountDAO.selectByExample(example);
//        System.out.println("集合大小=================" + applyamountlist.size());
/*
        if (applyamountlist.size() == 0) {
            params.put("success", "fails");
            params.put("errorCode", "查询成功");
            params.put("errorMessage", "没有数据！");
            try {
                //对data数据进行加密
                byte[] publicEncrypt = rsaUtils.encryptByPublicKey(data.toJSONString().getBytes("UTF-8"), appSecret);
                String pb = new String(publicEncrypt, "UTF-8");
                String byte2Base64 = base64Utils.encode(publicEncrypt);
                params.put("data", byte2Base64);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            JSONObject result = new JSONObject();
            String alist = StringUtils.strip(result.toJSONString(applyamountlist.get(0)), "[]");
//            System.out.println("alist为=================：" + alist);
            data = JSONObject.parseObject(alist);
            data.put("bizNo", bizNo2);
            int ac = Integer.parseInt(data.getString("applyAmount"));
            data.put("applyAmount", ac / 100 + "");
            result.put("data", data.toJSONString());
            log.info("获取用户授权信息数据接口返回参数 params1= " + data);
            try {
                byte[] publicEncrypt = rsaUtils.encryptByPublicKey(result.toJSONString().getBytes("UTF-8"), appSecret);
                String pb = new String(publicEncrypt, "UTF-8");
                String byte2Base64 = base64Utils.encode(publicEncrypt);
                params.put("data", byte2Base64);
            } catch (Exception e) {
                e.printStackTrace();
            }
            params.put("success", "true");
            params.put("errorCode", "");
            params.put("errorMessage", "");
        }*/
        try {
            byte[] publicEncrypt = rsaUtils.encryptByPublicKey(data.toJSONString().getBytes("UTF-8"), appSecret);
            String pb = new String(publicEncrypt, "UTF-8");
            String byte2Base64 = base64Utils.encode(publicEncrypt);
            params.put("data", byte2Base64);
        } catch (Exception e) {
            e.printStackTrace();
        }
        params.put("success", "true");
        params.put("errorCode", "");
        params.put("errorMessage", "");
        log.info("用户授权信息返回数据加密参数 params= " + params);
        return params.toJSONString();

    }

    @ApiOperation("4.2获取用户基本信息数据接口")
    @RequestMapping(value = "/getBaseInfoFromZDD", method = RequestMethod.GET)
    public String getBaseInfoFromZDD(@RequestParam("uid") String uid) {
        log.info("获取用户基本信息数据接口入参 uid= " + uid);

        JSONObject params = new JSONObject();
        params.put("uid", uid);

        //初始化返回参数
        JSONObject data = new JSONObject();
        data.put("uid", uid);
        data.put("mobile", "");
        data.put("realName", "");
        data.put("idCard", "");
        data.put("nation", "");
        data.put("age", "");
        data.put("addressPermanent", "");
        data.put("idCardValidDate", "");
        data.put("maritalStatus", "");
        data.put("education", "");
        data.put("institution", "");
        data.put("addressWork", "");
        data.put("addressHome", "");
        data.put("payday", "");
        data.put("companyName", "");
        data.put("companyPhone", "");
        data.put("sosContactName", "");
        data.put("sosContactRelation", "");
        data.put("sosContactPhone", "");
        data.put("sosContactName1", "");
        data.put("sosContactRelation1", "");
        data.put("sosContactPhone1", "");
        data.put("lastLoginIp", "");
        data.put("longitude", "");
        data.put("latitude", "");
        data.put("gpsAddress", "");
        data.put("regOs", "");
        data.put("regAppVersion", "");
        data.put("regFrom", "");
        data.put("regIp", "");
        data.put("addressBook", "");
        data.put("gpsHistory", "");
        data.put("bankCard", "");
        data.put("bankMobile", "");

        CertificationUserInfoExample example = new CertificationUserInfoExample();
        CertificationUserInfoExample.Criteria criteria = example.createCriteria();
        criteria.andUserIdEqualTo(uid);
        //通过userID查询用户基本信息数据
        List<CertificationUserInfo> certificationuserinfolist = certificationUserInfoDAO.selectByExample(example);
        System.out.println("集合大小=================" + certificationuserinfolist.size());
        if (certificationuserinfolist.size() == 0) {
            params.put("success", "fails");
            params.put("errorCode", "查询失败");
            params.put("errorMessage", "没有数据！");
            try {
                byte[] publicEncrypt = rsaUtils.encryptByPublicKey(data.toJSONString().getBytes("UTF-8"), appSecret);
                String byte2Base64 = base64Utils.encode(publicEncrypt);
                params.put("data", byte2Base64);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            JSONObject result = new JSONObject();

//            System.out.println("集合大小================="+certificationuserinfolist.size());

            String rs = StringUtils.strip(result.toJSONString(certificationuserinfolist.get(0)), "[]");
            data = JSONObject.parseObject(rs);
            data.put("idCardValidDate", data.getString("idCardValidDate").replace("~", "-"));
            result.put("data", data);
            log.info("获取用户基本信息数据接口返回参数 data= " + data);
            try {
                byte[] publicEncrypt = rsaUtils.encryptByPublicKey(result.toJSONString().getBytes("UTF-8"), appSecret);
                String byte2Base64 = base64Utils.encode(publicEncrypt);
                params.put("data", byte2Base64);
            } catch (Exception e) {
                e.printStackTrace();
            }
            params.put("success", "true");
            params.put("errorCode", "");
            params.put("errorMessage", "");

        }
        log.info("获取用户基本信息数据接口返回加密数据参数 params= " + params);
        return params.toJSONString();
    }

    @ApiOperation("4.3插入用户授权信息数据接口")
    @RequestMapping(value = "/insertDataTaskIdFromZDD")
    public JSONObject insertDataTaskIdFromZDD(@RequestBody String param) {
        log.info("获取用户授权信息数据接口入参 param= " + param);
        Map reMap = new HashMap();
        reMap.put("code", "100000");
        reMap.put("codeMsg", "操作成功！");
        //JSONObject result = new JSONObject();
        JSONObject params = JSONObject.parseObject(param);

        ExecutorService executorService = Executors.newFixedThreadPool(20);

        if (params.getString("type").equals("1")) {
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    moXieController.getCarrierInfo(param);
                }
            });
        } else if (params.getString("type").equals("2")) {
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    moXieController.getTaobaoInfo(param);
                    moXieController.getTaobaoReport(param);
                }
            });
        } else if (params.getString("type").equals("3")) {
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    moXieController.getCarrierReport(param);
                    moXieController.getEducationInfo(param);
                }
            });
        }
        System.out.println("===============================");
        //insert DB
        Accredit record = params.toJavaObject(Accredit.class);
        record.setCreateTime(new Date());
        log.info("获取用户授权信息数据后往risk_accredit表插入数据参数：" + JSONObject.toJSONString(record));
        int a = accreditDAO.insert(record);
        if (a != 1) {
            reMap.put("code", "0");
            reMap.put("codeMsg", "操作失败！");
        }
//        System.out.println(a);
        return new JSONObject(reMap);
    }


    @ApiOperation("4.4用户申请额度信息存储")
    @RequestMapping(value = "/insertApplyAmount")
    public JSONObject insertApplyAmount(@RequestBody String param) {
        // public JSONObject insertApplyAmountAndBaseInfo() {
        log.info("4用户申请额度信息存储 param= " + param);

        Map reMap = new HashMap();
        reMap.put("code", "100000");
        reMap.put("codeMsg", "操作成功！");

        JSONObject params = JSONObject.parseObject(param);

        ApplyAmount record = params.toJavaObject(ApplyAmount.class);
        ApplyAmountExample example = new ApplyAmountExample();
        ApplyAmountExample.Criteria criteria = example.createCriteria();
        criteria.andUserIdEqualTo(params.getString("userId"));
        List<ApplyAmount> applyAmountlist = applyAmountDAO.selectByExample(example);
        int a = 1;
        System.out.println("集合大小=================" + applyAmountlist.size());
        if (applyAmountlist.size() == 0) {
            record.setCreateTime(new Date());
            log.info("获取用户基本信息后往risk_applyamount表插入数据参数：" + JSONObject.toJSONString(record));
            a = applyAmountDAO.insert(record);
            System.out.println(a);
        } else {
            record.setUpdateTime(new Date());
            log.info("获取用户基本信息后往risk_applyamount表更新数据参数：" + JSONObject.toJSONString(record));
            a = applyAmountDAO.updateByExampleSelective(record, example);
            System.out.println(a);
        }

        if (a != 1) {
            reMap.put("code", "0");
            reMap.put("codeMsg", "操作失败！");
        }
        return new JSONObject(reMap);
    }

    @ApiOperation("4.5用户基本信息存储")
    @RequestMapping(value = "/insertBaseInfo")
    public JSONObject insertBaseInfo(@RequestBody String param) {
        // public JSONObject insertApplyAmountAndBaseInfo() {
        log.info("4.5用户基本信息存储 param= " + param);
        int a = 1;
        JSONObject params = JSONObject.parseObject(param);
//获取用户年龄
        String idCard = params.get("idCard").toString();
        int getage = computeAge.getAge(idCard);
        params.put("age", getage);
        params.put("addressBook", params.getString("mobilelog"));
        Map reMap = new HashMap();
        reMap.put("code", "100000");
        reMap.put("codeMsg", "操作成功！");
        CertificationUserInfo record = params.toJavaObject(CertificationUserInfo.class);
        CertificationUserInfoExample example = new CertificationUserInfoExample();
        CertificationUserInfoExample.Criteria criteria = example.createCriteria();
        criteria.andUserIdEqualTo(params.getString("userId"));
        List<CertificationUserInfo> certificationuserinfolist = certificationUserInfoDAO.selectByExample(example);
        System.out.println("集合大小=================" + certificationuserinfolist.size());
        if (certificationuserinfolist.size() == 0) {
            record.setCreateTime(new Date());
            log.info("获取用户基本信息后往risk_certificationuserinfo表插入数据参数：" + JSONObject.toJSONString(record));
            a = certificationUserInfoDAO.insert(record);
            System.out.println(a);
        } else {
            record.setUpdateTime(new Date());
            log.info("获取用户基本信息后往risk_certificationuserinfo表更新数据参数：" + JSONObject.toJSONString(record));
            a = certificationUserInfoDAO.updateByExampleSelective(record, example);
            System.out.println(a);
        }
        if (a != 1) {
            reMap.put("code", "0");
            reMap.put("codeMsg", "操作失败！");
        }
        return new JSONObject(reMap);
    }


    @ApiOperation("4.6获取用户设备指纹接口")
    @RequestMapping(value = "/getDeviceInfo", method = RequestMethod.GET)
    public String getDeviceInfo(@RequestParam("uid") String uid) {
        log.info("获取用户设备指纹接口 uid= " + uid);

        JSONObject params = new JSONObject();
        params.put("success", "true");
        params.put("errorCode", "");
        params.put("errorMessage", "");
        JSONObject data1 = new JSONObject();
        data1.put("gpsCity", "");
        data1.put("appVersion", "");
        data1.put("reputation", "89");
        data1.put("vendorId", "");
        data1.put("ipCountry", "中国");
        data1.put("deviceId", "1722B-000O8460453-52468711");
        data1.put("gpsStreetNo", "");
        data1.put("browserVersion", "56.0.2924");
        data1.put("isProxy", "");
        data1.put("gpsRegion", "");
        data1.put("gpsAddress", "");
        data1.put("deviceType", "PC");
        data1.put("deviceFirstTs", "2016-06-05 22:08:47");
        data1.put("app", "");
        data1.put("ipIsp", "联通");
        data1.put("isRootJailbreak", "");
        data1.put("ipLatitude", "31.3");
        data1.put("ip", "171.34.210.189");
        data1.put("trueIpRegion", "江西省");
        data1.put("gps", "");
        data1.put("gpsDistrict", "");
        data1.put("isVm", "false");
        data1.put("IDFA", "");
        data1.put("phone", "");
        data1.put("ipRegion", "江西省");
        data1.put("trueIp", "171.34.210.189");
        data1.put("trueIpCity", "南昌市");
        data1.put("screenWidth", "1440");
        data1.put("ipCity", "南昌市");
        data1.put("timezone", "+8");
        data1.put("useragent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/56.0.2924.87 Safari/537.36");
        data1.put("trueIpLatitude", "31.3");
        data1.put("imsi", "");
        data1.put("mac", "");
        data1.put("trueIpCountry", "中国");
        data1.put("isDebug", "false");
        data1.put("appSign", "");
        data1.put("didMessage", "");
        data1.put("osVersion", "10.11.6");
        data1.put("browser", "360");
        data1.put("ipLongitude", "120.58");
        data1.put("isSimulator", "false");
        data1.put("haveCheatApp", "");
        data1.put("trueIpIsp", "联通");
        data1.put("os", "MAC OS X");
        data1.put("ipCoordinate", "120.58 31.3");
        data1.put("screenHeight", "900");
        data1.put("gpsBusiness", "");
        data1.put("trueIpCoordinate", "120.58 31.3");
        data1.put("url", "https:fanqizha.tongfudun.com");
        data1.put("trueIpLongitude", "120.58");
        data1.put("gpsCountry", "");
        data1.put("imei", "");
        data1.put("isModify", "false");
        data1.put("isInit", "false");
        data1.put("errorCode", "gpsStreet");
        JSONObject location = new JSONObject();
        location.put("deviceCountry", "中国");
        location.put("deviceRegion", "江西省");
        location.put("deviceCity", "南昌市");
        location.put("deviceAccuracy", "LOW");
        data1.put("location", location.toJSONString());
        try {
            byte[] publicEncrypt = rsaUtils.encryptByPublicKey(data1.toJSONString().getBytes("UTF-8"), appSecret);
            String byte2Base64 = rsaUtils.byte2Base64(publicEncrypt);
            params.put("data", byte2Base64);
        } catch (Exception e) {
            e.printStackTrace();
        }
        log.info("获取用户授权信息数据接口返回参数 params= " + params);
        return params.toJSONString();

    }


    @ApiOperation("测试类")
    @RequestMapping(value = "/testFromZDD")
    public void testFromZDD() {

        JSONObject result = jingDongService.selectAccreditTaskId("123", "1");

        log.info("AccreditTaskId查询结果：" + result);

    }


}
