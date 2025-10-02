class Solution {
    var set = HashSet<ArrayList<Int>>()
    fun permute(nums: IntArray): List<List<Int>> {
        var ans = ArrayList<List<Int>>()
        getAllPermutation(nums, 0, ans)
        return ans
    }

    fun getAllPermutation(arr: IntArray, idx: Int, ans: ArrayList<List<Int>>) {
        if (idx == arr.size) {
            if (!set.contains(ArrayList(arr.toList()))) {
                ans.add(arr.toList())
                set.add(ArrayList(arr.toList()))
            }
            return
        }
        for (i in idx until arr.size) {
            swap(arr, idx, i)
            getAllPermutation(arr, idx + 1, ans)
            swap(arr, idx, i)
        }
    }

    fun swap(arr: IntArray, i: Int, j: Int) {
        var temp = arr[i]
        arr[i] = arr[j]
        arr[j] = temp
    }
}