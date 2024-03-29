package com.javdoe

import android.util.Log
import com.lagradost.cloudstream3.app
import com.lagradost.cloudstream3.utils.ExtractorApi
import com.lagradost.cloudstream3.utils.ExtractorLink
import com.lagradost.cloudstream3.utils.Qualities
import com.lagradost.cloudstream3.utils.getQualityFromName
import java.net.URI

open class DoodJav : ExtractorApi() {
    override var name = "DoodStream"
    override var mainUrl = "https://d000d.com"
    override val requiresReferer = true

    override suspend fun getUrl(url: String, referer: String?): List<ExtractorLink>? {
        val changedurl=url.replace("dooood.com","d000d.com")
        val response0 = app.get(changedurl).text // html of DoodStream page to look for /pass_md5/...
        val md5 =mainUrl+(Regex("/pass_md5/[^']*").find(response0)?.value ?: return null)  // get https://dood.ws/pass_md5/...
        val trueUrl = app.get(md5, referer = url).text + "zUEJeL3mUN?token=" + md5.substringAfterLast("/")   //direct link to extract  (zUEJeL3mUN is random)
        val quality = Regex("\\d{3,4}p").find(response0.substringAfter("<title>").substringBefore("</title>"))?.groupValues?.get(0)
        return listOf(
            ExtractorLink(
                this.name,
                this.name,
                trueUrl,
                mainUrl,
                getQualityFromName(quality),
                false
            )
        ) // links are valid in 8h

    }
}

open class Streamwish : ExtractorApi() {
    override var name = "Streamwish"
    override var mainUrl = "https://streamwish.to"
    override val requiresReferer = false

    override suspend fun getUrl(url: String, referer: String?): List<ExtractorLink>? {
        val responsecode=app.get(url)
        if (responsecode.code==200) {
            val serverRes = responsecode.document
            //Log.d("Test12","$serverRes")
            val script = serverRes.selectFirst("script:containsData(sources)")?.data().toString()
            //Log.d("Test12","$script")
            val headers = mapOf(
                "Accept" to "*/*",
                "Connection" to "keep-alive",
                "Sec-Fetch-Dest" to "empty",
                "Sec-Fetch-Mode" to "cors",
                "Sec-Fetch-Site" to "cross-site",
                "Origin" to url,
            )
            Regex("file:\"(.*)\"").find(script)?.groupValues?.get(1)?.let { link ->
                Log.d("Test9876", link)
                return listOf(
                    ExtractorLink(
                        this.name,
                        this.name,
                        link,
                        referer ?: "",
                        getQualityFromName(""),
                        URI(link).path.endsWith(".m3u8"),
                        headers
                    )
                )
            }
        }
        return null
    }
}

open class Vidhidepro : ExtractorApi() {
    override val name = "Vidhidepro"
    override val mainUrl = "https://vidhidepro.com"
    override val requiresReferer = false

    override suspend fun getUrl(
        url: String,
        referer: String?,
    ): List<ExtractorLink>? {
        val response =app.get(url).document
        //val response = app.get(url, referer = referer)
        val script = response.selectFirst("script:containsData(sources)")?.data().toString()
        //Log.d("Test9871",script)
        Regex("sources:.\\[.file:\"(.*)\".*").find(script)?.groupValues?.get(1)?.let { link ->
            //Log.d("Test9876",link)
            if (link.contains("m3u8"))
                return listOf(
                    ExtractorLink(
                        source = name,
                        name = name,
                        url = link,
                        referer = referer ?: "$mainUrl/",
                        quality = Qualities.Unknown.value,
                        isM3u8 = true
                    )
                )
        }
        return null
    }
}