package com.atguigu.yygh.hosp.controller.Api;


import com.atguigu.yygh.common.exception.YyghException;
import com.atguigu.yygh.common.helper.HttpRequestHelper;
import com.atguigu.yygh.common.result.Result;
import com.atguigu.yygh.common.result.ResultCodeEnum;
import com.atguigu.yygh.common.utils.MD5;
import com.atguigu.yygh.hosp.service.DepartmentService;
import com.atguigu.yygh.hosp.service.HospitalService;
import com.atguigu.yygh.hosp.service.HospitalSetService;
import com.atguigu.yygh.hosp.service.ScheduleService;
import com.atguigu.yygh.model.hosp.Department;
import com.atguigu.yygh.model.hosp.Hospital;
import com.atguigu.yygh.model.hosp.Schedule;
import com.atguigu.yygh.vo.hosp.DepartmentQueryVo;

import com.atguigu.yygh.vo.hosp.ScheduleQueryVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/api/hosp")
public class ApiController {

    @Autowired
    private HospitalService hospitalService;

    @Autowired
    private HospitalSetService hospitalSetService;

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private ScheduleService scheduleService;


    //刪除排班
    @PostMapping("schedule/remove")
    public Result remove(HttpServletRequest request){
        //獲取傳遞過來的醫院信息
        Map<String, String[]> requestMap = request.getParameterMap();
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(requestMap);
        //獲取醫院編號 和 排班編號
        String hoscode=(String)paramMap.get("hoscode");
        String hosScheduleId=(String)paramMap.get("hosScheduleId");

        //TODO 簽名校驗

        scheduleService.remove(hoscode,hosScheduleId);
        return Result.ok();

    }


    //查詢排班接口 *******
    @PostMapping("schedule/list")
    public Result findSchedule(HttpServletRequest request){
        //獲取傳遞過來的醫院信息
        Map<String, String[]> requestMap = request.getParameterMap();
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(requestMap);


        //獲取醫院編號
        String hoscode=(String)paramMap.get("hoscode");

        //獲取科室編號
        String depcode=(String)paramMap.get("depcode");

        //當前頁 和 每頁紀錄數
        int page= StringUtils.isEmpty(paramMap.get("page"))?1:Integer.parseInt((String)paramMap.get("page"));
        int limit= StringUtils.isEmpty(paramMap.get("limit"))?1:Integer.parseInt((String)paramMap.get("limit"));


        ScheduleQueryVo scheduleQueryVo=new ScheduleQueryVo();
        scheduleQueryVo.setHoscode(hoscode);
        scheduleQueryVo.setDepcode(depcode);

        //調用Service方法
        Page<Schedule> pageModel= scheduleService.findPageSchedule(page,limit,scheduleQueryVo);
        return Result.ok(pageModel);

    }


    //上傳排班接口
    @PostMapping("saveSchedule")
    public Result saveSchedule(HttpServletRequest request){
        //獲取傳遞過來的醫院信息
        Map<String, String[]> requestMap = request.getParameterMap();
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(requestMap);

        //簽名校驗

        scheduleService.save2(paramMap);
        return Result.ok();


    }

    //刪除科室接口
    @PostMapping("department/remove")
    public Result removeDepartment(HttpServletRequest request){
        //獲取傳遞過來的醫院信息
        Map<String, String[]> requestMap = request.getParameterMap();
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(requestMap);
        //醫院編號 和 科室編號
        String hoscode=(String)paramMap.get("hoscode");
        String depcode=(String)paramMap.get("depcode");

        //簽名校驗

        departmentService.remove(hoscode,depcode);
        return Result.ok();


    }
    //查詢科室接口
    @PostMapping("department/list")
    public Result findDepartment(HttpServletRequest request){

        //獲取傳遞過來的醫院信息
        Map<String, String[]> requestMap = request.getParameterMap();
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(requestMap);

        //獲取醫院編號
        String hoscode=(String)paramMap.get("hoscode");
        //當前頁 和 每頁紀錄數
        int page= StringUtils.isEmpty(paramMap.get("page"))?1:Integer.parseInt((String)paramMap.get("page"));
        int limit= StringUtils.isEmpty(paramMap.get("limit"))?1:Integer.parseInt((String)paramMap.get("limit"));


        DepartmentQueryVo departmentQueryVo=new DepartmentQueryVo();
        departmentQueryVo.setHoscode(hoscode);

        //調用Service方法
       Page<Department> pageModel= departmentService.findPageDepartment(page,limit,departmentQueryVo);
       return Result.ok(pageModel);


    }
    //上傳科室接口
    @PostMapping("saveDepartment")
    public Result saveDepartment(HttpServletRequest request){
        //獲取傳遞過來的醫院信息
        Map<String, String[]> requestMap = request.getParameterMap();
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(requestMap);

        //獲取醫院編號
        String hoscode=(String) paramMap.get("hoscode");

        //1.獲取醫院系統傳遞過來的簽名，簽名進行MD5加密
        String hospSign = (String)paramMap.get("sign");

        //2.根據傳遞過來醫院編號，查詢數據庫，查詢簽名
        String signKey= hospitalSetService.getSignKey1(hoscode);


        //3.把數據庫查詢的簽名進行MD5加密
        String signKeyMD5 = MD5.encrypt(signKey);

        //4.判斷簽名是否一致
        if(!hospSign.equals(signKeyMD5)){
            throw new YyghException(ResultCodeEnum.SIGN_ERROR);

        }

        //調用Service裡的方法
        departmentService.save1(paramMap);
        return Result.ok();


    }

    //查詢醫院接口
    @PostMapping("hospital/show")
    public Result getHospital(HttpServletRequest request){
        //獲取傳遞過來的醫院信息
        Map<String, String[]> requestMap = request.getParameterMap();
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(requestMap);

        //獲取醫院編號
        String hoscode=(String) paramMap.get("hoscode");

        //1.獲取醫院系統傳遞過來的簽名，簽名進行MD5加密
        String hospSign = (String)paramMap.get("sign");

        //2.根據傳遞過來醫院編號，查詢數據庫，查詢簽名
        String signKey= hospitalSetService.getSignKey1(hoscode);


        //3.把數據庫查詢的簽名進行MD5加密
        String signKeyMD5 = MD5.encrypt(signKey);

        //4.判斷簽名是否一致
       /* if(!hospSign.equals(signKeyMD5)){
            throw new YyghException(ResultCodeEnum.SIGN_ERROR);

        }*/
        //調用service方法實現根據醫院編號查詢
        Hospital hospital=hospitalService.getByHoscode(hoscode);
        return Result.ok(hospital);

    }


    //上傳醫院接口
    
    @PostMapping("saveHospital")
    public Result saveHosp(HttpServletRequest request){
        //獲取傳遞過來的醫院信息
        Map<String, String[]> requestMap = request.getParameterMap();
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(requestMap);

        //1.獲取醫院系統傳遞過來的簽名，簽名進行MD5加密
        String hospSign = (String)paramMap.get("sign");

        //2.根據傳遞過來醫院編號，查詢數據庫，查詢簽名
        String hoscode = (String)paramMap.get("hoscode");
        String signKey= hospitalSetService.getSignKey1(hoscode);


        //3.把數據庫查詢的簽名進行MD5加密
        String signKeyMD5 = MD5.encrypt(signKey);

        //4.判斷簽名是否一致
       /* if(!hospSign.equals(signKeyMD5)){
            throw new YyghException(ResultCodeEnum.SIGN_ERROR);

        }*/

        //傳輸過程中"+"轉換為了" "，因此我們要轉換回來
        String logoData = (String)paramMap.get("logoData");
        logoData=logoData.replaceAll(" ","+");
        paramMap.put("logoData",logoData);

        //調用service方法
        hospitalService.save(paramMap);
        return Result.ok();
    }
    
}
