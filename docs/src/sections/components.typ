#import "../lib.typ": *

= Codebase overview

== Components

/ Introduction : The projects is built upon the following components:

+ DAOs
  - AuctionDAO
  - ItemDAO
  - UserDAO

1. Entities
  - Auction
  - Item
  - User
  - Offer
  - ClosedAuction
  - OpenAuction
  - Result

These are coded following the JavaBeans model.

1. Servlets
  - AuctionServlet
  - BuyServlet
  - CloseServlet
  - HomeServlet
  - LoginServlet
  - OfferServlet
  - RegisterServlet
  - SellServlet
  - UploadItemServlet

+ Utils (short-term for Utilities)
  - DBUtils
  - Util
  - ThymeleafConfig

// #colbreak()

== DAOs methods

AuctionDAO methods:

- `getResultsByAuction(List<Auction> auctions)` \ 
- `insertOffer(int userId, int auctionId, double offeredPrice)`\ 
- `getMaxOfferOfAuction(int auctionId)`\ 
- `getMaxOffersByAuction(List<Auction> auctions)`\ 
- `allItemsByAuction(List<Auction> auctions)`\ 
- `findAllAuctionsNotClosed(int userId)`\ 
- `findAllOpenAuction(int userId)`\ 
- `findAllClosedAuctionsAndResult(int userId)`\ 
- `findAllAuctionClosed(int userId)`\ 
- `findById(int auctionId)`\ 
- `findAllOffersByAuction(int auctionId)`\ 
- `closeAuction(int auctionId)`\ 
- `findUserAddressById(int userId)`\ 
- `updateResult(int auctionId)`\ 
- `createAuction(int userId, String title, double startingPrice, int minIncrement, LocalDateTime ending_at)`\ 
- `insertItems(int auctionId, int[] items)`\ 
- `findAllOpenAuctionByKeywords(String[] keywords)`\ 
- `findAllWonAuctions(int userId)`\
- `find_open_historical_auctions(String[] visitedAuctions)`\

ItemDAO methods:

- `uploadItem(String name, String description, String file_path, double price, int user_id)`\ 
- `findAllItemInAuction(int auction_id)`\ 
- `findAllItemNotInAuction(int userId)`\ 
- `sellItemInAuction(int auction_id)`\ 
- `calculateTotalPrice(int[] itemIds)`\ 


UserDAO methods:

- `createUser(String username, String password, String first_name, String last_name, String address)`\ 
- `login(String username, String password)`\ 
- `findByUsername(String username)`\ 
- `mapRowToUser(ResultSet rs)`\ 
- `findNameById(int id)`\ 

== RIA subproject

One single javascript,in which exists following fucntion, everything will be initiallized after page is loaded.
- `showRegisterLink()` --- Load form for regitration
- `loginLink()` --- Load form for login
- `showPage(page, auctionId)` --- Show the page based on the page parameter and auctionId(optional)
- `saveHistory(auctionId)` --- Save a history of a auction in the localStorage
- `showError(message)` --- Load error messages on the top nav bar
- `showMessage(message)` --- Load notification messages on the top nav bar
- `getBuyPageInfo()` --- load search history table, won auction table
- `getSellPageInfo()` --- Load open auction table, closed auction table, available item table, set minimum endDate
- `getOfferPageInfo()` --- Load open auction table, closed auction table, available item table, set minimum offer
- `getDetailPageInfo(auctionId)` --- Load detailed information of an auction and its offer table
- `login()` --- Load last page visited after successful login
- `register()` --- Load login page after successful registration
- `uploadItem()` --- Load newly uploaded item to the available item table after successful upload
- `createAuction()` --- Load newly created auction to the open auction table after successful creation
- `offer()` --- Load renewed offer table in OFFERTA.html after successful offer
- `logout()` --- Load loginView and invalidate session

Utils:
- `formatDate(rawDateObj)` --- Format a LocalDateTime object to a string in the format "YYYY-MM-DD HH:mm:ss"
- `verifyInputs(username, password, firstName, lastName, address)` --- verify if the register inputs are valid

And finally the interfaces, which are the Typescript translation of the Record classes.

#pagebreak()

#show: table-styles.with()

#figure(
  placement: top,
  scope: "parent",
  table(
    columns: 4,
    align: horizon,
    inset: 7pt,
    table.header(
      table.cell(colspan: 2)[Client side],
      table.cell(colspan: 2)[Server side],
      [Event],
      [Action],
      [Event],
      [Action],
    ),

    [LoginView $=>$ Login form $=>$ Submit], [Data validation],                 [POST (`username`, `password`)],                                 [Credentials check],
    [LoginView $=>$ LastPage = 'sell_page'], [getSellPageInfo()],               [GET (`openAuctions, closedAuctions, availableItems`)],          [Queries openAuctions, closedAuctions, availableItems],
    [LoginView $=>$ LastPage = 'buy_page'], [getBuyPageInfo()],                 [GET (`wonAuctions, historyAuctions`)],                         [Queries wonAuctions, historyAuctions],
    [BuyView $=>$ Search button], [load search result table],                   [GET (`resultAuctions`)],                                    [Queries resultAuctions],
    [BuyView $=>$ Click on an auction], [load OfferView],                       [GET (`auction, offers, items`)],                               [Queries auction, offers, items],
    [OfferView $=>$ Offer form $=>$ offer], [load offer table],                 [POST (`offeredPrice`)],                                            [Insert offer in the offers table],
    [SellView $=>$ Click on an auction], [load DetailView],                     [GET (`auction, offers, items`)],                        [Queries auction, offers, items],
    [SellView $=>$ Auction form $=>$ Submit], [load openAuctions table],        [POST (`title, startingPrice, minIncrement, ending_at`)], [Insert auction in the auction table],
    [SellView $=>$ Item form $=>$ Submit], [load availableItems table],         [POST (`name, description, image, price`)],                     [Insert item in the items table],
    [DetailView $=>$ Click on close button], [change auction status to closed], [POST (`auctionId`)],                                       [Check if request is valid and update the auctions table],
    [navbar $=>$ Sell Button], [load SellView],                                 [GET (`openAuctions, closedAuctions, availableItems`)],         [Queries openAuctions, closedAuctions, availableItems],
    [navbar $=>$ Buy Button], [load BuyView],                                       [GET (`wonAuctions, historyAuctions`)],                                      [Queries wonAuctions, historyAuctions],
    [navbar $=>$ Logout Button], [load LoginView and clear localStorage],       [POST(`logout`)],                                                           [invalidate session],
  ),
  caption: [Events & Actions.],
)

#figure(
  placement: top,
  scope: "parent",
  table(
    columns: 4,
    align: horizon,
    table.header(
      table.cell(colspan: 2)[Client side],
      table.cell(colspan: 2)[Server side],
      [Event],
      [Controller],
      [Event],
      [Controller],
    ),
    [LoginView $=>$ Login form $=>$ Submit], [login()],[POST (`username`, `password`)],[loginServlet],
    [LoginView $=>$ LastPage = 'sell_page'],[getSellPageInfo()],[GET (`openAuctions, closedAuctions, availableItems`)],[SellServlet],
    [LoginView $=>$ LastPage = 'buy_page'],[getBuyPageInfo()],[GET (`wonAuctions, historyAuctions`)],[BuyServlet],
    [BuyView $=>$ Search button],[search()],[GET (`resultAuctions`)],[BuyServlet],
    [BuyView $=>$ Click on an auction],[getOfferPageInfo()],[GET (`auction, offers, items`)],[OfferServlet],
    [OfferView $=>$ Offer form $=>$ offer],[offer()],[POST (`offeredPrice`)],[OfferServlet],
    [SellView $=>$ Click on an auction],[getDetailPageInfo()],[GET (`auction, offers, items`)],[AuctionServlet],
    [SellView $=>$ Auction form $=>$ Submit],[createAuction()],[POST (`title, startingPrice, minIncrement, ending_at`)],[AuctionServlet],
    [SellView $=>$ Item form $=>$ Submit],[uploadItem()],[POST (`name, description, image, price`)],[uploadItemServlet],
    [DetailView $=>$ Click on close button],[closeAuction()],[POST (`auctionId`)],[CloseServlet],
    [navbar $=>$ Sell Button],[getSellPageInfo()],[GET (`openAuctions, closedAuctions, availableItems`)],[SellServlet],
    [navbar $=>$ Buy Button],[getBuyPageInfo()],[GET (`wonAuctions, historyAuctions`)],[BuyServlet],
    [navbar $=>$ Logout Button],[logout()],[GET(`logout`)],[LoginServlet],
  ),
  caption: [Events & Controllers (or event handlers).],
)
