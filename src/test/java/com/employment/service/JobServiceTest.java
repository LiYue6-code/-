package com.employment.service;

import com.employment.common.BizException;
import com.employment.common.PageResult;
import com.employment.entity.Job;
import com.employment.mapper.JobMapper;
import com.employment.query.JobQuery;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 学生端岗位查询业务单元测试：分页、空结果、详情校验、下架。
 */
@ExtendWith(MockitoExtension.class)
class JobServiceTest {

    @Mock
    private JobMapper jobMapper;

    @InjectMocks
    private JobService jobService;

    private JobQuery query(int page) {
        JobQuery q = new JobQuery();
        q.setPage(page);
        q.setSize(10);
        return q;
    }

    @Test
    @DisplayName("岗位检索：无匹配记录时返回空页，不再查询列表")
    void searchReturnsEmptyPage() {
        JobQuery q = query(2);
        when(jobMapper.count(q)).thenReturn(0L);

        PageResult<Job> page = jobService.search(q);

        assertEquals(0, page.getItems().size());
        assertEquals(2, page.getPage());
        assertEquals(0, page.getTotal());
        verify(jobMapper, never()).search(any());
    }

    @Test
    @DisplayName("岗位检索：按总数计算总页数并返回当页数据")
    void searchSplitsPages() {
        JobQuery q = query(1);
        Job job = new Job();
        job.setId(1L);
        job.setTitle("Java 开发工程师");
        when(jobMapper.count(q)).thenReturn(25L);
        when(jobMapper.search(q)).thenReturn(List.of(job));

        PageResult<Job> page = jobService.search(q);

        assertEquals(1, page.getItems().size());
        assertEquals(25, page.getTotal());
        assertEquals(3, page.getTotalPages());
    }

    @Test
    @DisplayName("岗位详情：岗位不存在时抛出业务异常")
    void getForStudentThrowsWhenMissing() {
        when(jobMapper.findById(9L)).thenReturn(null);

        assertThrows(BizException.class, () -> jobService.getForStudent(9L));
    }

    @Test
    @DisplayName("下架岗位：状态改为已截止并写库")
    void offlineMarksJobClosed() {
        Job job = new Job();
        job.setId(1L);
        job.setTitle("测试工程师");
        job.setStatus("招聘中");
        when(jobMapper.findById(1L)).thenReturn(job);

        jobService.offline(1L);

        assertEquals("已截止", job.getStatus());
        verify(jobMapper).update(job);
    }

    @Test
    @DisplayName("下架岗位：岗位不存在时不做任何写入")
    void offlineIgnoresMissingJob() {
        when(jobMapper.findById(9L)).thenReturn(null);

        jobService.offline(9L);

        verify(jobMapper, never()).update(any());
    }
}
