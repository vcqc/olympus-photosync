package org.mauritania.photosync.olympus.client

import java.io.{File, FileOutputStream}
import java.net.URL
import java.nio.channels.Channels
import org.slf4j.LoggerFactory
import scala.io.{BufferedSource, Source}

class CameraClient(
  configuration: CameraClientConfig
) {

  val logger = LoggerFactory.getLogger(this.getClass)

  def listFiles(): List[(String, Long)] = {
    logger.debug("listFiles")
    generateFilesListFromHtml(
      Source.fromURL(new URL(
        configuration.serverProtocol,
        configuration.serverName,
        configuration.serverPort,
        generateRelativeUrl)))
  }

  def downloadFile(fileId: String, localDestination: File) {
    logger.debug("downloadFile")
    val urlSourceFile = new URL(
      configuration.serverProtocol,
      configuration.serverName,
      configuration.serverPort,
      generateRelativeUrl.concat(fileId))
    val rbc = Channels.newChannel(urlSourceFile.openStream());
    val fos = new FileOutputStream(new File(localDestination, fileId));
    fos.getChannel().transferFrom(rbc, 0, Long.MaxValue);
  }

  private def generateFilesListFromHtml(html: BufferedSource): List[(String, Long)] = {
    val htmlLines = html.getLines()
    val fileIdsAndSize = htmlLines.flatMap(
      htmlLineToBeParsed =>
        htmlLineToBeParsed match {
          case configuration.fileRegex(fileId, fileSizeBytes) => Some((fileId, fileSizeBytes.toLong))
          case _ => None
        }
    ).toList

    fileIdsAndSize
  }

  private def generateRelativeUrl: String = {
    configuration.serverBaseUrl + "/" + configuration.serverFolderName + "/"
  }

}
