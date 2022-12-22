package com.ikurek.android.afpnm.processor

import com.google.firebase.perf.metrics.HttpMetric
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

internal abstract class TracingRequestProcessor {

    abstract val setRequestPayloadSize: Boolean

    abstract val setResponseContentType: Boolean

    abstract val setResponseHttpCode: Boolean

    abstract val setResponsePayloadSize: Boolean

    abstract fun process(chain: Interceptor.Chain): Response

    fun HttpMetric.setBaseMetrics(request: Request, response: Response) {
        if (setRequestPayloadSize) {
            setRequestPayloadSize(request.body?.contentLength() ?: -1)
        }
        if (setResponseHttpCode) {
            setHttpResponseCode(response.code)
        }
        if (setResponseContentType) {
            setResponseContentType(response.body?.contentType().toString())
        }
        if (setResponsePayloadSize) {
            setResponsePayloadSize(response.body?.contentLength() ?: -1)
        }
    }
}