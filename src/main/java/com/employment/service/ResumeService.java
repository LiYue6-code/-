package com.employment.service;

import com.employment.common.BizException;
import com.employment.entity.JobIntention;
import com.employment.entity.Resume;
import com.employment.mapper.JobIntentionMapper;
import com.employment.mapper.ResumeMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 学生简历与求职意向业务。
 */
@Service
public class ResumeService {

    private final ResumeMapper resumeMapper;
    private final JobIntentionMapper intentionMapper;

    public ResumeService(ResumeMapper resumeMapper, JobIntentionMapper intentionMapper) {
        this.resumeMapper = resumeMapper;
        this.intentionMapper = intentionMapper;
    }

    public List<Resume> list(Long studentId) {
        return resumeMapper.findByStudent(studentId);
    }

    public Resume get(Long id) {
        return resumeMapper.findById(id);
    }

    public Resume getDefault(Long studentId) {
        return resumeMapper.findDefault(studentId);
    }

    /** 校验并规范化简历，返回错误提示（null 表示通过）。 */
    public String validate(Resume resume) {
        if (resume.getTitle() == null || resume.getTitle().isBlank()) {
            return "简历标题不能为空";
        }
        if (resume.getTitle().trim().length() > 100) {
            return "简历标题不能超过 100 个字符";
        }
        if (resume.getSkills() != null && resume.getSkills().trim().length() > 500) {
            return "技能标签不能超过 500 个字符";
        }
        return null;
    }

    /** 新增简历：首份简历自动设为默认；勾选默认时先清除其余默认标记。 */
    public void create(Resume resume) {
        boolean first = resumeMapper.findByStudent(resume.getStudentId()).isEmpty();
        boolean asDefault = resume.isDefaultFlag() || first;
        resume.setDefaultFlag(asDefault);
        if (asDefault) {
            resumeMapper.clearDefault(resume.getStudentId());
        }
        resumeMapper.insert(resume);
    }

    /** 更新简历（含归属校验）。 */
    public void update(Long id, Long studentId, Resume form) {
        Resume existing = owned(id, studentId);
        form.setId(existing.getId());
        form.setStudentId(existing.getStudentId());
        form.setDefaultFlag(existing.isDefaultFlag());
        resumeMapper.update(form);
    }

    /** 删除简历；若删除的是默认简历，则把剩余第一份设为默认。 */
    public void delete(Long id, Long studentId) {
        owned(id, studentId);
        resumeMapper.deleteById(id);
        if (resumeMapper.findDefault(studentId) == null) {
            List<Resume> rest = resumeMapper.findByStudent(studentId);
            if (!rest.isEmpty()) {
                resumeMapper.setDefault(rest.get(0).getId(), studentId);
            }
        }
    }

    public void setDefault(Long id, Long studentId) {
        owned(id, studentId);
        resumeMapper.clearDefault(studentId);
        resumeMapper.setDefault(id, studentId);
    }

    public JobIntention getIntention(Long studentId) {
        return intentionMapper.findByStudent(studentId);
    }

    public void saveIntention(JobIntention intention) {
        intentionMapper.save(intention);
    }

    /** 校验简历归属。 */
    private Resume owned(Long id, Long studentId) {
        Resume resume = resumeMapper.findById(id);
        if (resume == null || studentId == null || !studentId.equals(resume.getStudentId())) {
            throw new BizException("简历不存在或无权操作");
        }
        return resume;
    }
}