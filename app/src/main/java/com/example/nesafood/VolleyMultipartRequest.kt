package com.example.nesafood

import com.android.volley.NetworkResponse
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.HttpHeaderParser
import com.android.volley.toolbox.Volley
import java.io.ByteArrayOutputStream
import java.io.IOException

open class VolleyMultipartRequest(
    method: Int,
    url: String,
    private val mListener: Response.Listener<NetworkResponse>,
    private val mErrorListener: Response.ErrorListener
) : Request<NetworkResponse>(method, url, mErrorListener) {

    override fun getBodyContentType() = "multipart/form-data;boundary=$boundary"

    private val boundary = "apiclient-${System.currentTimeMillis()}"

    override fun getBody(): ByteArray {
        val bos = ByteArrayOutputStream()
        try {
            val data = getByteData()
            if (data != null && data.isNotEmpty()) {
                for ((key, value) in data) {
                    bos.write(("--$boundary\r\n").toByteArray())
                    bos.write(("Content-Disposition: form-data; name=\"$key\"; filename=\"${value.fileName}\"\r\n").toByteArray())
                    bos.write("Content-Type: image/jpeg\r\n\r\n".toByteArray())
                    bos.write(value.content)
                    bos.write("\r\n".toByteArray())
                }
            }
            val params = getParams()
            if (params != null && params.isNotEmpty()) {
                for ((key, value) in params) {
                    bos.write(("--$boundary\r\n").toByteArray())
                    bos.write("Content-Disposition: form-data; name=\"$key\"\r\n\r\n".toByteArray())
                    bos.write(value.toByteArray())
                    bos.write("\r\n".toByteArray())
                }
            }
            bos.write(("--$boundary--\r\n".toByteArray()))
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return bos.toByteArray()
    }

    override fun parseNetworkResponse(response: NetworkResponse): Response<NetworkResponse> {
        return Response.success(response, HttpHeaderParser.parseCacheHeaders(response))
    }

    override fun deliverResponse(response: NetworkResponse) = mListener.onResponse(response)

    open fun getByteData(): Map<String, DataPart>? = null

    data class DataPart(val fileName: String, val content: ByteArray)
}
