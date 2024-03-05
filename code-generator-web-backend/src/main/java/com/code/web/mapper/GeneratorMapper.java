package com.code.web.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.code.web.model.entity.Generator;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author liang
 * @description 针对表【generator(代码生成器)】的数据库操作Mapper
 * @createDate 2024-02-27 11:46:40
 * @Entity generator.domain.Generator
 */
public interface GeneratorMapper extends BaseMapper<Generator> {

    @Select("SELECT id, distPath FROM generator WHERE isDelete = 1")
    List<Generator> listDeletedGenerator();
}




