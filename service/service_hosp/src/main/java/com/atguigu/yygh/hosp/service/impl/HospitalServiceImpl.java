package com.atguigu.yygh.hosp.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.yygh.cmn.client.DictFeignClient;
import com.atguigu.yygh.hosp.repository.HospitalRepository;
import com.atguigu.yygh.hosp.service.HospitalService;
import com.atguigu.yygh.model.hosp.Hospital;
import com.atguigu.yygh.vo.hosp.HospitalQueryVo;
import com.atguigu.yygh.vo.hosp.HospitalSetQueryVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class HospitalServiceImpl implements HospitalService {

    @Autowired
    private HospitalRepository hospitalRepository;

    @Autowired
    private DictFeignClient dictFeignClient;

    @Override
    public void save(Map<String, Object> paramMap) {

        //把參數map集合轉換對象 Hospital
        String mpaString = JSONObject.toJSONString(paramMap);
        Hospital hospital = JSONObject.parseObject(mpaString, Hospital.class);

        //判斷是否存在相同數據
        String hoscode = hospital.getHoscode();
        Hospital hospitalExist=hospitalRepository.getHospitalByHoscode(hoscode);
        //如果存在，進行修改
        if(hospitalExist!=null){
            hospital.setStatus(hospitalExist.getStatus());
            hospital.setCreateTime(hospitalExist.getCreateTime());
            hospital.setUpdateTime(new Date());
            hospital.setIsDeleted(0);
            hospitalRepository.save(hospital);


        }else{
            hospital.setStatus(0);
            hospital.setCreateTime(new Date());
            hospital.setUpdateTime(new Date());
            hospital.setIsDeleted(0);
            hospitalRepository.save(hospital);

        }
        //如果不存在，進行修改


    }

    @Override
    public Hospital getByHoscode(String hoscode) {
        Hospital hospitalByHoscode = hospitalRepository.getHospitalByHoscode(hoscode);
        return hospitalByHoscode;
    }

    //醫院列表(條件查詢分頁)
    @Override
    public Page<Hospital> selectHospPage(Integer page, Integer limit, HospitalQueryVo hospitalQueryVo) {

        //創建pageable對象
        Pageable pageable= PageRequest.of(page-1,limit);
        //創建條件匹配器
        ExampleMatcher matcher=
                ExampleMatcher.matching().
                        withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING)
                .withIgnoreCase(true);
        //hospitalSetQueryVo 對象轉換成 hospital對象
        Hospital hospital=new Hospital();
        BeanUtils.copyProperties(hospitalQueryVo,hospital);

        //創建對象
        Example<Hospital> example=Example.of(hospital,matcher);

        //調用方法實現查詢
        Page<Hospital> pages = hospitalRepository.findAll(example, pageable);

        pages.getContent().stream().forEach(item->{
            this.setHospitalHosType(item);
        });


        return pages;

    }

    //更新醫院狀態
    @Override
    public void updateStatus(String id, Integer status) {
        //根據id查詢醫院信息
        Hospital hospital = hospitalRepository.findById(id).get();
        //設置修改的值
        hospital.setStatus(status);
        hospital.setUpdateTime(new Date());
        hospitalRepository.save(hospital);
    }

    @Override
    public Map<String,Object> getHospById(String id) {
        Map<String,Object> result=new HashMap<>();
        Hospital hospital =
                this.setHospitalHosType(hospitalRepository.findById(id).get());

        //醫院基本信息(包含醫院等級)
        result.put("hospital",hospital);
        result.put("bookingRule",hospital.getBookingRule());
        hospital.setBookingRule(null);
        return result;
    }


    //獲取醫院名稱
    @Override
    public String getHospName(String hoscode) {
        Hospital hospital = hospitalRepository.getHospitalByHoscode(hoscode);
        if(hospital!=null){
            return hospital.getHosname();
        }
        return null;
    }


    //根據醫院名稱查詢
    @Override
    public List<Hospital> findByHosname(String hosname) {
        return hospitalRepository.findHospitalByHosnameLike(hosname);
    }

    //獲取查詢list集合，便利進行醫院等級封裝
    private Hospital setHospitalHosType(Hospital hospital) {
        //根據dictCode和value獲取醫院等級名稱
        String hostypeString = dictFeignClient.getName3("Hostype", hospital.getHostype());
        //查詢 省 市 地區
        String provinceString = dictFeignClient.getName(hospital.getProvinceCode());
        String citysString = dictFeignClient.getName(hospital.getCityCode());
        String DistrictString = dictFeignClient.getName(hospital.getDistrictCode());


        hospital.getParam().put("fullAddress",provinceString+citysString+DistrictString);
        hospital.getParam().put("hostypeString",hostypeString);
        return hospital;
    }
}
