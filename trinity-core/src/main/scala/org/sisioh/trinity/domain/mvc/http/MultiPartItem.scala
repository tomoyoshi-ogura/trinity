package org.sisioh.trinity.domain.mvc.http

import java.io._
import org.jboss.netty.buffer.{ChannelBuffer => NettyChannelBuffer}
import org.jboss.netty.handler.codec.http.multipart.{MixedFileUpload, HttpPostRequestDecoder}
import org.sisioh.scala.toolbox.Loan._
import org.sisioh.trinity.domain.io.buffer.ChannelBuffer
import org.sisioh.trinity.domain.io.http.{Request => IORequest}
import scala.collection.JavaConversions._
import scala.concurrent._

/**
 * マルチパートアイテムを表現する値オブジェクト。
 *
 * @param mixedFileUpload [[org.jboss.netty.handler.codec.http.multipart.MixedFileUpload]]
 */
case class MultiPartItem(mixedFileUpload: MixedFileUpload, ioChunkSize: Int = 1024) {

  val data: ChannelBuffer = mixedFileUpload.getChannelBuffer

  val name = mixedFileUpload.getName

  val contentType = mixedFileUpload.getContentType

  val fileName = mixedFileUpload.getFilename

  def writeToFile(path: String)(implicit executor: ExecutionContext): Future[Unit] = future {
    using(new FileOutputStream(path)) {
      fis =>
        val netty: NettyChannelBuffer = data
        while (netty.readable()) {
          netty.readBytes(fis, ioChunkSize)
        }
    }.get
  }

}

/**
 * コンパニオンオブジェクト。
 */
object MultiPartItem {

  /**
   * リクエストから[[org.sisioh.trinity.domain.mvc.http.MultiPartItem]]のマップを取得する。
   *
   * @param request `com.twitter.finagle.http.Request`
   * @return [[org.sisioh.trinity.domain.mvc.http.MultiPartItem]]のマップ
   */
  def apply(request: Request): Map[String, MultiPartItem] = {
    val httpPostRequestDecoder = new HttpPostRequestDecoder(request.underlying.netty)
    if (httpPostRequestDecoder.isMultipart) {
      httpPostRequestDecoder.getBodyHttpDatas.map {
        data =>
          data.getName -> MultiPartItem(data.asInstanceOf[MixedFileUpload])
      }.toMap
    } else Map.empty[String, MultiPartItem]
  }

}