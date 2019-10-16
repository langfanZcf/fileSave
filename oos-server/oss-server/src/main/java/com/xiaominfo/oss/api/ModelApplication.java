/*
 * Copyright (C) 2018 Zhejiang xiaominfo Technology CO.,LTD.
 * All rights reserved.
 * Official Web Site: http://www.xiaominfo.com.
 * Developer Web Site: http://open.xiaominfo.com.
 */

package com.xiaominfo.oss.api;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.xiaominfo.oss.module.model.OSSDeveloper;
import com.xiaominfo.oss.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/***
 * 前端url跳转Application
 * @since:oss-server 1.0
 * @author <a href="mailto:xiaoymin@foxmail.com">xiaoymin@foxmail.com</a>
 * 2018/06/16 19:59
 */
@Controller
@RequestMapping("/oss")
public class ModelApplication {

    @Value(value = "${material.root}")
    private String root;

    @Value(value = "${material.invokingRoot}")
    private String invokingRoot;

    @Autowired
    OSSInformationService ossInformationService;

    @Autowired
    OSSDeveloperService ossDeveloperService;

    @Autowired
    OSSAppInfoService ossAppInfoService;

    @Autowired
    OSSMaterialInfoService ossMaterialInfoService;

    @Autowired
    OSSStatisticDayService ossStatisticDayService;


    /**
     * 主面板
     *
     * @return
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        List<OSSDeveloper> ossDevelopers = ossDeveloperService.queryAllDevs();
        model.addAttribute("apps", ossAppInfoService.selectCount(new EntityWrapper<>()));
        model.addAttribute("devs", ossDeveloperService.selectCount(new EntityWrapper<>()));
        model.addAttribute("files", ossMaterialInfoService.selectCount(new EntityWrapper<>()));
        model.addAttribute("totalUseSpace", ossMaterialInfoService.queryTotalSpaceByteStr());
        String fileCounts = JSON.toJSONString(ossStatisticDayService.queryCurrentWeekStaticsDay());
        String devCounts = JSON.toJSONString(ossDevelopers);
        model.addAttribute("fileCounts", fileCounts);
        model.addAttribute("devCounts", devCounts);


        return "dashboard";
    }

    @GetMapping("/information")
    public String information(Model model) {
        model.addAttribute("ossInfo", ossInformationService.queryOne());
        return "information";
    }

    @GetMapping("/developer")
    public String developer() {
        return "developer";
    }

    @GetMapping("/appinfo")
    public String appinfo(Model model) {
        model.addAttribute("devList", ossDeveloperService.queryAllDevs());
        return "appinfo";
    }

    @GetMapping("/materialinfo")
    public String materialInfo(Model model) {
        return "material_info";
    }

}
