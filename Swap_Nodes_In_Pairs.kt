package practice

/**
 * Example:
 * var li = ListNode(5)
 * var v = li.`val`
 * Definition for singly-linked list.
 * class ListNode(var `val`: Int) {
 *     var next: ListNode? = null
 * }
 */
/*
Given a linked list, swap every two adjacent nodes and return its head. You must solve the problem without modifying the values in the list's nodes (i.e., only nodes themselves may be changed.)

Example 1:

Input: head = [1,2,3,4]
Output: [2,1,4,3]

Example 2:

Input: head = []
Output: []

Example 3:

Input: head = [1]
Output: [1]

Example 4:

Input: head = [1,2,3]
Output: [2,1,3]

Constraints:

The number of nodes in the list is in the range [0, 100].
0 <= Node.val <= 100
*/
class Solution {
    fun swapPairs(head: ListNode?): ListNode? {

        var dummy:ListNode? = ListNode()
        dummy?.next=head?.next
        var ptr=dummy
        var swap1=head
        var swap2=head?.next

        if(head?.next==null) return head

        while(swap2!=null){
            swap1?.next=swap2?.next
            swap2?.next=swap1
            ptr?.next=swap2
            ptr=swap1
            swap1=ptr?.next
            swap2=ptr?.next?.next
        }

        return dummy?.next
    }
}