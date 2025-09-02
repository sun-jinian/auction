#import "../lib.typ":*

#set align(horizon)

= abstract

In addition to the minimum requirements, we implemented several new features based on project guidelines and design decisions:

=== Navigation
- HTML version: Users can navigate to the Buy Page or Sell Page only through the Home Page.
- JavaScript version: There is no Home Page. After login, users are automatically redirected depending on their last action: 
  If the last action was creating an auction, the user is redirected to the Sell Page.In all other cases, the user is redirected to the Buy Page.

=== Logout and Navbar
- HTML version: The logout button is available only on Login.html.
- JavaScript version: A persistent navigation bar is always present, providing buttons for switching views, a logout button, and an area for displaying both success and error messages.
- Additionally, upon logout in the JavaScript version, the user’s last action and historical visited auctions are deleted.

=== Detail Page Behavior
- In both versions, the Detail Page displays the list of items.

=== Data Synchronization and Refresh Strategy
- HTML version: To ensure up-to-date information, the Buy Page, Detail Page, and Offer Page are configured with an automatic refresh timer every 5 seconds. This guarantees data consistency but introduces performance overhead and disrupts user experience, since the entire page (including static content) is reloaded.
- JavaScript version: Instead of periodic page-level refreshes, the application uses partial updates via asynchronous requests. This ensures the interface remains synchronized with the latest auction information without unnecessary re-rendering. For example, when a user places a new offer, the system updates only the Offer Page details (expiration time, maximum offer, etc.), ensuring that all participants see the latest bids without refreshing unrelated content.

For the documentation esthetics, we based it on the template from repo #link("https://github.com/VictuarVi/wt-project-2025")[on Github]