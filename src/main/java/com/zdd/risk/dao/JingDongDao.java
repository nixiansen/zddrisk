package com.zdd.risk.dao;

import com.zdd.risk.bean.Accredit;
import com.zdd.risk.bean.ApproveResult;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * Created by ZWYnqj on 2018/11/21.
 */

@Repository
public interface JingDongDao {


    @Select("( SELECT * FROM risk_accredit WHERE userId = #{userId} AND type = \"1\" ORDER BY createTime DESC LIMIT 1 ) UNION ALL ( SELECT * FROM risk_accredit WHERE userId = #{userId} AND type = \"2\" ORDER BY createTime DESC LIMIT 1 ) UNION ALL ( SELECT * FROM risk_accredit WHERE userId = #{userId} AND type = \"3\" ORDER BY createTime DESC LIMIT 1 )")
    public List<Accredit>  selectTaskids(@Param("userId") String uid);



    @Select("SELECT userId,approveCredit,approveResult FROM risk_approveresult WHERE userId = #{userId}  ORDER BY createTime DESC LIMIT 1")
    public List<ApproveResult>  selectByExample(@Param("userId") String uid);


    @Select("SELECT * FROM risk_applyamount WHERE userId = #{userId}  ORDER BY createTime DESC LIMIT 1")
    public List<ApproveResult>  selectBizNo(@Param("userId") String uid);


}
