/*  Student information for assignment:
 *
 *  On OUR honor, Issay and Bruno, this programming assignment is OUR own work
 *  and WE have not provided this code to any other student.
 *
 *  Number of slip days used: 0
 *
 *  Student 1 (Student whose Canvas account is being used)
 *  UTEID: ics552
 *  email address: issay422@gmail.com
 *  Grader name: Devon
 *
 *  Student 2
 *  UTEID: bhz87
 *  email address: bzavaleta4@yahoo.com
 *
 */

import java.util.LinkedList;

// Priority queue of nodes. Nodes are sorted in ascending order based on frequency. 
// Nodes of the same frequency will have the node put in later further back in the queue. 
public class PriorityQueue<E extends Comparable<E>> {
    LinkedList<E> con;

    /**
     * constructor for PriorityQueue
     */
    public PriorityQueue(){ 
        con = new LinkedList<>(); 
    }
    

    /**
     * adds node to queue, maintains ascending order and "fairness"
     * @param item the item to enqueue
     * pre: item != null 
     */ 
    public void enqueue(E item){
        if(item == null){
            throw new IllegalArgumentException("Illegal Argument: item != null");
        }
        int index = con.size(); 
        // loops backwards so that nodes of the same frequency will be placed "fairly". 
        while(index > 0 && item.compareTo(con.get(index - 1)) < 0){
            index--;
        }
        con.add(index, item);
    }

    /**
     * dequeue the first item in the queue
     * @return the item dequeued. 
     */
    public E dequeue(){
        E first = con.getFirst();
        con.removeFirst();
        return first; 
    }

    /**
     * gets the size of the internal linked list structure
     * @return the size of the queue
     */
    public int size(){
        return con.size(); 
    }

    /**
     * get the first value of the queue
     * @return the first value
     */
    public E getFirst(){
        return con.getFirst(); 
    }

    /**
     * toString for tree
     * @return a String representation of the queue. 
     */
    public String toString(){
        return con.toString();
    }
}