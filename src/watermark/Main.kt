package watermark

import java.awt.Color
import java.awt.Transparency
import java.awt.image.BufferedImage
import java.io.File
import java.lang.NumberFormatException
import javax.imageio.ImageIO
import kotlin.system.exitProcess

fun main() {
    val inputFile = inputImage()
    val inputImage: BufferedImage = ImageIO.read(inputFile)
    val watermarkFile = watermarkImage(ImageIO.read(inputFile))
    val watermarkImage: BufferedImage = ImageIO.read(watermarkFile)
    if (watermarkImage.transparency == Transparency.TRANSLUCENT) {
        println("Do you want to use the watermark's Alpha channel?")
        if (readln().lowercase() == "yes") {
            val weight = watermarkTransparencyWeight()
            val outputFile = outputImage()
            val outputImage = BufferedImage(inputImage.width, inputImage.height, BufferedImage.TYPE_INT_RGB)

            for (x in 0 until inputImage.width) {
                for (y in 0 until inputImage.height) {
                    val inputPixelColor = Color(inputImage.getRGB(x, y), true)
                    val watermarkPixelColor = Color(watermarkImage.getRGB(x, y), true)

                    if (watermarkPixelColor.alpha == 255) {
                        val color = Color(
                            (weight * watermarkPixelColor.red + (100 - weight) * inputPixelColor.red) / 100,
                            (weight * watermarkPixelColor.green + (100 - weight) * inputPixelColor.green) / 100,
                            (weight * watermarkPixelColor.blue + (100 - weight) * inputPixelColor.blue) / 100,
                        )
                        outputImage.setRGB(x, y, color.rgb)
                    } else if (watermarkPixelColor.alpha == 0) {
                        outputImage.setRGB(x, y, Color(inputImage.getRGB(x, y)).rgb)
                    }
                }
            }
            saveImage(outputImage, outputFile)
        } else {
            watermarkWithoutAlphaChannel(inputImage, watermarkImage)
        }
    } else {
        println("Do you want to set a transparency color?")
        if (readln().lowercase() == "yes") {
            val transparencyColor = transparencyColor()
            watermarkWithTransparencyColor(inputImage, watermarkImage, transparencyColor)
        } else {
            watermarkWithoutAlphaChannel(inputImage, watermarkImage)
        }
    }
}

fun inputImage(): File {
    println("Input the image filename:")
    val fileName = readln()
    val file = File(fileName)
    if (file.exists()) {
        val image: BufferedImage = ImageIO.read(file)
        if (image.colorModel.numColorComponents != 3) {
            println("The number of image color components isn't 3.")
            exitProcess(0)
        } else if (image.colorModel.pixelSize != 32 && image.colorModel.pixelSize != 24) {
            println("The image isn't 24 or 32-bit.")
            exitProcess(0)
        }
    } else {
        println("The file $fileName doesn't exist.")
        exitProcess(0)
    }
    return file
}

fun watermarkImage(inputImage: BufferedImage): File {
    println("Input the watermark image filename: ")
    val fileName = readln()
    val file = File(fileName)
    if (file.exists()) {
        val watermark: BufferedImage = ImageIO.read(file)
        if (watermark.colorModel.numColorComponents != 3) {
            println("The number of watermark color components isn't 3.")
            exitProcess(0)
        } else if (watermark.colorModel.pixelSize != 32 && watermark.colorModel.pixelSize != 24) {
            println("The watermark isn't 24 or 32-bit.")
            exitProcess(0)
        } else if (watermark.width * watermark.height != inputImage.width * inputImage.height) {
            println("The image and watermark dimensions are different.")
            exitProcess(0)
        }
    } else {
        println("The file $fileName doesn't exist.")
        exitProcess(0)
    }
    return file
}

fun watermarkTransparencyWeight(): Int {
    println("Input the watermark transparency percentage (Integer 0-100):")
    try {
        val weight = readln().toInt()
        if (weight !in 0..100) {
            println("The transparency percentage is out of range.")
            exitProcess(0)
        }
        return weight
    } catch (e: NumberFormatException) {
        println("The transparency percentage isn't an integer number.")
        exitProcess(0)
    }
}

fun outputImage(): File {
    println("Input the output image filename (jpg or png extension):")
    val fileName = readln()
    val file = File(fileName)
    if (file.extension != "jpg" && file.extension != "png") {
        println("""The output file extension isn't "jpg" or "png".""")
        exitProcess(0)
    }
    return file
}

fun saveImage(input: BufferedImage, output: File) {
    ImageIO.write(input, output.extension, output)
    println("The watermarked image $output has been created.")
}

fun watermarkWithoutAlphaChannel(inputImage: BufferedImage, watermarkImage: BufferedImage) {
    val weight = watermarkTransparencyWeight()
    val outputFile = outputImage()
    val outputImage = BufferedImage(inputImage.width, inputImage.height, BufferedImage.TYPE_INT_RGB)

    for (x in 0 until inputImage.width) {
        for (y in 0 until inputImage.height) {
            val inputPixelColor = Color(inputImage.getRGB(x, y))
            val watermarkPixelColor = Color(watermarkImage.getRGB(x, y))

            val color = Color(
                (weight * watermarkPixelColor.red + (100 - weight) * inputPixelColor.red) / 100,
                (weight * watermarkPixelColor.green + (100 - weight) * inputPixelColor.green) / 100,
                (weight * watermarkPixelColor.blue + (100 - weight) * inputPixelColor.blue) / 100,
            )
            outputImage.setRGB(x, y, color.rgb)
        }
    }
    saveImage(outputImage, outputFile)
}

fun transparencyColor(): Color {
    println("Input a transparency color ([Red] [Green] [Blue]):")
    try {
        val colorInput = readln().split(" ")
        if (colorInput.size != 3) {
            println("The transparency color input is invalid.")
            exitProcess(0)
        }
        if (colorInput[0].toInt() !in 0..255
            || colorInput[1].toInt() !in 0..255
            || colorInput[2].toInt() !in 0..255
        ) {
            println("The transparency color input is invalid.")
            exitProcess(0)
        }
        return Color(colorInput[0].toInt(), colorInput[1].toInt(), colorInput[2].toInt())
    } catch (e: NumberFormatException) {
        println("The transparency color input is invalid.")
        exitProcess(0)
    }
}

fun watermarkWithTransparencyColor(inputImage: BufferedImage, watermarkImage: BufferedImage, transparencyColor: Color) {
    val weight = watermarkTransparencyWeight()
    val outputFile = outputImage()
    val outputImage = BufferedImage(inputImage.width, inputImage.height, BufferedImage.TYPE_INT_RGB)

    for (x in 0 until inputImage.width) {
        for (y in 0 until inputImage.height) {
            val inputPixelColor = Color(inputImage.getRGB(x, y))
            val watermarkPixelColor = Color(watermarkImage.getRGB(x, y))

            if (watermarkPixelColor.red == transparencyColor.red
                && watermarkPixelColor.green == transparencyColor.green
                && watermarkPixelColor.blue == transparencyColor.blue
            ) {
                outputImage.setRGB(x, y, Color(inputImage.getRGB(x, y)).rgb)
            } else {
                val color = Color(
                    (weight * watermarkPixelColor.red + (100 - weight) * inputPixelColor.red) / 100,
                    (weight * watermarkPixelColor.green + (100 - weight) * inputPixelColor.green) / 100,
                    (weight * watermarkPixelColor.blue + (100 - weight) * inputPixelColor.blue) / 100,
                )
                outputImage.setRGB(x, y, color.rgb)
            }
        }
    }
    saveImage(outputImage, outputFile)
}

