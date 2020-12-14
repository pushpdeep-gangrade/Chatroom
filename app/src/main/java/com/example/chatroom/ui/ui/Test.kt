//package com.example.chatroom.ui.ui
//
//import cz.msebera.android.httpclient.client.HttpClient
//import cz.msebera.android.httpclient.client.methods.HttpPost
//import cz.msebera.android.httpclient.client.utils.URIBuilder
//import cz.msebera.android.httpclient.entity.ByteArrayEntity
//import cz.msebera.android.httpclient.entity.StringEntity
//import cz.msebera.android.httpclient.impl.client.HttpClients
//import cz.msebera.android.httpclient.util.EntityUtils
//
//
//object Test {
//    @JvmStatic
//    fun main(args: Array<String>) {
//        val httpclient: HttpClient =
//            HttpClients.createDefault()
//        try {
//            val builder =
//                URIBuilder("https://amad-vision-api-image-to-text.cognitiveservices.azure.com/vision/v3.1/ocr")
//
//            //    builder.setParameter("language", "unk");
//            builder.setParameter("detectOrientation", "true")
//            val uri = builder.build()
//            val request = HttpPost(uri)
//            request.setHeader("Content-Type", "application/octet-stream")
//            request.setHeader("Ocp-Apim-Subscription-Key", "{subscription key}")
//            // Request body
//            val entity = ByteArrayEntity(baos.toByteArray())
//            val reqEntity = StringEntity("{body}")
//            request.entity = reqEntity
//            val response = httpclient.execute(request)
//            val entity = response.entity
//            if (entity != null) {
//                println(EntityUtils.toString(entity))
//            }
//        } catch (e: Exception) {
//            println(e.message)
//        }
//
//    }
//}