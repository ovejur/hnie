package cn.edu.hnie.zyjh.function.controller;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.plugins.Page;

import cn.edu.hnie.common.annotation.Log;
import cn.edu.hnie.common.excel.imports.result.ExcelImportResult;
import cn.edu.hnie.common.utils.Query;
import cn.edu.hnie.common.utils.R;
import cn.edu.hnie.common.utils.ShiroUtils;
import cn.edu.hnie.zyjh.base.BatchBaseController;
import cn.edu.hnie.zyjh.function.entity.InfCompanyEmployee;
import cn.edu.hnie.zyjh.function.service.InfCompanyEmployeeService;

@RestController
@RequestMapping("/company/employee")
public class CompanyEmployeeController extends BatchBaseController {
	/**
	 * <查询企业员工信息>
	 * 
	 * @param params 查询条件
	 * @return 员工列表，同时返回分页参数
	 */
	@Autowired
	private InfCompanyEmployeeService employeeService;
	@RequestMapping("/list")
	@RequiresPermissions("company:employee:list")
	public R getCompanyEmployeeList(@RequestParam Map<String, Object> params) {
		
		Long deptId = ShiroUtils.getUser().getDeptId();
		if(deptId!=1L){
			params.put("deptId", deptId);
		}
		// 1.查询条件，从前端传入
		 Query query = new Query(params);
		// 2. 分页参数，查询企业人员表。注意只查询当前用户归宿的部门签约企业的员工
		// 查询的时候最少要带上学年，后端在基类中获取部门标识，判断是否是一级部门，如果是不需作为查询条件，否则必须作为查询条件
		// 学校管理人员，学院管理人员，企业导师，企业管理人员，学校导师可能查询企业员工信息
		 Page<InfCompanyEmployee> pageUtil = new Page<InfCompanyEmployee>(query.getPage(), query.getLimit());

		// 3.调用service层，根据条件查询企业员工信息，分页查询
		 pageUtil = employeeService.getEmployeeList(pageUtil, params);

		// 4.把分页参数传回前端
		return R.ok().put("page", pageUtil);

		//return null;
	}

	/**
	 * <批量导入学生配置>
	 * 
	 * @throws Exception
	 */
	@Log("批量导入企业员工")
	@RequestMapping("/batch")
	public R batch(HttpServletRequest request) throws Exception {
		// 1 调用基类的上传方法，把上传的excel转换成指定模版的对象
		// 1.1 获取经过转换的对象，catch下，看是否有异常，有异常的话转换成消息，返回到前端
		// 1.2第二参数是文件模版中的Id,因为一个模版中可以映射成多个excel
		ExcelImportResult result = super.fileUpload(request, "comEmployee");
		List<InfCompanyEmployee> listBean = result.getListBean();
		employeeService.addEmployeeBatch(listBean);

		// 2. 调用service层新增接口(批量接口，需要重新定义)
		// 增加企业人员不需要增加用户和配置权限
		// 注意type=1

		// 返回正确的的结果
		return R.ok();
	}
	
	/**
	 * <下载批量导入企业信息的excel模版>
	 * 
	 * @throws Exception
	 */
	@RequestMapping("/download")
	public ResponseEntity<byte[]> download(HttpServletRequest request) throws Exception {
		return super.downLoad(request, "employee");
	}
	
	/**
	 * <校验导入的excel是否与模版匹配>
	 * 只校验表头，不校验内容
	 * 
	 * @throws Exception
	 * 
	 */
	@RequestMapping(value = "/check", method = RequestMethod.POST)
	public R check(HttpServletRequest request) throws Exception {

		super.check(request, "employee");
		return R.ok();
	}

	/**
	 * <保存 系统配置>
	 * 
	 * @param systemConfig
	 */
	@Log("保存企业员工配置")
	@RequestMapping("/save")
	@RequiresPermissions("company:employee:save")
	public R save(@RequestBody InfCompanyEmployee infCompanyEmployee) {
		// 1.判断主键是否存在，存在就是修改，否则就是新增
		Long id = infCompanyEmployee.getTeacherId();
		if(id==null){
			//添加
			employeeService.addEmployee(infCompanyEmployee);
		}else{
			//修改
			employeeService.updateById(infCompanyEmployee);
		}
		// 2. 调用service层的修改或者新增接口
		// 2.1 employee_id系统自动生成，且需要提交前获取
		// 2.2 employee_code，由前缀+employee_id，该Id作为用户的userId和初始化密码
		// 3.在新增企业员工时，需要为企业员工增加用户角色、新增权限，使用默认的用户名和密码
		// 权限需要查询权限配置表，根据类型读取权限id(user_role_def)，涉及到的表有sys_user,sys_user_role
		

		// 返回正确的的结果
		return R.ok();
	}

	/**
	 * <删除企业员工>
	 * 
	 */
	@Log("删除企业员工")
	@RequestMapping("/delete")
	@RequiresPermissions("company:employee:delete")
	public R deleteByIds(@RequestBody Long[] ids) {

		// 1.调用后端的修改接口，删除只修改员工的状态为无效
		// 2.支持同时删除多个员工，由前端控制传入一个id数组
		// sysConfigSrv.deleteBatchIds(Arrays.asList(ids));

		// 1.1 企业员工的角色如果是管理人员，需要删除权限(用户和角色都需要生存)

		// 返回正确的的结果
		return R.ok();
	}
	
	/*
	 * 参数为:学年的id
	 */
	@Log("统计企业导师的人数")
	@RequestMapping("count")
	public R getCompanyCount(@RequestParam Map<String, Object> param){
		
		//调用这个方法的是 	教务处管理员 	学生 	校内导师 	学院管理员
		Long deptId = ShiroUtils.getUser().getDeptId();
		Integer count = 0;
		if(deptId==1L){
			count = employeeService.getTotalCount(param);
		}else{
			param.put("deptId",deptId);
			count = employeeService.getCompanyEmpCooperWithDept(param);
		}
		
		return R.ok().put("msg", count);
	}
	
	
}
