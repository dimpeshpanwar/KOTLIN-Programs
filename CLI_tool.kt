import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*
import javax.imageio.ImageIO
import java.awt.image.BufferedImage


// import org.apache.poi.ss.usermodel.WorkbookFactory
// import org.apache.poi.ss.usermodel.Cell
// import com.itextpdf.text.pdf.PdfReader
// import com.itextpdf.text.pdf.parser.PdfTextExtractor

class FileOperations {
    fun listFiles(directory: String) {
        val dir = File(directory)
        if (dir.exists() && dir.isDirectory) {
            println("Files in $directory:")
            dir.listFiles()?.forEach { println(it.name) }
        } else {
            println("Invalid directory")
        }
    }

    fun copyFile(source: String, destination: String) {
        try {
            Files.copy(Paths.get(source), Paths.get(destination))
            println("File copied successfully")
        } catch (e: Exception) {
            println("Error copying file: ${e.message}")
        }
    }

    fun deleteFile(filePath: String) {
        val file = File(filePath)
        if (file.exists()) {
            if (file.delete()) {
                println("File deleted successfully")
            } else {
                println("Failed to delete the file")
            }
        } else {
            println("File does not exist")
        }
    }
}

class DataExtraction {
     // function to extract content of text file.
    fun extractTextFromFile(filePath: String) {
        try {
            val content = File(filePath).readText()
            println("File content:")
            println(content)
        } catch (e: Exception) {
            println("Error reading file: ${e.message}")
        }
    }

    // Require Libraries to implement
    // library Name - Apache POI
   /* fun extractDataFromExcel(filePath: String) {
        try {
            val workbook = WorkbookFactory.create(File(filePath))
            val sheet = workbook.getSheetAt(0)
            for (row in sheet) {
                for (cell in row) {
                    print("${cell.toString()}\t")
                }
                println()
            }
            workbook.close()
        } catch (e: Exception) {
            println("Error extracting data from Excel: ${e.message}")
        }
    } */

      // Require Library to implement
    // library name - iText
   /* fun extractTextFromPDF(filePath: String) {
        try {
            val reader = PdfReader(filePath)
            val pages = reader.numberOfPages
            for (i in 1..pages) {
                println(PdfTextExtractor.getTextFromPage(reader, i))
            }
            reader.close()
        } catch (e: Exception) {
            println("Error extracting text from PDF: ${e.message}")
        }
    }*/

}

class ImageOperations {
    fun convertImageFormat(inputPath: String, outputPath: String, format: String) {
        try {
            val inputImage = ImageIO.read(File(inputPath))
            val outputFile = File(outputPath)
            ImageIO.write(inputImage, format, outputFile)
            println("Image converted successfully")
        } catch (e: Exception) {
            println("Error converting image: ${e.message}")
        }
    }

    fun resizeImage(inputPath: String, outputPath: String, width: Int, height: Int) {
        try {
            val inputImage = ImageIO.read(File(inputPath))
            val resizedImage = BufferedImage(width, height, inputImage.type)
            val g = resizedImage.createGraphics()
            g.drawImage(inputImage, 0, 0, width, height, null)
            g.dispose()
            ImageIO.write(resizedImage, "jpg", File(outputPath))
            println("Image resized successfully")
        } catch (e: Exception) {
            println("Error resizing image: ${e.message}")
        }
    }
}

class CLI {
    private val scanner = Scanner(System.`in`)
    private val fileOps = FileOperations()
    private val dataExtraction = DataExtraction()
    private val imageOps = ImageOperations()

    fun start() {
        println("Welcome to the Advanced CLI!")
        var running = true
        while (running) {
            println("\nPlease choose an operation:")
            println("1. List files in a directory")
            println("2. Copy a file")
            println("3. Delete a file")
            println("4. Extract text from a file")
            println("5. Convert image format")
            println("6. Resize an image")
            println("7. Exit")
            print("Enter your choice (1-7): ")

            when (scanner.nextLine()) {
                "1" -> listFiles()
                "2" -> copyFile()
                "3" -> deleteFile()
                "4" -> extractText()
                "5" -> convertImage()
                "6" -> resizeImage()
                "7" -> {
                    running = false
                    println("Thank you for using the Advanced CLI. Goodbye!")
                }
                else -> println("Invalid option. Please try again.")
            }
        }
    }

    private fun listFiles() {
        print("Enter directory path: ")
        val dir = scanner.nextLine()
        fileOps.listFiles(dir)
    }

    private fun copyFile() {
        print("Enter source file path: ")
        val source = scanner.nextLine()
        print("Enter destination file path: ")
        val destination = scanner.nextLine()
        fileOps.copyFile(source, destination)
    }

    private fun deleteFile() {
        print("Enter file path to delete: ")
        val filePath = scanner.nextLine()
        fileOps.deleteFile(filePath)
    }

    private fun extractText() {
        print("Enter file path to extract text from: ")
        val filePath = scanner.nextLine()
        dataExtraction.extractTextFromFile(filePath)
    }

    private fun convertImage() {
        print("Enter input image path: ")
        val inputPath = scanner.nextLine()
        print("Enter output image path: ")
        val outputPath = scanner.nextLine()
        print("Enter desired format (e.g., jpg, png): ")
        val format = scanner.nextLine()
        imageOps.convertImageFormat(inputPath, outputPath, format)
    }

    private fun resizeImage() {
        print("Enter input image path: ")
        val inputPath = scanner.nextLine()
        print("Enter output image path: ")
        val outputPath = scanner.nextLine()
        print("Enter new width: ")
        val width = scanner.nextLine().toIntOrNull() ?: 0
        print("Enter new height: ")
        val height = scanner.nextLine().toIntOrNull() ?: 0
        imageOps.resizeImage(inputPath, outputPath, width, height)
    }
}

fun main() {
    val cli = CLI()
    cli.start()
}