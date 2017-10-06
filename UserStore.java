    /**
    * A Hash Map is used for fast access and insertion of users by ID (used by getUser())
    * An AVL Tree is used for fast insertion and sorting of users by the date they joined on (used by getUsers(), getUsersContaining() and getUsersJoinedBefore())
    * 
    * HasMap class and all classes used by it designed with the help of Shalin Doshi during CS126 labs
    * AVL Tree class based on code found at : http://coding-for-fun-in-java.blogspot.co.uk/2012/04/self-balancing-tree-in-java.html
    * Assumed an empty search should return all users
    * "Date before" considered inclusive according to updates on module web page
    * @author: 1525713
    */

package uk.ac.warwick.java.cs126.services;
import uk.ac.warwick.java.cs126.models.User;
import java.util.Date;

public class UserStore implements IUserStore {
    
    //class based on KeyValuePair used in cs126 labs
    class KeyUserPair {

        protected int key;
        protected User user;
        
        public KeyUserPair(int k, User u) {
            key = k;
            user = u;
        }
        
        public int getKey() {
            return key;
        }
        
        public User getUser() {
            return user;
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
    
    //based on the KeyValuePairLinkedList class from the CS126 labs for easy use of Usec class specific methods
    class KeyUserPairLinkedList {

        protected ListElement<KeyUserPair> head;
        protected int size;
        
        public KeyUserPairLinkedList() {
            head = null;
            size = 0;
        }
        
        public void add(int key, User user) {
            this.add(new KeyUserPair(key,user));
        }

        public boolean add(KeyUserPair kup) {
            ListElement<KeyUserPair> new_element = new ListElement<>(kup);
            ListElement<KeyUserPair> tmp = head;
            //add this user to an existing list in this bucket or create a new list of users if this is the one in the bucket
            while(tmp != null) {
                if(tmp.getValue().getKey() > new_element.getValue().getKey()) {
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
        
        public ListElement<KeyUserPair> getHead() {
            return head;
        }
        
        public KeyUserPair get(int key) {
            ListElement<KeyUserPair> tmp = head;
            while(tmp != null) {
                if(tmp.getValue().getUser().getId() == key) {
                    return tmp.getValue();
                }
                tmp = tmp.getNext();
            }
            
            return null;
        }
    }

    //class based on HashMap class developed in labs
    class HashMap {

        protected KeyUserPairLinkedList[] table;
        
        public HashMap() {
            this(16001);
        }
        
        public HashMap(int s) {
            table = new KeyUserPairLinkedList[s];
            initTable();
        }
        
        protected void initTable() {
            for(int i = 0; i < table.length; i++) {
                table[i] = new KeyUserPairLinkedList();
            }
        }

        public boolean add(int key, User user) {
            int location = key % table.length;
            if(table[location].get(key) == null) {
                table[location].add(key,user);
                return true;
            }
            return false;
        }

        public User get(int key) {
            int location = key % table.length;
            KeyUserPair pair = table[location].get(key);
            if(pair != null) return pair.getUser();
            return null;
        }
    }
    
    class Node {
        protected User user;
        protected int height;
        protected Node left;
        protected Node right;
    
        Node(User user) {
            this.user = user;
            height = 0;
            left = null;
            right = null;
        }
    }

    class AVLTree {
        
        private Node root;
        private int size;
        private int count;
        private ListElement<User> usersList;
        
        public AVLTree () {
            root = null;
            size = 0;
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
            //rotate the tree to the left and preserve sortedness
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
        
        public void insert(User user) {
            root = insert(root, user);
        }
        public Node insert(Node tree, User user) {
            
            //the height of a tree is the number of nodes in the longest chain of nodes in that tree
            int lh = 0; //the height of the left tree
            int rh = 0; //the height of the right tree
            int balance;    //the difference between the left and right subtrees of this node (the difference should never be more than 1)
            
            if(tree == null) {
                tree = new Node(user);
                return tree;
            }
            else if(user.getDateJoined().getTime() > tree.user.getDateJoined().getTime()) {
                tree.right = insert(tree.right, user);
            }
            else if(user.getDateJoined().getTime() <= tree.user.getDateJoined().getTime()) {
                tree.left = insert(tree.left, user);
            }            
            
            //set the height of the new (modified) tree       
            if (tree.left != null) lh = tree.left.height;
            if (tree.right != null) rh = tree.right.height;
            
            if(lh >= rh) tree.height = lh + 1;
            else tree.height = rh + 1;
            balance = lh - rh;
            
            //left-left case
            if( (balance > 1) && (user.getDateJoined().getTime() <= tree.left.user.getDateJoined().getTime()) ) {
                //rotate this right
                return rotateRight(tree);
            }
            //left-right case
            else if( (balance > 1) && (user.getDateJoined().getTime() > tree.left.user.getDateJoined().getTime()) ) {      
                //rotate left tree left
                tree.left = rotateLeft(tree.left);
                //rotate this right
                return rotateRight(tree);
            }
            //right-right case
            else if( (balance < -1) && (user.getDateJoined().getTime() > tree.right.user.getDateJoined().getTime()) ) {       
                //rotate this left
                return rotateLeft(tree);
            }
            //right-left case
            else if( (balance < -1) && (user.getDateJoined().getTime() <= tree.right.user.getDateJoined().getTime()) ) {     
                //rotate right tree right
                tree.right = rotateRight(tree.right);
                //rotate this left
                return rotateLeft(tree);
            }
            return tree;
        }
        
        //gets all the users sorted in descending order by date
        public User[] getInOrder() {
            User[] allUsers = new User[size];   //array will store users to be returned
            count = 0;
            getInOrder(root, allUsers);
            return allUsers;
        }
        public void getInOrder(Node tree, User[] allUsers) {
            if(tree != null) {
                getInOrder(tree.right, allUsers);
                allUsers[count] = tree.user;
                count++;
                getInOrder(tree.left, allUsers);
            }
        }
        
        //returns all the users containing query in their message, sorted in descending order by date
        public User[] getContaining(String query) {
            usersList = new ListElement<>(null);    //list will store users while tree is being traversed
            count = 0;  //counts users to declare the array to be returned with appropriate size
            if( (query != null) && (!query.isEmpty()) )  {
                getContaining(root, query); //look for users containing the query in their name starting from root
                User[] containing = new User[count];
                for(int i = 0; (i < count) && (usersList != null); i ++) {
                    containing[i] = usersList.getValue();
                    usersList = usersList.getNext();
                }
                usersList = null;
                return containing;
            } else return getInOrder(); //if query was empty, return all users
        }
        private void getContaining(Node tree, String query) {
            if(tree != null) {
                getContaining(tree.left, query);    //checks users in the subtree to the left of this node
                //check if this user name contains the string and if so, add this user to the list
                if(tree.user.getName().toLowerCase().contains(query.toLowerCase())) {
                    ListElement<User> tmp = new ListElement<>(tree.user);
                    tmp.setNext(usersList);
                    usersList.setPrev(tmp);
                    usersList = tmp;
                    count++;
                }
                getContaining(tree.right, query);   //checks users in the subtree to the right of this node
            }
        }
        
        //returns an array of all users that joined before the date (inclusive - according to updates on the module web page)
        public User[] getBefore(Date dateBefore) {
            usersList = new ListElement<>(null);    //the list that will store users as the tree is being processed
            count = 0;  //counts the found users in order to declare the array with appropriate size
            getBefore(root, dateBefore);    //get the users by starting at the root of the main tree
            User[] containing = new User[count];    
            //move the users from the list into the array
            for(int i = 0; (i < count) && (usersList != null); i ++) {
                containing[i] = usersList.getValue();
                usersList = usersList.getNext();
            }
            usersList = null;
            return containing;
        }
        private void getBefore(Node tree, Date dateBefore) {
            if(tree != null) {
                getBefore(tree.left, dateBefore);   //get the users in the subtree to the left of this node
                //whether this date is before, after or equal to dateBefore, elements to the left only get earlier and should be checked
                //this date is from before dateBefore, but later dates (to the right) might be too
                if( tree.user.getDateJoined().before(dateBefore) ) {
                    //add this date to the list
                    ListElement<User> tmp = new ListElement<>(tree.user);
                    tmp.setNext(usersList);
                    usersList.setPrev(tmp);
                    usersList = tmp;
                    count++;
                    getBefore(tree.right, dateBefore);  //get users in the subtree to the right of this node
                } else if( tree.user.getDateJoined().equals(dateBefore)) {
                    //there is no point in checking later dates but this date is still valid
                    ListElement<User> tmp = new ListElement<>(tree.user);
                    tmp.setNext(usersList);
                    usersList.setPrev(tmp);
                    usersList = tmp;
                    count++;
                }
            }
        }
    }
    
    protected HashMap users;
    protected AVLTree orderedUsers;
       
    public UserStore() {
        this.users = new HashMap(16001); //the hashmap with 16k locations will be able to store users from year 2000 to 2051
        this.orderedUsers = new AVLTree();
    }

    public boolean addUser(User usr) {
        orderedUsers.incSize();
        if( users.add(usr.getId(),usr) ) {
            orderedUsers.insert(usr);
            return true;
        }
        return false;
    }

    public User getUser(int uid) {
        return users.get(uid);
    }

    public User[] getUsers() {
        return orderedUsers.getInOrder();
    }
        
    //return an array of all the Users whose names contain the given queryString, sorted such that the most recently joined User is first
    public User[] getUsersContaining(String query) {
        return orderedUsers.getContaining(query);
    }
        
    //return an array of all the Users joined before the given date, sorted such that the most recently joined User is first in the list
    public User[] getUsersJoinedBefore(Date dateBefore) {
        return orderedUsers.getBefore(dateBefore);
    }
}