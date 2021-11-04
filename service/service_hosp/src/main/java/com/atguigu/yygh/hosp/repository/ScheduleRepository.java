package com.atguigu.yygh.hosp.repository;

import com.atguigu.yygh.model.hosp.Schedule;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface ScheduleRepository extends MongoRepository<Schedule,String> {

    //根據醫院編號查詢 和 排班編號查詢
    Schedule getScheduleByHoscodeAndHosScheduleId(String hoscode, String hosScheduleId);


    //根據醫院編號 科室編號和工作日期 查詢排班詳細信息
    List<Schedule> findScheduleByHoscodeAndDepcodeAndWorkDate(String hoscode, String depcode, Date toDate);
}
