/**
 * Advanced String Similarity Analyzer
 *
 * A comprehensive utility library for analyzing string similarity using multiple algorithms.
 * Useful for fuzzy matching, autocomplete, spell checking, and text comparison.
 *
 * @author Your Name
 * @version 1.0.0
 */

import kotlin.math.max
import kotlin.math.min
import kotlin.math.abs

/**
 * Main class for string similarity analysis with multiple algorithms
 */
class StringSimilarityAnalyzer {

    /**
     * Result class containing similarity metrics
     */
    data class SimilarityResult(
        val levenshteinDistance: Int,
        val levenshteinSimilarity: Double,
        val jaccardSimilarity: Double,
        val cosineSimilarity: Double,
        val jaroWinklerSimilarity: Double,
        val longestCommonSubsequenceLength: Int,
        val hammingDistance: Int?,
        val overallSimilarity: Double
    ) {
        override fun toString(): String {
            return """
                |Similarity Analysis Results:
                |---------------------------
                |Levenshtein Distance: $levenshteinDistance
                |Levenshtein Similarity: ${"%.2f".format(levenshteinSimilarity * 100)}%
                |Jaccard Similarity: ${"%.2f".format(jaccardSimilarity * 100)}%
                |Cosine Similarity: ${"%.2f".format(cosineSimilarity * 100)}%
                |Jaro-Winkler Similarity: ${"%.2f".format(jaroWinklerSimilarity * 100)}%
                |LCS Length: $longestCommonSubsequenceLength
                |Hamming Distance: ${hammingDistance ?: "N/A (strings of different length)"}
                |Overall Similarity Score: ${"%.2f".format(overallSimilarity * 100)}%
            """.trimMargin()
        }
    }

    /**
     * Calculates the Levenshtein distance between two strings.
     * Time complexity: O(m*n), Space complexity: O(m*n)
     */
    fun levenshteinDistance(s1: String, s2: String): Int {
        val m = s1.length
        val n = s2.length

        if (m == 0) return n
        if (n == 0) return m

        val dp = Array(m + 1) { IntArray(n + 1) }

        for (i in 0..m) dp[i][0] = i
        for (j in 0..n) dp[0][j] = j

        for (i in 1..m) {
            for (j in 1..n) {
                val cost = if (s1[i - 1] == s2[j - 1]) 0 else 1
                dp[i][j] = minOf(
                    dp[i - 1][j] + 1,
                    dp[i][j - 1] + 1,
                    dp[i - 1][j - 1] + cost
                )
            }
        }

        return dp[m][n]
    }

    /**
     * Calculates normalized Levenshtein similarity (0.0 to 1.0)
     */
    fun levenshteinSimilarity(s1: String, s2: String): Double {
        val maxLen = max(s1.length, s2.length)
        if (maxLen == 0) return 1.0
        val distance = levenshteinDistance(s1, s2)
        return 1.0 - (distance.toDouble() / maxLen)
    }

    /**
     * Calculates Jaccard similarity coefficient based on character sets
     */
    fun jaccardSimilarity(s1: String, s2: String): Double {
        if (s1.isEmpty() && s2.isEmpty()) return 1.0

        val set1 = s1.toSet()
        val set2 = s2.toSet()

        val intersection = set1.intersect(set2).size
        val union = set1.union(set2).size

        return if (union == 0) 0.0 else intersection.toDouble() / union
    }

    /**
     * Calculates cosine similarity using character frequency vectors
     */
    fun cosineSimilarity(s1: String, s2: String): Double {
        if (s1.isEmpty() && s2.isEmpty()) return 1.0
        if (s1.isEmpty() || s2.isEmpty()) return 0.0

        val freq1 = s1.groupingBy { it }.eachCount()
        val freq2 = s2.groupingBy { it }.eachCount()

        val allChars = freq1.keys + freq2.keys

        var dotProduct = 0.0
        var mag1 = 0.0
        var mag2 = 0.0

        for (char in allChars) {
            val f1 = freq1[char] ?: 0
            val f2 = freq2[char] ?: 0
            dotProduct += f1 * f2
            mag1 += f1 * f1
            mag2 += f2 * f2
        }

        val magnitude = kotlin.math.sqrt(mag1) * kotlin.math.sqrt(mag2)
        return if (magnitude == 0.0) 0.0 else dotProduct / magnitude
    }

    /**
     * Calculates Jaro similarity
     */
    private fun jaroSimilarity(s1: String, s2: String): Double {
        if (s1 == s2) return 1.0
        if (s1.isEmpty() || s2.isEmpty()) return 0.0

        val matchDistance = max(s1.length, s2.length) / 2 - 1
        val s1Matches = BooleanArray(s1.length)
        val s2Matches = BooleanArray(s2.length)

        var matches = 0
        var transpositions = 0

        for (i in s1.indices) {
            val start = max(0, i - matchDistance)
            val end = min(i + matchDistance + 1, s2.length)

            for (j in start until end) {
                if (s2Matches[j] || s1[i] != s2[j]) continue
                s1Matches[i] = true
                s2Matches[j] = true
                matches++
                break
            }
        }

        if (matches == 0) return 0.0

        var k = 0
        for (i in s1.indices) {
            if (!s1Matches[i]) continue
            while (!s2Matches[k]) k++
            if (s1[i] != s2[k]) transpositions++
            k++
        }

        return (matches.toDouble() / s1.length +
                matches.toDouble() / s2.length +
                (matches - transpositions / 2.0) / matches) / 3.0
    }

    /**
     * Calculates Jaro-Winkler similarity (gives more weight to common prefixes)
     */
    fun jaroWinklerSimilarity(s1: String, s2: String, prefixScale: Double = 0.1): Double {
        val jaroSim = jaroSimilarity(s1, s2)

        var prefixLength = 0
        val maxPrefix = min(min(s1.length, s2.length), 4)

        for (i in 0 until maxPrefix) {
            if (s1[i] == s2[i]) prefixLength++ else break
        }

        return jaroSim + prefixLength * prefixScale * (1.0 - jaroSim)
    }

    /**
     * Calculates the length of the longest common subsequence
     */
    fun longestCommonSubsequence(s1: String, s2: String): Int {
        val m = s1.length
        val n = s2.length
        val dp = Array(m + 1) { IntArray(n + 1) }

        for (i in 1..m) {
            for (j in 1..n) {
                dp[i][j] = if (s1[i - 1] == s2[j - 1]) {
                    dp[i - 1][j - 1] + 1
                } else {
                    max(dp[i - 1][j], dp[i][j - 1])
                }
            }
        }

        return dp[m][n]
    }

    /**
     * Calculates Hamming distance (only for strings of equal length)
     */
    fun hammingDistance(s1: String, s2: String): Int? {
        if (s1.length != s2.length) return null

        return s1.indices.count { s1[it] != s2[it] }
    }

    /**
     * Comprehensive analysis using all available algorithms
     */
    fun analyze(s1: String, s2: String): SimilarityResult {
        val levDistance = levenshteinDistance(s1, s2)
        val levSimilarity = levenshteinSimilarity(s1, s2)
        val jaccardSim = jaccardSimilarity(s1, s2)
        val cosineSim = cosineSimilarity(s1, s2)
        val jaroWinklerSim = jaroWinklerSimilarity(s1, s2)
        val lcsLength = longestCommonSubsequence(s1, s2)
        val hammingDist = hammingDistance(s1, s2)

        // Calculate overall similarity as weighted average
        val overallSim = (levSimilarity * 0.3 +
                jaccardSim * 0.2 +
                cosineSim * 0.2 +
                jaroWinklerSim * 0.3)

        return SimilarityResult(
            levenshteinDistance = levDistance,
            levenshteinSimilarity = levSimilarity,
            jaccardSimilarity = jaccardSim,
            cosineSimilarity = cosineSim,
            jaroWinklerSimilarity = jaroWinklerSim,
            longestCommonSubsequenceLength = lcsLength,
            hammingDistance = hammingDist,
            overallSimilarity = overallSim
        )
    }

    /**
     * Finds the most similar string from a list of candidates
     */
    fun findMostSimilar(target: String, candidates: List<String>): Pair<String, Double>? {
        if (candidates.isEmpty()) return null

        return candidates.map { candidate ->
            candidate to analyze(target, candidate).overallSimilarity
        }.maxByOrNull { it.second }
    }

    /**
     * Filters and ranks strings by similarity threshold
     */
    fun filterBySimilarity(
        target: String,
        candidates: List<String>,
        threshold: Double = 0.5
    ): List<Pair<String, Double>> {
        return candidates.map { candidate ->
            candidate to analyze(target, candidate).overallSimilarity
        }.filter { it.second >= threshold }
            .sortedByDescending { it.second }
    }

    /**
     * Suggests corrections for a potentially misspelled word
     */
    fun suggestCorrections(
        word: String,
        dictionary: List<String>,
        maxSuggestions: Int = 5,
        minSimilarity: Double = 0.6
    ): List<String> {
        return filterBySimilarity(word, dictionary, minSimilarity)
            .take(maxSuggestions)
            .map { it.first }
    }
}

/**
 * Extension functions for convenient string similarity checks
 */
fun String.similarityTo(other: String): Double {
    return StringSimilarityAnalyzer().analyze(this, other).overallSimilarity
}

fun String.isApproximately(other: String, threshold: Double = 0.8): Boolean {
    return this.similarityTo(other) >= threshold
}

/**
 * Demo usage examples
 */
fun main() {
    val analyzer = StringSimilarityAnalyzer()

    println("=== Example 1: Comparing two words ===")
    val result1 = analyzer.analyze("kitten", "sitting")
    println(result1)

    println("\n=== Example 2: Finding similar strings ===")
    val target = "programing" // intentional typo
    val dictionary = listOf(
        "programming", "program", "programmer", "programs",
        "debugging", "coding", "development"
    )

    val suggestions = analyzer.suggestCorrections(target, dictionary, maxSuggestions = 3)
    println("Did you mean: ${suggestions.joinToString(", ")}?")

    println("\n=== Example 3: Using extension functions ===")
    val similarity = "hello".similarityTo("hallo")
    println("Similarity between 'hello' and 'hallo': ${"%.2f".format(similarity * 100)}%")
    println("Is 'color' approximately 'colour'? ${"color".isApproximately("colour", 0.7)}")

    println("\n=== Example 4: Finding most similar match ===")
    val names = listOf("John", "Jane", "Joan", "Jean", "Jon")
    val mostSimilar = analyzer.findMostSimilar("Jane", names.filter { it != "Jane" })
    mostSimilar?.let { (name, score) ->
        println("Most similar to 'Jane': $name (${"%.2f".format(score * 100)}% match)")
    }
}