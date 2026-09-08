package org.example.rewardservice.service.Impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.rewardservice.entity.TaskAccept;
import org.example.rewardservice.mapper.TaskAcceptMapper;
import org.example.rewardservice.service.TaskAcceptService;
import org.springframework.stereotype.Service;

@Service
public class TaskAcceptServiceImpl extends ServiceImpl<TaskAcceptMapper, TaskAccept> implements TaskAcceptService {
}
