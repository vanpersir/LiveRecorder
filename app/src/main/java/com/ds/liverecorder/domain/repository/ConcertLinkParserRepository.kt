package com.ds.liverecorder.domain.repository

import com.ds.liverecorder.domain.model.Concert

/**
 * 演出链接解析仓库接口
 * 定义链接解析的相关操作
 */
interface ConcertLinkParserRepository {
    
    /**
     * 解析链接并返回演出信息
     * @param link 链接文本
     * @return 解析出的演出信息
     */
    suspend fun parseLink(link: String): Concert?
}