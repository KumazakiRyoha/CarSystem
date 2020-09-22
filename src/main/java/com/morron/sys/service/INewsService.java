package com.morron.sys.service;

import com.morron.sys.domain.News;
import com.morron.sys.utils.DataGridView;
import com.morron.sys.vo.NewsVo;

public interface INewsService {

    /**
     * 查询所有公告
     * @param newsVo
     * @return
     */
    public DataGridView queryAllNews(NewsVo newsVo);

    /**
     * 添加公告
     * @param newsVo
     */
    public void addNews(NewsVo newsVo);

    /**
     * 删除公告
     * @param newsid
     */
    public void deleteNews(Integer newsid);

    /**
     * 批量删除公告
     * @param ids
     */
    public void deleteBatchNews(Integer[] ids);

    /**
     * 更新公告
     * @param newsVo
     */
    public void updateNews(NewsVo newsVo);

    /**
     * 通过id查询一条公告
     * @param id
     * @return
     */
    News queryNewsById(Integer id);

}
