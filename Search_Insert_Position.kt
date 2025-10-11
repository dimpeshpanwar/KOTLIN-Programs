/*
Given a sorted array of distinct integers and a target value, return the index if the target is found.
If not, return the index where it would be if it were inserted in order.
You must write an algorithm with O(log n) runtime complexity.

Example 1:
Input: nums = [1,3,5,6], target = 5
Output: 2

Example 2:
Input: nums = [1,3,5,6], target = 2
Output: 1

Example 3:
Input: nums = [1,3,5,6], target = 7
Output: 4

Constraints:
1 <= nums.length <= 104
-104 <= nums[i] <= 104
*/

fun SearchInsert(nums: IntArray, target: Int): Int
{
    var low = 0
    var high = nums.size - 1

    while(low <= high)
    {
        val mid = (low + high)/2

        if(mid == target)
        {
            return mid
        }
        else if(mid < target)
        {
            low = low + 1
        }
        else {
            high = high - 1
        }
    }
    return low;
}

fun main()
{
    val nums = intArrayOf(1, 3, 5, 6)
    val target = 5
    val index = SearchInsert(nums, target)
    println("The index of the target value is: $index")
}