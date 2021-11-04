package com.atguigu.yygh.hosp.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.yygh.hosp.repository.ScheduleRepository;
import com.atguigu.yygh.hosp.service.DepartmentService;
import com.atguigu.yygh.hosp.service.HospitalService;
import com.atguigu.yygh.hosp.service.ScheduleService;
import com.atguigu.yygh.model.hosp.Department;
import com.atguigu.yygh.model.hosp.Schedule;
import com.atguigu.yygh.vo.hosp.BookingScheduleRuleVo;
import com.atguigu.yygh.vo.hosp.ScheduleQueryVo;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;

import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ScheduleServiceImpl implements ScheduleService {

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private HospitalService hospitalService;

    @Autowired
    private DepartmentService departmentService;

    //上傳排班接口
    @Override
    public void save2(Map<String, Object> paramMap) {
        //param 轉換成 department 對象
        String paramMapString= JSONObject.toJSONString(paramMap);
        Schedule schedule= JSONObject.parseObject(paramMapString,Schedule.class);

        //根據醫院編號查詢 和 排班編號查詢
        Schedule scheduleExist=scheduleRepository
                .getScheduleByHoscodeAndHosScheduleId(schedule.getHoscode(),schedule.getHosScheduleId());

        if(scheduleExist!=null){
            scheduleExist.setUpdateTime(new Date());
            scheduleExist.setIsDeleted(0);
            scheduleExist.setStatus(1);
            scheduleRepository.save(scheduleExist);
        }else{
            schedule.setCreateTime(new Date());
            schedule.setUpdateTime(new Date());
            schedule.setIsDeleted(0);
            schedule.setStatus(1);
            scheduleRepository.save(schedule);
        }

    }

    @Override
    //查詢排班接口
    public Page<Schedule> findPageSchedule(int page,
                                           int limit,
                                           ScheduleQueryVo scheduleQueryVo) {
        //創建Pageable對象，設置當前頁和每頁紀錄數
        //0是你的第一頁
        Pageable pageable= PageRequest.of(page-1,limit);
        //創建Example對象
        Schedule schedule=new Schedule();
        BeanUtils.copyProperties(scheduleQueryVo,schedule);
        schedule.setIsDeleted(0);

        ExampleMatcher matcher=ExampleMatcher.matching()
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING)
                .withIgnoreCase(true);
        Example<Schedule> example=Example.of(schedule,matcher);


        Page<Schedule> all = scheduleRepository.findAll(example, pageable);
        return all;
    }


    //刪除排班
    @Override
    public void remove(String hoscode, String hosScheduleId) {
        //根據醫院編號和排班編號查詢信息
        Schedule schedule = scheduleRepository.getScheduleByHoscodeAndHosScheduleId(hoscode, hosScheduleId);
        if(schedule!=null){
            scheduleRepository.deleteById(schedule.getId());

        }
    }

    //根據醫院編號 和 科室編號 ，查詢排班規則數據
    @Override
    public Map<String, Object> getRuleSchedule
            (long page, long limit, String hoscode, String depcode) {
        //1.根據醫院編號 和 科室編號 查詢
        Criteria criteria=Criteria.where("hoscode")
                .is(hoscode).and("depcode").is(depcode);
        //2.根據工作日期workdate進行分組
        Aggregation agg= Aggregation.newAggregation(
                Aggregation.match(criteria),//匹配條件
                Aggregation.group("workDate") //分組字段
                .first("workDate").as("workDate")
                //3.統計號源數量
                .count().as("docCount")
                .sum("reservedNumber").as("reservedNumber")
                .sum("availableNumber").as("availableNumber"),
                //排序
                Aggregation.sort(Sort.Direction.DESC,"workDate"),
                //4.實現分頁
                Aggregation.skip((page-1)*limit),
                Aggregation.limit(limit)
        );

        //調用方法 最終執行
        AggregationResults<BookingScheduleRuleVo>
                aggResults = mongoTemplate.aggregate
                (agg, Schedule.class, BookingScheduleRuleVo.class);

        List<BookingScheduleRuleVo> bookingScheduleRuleVoList
                = aggResults.getMappedResults();

        //分組查詢後的總記錄數
        Aggregation totalAgg= Aggregation.newAggregation(
                Aggregation.match(criteria),
                Aggregation.group("workDate")
        );
        AggregationResults<BookingScheduleRuleVo>
                totalAggResults = mongoTemplate.aggregate
                (totalAgg, Schedule.class, BookingScheduleRuleVo.class);

        int total = totalAggResults.getMappedResults().size();

        //把日期對應星期幾
        for (BookingScheduleRuleVo bookingScheduleRuleVo:bookingScheduleRuleVoList){
            Date workDate = bookingScheduleRuleVo.getWorkDate();
            String dayOfWeek = this.getDayOfWeek(new DateTime(workDate));
            bookingScheduleRuleVo.setDayOfWeek(dayOfWeek);
        }
        //設置最終數據，進行返回
        Map<String,Object> result=new HashMap<>();
        result.put("bookingScheduleRuleList",bookingScheduleRuleVoList);
        result.put("total",total);

        //獲取醫院名稱
        String hosname=hospitalService.getHospName(hoscode);
        //其他基礎數據
        Map<String,String> baseMap=new HashMap<>();

        baseMap.put("hosname",hosname);
        result.put("baseMap",baseMap);
        return result;
    }

    //根據醫院編號 科室編號和工作日期 查詢排班詳細信息
    @Override
    public List<Schedule> getDetailSchedule(String hoscode, String depcode, String workDate) {

        //根據參數查詢mongoDB
        List<Schedule> scheduleList=scheduleRepository.findScheduleByHoscodeAndDepcodeAndWorkDate
                (hoscode,depcode,new DateTime(workDate).toDate());
        //把得到list集合便利，向設置其他值 醫院名稱 科室名稱 日期對應星期
        scheduleList.stream().forEach(item->{
            this.packageSchedule(item);
        });
        return scheduleList;
    }

    //封裝排班詳情其他值
    private void packageSchedule(Schedule schedule) {
        //設置醫院名稱
        schedule.getParam().put("hosname",hospitalService.getHospName(schedule.getHoscode()));
        //設置科室名稱
        schedule.getParam().put("depname",departmentService.getDepName(schedule.getHoscode(),schedule.getDepcode()));
        //設置日期對應星期
        schedule.getParam().put("dayOfWeek",this.getDayOfWeek(new DateTime(schedule.getWorkDate())));

    }

    private String getDayOfWeek(DateTime dateTime) {
        String dayOfWeek = "";
        switch (dateTime.getDayOfWeek()) {
            case DateTimeConstants.SUNDAY:
                dayOfWeek = "周日";
                break;
            case DateTimeConstants.MONDAY:
                dayOfWeek = "周一";
                break;
            case DateTimeConstants.TUESDAY:
                dayOfWeek = "周二";
                break;
            case DateTimeConstants.WEDNESDAY:
                dayOfWeek = "周三";
                break;
            case DateTimeConstants.THURSDAY:
                dayOfWeek = "周四";
                break;
            case DateTimeConstants.FRIDAY:
                dayOfWeek = "周五";
                break;
            case DateTimeConstants.SATURDAY:
                dayOfWeek = "周六";
            default:
                break;
        }
        return dayOfWeek;
    }

}
