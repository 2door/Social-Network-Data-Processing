/**
 * Hash map contains at each element an id and 4 AVL Trees of followers and follows of user with that id for easy insertion, acces and sorting (used by all methods except getTopUsers())
 * Doubly linked list of user ids sorted according to each user's popularity for easy sorting while adding (used by getTopUsers())
 * 
 * FollowHashMap based on HashMap class created with Shalin Doshi during CS126 labs
 * All AVL Trees in Relations class based on code found here: http://coding-for-fun-in-java.blogspot.co.uk/2012/04/self-balancing-tree-in-java.html
 * Assumed that only users with followers shoud be returned by getTopUsers()
 * @author: 1525713
 */

package uk.ac.warwick.java.cs126.services;

import uk.ac.warwick.java.cs126.models.Weet;
import uk.ac.warwick.java.cs126.models.User;

import java.util.Date;


public class FollowerStore implements IFollowerStore {
        
    //class based on KeyValuePair class used in cs126 labs
    class IdPopPair {
        protected int id;
        protected int pop; //pop stands for popularity (number of followers)
        
        public IdPopPair(int i) {
            id = i;
            pop = 1;
        }
        public int getId() {
            return id;
        }
        public int getPop() {
            return pop;
        }
        public void incPop() {
            pop++;
        }
    }
    
    //class based on KeyValuePair class used in cs126 labs
    class IdDatePair {
        protected int id;
        protected Date date;
        
        public IdDatePair(int i, Date d) {
            id = i;
            date = d;
        }
        
        public int getId() {
            return id;
        }
        public Date getDate() {
            return date;
        }
    }
    
    //class based on KeyValuePair class used in cs126 labs
    class IdRelationsPair {

        protected int id;
        protected Relations rel;
        
        public IdRelationsPair(int i) {
            id = i;
            rel = new Relations();
        }
        
        public int getId() {
            return id;
        }
        
        public Relations getRelations() {
            return rel;
        }
    }
    
    //class used in cs126 lab (hash maps)
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
            return this.next;
        }
        
        public ListElement<E> getPrev() {
            return this.prev;
        }
        
        public void setNext(ListElement<E> e) {
            this.next = e;
        }
        
        public void setPrev(ListElement<E> e) {
            this.prev = e;
        }
    }
    
    //class based on KeyValuePairLinkedList class used in cs126 labs
    class IdRelationsPairLinkedList {

        protected ListElement<IdRelationsPair> head;
        protected int size;
        
        public IdRelationsPairLinkedList() {
            head = null;
            size = 0;
        }

        public void add(IdRelationsPair krp) {
            ListElement<IdRelationsPair> new_element = new ListElement<>(krp);
            new_element.setNext(head);
            head = new_element;
            size++;
        }
        
        public int size() {
            return size;
        }
        
        public boolean isEmpty(){
            return head==null;
        }
        
        public ListElement<IdRelationsPair> getHead() {
            return head;
        }
        
        public IdRelationsPair get(int id) {
            ListElement<IdRelationsPair> temp = head;
            
            while(temp != null) {
                if(temp.getValue().getId() == id) {
                    return temp.getValue();
                }
                temp = temp.getNext();
            }
            return null;
        }
    }

    class Node {
        protected int user; //the user id
        protected int height;   //length of the longest chain of nodes starting at this node
        protected Date date;    //the date on which this user followed/was followed
        protected Node left;    //root of the subtree to the left of this node
        protected Node right;   //root of the subtree to the right of this node
    
        Node(int user, Date date) {
            this.user = user;
            this.date = date;
            height = 0;
            left = null;
            right = null;
        }
    }

    class AVLTree {
        
        private Node root;
        private int size;
        private int count;
        private boolean notHere;    //turns false when trying to insert a relation which exists already
        private boolean byId;       //true if the tree is sorted by id, false if by date
        
        public AVLTree (boolean id) {
            root = null;
            size = 0;
            notHere = true;
            byId = id;
        }
        
        public int size() {
            return size;
        }
        public void incSize() {
            size++;
        }
        
        private Node rotateRight(Node x) {
             //the height of a tree is the number of nodes in the longest chain of nodes in that tree
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
        
        public void insertByDate(int user, Date date) {
            //before trying to insert, check the type of tree
            if(!byId) {
                incSize();
                root = insertByDate(root, user, date);
            }//dont insert by date in a tree sorted by id
        }
        public Node insertByDate(Node tree, int user, Date date) {
            
             //the height of a tree is the number of nodes in the longest chain of nodes in that tree
            int lh = 0; //the height of the left tree
            int rh = 0; //the height of the right tree
            //rotate the tree to the right and preserve sortedness
            //balance is the difference between the heights of left and right tree (shouldnt be more than 1)
            int balance = 0;
            
            if(tree == null) {
                tree = new Node(user, date);
                return tree;
            } else if(date.getTime() > tree.date.getTime()) {
                tree.right = insertByDate(tree.right, user, date);
            } else if(date.getTime() <= date.getTime()) {
                tree.left = insertByDate(tree.left, user, date);
            }            
            
            //set the height of the new (modified) tree       
            if (tree.left != null) lh = tree.left.height;
            if (tree.right != null) rh = tree.right.height;
            
            if(lh >= rh) tree.height = lh + 1;
            else tree.height = rh + 1;
            balance = lh - rh;
            
            //left-left case
            if( (balance > 1) && (date.getTime() <= tree.date.getTime()) ) {
                //rotate this right
                return rotateRight(tree);
            }//left-right case
            else if( (balance > 1) && (date.getTime() > tree.date.getTime()) ) {      
                //rotate left tree left
                tree.left = rotateLeft(tree.left);
                //rotate this right
                return rotateRight(tree);
            }//right-right case
            else if( (balance < -1) && (date.getTime() > tree.right.date.getTime()) ) {       
                //rotate this left
                return rotateLeft(tree);
            }//right-left case
            else if( (balance < -1) && (date.getTime() <= tree.right.date.getTime()) ) {     
                //rotate right tree right
                tree.right = rotateRight(tree.right);
                //rotate this left
                return rotateLeft(tree);
            }
            //the root of the new, rebalanced tree
            return tree;
        }
        
        public boolean insertById(int user) {
            //check what type of tree this is
            if(byId) {
                notHere = true; //assume the relation isnt here
                root = insertById(root, user);
                if(notHere) incSize();  //if the relation wasn't in the tree, the nuber of relations has increased
                return notHere;
            } else {
                //dont insert by id in a tree sorted by date
                return false;
            }
        }
        public Node insertById(Node tree, int user) {
            
             //the height of a tree is the number of nodes in the longest chain of nodes in that tree
            int lh = 0; //the height of the left tree
            int rh = 0; //the height of the right tree
            //rotate the tree to the right and preserve sortedness
            //balance is the difference between the heights of left and right tree (shouldnt be more than 1)            
            int balance = 0;
            
            if(tree == null) {
                tree = new Node(user, null);
                return tree;
            }
            else if(user > tree.user) {
                tree.right = insertById(tree.right, user);
            } else if(user < tree.user) {
                tree.left = insertById(tree.left, user);
            } 
            if(user == tree.user) {
                notHere = false;
                return tree;
            }    
            
            //set the height of the new (modified) tree       
            if (tree.left != null) lh = tree.left.height;
            if (tree.right != null) rh = tree.right.height;
            
            if(lh >= rh) tree.height = lh + 1;
            else tree.height = rh + 1;
            balance = lh - rh;
            
            //left-left case
            if( (balance > 1) && (user < tree.user) ) {
                //rotate this right
                return rotateRight(tree);
            }//left-right case
            else if( (balance > 1) && (user > tree.user) ) {      
                //rotate left tree left
                tree.left = rotateLeft(tree.left);
                //rotate this right
                return rotateRight(tree);
            } //right-right case
            else if( (balance < -1) && (user > tree.user) ) {       
                //rotate this left
                return rotateLeft(tree);
            }//right-left case
            else if( (balance < -1) && (user < tree.user) ) {     
                //rotate right tree right
                tree.right = rotateRight(tree.right);
                //rotate this left
                return rotateLeft(tree);
            }
            return tree;
        }
        
        //returns an array of all the users (sorted according to the type of tree)
        public int[] getInOrder() {
            if(size > 0) {
                int[] allUsers = new int[size];
                count = 0;
                getInOrder(root, allUsers);
                return allUsers;
            }
            return new int[0];
        }
        public void getInOrder(Node tree, int[] allUsers) {
            if(tree != null) {
                getInOrder(tree.right, allUsers);   //get all users to the right of this node
                allUsers[count] = tree.user;        //add this user to the array
                count++;
                getInOrder(tree.left, allUsers);    //get all users to the left of this node
            }
        }
        
        //returns the head of a list containing the users sorted by date (with each of their dates as well)
        public ListElement<IdDatePair> getWithDate() {
            //only possible if tree is sorted by date (check)
            if(!byId) {
                ListElement<IdDatePair> followList = null;
                followList = getWithDate(root, followList);
                return followList;
            } else return null;
        }
        public ListElement<IdDatePair> getWithDate(Node tree, ListElement<IdDatePair> followList) {
            if(tree != null) {
                followList = getWithDate(tree.left, followList);    //get all users to the left of this node
                //get this one
                ListElement<IdDatePair> tmp = new ListElement<>(new IdDatePair(tree.user, tree.date));
                //connect to doubly linked list
                tmp.setNext(followList);
                if(followList != null) followList.setPrev(tmp);
                followList = tmp;
                followList = getWithDate(tree.right, followList);
            }
            return followList;
        }
    }
    
    //stores followers and follows sorted by id and date for a user
    class Relations {
        AVLTree followingById;
        AVLTree followersById;
        AVLTree followingByDate;
        AVLTree followersByDate;
        
        public Relations() {
            followingById = new AVLTree(true);  //true value will be given to byId property
            followersById = new AVLTree(true);
            followingByDate = new AVLTree(false);   //false value will be given to byId property
            followersByDate = new AVLTree(false);
        }
        
        //try to insert into .ById and if "notHere" gets set to false, dont insert in .ByDate
        public boolean addFollower(int uid, Date date) {
            if(followersById.insertById(uid)) {
                followersByDate.insertByDate(uid, date);
                return true;
            } else {
                return false;
            }
        }
        
        public boolean addFollowing(int uid, Date date) {
            if(followingById.insertById(uid)) {
                followingByDate.insertByDate(uid, date);
                return true;
            } else {
                return false;
            }
        }
        
        //these methods return the users stored in each tree sorted according to their type (byId or byDate)
        public int[] getFollowersId() {
            return followersById.getInOrder();
        }
        public int[] getFollowersDate() {
            return followersByDate.getInOrder();
        }
        public int[] getFollowingId() {
            return followingById.getInOrder();
        }
        public int[] getFollowingDate() {
            return followingByDate.getInOrder();
        }
        
        public int getNumFollowers() {
            return followersByDate.size();
        }
        
        public ListElement<IdDatePair> getFollowersWithDate() {
            return followersByDate.getWithDate();
        }
        public ListElement<IdDatePair> getFollowingWithDate() {
            return followingByDate.getWithDate();            
        }
    }
    //class based on HashMap class used in labs
    @SuppressWarnings("unchecked")
    public class FollowHashMap {

        protected IdRelationsPairLinkedList[] table;
        protected int maxLocation;
        
        public FollowHashMap() {
            this(16001);
        }
        
        public FollowHashMap(int s) {
            table = new IdRelationsPairLinkedList[s];
            maxLocation = s-1;
            initTable();
        }
        
        protected void initTable() {
            for(int i = 0; i < table.length; i++) {
                table[i] = new IdRelationsPairLinkedList();
            }
        }
        
        //adds the following relationship to id1, and the followed relationship to id2
        public boolean add(int id1, int id2, Date date) {
            int location = id1 % table.length;
            //check if there are any relations for this user already
            IdRelationsPair pair = table[location].get(id1);
            if(pair != null) {
                if(!pair.getRelations().addFollowing(id2, date))
                    return false;
            } else {
                //if not, create the pair and add this relation
                table[location].add(new IdRelationsPair(id1));
                //sanity check
                if(!table[location].get(id1).getRelations().addFollowing(id2, date))
                    return false;
            }
            location = id2 % table.length;
            //check if there are any relations for this user already
            pair = table[location].get(id2);
            if(pair != null) {               
                return pair.getRelations().addFollower(id1, date);
            } else {
                //if not, create the pair and add this relation
                table[location].add(new IdRelationsPair(id2));
                //sanity check
                return table[location].get(id2).getRelations().addFollower(id1, date);
            }
        }
        
        //gets the followers of the user with id=id sorted by id
        public int[] getFollowersId(int id) {
            int location = id % table.length;
            IdRelationsPair pair = table[location].get(id);
            //if user has any relations, otherwise return empty array
            if(pair != null) {             
                return pair.getRelations().getFollowersId();
            }
            return new int[0];
        }
        //get users followed by passed user sorthed by id
        public int[] getFollowingId(int id) {
            int location = id % table.length;
            //if user has any relations, otherwise return empty array
            IdRelationsPair pair = table[location].get(id);
            if(pair != null) {           
                return pair.getRelations().getFollowingId();
            } 
            return new int[0];
        }
        //get ids of followers of passed user sorted by date
        public int[] getFollowersDate(int id) {
            int location = id % table.length;
            //if user has any relations, otherwise return empty array
            IdRelationsPair pair = table[location].get(id);
            if(pair != null) {
                return pair.getRelations().getFollowersDate();
            }
            return new int[0];
        }
        //get ids of users followed by passed user sorted by date
        public int[] getFollowingDate(int id) {
            int location = id % table.length;
            //if user has any relations, otherwise return empty array
            IdRelationsPair pair = table[location].get(id);
            if(pair != null) {
                return pair.getRelations().getFollowingDate();
            } else {
                int[] ret = new int[0];
                return ret;
            }
        }
        
        //returnsthe number of followers this user has
        public int getNumFollowers(int id) {
            int location = id % table.length;
            //if user has any relations, otherwise return 0
            IdRelationsPair pair = table[location].get(id);
            if(pair != null) {
                return pair.getRelations().getNumFollowers();
            }
            return 0;
        }
        
        //gets an array of mutual follows of users with thenpassed ids
        public int[] getMutualFollows(int id1, int id2) {
            int location = id1 % table.length;
            int count = 0;  //counts found users to declare array to be returned with appropriate size
            IdRelationsPair pair = table[location].get(id1);
            //lists which will store follows for each user
            ListElement<IdDatePair> follows1 = null;
            ListElement<IdDatePair> follows2 = null;
            ListElement<IdDatePair> mutual = null;
            if(pair != null) {
                follows1 = pair.getRelations().getFollowingWithDate();
                if(follows1 == null) return new int[0]; //if this user has no follows, there are no mutual follows
            } else return new int[0];   //if this user has no follows, there are no mutual follows
            location = id2 % table.length;
            pair = table[location].get(id2);
            if(pair != null) {
                follows2 = pair.getRelations().getFollowingWithDate();
                if(follows2 == null) return new int[0]; //if this user has no follows, there are no mutual follows
            } else return new int[0];   //if this user has no follows, there are no mutual follows
            
            //create an array of mutual follows from the lists (each with the earliest date of the two relations)
            while(follows1 != null) {
                ListElement<IdDatePair> tmp = follows2;
                while(tmp != null) {
                    if(tmp.getValue().getId() == follows1.getValue().getId()) {
                        count++;
                        if(tmp.getValue().getDate().compareTo(follows1.getValue().getDate()) <= 0) {
                            //this is earlier
                            mutual = addToMutual(mutual, tmp.getValue());
                        } else mutual = addToMutual(mutual, follows1.getValue());
                    }
                    tmp = tmp.getNext();
                }
                follows1 = follows1.getNext();
            }
            int[] mutualFollows = new int[count];   //array to be returned, with according size
            int i  = 0;
            //move users from list into array
            while(mutual != null && i < count) {
                mutualFollows[i] = mutual.getValue().getId();
                System.out.println(mutualFollows[i]);
                mutual = mutual.getNext();
                i++;
            }
            return mutualFollows;
        }
        //gets an array of mutual followers of users with thenpassed ids        
        public int[] getMutualFollowers(int id1, int id2) {
            int location = id1 % table.length;
            int count = 0;//counts found users to declare array to be returned with appropriate size
            IdRelationsPair pair = table[location].get(id1);
            //lists which will store follows for each user
            ListElement<IdDatePair> followers1 = null;
            ListElement<IdDatePair> followers2 = null;
            ListElement<IdDatePair> mutual = null;
            if(pair != null) {
                followers1 = pair.getRelations().getFollowersWithDate();
                if(followers1 == null) return new int[0];   //if this user has no followers, there are no mutual follows
            } else return new int[0];   //if this user has no followers, there are no mutual follows
            location = id2 % table.length;
            pair = table[location].get(id2);
            if(pair != null) {
                followers2 = pair.getRelations().getFollowersWithDate();
                if(followers2 == null) return new int[0];   //if this user has no followers, there are no mutual follows
            } else return new int[0];   //if this user has no followerss, there are no mutual follows
            
            //create a list of mutual followers (each with the earliest date of the two relations)
            while(followers1 != null) {
                ListElement<IdDatePair> tmp = followers2;
                while(tmp != null) {
                    if(tmp.getValue().getId() == followers1.getValue().getId()) {
                        count++;
                        if(tmp.getValue().getDate().compareTo(followers1.getValue().getDate()) <= 0) {
                            //this is earlier
                            mutual = addToMutual(mutual, tmp.getValue());
                        } else mutual = addToMutual(mutual, followers1.getValue());
                    }
                    tmp = tmp.getNext();
                }
                followers1 = followers1.getNext();
            }
            int[] mutualFollowers = new int[count]; //array to be returned
            int i = 0;
            //move users from list into array
            while(mutual != null && i < count) {
                mutualFollowers[i] = mutual.getValue().getId();
                System.out.println(mutualFollowers[i]);
                mutual = mutual.getNext();
                i++;
            }
            return mutualFollowers;
        }
        //add the passed relation (id and date) to the passed list
        public ListElement<IdDatePair> addToMutual(ListElement<IdDatePair> mutual, IdDatePair pair) {
            if(mutual == null) return new ListElement<>(pair);
            ListElement<IdDatePair> newPair = new ListElement<>(pair);
            ListElement<IdDatePair> tmp = mutual;
            while(tmp != null) {
                if(newPair.getValue().getDate().compareTo(tmp.getValue().getDate()) >= 0) {
                    if(tmp == mutual) {
                        newPair.setNext(mutual);
                        mutual.setPrev(newPair);
                        return newPair;
                    } else {
                        newPair.setPrev(tmp.getPrev());
                        newPair.setNext(tmp);
                        tmp.getPrev().setNext(newPair);
                        tmp.setPrev(newPair);
                        return mutual;
                    }
                } else if(tmp.getNext() == null) {
                    tmp.setNext(newPair);
                    newPair.setPrev(tmp);
                    return mutual;
                } else tmp = tmp.getNext();
            }
            return mutual; //just for java to be pleased
        }
    }
    
    //a linked list of the top users (most followed)
    class TopList {
        ListElement<IdPopPair> head;    //end from which search starts
        ListElement<IdPopPair> tail;    //end at which new users are inserted
        int size;
        
        public TopList() {
            head = null;
            tail = null;
            size = 0;
        }
        
        public void add(int id) {
            boolean added = false;  //assume the user was in the list
            //if this is the first user added
            if(head == null) {
                head = new ListElement<>(new IdPopPair(id));
                tail = head;
                added = true;
                size++;
            }
            else {
                //search for this user
                ListElement<IdPopPair> tmp = head;
                while( (tmp != null) && (!added) ) {
                    if(tmp.getValue().getId() == id) {
                        tmp.getValue().incPop();
                        added = true;   //user was found and their 'popularity' was incremented
                    }
                    else tmp = tmp.getNext();
                }
                if(added) {
                    //keep moving the user up in the list if the popularity of user above is lower
                    while(  (tmp.getPrev() != null) && (tmp.getValue().getPop() > tmp.getPrev().getValue().getPop()) ) {
                        ListElement<IdPopPair> prev = tmp.getPrev();
                        if(prev.getPrev() != null) {
                            prev.getPrev().setNext(tmp);
                        }
                        tmp.setPrev(prev.getPrev());
                        if(tmp.getNext() != null) {
                            tmp.getNext().setPrev(prev);
                        }
                        prev.setNext(tmp.getNext());
                        tmp.setNext(prev);
                        prev.setPrev(tmp);
                        if(tmp.getPrev() == null) head = tmp;
                        if(prev.getNext() == null) tail = prev;
                    }
                } else {
                    //if user was not in the list, add to tail and leave there (will have popularity 1 <= least popular)
                    tail.setNext(new ListElement<>(new IdPopPair(id)));
                    tail.getNext().setPrev(tail);
                    tail = tail.getNext();
                    size++;
                }
            }
        }
        
        //return all the users with followers sorted in descending order by popularity
        public int[] getAll() {
            int[] all = new int[size];
            int count = 0;
            ListElement<IdPopPair> tmp = head;
            while(tmp != null) {
                all[count] = tmp.getValue().getId();
                System.out.println(all[count]);
                tmp = tmp.getNext();
                count++;
            }
            return all;
        }
        
    }
    
    FollowHashMap relations;
    TopList top;
    
    public FollowerStore() {
        relations = new FollowHashMap();
        top = new TopList();
    }

    public boolean addFollower(int uid1, int uid2, Date followDate) {
        if(relations.add(uid1, uid2, followDate)) {
            top.add(uid2);
            return true;
        } 
        return false;
    }  

    public int[] getFollowers(int uid) {
        return relations.getFollowersDate(uid);
    }

    public int[] getFollows(int uid) {
        return relations.getFollowingDate(uid);
    }

    public boolean isAFollower(int uidFollower, int uidFollows) {
        int[] followers = relations.getFollowersId(uidFollows); //returns an empty array if uidFollows has no followers
        for(int i = 0; i < followers.length; i++)
            if(followers[i] == uidFollower) return true;
        return false;
    }

    public int getNumFollowers(int uid) {
        return relations.getNumFollowers(uid);
    }

    public int[] getMutualFollowers(int uid1, int uid2) {
        return relations.getMutualFollowers(uid1, uid2);
    }

    public int[] getMutualFollows(int uid1, int uid2) {
        return relations.getMutualFollows(uid1, uid2);
    }

    public int[] getTopUsers() {
        return top.getAll();
    }

}
