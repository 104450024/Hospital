package com.atguigu.yygh.hosp.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.yygh.hosp.repository.DepartmentRepository;
import com.atguigu.yygh.hosp.service.DepartmentService;
import com.atguigu.yygh.model.hosp.Department;
import com.atguigu.yygh.vo.hosp.DepartmentQueryVo;

import com.atguigu.yygh.vo.hosp.DepartmentVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;


@Service
public class DepartmentServiceImpl implements DepartmentService {

    @Autowired
    private DepartmentRepository departmentRepository;


    //上傳科室接口
    @Override
    public void save1(Map<String, Object> paramMap) {
        //param 轉換成 department 對象
        String paramMapString= JSONObject.toJSONString(paramMap);
        Department department= JSONObject.parseObject(paramMapString,Department.class);

        //根據醫院編號查詢
        Department departmentExist=departmentRepository
                .getDepartmentByHoscodeAndDepcode(department.getHoscode(),department.getDepcode());

        if(departmentExist!=null){
            departmentExist.setUpdateTime(new Date());
            departmentExist.setIsDeleted(0);
            departmentRepository.save(departmentExist);
        }else{
            department.setCreateTime(new Date());
            department.setUpdateTime(new Date());
            department.setIsDeleted(0);
            departmentRepository.save(department);
        }



    }

    @Override
    public Page<Department> findPageDepartment(int page, int limit, DepartmentQueryVo departmentQueryVo) {

        //創建Pageable對象，設置當前頁和每頁紀錄數
        //0是你的第一頁
        Pageable pageable= PageRequest.of(page-1,limit);
        //創建Example對象
        Department department=new Department();
        BeanUtils.copyProperties(departmentQueryVo,department);
        department.setIsDeleted(0);

        ExampleMatcher matcher=ExampleMatcher.matching()
        .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING)
        .withIgnoreCase(true);
        Example<Department> example=Example.of(department,matcher);


        Page<Department> all = departmentRepository.findAll(example, pageable);
        return all;

    }


    //刪除科室接口
    @Override
    public void remove(String hoscode, String depcode) {
        //根據醫院標號 和科室標號查詢
        Department department = departmentRepository.getDepartmentByHoscodeAndDepcode(hoscode, depcode);
        if(department!=null){
            //調用方法刪除
            departmentRepository.deleteById(department.getId());
        }

    }


    //根據醫院邊號，查詢醫院所有科室列表
    @Override
    public List<DepartmentVo> findDeptTree(String hoscode) {

        //創建List集合，用於最終數據封裝
        List<DepartmentVo> result=new ArrayList<>();

        //根據醫院編號，查詢醫院所有科室信息
        Department departmentQuery=new Department();
        departmentQuery.setHoscode(hoscode);
        Example example = Example.of(departmentQuery);
        //所有科室列表 department
        List<Department> departmentList = departmentRepository.findAll(example);

        //根據大科室編號  bigcode 分組，獲取每個大科室裡面下級子科室
        //大科室分類
        Map<String, List<Department>> departmentMap = departmentList.stream().
                collect(Collectors.groupingBy(Department::getBigcode));
        //便利map集合，departmentMap
        for (Map.Entry<String,List<Department>> entry: departmentMap.entrySet()) {

            //大科室編號
            String bigCode = entry.getKey();
            //大科室編號對應的全部數據
            List<Department> department1List = entry.getValue();

            //封裝大科室
            DepartmentVo departmentVo1=new DepartmentVo();
            departmentVo1.setDepcode(bigCode);
            departmentVo1.setDepname(department1List.get(0).getBigname());

            //封裝小科室
            List<DepartmentVo> children =new ArrayList<>();
            for (Department department:department1List){
                DepartmentVo departmentVo2=new DepartmentVo();
                departmentVo2.setDepcode(department.getDepcode());
                departmentVo2.setDepname(department.getDepname());
                //封裝到List集合
                children.add(departmentVo2);
            }
            //把小科室list集合放到大科室children裡面

            departmentVo1.setChildren(children);
            //放到最終result裡面
            result.add(departmentVo1);
        }
        //返回
        return result;
    }

    //根據科室編號，和醫院編號，查詢科室名稱
    @Override
    public Object getDepName(String hoscode, String depcode) {
        Department department= departmentRepository.getDepartmentByHoscodeAndDepcode(hoscode, depcode);

        if(department!=null){
            return department.getDepname();
        }
        return null;
    }
}
