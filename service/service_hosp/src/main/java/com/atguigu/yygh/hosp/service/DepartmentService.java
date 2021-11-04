package com.atguigu.yygh.hosp.service;

import com.atguigu.yygh.model.hosp.Department;
import com.atguigu.yygh.vo.hosp.DepartmentQueryVo;
import com.atguigu.yygh.vo.hosp.DepartmentVo;
import org.springframework.data.domain.Page;


import java.util.List;
import java.util.Map;

public interface DepartmentService {
    void save1(Map<String, Object> paramMap);

    //查詢科室接口
    Page<Department> findPageDepartment(int page, int limit, DepartmentQueryVo departmentQueryVo);

    //刪除科室接口
    void remove(String hoscode, String depcode);

    //根據醫院邊號，查詢醫院所有科室列表
    List<DepartmentVo> findDeptTree(String hoscode);

    Object getDepName(String hoscode, String depcode);
}
