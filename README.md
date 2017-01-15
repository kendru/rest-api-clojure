<h1>Frameworks</h1>

<li><a href="https://github.com/weavejester/compojure">Compojure</a> is a small routing library for Ring that allows web applications to be composed of small, independent parts.
<li><a href="https://github.com/ring-clojure/ring-json">Ring-JSON</a> is a standard Ring middleware functions for handling JSON requests and responses.
<li><a href="https://github.com/clj-time/clj-time">clj-time</a> is a date and time library for Clojure, wrapping the Joda Time library.
<li><a href="https://github.com/jakemcc/lein-test-refresh">lein-test-refresh</a>
 is a Leiningen plug-in that automatically refreshes and then runs your clojure.test tests when a file in your project changes.


<h1>Exercise</h1>

This exercise consists in implementing those basic features of a checking account:

<b>First step:</b> adding the operations on that checking account

Create a HTTP service in which you can add an operation to a given checking account, identified by the account number. This operation will contain the account number, a short description, a amount and the date it happened. Keep in mind you have to support both credit and debit operations, i.e, both putting and taking money out of the account.

E.g:
Deposit 1000.00 at 15/10
Purchase on Amazon 3.34 at 16/10
Purchase on Uber 45.23 at 16/10
Withdrawal 180.00 at 17/10

Deposits can take days to be acknowledged properly, so you should support insertion in any date order.

<b>Second step:</b> Get the current balance

Create a HTTP endpoint which returns the current balance of a given account.
This balance is the sum of all operations until today, so the customer can know how much money they still have.

E.g: for the sample above, the customer would have 1000.00 - 3.34 - 45.23 - 180.00 = 771.43

<b>Third step:</b> Get the bank statement

Create a HTTP endpoint which returns the bank statement of a period of dates.
This statement will contain the operations of each day and the balance at the end of each day.

E.g:
15/10:
Deposit 1000.00
Balance: 1000.00

16/10:
Purchase on Amazon 3.34
Purchase on Uber 45.23
Balance: 951.43

17/10:
Withdrawal 180.00
Balance: 771.43

<b>Forth step:</b> Compute periods of debt.

Create a HTTP endpoint which returns the periods which the account's balance
was negative, i.e, periods when the bank can charge interest on that account.

E.g: if we have two more operations (current balance is 771.43):
Purchase of a flight ticket 800.00 at 18/10
Deposit 100.00 at 25/10

The endpoint would return:
Principal: 28.57
Start: 18/10
End: 24/10

This endpoint should return multiple periods, if applicable, and omit the "End:"date if the account's balance is currently negative.

All HTTP endpoints should accept and return JSON payloads as requests and
responses. 

<h1>Endpoints</h1>

<b>Second step:</b> Get the current balance

GET /balance/:acc-number

Use for example: /balance/12345678
