package cn.edu.hnie.modules.job.service;

import java.util.Map;

import cn.edu.hnie.modules.job.entity.ScheduleJobLog;

import com.baomidou.mybatisplus.plugins.Page;
import com.baomidou.mybatisplus.service.IService;

/**
 * 定时任务日志
 * 
 * @author theodo
 * @email 36780272@qq.com
 * @date 2016年12月1日 下午10:34:48
 */
public interface ScheduleJobLogService extends IService<ScheduleJobLog> {

	Page<ScheduleJobLog> queryPageList(Page<ScheduleJobLog> pageUtil, Map<String, Object> map);

}
