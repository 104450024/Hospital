package com.atguigu.yygh.hosp.repository;

import com.atguigu.yygh.model.hosp.Hospital;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface HospitalRepository extends MongoRepository<Hospital,String> {


    //判斷是否存在數據
    Hospital getHospitalByHoscode(String hoscode);


    //根據醫院名稱查詢
    List<Hospital> findHospitalByHosnameLike(String hosname);
}
