
fun lengthOfLongestSubstring(s: String): Int {
    var maxLength = 0
    val queue = LinkedList<Char>()
    for (i in s.indices) {
        if (queue.isNotEmpty()) {
            when {
                queue.first == s[i] -> queue.poll()
                queue.last == s[i] -> queue.clear()
                queue.contains(s[i]) -> {
                    while (queue.isNotEmpty()) {
                        if (queue.poll() == s[i]) break
                    }
                }
            }
        }

        maxLength = max(maxLength, queue.size+1)
        queue.offer(s[i])
    }

    return maxLength
}


import kotlin.math.*

class Solution {
    private val hashMap = mutableMapOf<Char, Int>()
    private var longest = Pair<Int, Int>(0, 1)

    fun lengthOfLongestSubstring(s: String): Int {
        if (s.length == 0) {
            return 0
        }

        var start = 0

        for (i in 0 until s.length) {
            val letter = s[i]
            if (hashMap.containsKey(letter)) {
                // start = max(start, hashMap[letter]!! + 1)
                // why use max() ? "abba": if we take not max, then when visiting second 'a' ->
                // we'll take b, but don't need it as it'll include duplicate
                start = hashMap[letter]!! + 1
                // +1 as we don't need current that has duplication, but next letter after it
            }
            val (firstIdx: Int, secondIdx: Int) = longest

            if (secondIdx - firstIdx < i - start + 1) {
                longest = Pair<Int, Int>(start, i+1)
                // +1 is to include current last letter as otherwise it will be excluding
            }
            hashMap.put(letter, i)
        }
        val (firstIdx: Int, secondIdx: Int) = longest

        return secondIdx - firstIdx
    }
}