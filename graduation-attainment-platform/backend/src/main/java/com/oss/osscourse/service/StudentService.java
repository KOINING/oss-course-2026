package com.oss.osscourse.service;

import com.oss.osscourse.common.PageResult;
import com.oss.osscourse.dto.student.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface StudentService {

    /**
     * 查询学生列表
     * @param request 查询条件
     * @return 学生列表
     */
    List<StudentResponse> listStudents(StudentQueryRequest request);

    /**
     * 查询学生下拉列表（供选择使用）
     * @return 学生列表
     */
    List<StudentResponse> listStudentsForSelect();

    /**
     * 查询学生入学年份下拉选项
     * @return 数据库中已有学生入学年份列表
     */
    List<Integer> listEnrollmentYears();

    /**
     * 批量导入学生基础信息
     * @param file 导入文件
     * @return 导入结果
     */
    StudentImportResult importStudents(MultipartFile file);

    /**
     * 根据ID查询学生详情
     * @param studentId 学生ID
     * @return 学生详情
     */
    StudentResponse getStudentById(Long studentId);

    /**
     * 新增或更新学生
     * @param request 保存请求
     */
    void saveStudent(StudentSaveRequest request);

    /**
     * 更新学生状态
     * @param request 状态更新请求
     */
    void updateStudentStatus(StudentStatusRequest request);

    /**
     * 分页查询学生列表
     * @param request 查询条件（含分页参数）
     * @return 分页结果
     */
    PageResult<StudentResponse> listStudentsByPage(StudentQueryRequest request);

    /**
     * 删除学生
     * @param studentId 学生ID
     */
    void deleteStudent(Long studentId);
}
