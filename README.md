# Social Network Data Processing
## Context
Coursework for first year module CS126 Design of Information Structures.
Module teaches about data structures (and their efficiency and use) and algorithms used to process these.

## Purpose
To develop Java classes which store manipulate user data and their post on a fictional social media website named Witter.
Tests included requesting specific sets of data (e.g. top users, top tags, top posts, most recent posts).
> note: data was sent in random order.

Assesment based on correctness of results, efficiency, memory usage and quality of code.

## Mark
96% for Coursework.
90% module total.
Runner-up for Morgan Stanley prize.

## Classes
### FollowerStore
Stores data of users' followers using Hash Tables, self-balancing AVL Trees and Linked Lists.
There are 4 AVL Trees for each user containing their followers and the users following them in order to efficiently insert and access this data (either by follow date or by ID).
A Doubly Linked List contains all users sorted by their popularity (calculated based on followers) for quick insertion and traversing.

### UserStore
Stores data of all users.
A Hash Map is used to store all users for quick insertion and individual retrieval.
A self-balancing AVL Tree is used in order to efficiently insert users ordered by their join date.

### WeetStore
Stores data of all posts ("weets") by all users.
A Hash Map of self-balancing AVL Trees stores the sorted posts (tree elements, sorted by date) of each user (map position) for fast access to all user posts and quick insertion of new posts for each user.
A self-balancing AVL Tree stores, for each day of activity, self-balancing AVL Trees containing the posts o that day, sorted by time. This ensures efficient retrieval of posts during specific times and quick insertion.
>note: due to the large volume of posts, the double layer of trees ensures that insertion and searching do not require to skim through unnecessary posts on days that are not close to the requested date of a search, or to the date of a new post.
