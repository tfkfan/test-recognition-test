import java.awt.image.BufferedImage
import java.io.File
import java.io.IOException
import java.nio.charset.Charset
import java.nio.file.Files
import javax.imageio.ImageIO
import kotlin.math.min

const val frameWidth = 70
const val whiteBg: Int = -1
const val grayBg = -8882056

const val leftRankOffset = 149
const val topRankOffset = 589
const val rankFrameWidth = 30
const val rankFrameHeight = 27

const val leftSuitOffset = 149
const val topSuitOffset = 617
const val suitFrameWidth = 23
const val suitFrameHeight = 21

fun processImage(image: BufferedImage): String {
    if (image.getRGB(0, 0) == grayBg) {
        for (y in 0..<image.height) for (x in 0..<image.width)
            if (image.getRGB(x, y) == grayBg) image.setRGB(x, y, whiteBg)
    }
    var isCardRank = false
    val stringBuilder = StringBuilder()
    for (y in 0..<image.height) {
        for (x in 0..<image.width) {
            val rgb: Int = image.getRGB(x, y)
            if (rgb == whiteBg) isCardRank = true
            stringBuilder.append(if (rgb == whiteBg) " " else "*")
        }
        stringBuilder.appendLine()
    }
    return if (!isCardRank) "" else stringBuilder.toString()
}

fun parseCards(image: BufferedImage, frameWidth: Int, cardsNum: Int): List<Card> {
    val cards: MutableList<Card> = mutableListOf()
    for (z in 0..<cardsNum) {
        val rankStr = processImage(
            image.getSubimage(
                z * frameWidth + leftRankOffset, topRankOffset, rankFrameWidth, rankFrameHeight
            )
        )
        val suitStr = processImage(
            image.getSubimage(
                z * frameWidth + leftSuitOffset, topSuitOffset, suitFrameWidth, suitFrameHeight
            )
        )
        if (rankStr.isNotBlank() && suitStr.isNotBlank()) cards.add(Card(rankStr, suitStr))
    }
    return cards
}

fun levenshtein(targetStr: String, sourceStr: String): Int {
    val m = targetStr.length
    val n = sourceStr.length
    val delta = Array(m + 1) { IntArray(n + 1) }
    for (i in 1..m) delta[i][0] = i
    for (j in 1..n) delta[0][j] = j
    for (j in 1..n) for (i in 1..m) {
        if (targetStr[i - 1] == sourceStr[j - 1]) delta[i][j] = delta[i - 1][j - 1] else delta[i][j] = min(
            (delta[i - 1][j] + 1).toDouble(),
            min((delta[i][j - 1] + 1).toDouble(), (delta[i - 1][j - 1] + 1).toDouble())
        ).toInt()
    }
    return delta[m][n]
}

fun findSymbol(assets: List<AssetsFileContent>, targetStr: String): String {
    var min = 1000000
    var value = ""
    for (assetsFileContent in assets) {
        val levenshtein = levenshtein(assetsFileContent.content, targetStr)
        if (levenshtein < min) {
            min = levenshtein
            value = assetsFileContent.fileName
        }
    }
    return value
}

fun getAssetsContent(directory: String): List<AssetsFileContent> {
    return File(directory).listFiles()?.map {
        AssetsFileContent(it.name.replace(".txt", ""), Files.readString(it.toPath(), Charset.forName("UTF-8")))
    } ?: throw IOException("Files not found")
}

fun main(args: Array<String>) {
    val suites: List<AssetsFileContent> = getAssetsContent("assets/suit")
    val ranks: List<AssetsFileContent> = getAssetsContent("assets/rank")
    for (file in File("imgs_marked").listFiles()!!) {
        println("${file.name} - ${
            parseCards(ImageIO.read(file), frameWidth, 5).map {
                "${findSymbol(ranks, it.rank)}${findSymbol(suites, it.suit)}"
            }.reduce { acc, s ->
                acc.plus(s)
            }
        }")
    }
}