package com.xiaominfo.oss;


import com.alibaba.fastjson.JSON;
import com.xiaominfo.oss.service.OSSStatisticDayService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class OssServerApplicationTests {

    @Autowired
    OSSStatisticDayService ossStatisticDayService;

    @Test
    public void contextLoads() {
    }


    @Test
    public void test() {
        System.out.println(JSON.toJSONString(ossStatisticDayService.queryCurrentWeekStaticsDay()));
        ;
    }

}
