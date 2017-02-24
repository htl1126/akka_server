// Run:
// sbt run
// Requests:
// Upload a file: curl --form "fileUpload=@note" http://localhost:8080/fileUpload

import java.nio.file.Paths
import java.util._
import java.io.File

import scala.util.{Success, Failure}
import scala.collection.mutable.Map
import scala.io

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.FileIO

object DataServer extends App {
  // used to run the actors
  implicit val system = ActorSystem("my-system")
  // materializes underlying flow definition into a set of actors
  implicit val materializer = ActorMaterializer()

  val uploadDirectory = "./upload_files/"
  var fileCount = 0
  var fileRef = Map[Int, (String, String)]()

  def deleteFile(filename: String): Boolean = {new File(filename).delete()}
  def createUploadDir(dirname: String): Unit = {
    val directory = new File(dirname)
    if (! directory.exists())
        directory.mkdir()
  }

  // Simple CSV format validation: check column consistency
  def validateCSV(filepath: String): Boolean = {
    var i = 0
    var num_col = 0
    val bufferedSource = io.Source.fromFile(filepath)
    for(line <- bufferedSource.getLines)
    {
        if (i == 0)
            num_col = line.split(',').length
        else
            if (line.split(',').length != num_col)
                return false
        i += 1
    }
    return true
  }

  def validateJSON(filepath: String): Boolean = {
    return true
  }

  val route =
    path("fileUpload") {
      post {
        fileUpload("fileUpload") {
          case (fileInfo, fileStream) =>
            createUploadDir(uploadDirectory)
            var fileName = fileInfo.fileName
            val fileExt = fileName.substring(fileName.lastIndexOf('.') + 1)
            // Rename the file if a file with same name exists
            // Ex. "a.csv" -> "a_1.csv"
            if ((new File(uploadDirectory + fileName)).exists())
                fileName = fileName.substring(0, fileName.lastIndexOf('.')) + "_1." + fileExt
            val sink = FileIO.toPath(Paths.get(uploadDirectory) resolve fileName)
            val writeResult = fileStream.runWith(sink)
            onSuccess(writeResult) { result =>
              result.status match {
                case Success(_) => 
                  fileRef += (fileCount -> (fileName, fileExt))
                  fileCount += 1
                  // Check validity of JSON/CSV file here
                  if (fileExt == "csv")
                      {
                        if (validateCSV(uploadDirectory + fileName))
                            complete(s"CSV file ${fileName} was uploaded successfully with reference No. ${fileCount - 1}")
                        else
                        {
                          fileRef -= fileCount
                          fileCount -= 1
                          if (deleteFile(uploadDirectory + fileName))
                             complete("You submitted a non-CSV file. It is deleted.")
                          else
                             complete("Invalid file deletion error.")
                        }
                      }
                  else if (fileExt == "json")
                      {
                        if (validateJSON(uploadDirectory + fileName))
                            complete(s"JSON file ${fileName} was uploaded successfully with reference No. ${fileCount - 1}")
                        else
                        {
                          fileRef -= fileCount
                          fileCount -= 1
                          if (deleteFile(uploadDirectory + fileName))
                             complete("You submitted a non-JSON file. It is deleted.")
                          else
                             complete("Invalid file deletion error.")
                        }
                      }
                  else
                  {
                    fileRef -= fileCount
                    fileCount -= 1
                    if (deleteFile(uploadDirectory + fileName))
                        complete("You submitted a non-CSV/JSON file. It is deleted.")
                    else
                        complete("Invalid file deletion error.")
                  }
                case Failure(e) => throw e
              }
            }
        }
      }
    } ~
    pathPrefix("header" / IntNumber) { refId =>
      get {
        if (fileRef.contains(refId))
        {
          val record = fileRef(refId)
          if (record._2 == "csv")
              complete("csv file")
          else
              complete("json file")
        }
        else
            complete("Invalid reference")
      }
    } ~
    path("join") {
      post{ // need to be post
        parameters("tbl1", "tbl2", "column") { (tbl1, tbl2, column) =>
          complete(tbl1 + " " + tbl2 + " " + column)
        }
      }
    }

  // start the server
  val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)

  // wait for the user to stop the server
  println("Press <enter> to exit.")
  Console.in.read.toChar

  // gracefully shut down the server
  import system.dispatcher
  bindingFuture
    .flatMap(_.unbind())
    .onComplete(_ => system.shutdown())
}
