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


    @Select("SELECT taskId,type FROM risk_accredit WHERE userId = #{userId} ")
    public List<Accredit>  selectTaskids(@Param("userId") String uid);



    @Select("SELECT * FROM risk_approveresult WHERE userId = #{userId}  ORDER BY createTime DESC LIMIT 1")
    public List<ApproveResult>  selectByExample(@Param("userId") String uid);

}
