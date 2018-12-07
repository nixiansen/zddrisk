package com.zdd.risk.utils;


import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by ZWYnqj on 2018/11/28.
 */
@Component
public class ComputeAge {

//    String idCard="445281199304110839";


    public int getAge(String idCard) {

        int idCardYear = Integer.parseInt(idCard.substring(6,10));

        Calendar cal = Calendar.getInstance();


//        Date d=new Date();
//        SimpleDateFormat sdf=new SimpleDateFormat("yyyy");
//        String dyear=sdf.format(d);
        int dyear = cal.get(Calendar.YEAR);
        int age=dyear-idCardYear;

        return age;

    }

    public static void main(String[] args) {
//        ComputeAge comp=new ComputeAge();
//        int a=  comp.getAge();
//        System.out.println(a);
    }

}
