#import "../lib.typ" :*

= Specifications completion


== HTML-specific features

In addition to the requirements, we implemented a series of new features:

- logout button in Login.html

- The top navigation bar, where the username is shown and buttons to relocate are located

- Success and error messages for different operations


In the traditional HTML-based application, every user action results in a full page reload. This means that whenever the user interacts with the system—such as navigating to another view, submitting a form, or clicking a button—the browser either redirects to a different page or reloads the current one.

To ensure that users see the most up-to-date information, especially in a competitive environment like auctions, certain pages are configured with an automatic refresh timer. In particular, the BuyPage, DetailPage, and OfferPage are refreshed every 5 seconds. This periodic reloading guarantees that users always receive the latest information about active offers and auction expiration times. However, while this approach ensures data consistency, it comes at the cost of performance overhead and user experience disruption, since each refresh reloads the entire page including static content that has not changed.
Every action from user will be redirected to another page, or same page but reloading it.

== RIA-specific features<ria-specifics>

In contrast, within a Rich Internet Application (RIA), we eliminate the reliance on page-level refresh timers. Instead of reloading the entire page, the application uses partial updates (e.g., via asynchronous requests) to keep the interface synchronized with the latest server state.

For example, when a user places a new offer, the system automatically reloads only the offer table rather than refreshing the entire page. This ensures that the most recent bids from all participants are displayed without unnecessary re-rendering of unrelated content. Similarly, other data tables—such as auction listings, user history, or closed auction results—are refreshed individually only when needed.

This RIA approach significantly improves responsiveness, reduces bandwidth consumption, and provides a smoother user experience, since users are no longer interrupted by constant full-page reloads. It also better reflects real-time interactions, which is particularly valuable in time-sensitive scenarios like online auctions.

