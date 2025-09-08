package com.ds.liverecorder.domain.usecase

import com.ds.liverecorder.domain.model.Concert
import com.ds.liverecorder.domain.repository.ConcertLinkParserRepository

/**
 * 解析演出链接用例
 * 负责处理演出链接解析的业务逻辑
 */
class ParseConcertLinkUseCase(
    private val concertLinkParserRepository: ConcertLinkParserRepository
) {
    /**
     * 解析演出链接
     * @param link 演出链接
     * @return 解析出的演出信息，如果无法解析则返回null
     */
    suspend operator fun invoke(link: String): Concert? {
        return concertLinkParserRepository.parseLink(link)
    }
}