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
     * @param sharedText 分享文本，其中包含 演出链接
     * @return 解析出的演出信息，如果无法解析则返回null
     */
    suspend operator fun invoke(sharedText: String): Concert? {
        // 使用正则表达式从sharedText中提取URL
        val urlRegex = Regex("https?://[\\w.-]+[\\w/?&=%.-]*")
        val matchResult = urlRegex.find(sharedText)
        val link = matchResult?.value

        return if (link != null) {
            concertLinkParserRepository.parseLink(link)
        } else {
            null
        }
    }
}