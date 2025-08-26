#import "../lib.typ": *

= Project submission breakdown<project-breakdown>

== Database logic

#let db_legend = (
  entity("Entity"),
  attr("Attribute"),
  attr_spec([Attribute \ specification]),
  rel("Relationship"),
)
#legend(db_legend)

Each #entity[user] has a #attr[id], #attr[username], #attr[password], #attr[first_name], #attr[last_name], #attr[address]. Each #entity[auction] has #attr[id], #attr[creator], #attr[title], #attr[start price], #attr[minimum increment], #attr[expiration], #attr[status], #attr[time of creation] and #attr[file]. Furthermore:

- Suppose the #attr_spec[id] is auto-increment
- Suppose #attr_spec[status] of auction can be closed only on creator's request, even expired
- Suppose user can make offer if and only if 2 conditions are met: auction is open & not exipired
- One can not make an offer on his own auction
- There are no deletion in database after auction closed or items sold, only make it not visible

After the login, the user is able to #rel[create items] by loading their data and then put them in an auction. A #entity[auction] contains #rel[a set of items]. A playlist has a #attr[title].

#colbreak()

== Behaviour

#let bhr_legend = (
  user_action("User action"),
  server_action("Server action"),
  page("HTML page"),
  element("Page element"),
)
#legend(bhr_legend)

After the login, the user #user_action[accesses] the #page[HOME PAGE] which has two link sublink that direct to #page[SELL] or #page[BUY], #page[SELL] #server_action[displays] the #element[list of closed and open auctions] ordered by descending creation date and #element[list of items] created that are available, ; a #element[form to create item] and a #element[form to create a auction]. 
The auction form:
- #server_action[displays] the #element[list of open auction and closed auction] ordered by expiration date descending
- Allows to #user_action[select] one or more items

The item form:
- #server_action[displays] the #element[list of user items] available to put in auction

When a user #user_action[clicks] on a #element[auction id] in the #page[SELL], the application #server_action[loads] the #page[DETTAGLIO]\; It contains a #element[table of offers made by users] and a #element[block of information on this auction].

- Every cell contains the offer's id, user'name who made that offer, price they offered and when they made offer
- The offers are ordered by the time they made offer


#figure(
  placement: bottom,
  scope: "parent",
  image(
    width: 100%,
    height: 100%,
    fit: "cover",
    "../img/ifml/ifml-pure_html.png",
  ),
  caption: "IFML diagram (HTML).",
)<html-ifml-diagram>

#pagebreak()
== JavaScript version

Create a client-server web application that modifies the previous specification as follows:

- After the login, the entire application is built as a single webapp#comment("RIA")

- If the user accesses the application for the first time, it displays the content of the BUY page.
  If the user has already used the application, it displays the content of the SELL page if the user’s last action was the creation of an auction; otherwise, it displays the content of the BUY page with the list (possibly empty) of auctions that the user previously clicked on and that are still open.

  The information about the last action performed and the visited auctions is stored on the client side for a duration of one month.

- Every user interaction is handled without completely reloading the page, but instead triggers an asynchronous server call and, if necessary, updates only the content that needs to be refreshed as a result of the event.

/ Saving history : The application must allow the user to save last action and all historical visit locally. From the #page[LOGIN VIEW], the user is able to #user_action[access] a either #page[BUY VIEW] if last action is NOT #user_action[creation of an auction], otherwise user #user_action[access] #page[SELL VIEW], every time the user #user_action[visit] #page[OFFERTA VIEW], it will save a history locally for 30 days.

#colbreak()

Databse remains the same as before.


#figure(
  placement: top,
  scope: "parent",
  caption: [ER diagram (HTML).],
  rect(
    radius: 4pt,
    image("../img/er/er_diagram.png", width: 100%)
  )
)<er-diagram>

#figure(
  placement: bottom,
  scope: "parent",
  image(
    width: 110%,
    height: 100%,
    fit: "cover",
    "../img/ifml/ifml-ria.png",
  ),
  caption: "IFML diagram (RIA).",
)<ria-ifml-diagram>
