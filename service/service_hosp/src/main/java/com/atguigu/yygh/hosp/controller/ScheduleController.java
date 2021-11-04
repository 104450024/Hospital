package com.atguigu.yygh.hosp.controller;


import com.atguigu.yygh.common.result.Result;
import com.atguigu.yygh.hosp.service.ScheduleService;
import com.atguigu.yygh.model.hosp.Schedule;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/hosp/Schedule")
/*@CrossOrigin*/
public class ScheduleController {

    @Autowired
    private ScheduleService scheduleService;

    //根據醫院編號 和 科室編號 ，查詢排班規則

    @ApiOperation(value="查詢排班規則數據")
    @GetMapping("getScheduleRule/{page}/{limit}/{hoscode}/{depcode}")
    public Result getScheduleRule(@PathVariable long page,
                                  @PathVariable long limit,
                                  @PathVariable String hoscode,
                                  @PathVariable String depcode){

       Map<String,Object> map=
               scheduleService.getRuleSchedule(page,limit,hoscode,depcode);
       return Result.ok(map);

    }
    //根據醫院編號 科室編號和工作日期 查詢排班詳細信息
    @ApiOperation(value = "查詢排班詳細信息")
    @GetMapping("getScheduleDetail/{hoscode}/{depcode}/{workDate}")
    public Result getScheduleDetail(@PathVariable String hoscode,
                                    @PathVariable String depcode,
                                    @PathVariable String workDate){

       List<Schedule> list=
               scheduleService.getDetailSchedule(hoscode,depcode,workDate);
       return Result.ok(list);
    }


}
