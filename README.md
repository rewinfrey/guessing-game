### Guessing Game ###

<hr />

##### About #####

This is an 8th Light Clojure 101 homework assignment.

Personally I'm also interested in using this repo as a way to play with Datomic in a simple, easy to configure web app.

The guessing game rules are as follows:

Users submit a number (between 1 and 100) as their guess.

1. If the user's number is higher than the application selected correct number, a message is shown to the user saying "too high".
2. If the user's number is lower than the choosen number, a "too low" message is displayed to the user.
3. The user is given 7 guesses to find the correct number.
4. And if the user correctly guesses the number, "correct" is displayed and the game resets.

Extra technical challenges related to the web app:

1. If the user has no previous sesssion (as determined by a cookie value), then a new session is created.
2. For each guess, the session keeps track of the total number of guesses remaining, and displays this to the user.
3. Using datomic, the user can see the history of all their previous games (as a timeline view of the their web history).
4. The presentation layer should be fully responsive.
5. If the user is unable to correctly guess the number after the 7th guess, the correct number is displayed, and the user is prompted to start again.

##### Usage #####

Clone the repo.

`$ lein deps`

Ensure the specs pass:

`$ lein spec`

Start the server:

`$ lein ring server`

Investigate: how to make datomic shareable through a repo?
