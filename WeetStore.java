/**
 * A Hash Map where each element in a bucket is indexed by user id and contains an AVL Tree of their weets, sorted by date, ensures fast access and insertion for a weets user (used in getUser())
 * An AVL Tree where each element represents a day, sorted by date, and contains an AVL Tree of weets on that day, sorted by date, ensures fast insertion and access to weets (used by getWeetsOn(), getWeets() and getWeetsContaining())
 * A Doubly Linked List adds and sorts tags/topics while they are being added and returns an array of the top 10 topics
 * 
 * HashMap class and all classes used by it designed with the help of Shalin Doshi in the CS126 labs
 * Both AVL Trees based on code found at : http://coding-for-fun-in-java.blogspot.co.uk/2012/04/self-balancing-tree-in-java.html
 * Use of Calendar class inspired from : http://stackoverflow.com/questions/9474121/i-want-to-get-year-month-day-etc-from-java-date-to-compare-with-gregorian-calen
 * Assumed an empty search should return all weets
 * "Date before" considered inclusive according to updates on module web page
 * @author: 1525713
 */

package uk.ac.warwick.java.cs126.services;

import uk.ac.warwick.java.cs126.models.User;
import uk.ac.warwick.java.cs126.models.Weet;

import java.io.BufferedReader;
import java.util.Date;
import java.util.Calendar;
import java.io.FileReader;
import java.text.ParseException;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;


public class WeetStore implements IWeetStore {
    
    //class based on KeyValuePair class used in cs126 labs
    class KeyValuePair<K extends Comparable<K>,V> implements Comparable<KeyValuePair<K,V>>{

        protected K key;
        protected V value;
        
        public KeyValuePair(K k, V v) {
            key = k;
            value = v;
        }
        
        public K getKey() {
            return key;
        }
        
        public V getValue() {
            return value;
        }
        
        public void setValue(V v) {
            value = v;
        }
        
        public int compareTo(KeyValuePair<K,V> o) {
            return o.getKey().compareTo(this.getKey());
        }
    }
    
    //class based on ListElement class used in CS126 labs
    class ListElement<E> {
        private final E value;
        private ListElement<E> next;
        private ListElement<E> prev;
        
        public ListElement(E value) {
            this.value = value;
        }
        
        public E getValue() {
            return this.value;
        }
        
        public ListElement<E> getNext() {
            return next;
        }
        
        public ListElement<E> getPrev() {
            return prev;
        }
        
        public void setNext(ListElement<E> e) {
            next = e;
        }
        
        public void setPrev(ListElement<E> e) {
            prev = e;
        }
    }
    
    //class based on KeyValuePairLinkedList class used in CS126 labs   
    class KeyValuePairLinkedList<K extends Comparable<K>,V> {

        protected ListElement<KeyValuePair<K,V>> head;
        protected int size;
        
        public KeyValuePairLinkedList() {
            head = null;
            size = 0;
        }
        
        public void add(K key, V value) {
            this.add(new KeyValuePair<>(key, value));
        }

        public boolean add(KeyValuePair<K,V> kvp) {
            ListElement<KeyValuePair<K,V>> new_element = new ListElement<>(kvp);
            ListElement<KeyValuePair<K,V>> tmp = head;
            
            while(tmp != null) {
                if( tmp.getValue().compareTo(new_element.getValue()) > 0 ) {
                    new_element.setNext(tmp);
                    if(tmp.getPrev() == null) {
                        head = new_element;
                        tmp.setPrev(new_element);
                        return true;
                    }
                    tmp.getPrev().setNext(new_element);
                    new_element.setPrev(tmp.getPrev());
                    tmp.setPrev(new_element);
                    return true;
                }
                if(tmp.getNext() == null) {
                    tmp.setNext(new_element);
                    return true;
                }
                tmp = tmp.getNext();
            }
            head = new_element; 
            return false;
        }
        
        public int size() {
            return size;
        }
        
        public ListElement<KeyValuePair<K,V>> getHead() {
            return head;
        }
        
        public KeyValuePair<K,V> get(K key) {
            ListElement<KeyValuePair<K,V>> tmp = head;
            while(tmp != null) {
                if(tmp.getValue().getKey() == key) {
                    return tmp.getValue();
                }
                tmp = tmp.getNext();
            }
            return null;
        }
    }
    
    //class based on HashMap from CS126 labs
    @SuppressWarnings("unchecked")
    class HashMap<K extends Comparable<K>,V> {

        protected KeyValuePairLinkedList[] table;
        
        public HashMap() {
            this(16001);
        }
        
        public HashMap(int s) {
            table = new KeyValuePairLinkedList[s];
            initTable();
        }
        
        protected void initTable() {
            for(int i = 0; i < table.length; i++) {
                table[i] = new KeyValuePairLinkedList<>();
            }
        }

        public boolean add(K key, V value) {
            int location = key.hashCode() % table.length;
            if(table[location].get(key) == null) {
                table[location].add(key, value);
                return true;
            }
            return false;
        }

        public V get(K key) {
            int location = key.hashCode() % table.length;
            KeyValuePair<K,V> pair = table[location].get(key);
            if(pair != null) return (V)pair.getValue();
            return null;
        }
    }
    
    class Node {
        protected Weet weet;
        protected int height;   //the height is the length of the longest chain of nodes starting at the node
        protected Node left;    //the node which is to the left of this node (which has a lower value that this node)
        protected Node right;   //the node which is to the higher of this node (which has a higher value that this node)
    
        Node(Weet weet) {
            this.weet = weet;
            height = 0;
            left = null;
            right = null;
        }
    }

    class WeetTree {
        
        private Node root;
        private int localCount;
        private ListElement<Weet> localList;
        
        public WeetTree () {
            root = null;
            localList = null;
        }
        
        //rotates the tree/subtree with x as root
        private Node rotateRight(Node x) {
            //the height of a tree is the number of nodes in the longest chain of nodes in that tree (including the root)
            int lh; //the height of the left tree
            int rh; //the height of the right tree
            //rotate the tree to the right and preserve sortedness
            Node newRoot = x.left; 
            Node tmp = newRoot.right;
            newRoot.right = x;
            x.left = tmp;
            
            // update the height of x node of Tree
            lh = 0;
            rh = 0;
            if (x.left != null) lh = x.left.height;
            if (x.right != null) rh = x.right.height;

            if(lh >= rh) x.height = lh + 1;
            else x.height = rh + 1;
        
            // update the height of newRoot node of Tree
            lh = 0;
            rh = 0;
            if (newRoot.left != null) lh = newRoot.left.height;
            if (newRoot.right != null) rh = newRoot.right.height;
            
            if(lh >= rh) newRoot.height = lh + 1;
            else newRoot.height = rh + 1;
        
            // newRoot is the head of new tree to be returned
            return newRoot;
        }
        
        private Node rotateLeft(Node x) {
            //the height of a tree is the number of nodes in the longest chain of nodes in that tree
            int lh; //the height of the left tree
            int rh; //the height of the right tree
            //rotate the tree to the right and preserve sortedness
            Node newRoot = x.right;
            Node tmp = newRoot.left;
            newRoot.left = x;
            x.right = tmp;
            
            // update the height of x node of Tree
            lh = 0;
            rh = 0;
            if (x.left != null) lh = x.left.height;
            if (x.right != null) rh = x.right.height;

            if(lh >= rh) x.height = lh + 1;
            else x.height = rh + 1;
        
            // update the height of newRoot node of Tree
            lh = 0;
            rh = 0;
            if (newRoot.left != null) lh = newRoot.left.height;
            if (newRoot.right != null) rh = newRoot.right.height;
            
            if(lh >= rh) newRoot.height = lh + 1;
            else newRoot.height = rh + 1;
        
            // newRoot is the head of new tree to be returned
            return newRoot;
        }
        
        public void insert(Weet weet) {
            //insert the weet at the root of the main tree
            root = insert(root, weet);
        }
        public Node insert(Node tree, Weet weet) {
            
            int lh = 0; //the height of the left tree
            int rh = 0; //the height of the right tree
            int balance = 0;    //the tree is balanced if the height of its left and right subtrees differ by a maximum of 1
            
            //if inserting into an empty tree or as a leaf
            if(tree == null) {
                tree = new Node(weet);
                return tree;
            }
            else if(weet.getDateWeeted().getTime() > tree.weet.getDateWeeted().getTime()) {
                tree.right = insert(tree.right, weet);  //if the date of the weet being inserted is later than the date of the weet at this node, insert to right
            }
            else if(weet.getDateWeeted().getTime() <= tree.weet.getDateWeeted().getTime()) {
                tree.left = insert(tree.left, weet);    //otherwise, insert to left
            }            
            
            //set the height of the new (modified) tree       
            if (tree.left != null) lh = tree.left.height;
            if (tree.right != null) rh = tree.right.height;
            
            if(lh >= rh) tree.height = lh + 1;
            else tree.height = rh + 1;
            balance = lh - rh;
            
            //left-left case
            if( (balance > 1) && (weet.getDateWeeted().getTime() <= tree.left.weet.getDateWeeted().getTime()) ) {
                //rotate this right
                return rotateRight(tree);
            }
            //left-right case
            else if( (balance > 1) && (weet.getDateWeeted().getTime() > tree.left.weet.getDateWeeted().getTime()) ) {      
                //rotate left tree left
                tree.left = rotateLeft(tree.left);
                //rotate this right
                return rotateRight(tree);
            }
            //right-right case
            else if( (balance < -1) && (weet.getDateWeeted().getTime() > tree.right.weet.getDateWeeted().getTime()) ) {       
                //rotate this left
                return rotateLeft(tree);
            }
            //right-left case
            else if( (balance < -1) && (weet.getDateWeeted().getTime() <= tree.right.weet.getDateWeeted().getTime()) ) {     
                //rotate right tree right
                tree.right = rotateRight(tree.right);
                //rotate this left
                return rotateLeft(tree);
            }
            //return the root of the new tree (after insertion and rotations)
            return tree;
        }
        
        //creates a list of the weets and returns their number so that the array to be returned is declared with according size
        public void getInOrder() {
            getInOrder(root);   //gets weets in the main tree by passing the root
        }
        public void getInOrder(Node tree) {
            if(tree != null) {
                getInOrder(tree.right); //get weets in the subtree that is to the right of this node
                //get the weet stored at this node
                allWeets[count] = tree.weet;
                count++;
                //get weets in the subtree that is to the lef of this node
                getInOrder(tree.left);
            }
        }
        public int getInList() {
            localCount = 0; //the counter for weets in the list
            localList = new ListElement<>(null);    //a list of weets in this particular tree
            weetList = localList;   //initialize the global list of weets
            getInList(root);   //gets weets in the main tree by passing the root
            return localCount;  //return the number of weets to be retrieved
        }
        public void getInList(Node tree) {
            if(tree != null) {
                getInList(tree.right); //get weets in the subtree that is to the right of this node
                //get the weet stored at this node
                ListElement<Weet> tmp = new ListElement<>(tree.weet);
                tmp.setNext(localList);
                localList.setPrev(tmp);
                localList = tmp;
                localCount++;
                //get weets in the subtree that is to the lef of this node
                getInList(tree.left);
            }
        }
        
        //creates a list of the weets containg the query string and counts their number so that the array to be returned is declared with according size
        public void getContaining(String query) {
            localList = new ListElement<>(null);    //initialize the list which will store weets in this tree
            if( (query != null) && (!query.isEmpty()) )  {
                getContaining(root, query); //checks the weets stored in the main tree by passing its root
            }
            else getInOrder();  //if no actual string of characters was specified, return all weets (ASSUMPTION)
        }
        private void getContaining(Node tree, String query) {
            if(tree != null) {
                getContaining(tree.left, query);    //check weets in the subtree tat is to the left of this node
                if(tree.weet.getMessage().toLowerCase().contains(query.toLowerCase())) { //check if the weet stored at this node contains the query string
                    //add the weet stored at this node to the list of weets to be outputed
                    ListElement<Weet> tmp = new ListElement<>(tree.weet);
                    tmp.setNext(weetList);
                    weetList.setPrev(tmp);
                    weetList = tmp;
                    count++;
                }
                getContaining(tree.right, query);   //ckeck weets in the subtree tat is to the right of this node
            }
        }
        
        //creates a list of the weets on dates before dateBefore (inclusive - according to updates on module page) and counts their number so that the array to be returned is declared with according size        
        public void getBefore(Date dateBefore) {
            weetList = new ListElement<>(null); //initialize the global list of weets    
            getBefore(root, dateBefore);    //checks the weets in the main tree by passing its root
        }
        private void getBefore(Node tree, Date dateBefore) {
            if(tree != null) {
                getBefore(tree.left, dateBefore);   //check the weets in the subtree to the left of this node 
                //(whether this date is less, equal or greater than dateBefore, the weets in the left subtree only get smaller and should be checked)
                //if the weet stored at this node is from a date before dateBefore (inclusive), add to the list 
                if( tree.weet.getDateWeeted().before(dateBefore) ) {
                    ListElement<Weet> tmp = new ListElement<>(tree.weet); //add the weet stored at this node to the list
                    tmp.setNext(weetList);
                    weetList.setPrev(tmp);
                    weetList = tmp;
                    count++;
                    getBefore(tree.right, dateBefore);  //since this weet is from before dateBefore, there may still be weets from after this date with the same property
                } else if( tree.weet.getDateWeeted().equals(dateBefore)) {
                    ListElement<Weet> tmp = new ListElement<>(tree.weet);   //add the weet stored at this node to the list
                    tmp.setNext(weetList);
                    weetList.setPrev(tmp);
                    weetList = tmp;
                    count++;
                    //there is no point looking at weets to the right of this one, since they will be from a later date and so will be from after dateBefore
                }
            }
        }
    }
    
    //special type of node for the DateTree structure
    class DateNode {
        protected KeyValuePair<Integer,WeetTree> weets; //each node holds a weet
        protected int height;       //the height is the length of the longest chain of nodes starting at the node
        protected DateNode left;    //the node which is to the left of this node (which has a lower value that this node)
        protected DateNode right;   //the node which is to the higher of this node (which has a higher value that this node)
    
        DateNode(Weet weet) {
            WeetTree tmp = new WeetTree();
            tmp.insert(weet);
            Calendar cal = Calendar.getInstance();
            cal.setTime(weet.getDateWeeted());
            this.weets = new KeyValuePair<>( (cal.get(Calendar.YEAR)*1000) + (cal.get(Calendar.DAY_OF_YEAR) ),tmp); //key will represent the date of the weet (format: yyyyddd)
            height = 0;
            left = null;
            right = null;
        }
    }
    
    //each node in this sorted represents one day and contains a WeetTree where weets are sorted according to their full Date value
    class DateTree {
        
        private DateNode root;
        private int size;
        
        public DateTree () {
            root = null;
            size = 0;
        }
        
        public int size() {
            return size;
        }
        public void incSize() {
            size++;
        }
        
        private DateNode rotateRight(DateNode x) {
            //the height of a tree is the number of nodes in the longest chain of nodes in that tree
            int lh; //the height of the left tree
            int rh; //the height of the right tree
            //rotate the tree to the right and preserve sortedness
            DateNode newRoot = x.left;
            DateNode tmp = newRoot.right;
            newRoot.right = x;
            x.left = tmp;
            
            // update the height of x node of Tree
            lh = 0;
            rh = 0;
            if (x.left != null) lh = x.left.height;
            if (x.right != null) rh = x.right.height;

            if(lh >= rh) x.height = lh + 1;
            else x.height = rh + 1;
        
            // update the height of newRoot node of Tree
            lh = 0;
            rh = 0;
            if (newRoot.left != null) lh = newRoot.left.height;
            if (newRoot.right != null) rh = newRoot.right.height;
            
            if(lh >= rh) newRoot.height = lh + 1;
            else newRoot.height = rh + 1;
        
            // newRoot is the head of new tree to be returned
            return newRoot;
        }
        
        private DateNode rotateLeft(DateNode x) {
            //the height of a tree is the number of nodes in the longest chain of nodes in that tree
            int lh; //the height of the left tree
            int rh; //the height of the right tree
            //rotate the tree to the left and preserve sortedness
            DateNode newRoot = x.right;
            DateNode tmp = newRoot.left;
            newRoot.left = x;
            x.right = tmp;
            
            // update the height of x node of Tree
            lh = 0;
            rh = 0;
            if (x.left != null) lh = x.left.height;
            if (x.right != null) rh = x.right.height;

            if(lh >= rh) x.height = lh + 1;
            else x.height = rh + 1;
        
            // update the height of newRoot node of Tree
            lh = 0;
            rh = 0;
            if (newRoot.left != null) lh = newRoot.left.height;
            if (newRoot.right != null) rh = newRoot.right.height;
            
            if(lh >= rh) newRoot.height = lh + 1;
            else newRoot.height = rh + 1;
        
            // newRoot is the head of new tree to be returned
            return newRoot;
        }
        
        public void insert(Weet weet) {
            //System.out.println("Adding to tree " + weet.getPrettyDateWeeted());
            root = insert(root, weet);
        }
        public DateNode insert(DateNode tree, Weet weet) {
            
            //the height of a tree is the number of nodes in the longest chain of nodes in that tree
            int lh = 0; //the height of the left tree
            int rh = 0; //the height of the right tree
            int balance;    //the difference between the left and right subtrees of this node (the difference should never be more than 1)
            
            Calendar cal = Calendar.getInstance();
            cal.setTime(weet.getDateWeeted());
            //key represents the date of the weet (format: yyyyddd)
            if(tree == null) {
                tree = new DateNode(weet);
                return tree;
            } else if( (cal.get(Calendar.YEAR)*1000) + (cal.get(Calendar.DAY_OF_YEAR)) > tree.weets.getKey()) {
                tree.right = insert(tree.right, weet);
            } else if( (cal.get(Calendar.YEAR)*1000) + (cal.get(Calendar.DAY_OF_YEAR)) < tree.weets.getKey()) {
                tree.left = insert(tree.left, weet);
            } else {
                tree.weets.getValue().insert(weet);
            }
            
            //set the height of the new (modified) tree       
            if (tree.left != null) lh = tree.left.height;
            if (tree.right != null) rh = tree.right.height;
            
            if(lh >= rh) tree.height = lh + 1;
            else tree.height = rh + 1;
            balance = lh - rh;
            
            //left-left case
            if( (balance > 1) && ( (cal.get(Calendar.YEAR)*1000) + (cal.get(Calendar.DAY_OF_YEAR)) < tree.weets.getKey() ) ) {
                return rotateRight(tree);
            }
            //left-right case            
            else if( (balance > 1) && ( (cal.get(Calendar.YEAR)*1000) + (cal.get(Calendar.DAY_OF_YEAR)) > tree.weets.getKey() ) ) {
                tree.left = rotateLeft(tree.left);
                return rotateRight(tree);
            }
            //right-right case            
            else if( (balance < -1) && ( (cal.get(Calendar.YEAR)*1000) + (cal.get(Calendar.DAY_OF_YEAR)) > tree.weets.getKey() ) ) {       
                return rotateLeft(tree);
            }
            //right-left case
            else if( (balance < -1) && ( (cal.get(Calendar.YEAR)*1000) + (cal.get(Calendar.DAY_OF_YEAR)) < tree.weets.getKey() ) ) {     
                tree.right = rotateRight(tree.right);
                return rotateLeft(tree);
            }
            //return the root of the new, rebalanced tree
            return tree;
        }
        
        //gets all the weets sorted in descending order by date
        public void getInOrder() {
            allWeets = new Weet[size];  //initialize the array that will store weets
            count = 0;
            getInOrder(root);
        }
        public void getInOrder(DateNode tree) {
            if(tree != null) {
                getInOrder(tree.right);
                tree.weets.getValue().getInOrder();
                getInOrder(tree.left);
            }
        }
        
        //returns all the weets containing query in their message, sorted in descending order by date
        public void getContaining(String query) {
            weetList = new ListElement<>(null); //initialize the list that will store the weets
            count = 0;  //count found weets to initialize array to be returned
            if( (query != null) && (!query.isEmpty()) )  {
                getContaining(root, query); //look through tree starting at root
                //move weets from list into array
                allWeets = new Weet[count];
                for(int i = 0; (i < count) && (weetList != null); i ++) {
                    allWeets[i] = weetList.getValue();
                    weetList = weetList.getNext();
                }
                weetList = null;
            }
            //if query was empty return all weets
            else getInOrder();
        }
        private void getContaining(DateNode tree, String query) {
            if(tree != null) {
                getContaining(tree.left, query);            //get weets in the tree to the left of this noes
                tree.weets.getValue().getContaining(query); //get weets in the tree located at this node
                getContaining(tree.right, query);           //get weets in the tree to the right of this node
            }
        }
        
        //gets weets before dateBefore (inclusive - as specified in the updates from the module web page)
        public void getBefore(Date dateBefore) {
            weetList = new ListElement<>(null); //initialize the list which will hold the weets
            count = 0;  //used to count found weets in order to initialize the array which will be returned
            getBefore(root, dateBefore);
            if(count > 0) {
                //move weets from list into array
                allWeets = new Weet[count];    
                for(int i = 0; (i < count) && (weetList != null); i ++) {
                    allWeets[i] = weetList.getValue();
                    weetList = weetList.getNext();
                }
                weetList = null;
            } else {
                allWeets = new Weet[0];
                weetList = null;
            }
        }
        private void getBefore(DateNode tree, Date dateBefore) {
            if(tree != null) {
                //whether the date of the weets at this node is later, equal or before dateBefore, the weets to the left will only be on earlyer dates so should be checked
                getBefore(tree.left, dateBefore);
                Calendar cal = Calendar.getInstance();
                cal.setTime(dateBefore);
                //key represents the date of the weet (format: yyyyddd)
                if( tree.weets.getKey() < ( (cal.get(Calendar.YEAR)*1000) + (cal.get(Calendar.DAY_OF_YEAR)) ) ) {
                    //the weets at this node respect the condition and later weets might too
                    tree.weets.getValue().getBefore(dateBefore);
                    getBefore(tree.right, dateBefore);
                } else if( tree.weets.getKey() == ( (cal.get(Calendar.YEAR)*1000) + (cal.get(Calendar.DAY_OF_YEAR)) ) ) {
                    //the weets at this location are from the exact date so later weets will all be from later dates
                    tree.weets.getValue().getBefore(dateBefore);
                }
            }
        }
        
        //gets the weets which were posted on the passed date
        public WeetTree getOnDate(Date dateOn) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(dateOn);
            return getOnDate(root, (cal.get(Calendar.YEAR)*1000) + (cal.get(Calendar.DAY_OF_YEAR)) );
        }
        //dayOn will represet the day (format: yyyyddd)
        private WeetTree getOnDate(DateNode tree, int dayOn) {
            if(tree != null) {
                if(tree.weets.getKey() > dayOn) return getOnDate(tree.left, dayOn);
                else if(tree.weets.getKey() < dayOn) return getOnDate(tree.right, dayOn);
                else if(tree.weets.getKey() == dayOn) return tree.weets.getValue();
            }
            return null;
        }
    }
    
    //stores the tags to be returned by getTrending
    class TrendingList {
        protected ListElement<KeyValuePair<String,Integer>> tags;   //key is the tag and value represents the number of occurances
        protected int listSize;
        
        public TrendingList() {
            tags = null;
            listSize = 0;
        }
        
        public String[] getTrending() {
            if(listSize < 10) return null;
            String[] trending = new String[10];
            ListElement<KeyValuePair<String,Integer>> temp = tags;
            for(int i = 0; (i < 10) && (temp != null); i++) {
                trending[i] = temp.getValue().getKey();
                temp = temp.getNext();
            }
            return trending;
        }
        
        //strips tags from message and adds to list
        public void addTags(Weet weet) {
            String message = weet.getMessage();
            boolean processing = false; //keeps track of whether the method has started recording a topic
            int begins = 0; //stores the index of the first character of tag being processed 
            int ends = 0;   //stores the index of the last recorded character of tag being processed
            for (int i = 0; i < message.length(); i++){
                char c = message.charAt(i);
                //if a tag begins at this character
                if(c == '#') {
                    //if no tags were being processed when reached this tag
                    if(!processing) {
                        processing = true;
                        begins = i;
                        ends = i;
                    }
                    else {
                        //chained hashtags - first has ended - add hashtag
                        String newTag = message.substring(begins, ends);
                        addTag(newTag);
                        begins = i;
                        ends = i;
                    }
                } else if(processing) {
                    if(Character.isWhitespace(c) || c == '.' || c == ',' || c == '!' || c == '?') {
                        //reached end of tag - add hashtag
                        String newTag = message.substring(begins, ends);
                        addTag(newTag);
                        processing = false;
                    } else {
                        //still processing current hashtag - keep going
                        ends = i;                      
                        if(i == message.length() - 1) {
                            //no other characters left - add hashtag
                            String newTag = message.substring(begins);
                            addTag(newTag);
                            processing = false;
                        }
                    }
                }
            }
        }
        private void addTag(String newTag) {
            if(tags != null) {
                ListElement<KeyValuePair<String,Integer>> temp = tags;
                while( (!temp.getValue().getKey().equals(newTag)) && (temp.getNext() != null) ) {
                    temp = temp.getNext();
                }
                if( temp.getValue().getKey().equals(newTag) ) {
                    //tag already in list
                    temp.getValue().setValue( new Integer(temp.getValue().getValue() + 1) );
                    //while the tag one elemen higher has a smaller nr of occurances, swap it with this one
                    while( (temp.getPrev() != null) && (temp.getValue().getValue() > temp.getPrev().getValue().getValue()) ) {
                        ListElement<KeyValuePair<String,Integer>> prev = temp.getPrev();
                        if(prev.getPrev() != null) {
                            prev.getPrev().setNext(temp);
                        }
                        if(temp.getNext() != null){
                            temp.getNext().setPrev(prev);
                        }
                        prev.setNext(temp.getNext());
                        temp.setPrev(prev.getPrev());
                        temp.setNext(prev);
                        prev.setPrev(temp);
                        //if tag just got to the 'head' of the list
                        if(temp.getPrev() == null) tags = temp;
                    }
                } else {
                    //tag insn't in list
                    temp.setNext(new ListElement<>(new KeyValuePair<>(newTag,1)));
                    temp.getNext().setPrev(temp);
                    listSize++;
                }
            } else {
                //first tag added
                tags = new ListElement<>(new KeyValuePair<>(newTag,1));
            }
        }
    }
    
    protected HashMap<Integer,Weet> weets;      //stores weets by id for fast individual access
    protected HashMap<Integer,WeetTree> users;  //stores users and their weets by id for fast access
    protected TrendingList topics;  //topics for getTrending
    protected DateTree dateTree;    //stores weets for each day, sorted by date
    protected Weet[] allWeets;      //for any array returns
    protected int count;
    protected ListElement<Weet> weetList;   //used to store weets before they are moved to allWeets

    public WeetStore() {
        weets = new HashMap<>();
        users = new HashMap<>();
        topics = new TrendingList();
        dateTree = new DateTree();
        weetList = new ListElement<>(null);
        allWeets = new Weet[0];
    }

    public boolean addWeet(Weet weet) {
        //if the weet hasn't been added before
        if( weets.add(weet.getId(), weet) ) {
            dateTree.incSize();     //increase the weet counter
            dateTree.insert(weet);  //insert weet into the tree sorted by date
            topics.addTags(weet);   //add any tags in this weet to the topic list
            WeetTree tmp = new WeetTree();
            tmp.insert(weet);
            //try adding a weet tree for the user that posted this weet
            if(!users.add(weet.getUserId(), tmp)) {
                //if there was a tree there already, add this weet to it
                users.get(weet.getUserId()).insert(weet);
            }
            return true;
        }
        return false;
    }
    
    public Weet getWeet(int wid) {
        return weets.get(wid);
    }

    public Weet[] getWeets() {
        dateTree.getInOrder();
        return allWeets;
    }

    public Weet[] getWeetsByUser(User usr) {
        count = 0;
        WeetTree userTree = users.get(usr.getId());
        if(userTree != null) {
            count = userTree.getInList();
            if(count > 0) {
                allWeets = new Weet[count]; //array to be returned, with size=count of weets this user has
                //move weets from list into array
                for(int i = 0; (i < count) && (weetList.getPrev() != null); i ++) {
                    weetList = weetList.getPrev();
                    allWeets[i] = weetList.getValue();
                }
                weetList = null;
                return allWeets;
            }
        }
        allWeets = new Weet[0];
        return allWeets;
    }

    public Weet[] getWeetsContaining(String query) {
        dateTree.getContaining(query);
        return allWeets;
    }

    public Weet[] getWeetsOn(Date dateOn) {
        count = 0;
        WeetTree weetsOn = dateTree.getOnDate(dateOn);
        if(weetsOn != null) {
            count = weetsOn.getInList();
            if(count > 0) {
                allWeets = new Weet[count]; //array to be returned, with size=count of weets on this date
                //move weets into array
                for(int i = 0; (i < count) && (weetList.getPrev() != null); i++) {
                    weetList = weetList.getPrev();
                    allWeets[i] = weetList.getValue();
                }
                weetList = null;
                return allWeets;
            }
        }
        allWeets = new Weet[0];
        return allWeets;
    }

    public Weet[] getWeetsBefore(Date dateBefore) {
        dateTree.getBefore(dateBefore);
        return allWeets;
    }

    public String[] getTrending() {
       return topics.getTrending();
    }

}
