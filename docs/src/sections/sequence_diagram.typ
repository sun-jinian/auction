#import "../lib.typ": *

#show: thymeleaf_trick.with()

= Sequence diagrams

#pagebreak()

#seq_diagram(
  "LoginServlet sequence diagram",
  diagram({
    _par("A", display-name: "Client")
    _par("B", display-name: "LoginServlet")
    _par("G", display-name: "Request")
    _par("H", display-name: "ctx")
    _par("C", display-name: "Thymeleaf", shape: "custom", custom-image: thymeleaf)
    _par("D", display-name: "UserDAO")
    _par("E", display-name: "Session")
    _par("F", display-name: "Home")

    // get
    _seq("A", "B", enable-dst: true, comment: "doGet()")
    _seq("B", "G", enable-dst: true, comment: [getParameter (\"logout\")])
    _seq("G", "B", disable-src: true, comment: [return logout])
    _seq("B", "G", comment: [[logout = 1] \ ? getSession(false)])
    _seq("G", "B", disable-src: true, comment: [session.invalidate()])
    _seq("B", "A", enable-dst: true, comment: "Login.html")

    // post
    _seq("A", "B", enable-dst: true, comment: "doPost()")
    _seq("B", "D", enable-dst: true, comment: [getParameter (\"username\")])
    _seq("D", "B", disable-src: true, comment: [return username])
    _seq("B", "D", enable-dst: true, comment: [getParameter (\"password\")])
    _seq("D", "B", disable-src: true, comment: [return password])
    _seq("B", "D", enable-dst: true, comment: "checkUser (nickname,password)")
    _seq("D", "B", disable-src: true, comment: "return result")
    _seq("B", "H", comment: [[result == 0 || result == 3] \ ? setVariable ("error", message)])
    _seq("B", "C", enable-dst: true, comment: "process (Login.html, ctx)", lifeline-style: (fill: rgb("#005F0F")))
    _seq("C", "B", disable-src: true, comment: "Login.html")
    _seq("B", "A", disable-src: true, comment: "Login.html")
    _seq("B", "D", enable-dst: true, comment: "findByUsername (nickname)")
    _seq("D", "B", disable-src: true, comment: "return user")
    _seq("B", "E", comment: [[user != null] \ ? setAttribute("user", user)])
    _seq("B", "F", disable-src: true, comment: "Redirect")
  }),
  comment: [
    Upon first doGet() request from Client, server redirect it to static Login page.Afterwards, the User inserts their credentials.
    Those values are sent to LoginServlet via doPost(), and then passed to the `checkUser()` function that return an integer,0 if password is wrong, 3 if username not found, 1 if all goes well, then `findByUsername()` returns `user`, then Client is redirected to their Home and the `user` variable is set for the current session.

    If there has been some error in the process -- the credentials are incorrect, database can't be accessed... -- then the servlet will set context variable `error` to `message`, which then will be process by thymeleaf engine and print a Login.html with `message`.

    If user click on logout button, it will send a `doGet()` with parameter `logout` = 1, then the current session will be invalidated.
  ],
  label_: "login-sequence",
  comment_next_page_: true,
)

#seq_diagram(
  [RegisterServlet sequence diagram],
  diagram({
    _par("A", display-name: "Client")
    _par("B", display-name: "RegisterServlet")
    _par("H", display-name: "Request")
    _par("G", display-name: "ctx")
    _par("C", display-name: "Thymeleaf", shape: "custom", custom-image: thymeleaf)
    _par("D", display-name: "UserDAO")

    // post
    _seq("A", "B", enable-dst: true, comment: "doPost()")
    _seq("B", "H", enable-dst: true, comment: [getParameter (\"username\")])
    _seq("H", "B", disable-src: true, comment: [return username])
    _seq("B", "H", enable-dst: true, comment: [getParameter (\"password\")])
    _seq("H", "B", disable-src: true, comment: [return password])
    _seq("B", "H", enable-dst: true, comment: [getParameter (\"first_name\")])
    _seq("H", "B", disable-src: true, comment: [return first_name])
    _seq("B", "H", enable-dst: true, comment: [getParameter (\"last_name\")])
    _seq("H", "B", disable-src: true, comment: [return last_name])
    _seq("B", "H", enable-dst: true, comment: [getParameter (\"address\")])
    _seq("H", "B", disable-src: true, comment: [return address])
    _seq("B", "D", enable-dst: true, comment: "createUser(user)")
    _seq("D", "B", disable-src: true, comment: "return result")
    _seq("B", "A", comment: [[result = 1] ? redirect \ Login.html?])
    _seq("B", "G", comment: [[result != 1] \ ? setVariable (\"error\", message)])
    _seq("B", "C", enable-dst: true, comment: "process (Register.html, ctx)", lifeline-style: (fill: rgb("#005F0F")))
    _seq("C", "B", disable-src: true, comment: "register.html")
    _seq("B", "A", disable-src: true, comment: "register.html")
  }),
  comment_next_page_: true,
  comment: [
    In Login.html, user can be directed to a initially static Register.html page, as per the Login sequence diagram, once all the parameters are gathered and verified (omitted for simplicity), if registration is successful, Client will be redirected to a static Login.html, if operation fails, there can't be two Users with the same useranme. If that happen, then `createUser()` returns `0` and then the servlet will set context variable `error` to `message`, which then will be process by thymeleaf engine and print a Register.html with `message`, same goes for other type of errors (omitted for simplicity).

  ],
  label_: "register-sequence",
)

#seq_diagram(
  [HomeServlet sequence diagram],
  diagram({
    _par("A", display-name: "Client")
    _par("B", display-name: "HomeServlet")
    _par("F", display-name: "Session")
    _par("C", display-name: "LoginServlet")
    _par("D", display-name: "ctx")
    _par("E", display-name: "Thymeleaf", shape: "custom", custom-image: thymeleaf)

    _seq("A", "B", enable-dst: true, comment: "doGet()")
    _seq("B", "F", enable-dst: true, comment: [getAttribute (\"user\")])
    _seq("F", "B", disable-src: true, comment: [return user])
    _seq("B", "C", enable-dst: true, comment: [[user == null] \ ? redirect /login])
    _seq("B", "D", comment: [[user != null] \ ? setVariable (\"user\", user)])
    _seq("B", "E", enable-dst: true, comment: "process (Home.html,ctx)", lifeline-style: (fill: rgb("#005F0F")))
    _seq("E", "B", disable-src: true, comment: "Home.html")
    _seq("B", "A", disable-src: true, comment: "Home.html")
  }),
  comment: [
    Once the Login is complete, the User is redirected to their HomePage, which only has 2 button, `SELL` and `BUY`, first is handled by `SellServlet`, second handled by `BuyServlet`.
    
    From now on, verify user's session will be omitted since it is the same for every Servlet, unless it has another use.
  ],
  label_: "home-sequence",
  comment_next_page_: true,
)

#seq_diagram(
  [SellServlet sequence diagram],
  diagram({
    _par("A", display-name: "Home")
    _par("B", display-name: "SellServlet")
    _par("G", display-name: "Session")
    _par("C", display-name: "AuctionDAO")
    _par("F", display-name: "ItemDAO")
    _par("D", display-name: "ctx")
    _par("E", display-name: "Thymeleaf", shape: "custom", custom-image: thymeleaf)

    _seq("A", "B", enable-dst: true, comment: "doGet()")
    _seq("B", "G", enable-dst: true, comment: [getAttribute(\"user\")])
    _seq("G", "B", disable-src: true, comment: [return user])
    _seq("B", "C", enable-dst: true, comment: [findAllOpenAuction(user_id)])
    _seq("C", "B", disable-src: true, comment: [return openAuctions])
    _seq("B", "C", enable-dst: true, comment: [findAllClosedAuctionsAndResult(user_id)])
    _seq("C", "B", disable-src: true, comment: [return closedAuction])
    _seq("B", "F", enable-dst: true, comment: [findAllItemNotInAuction(user_id)])
    _seq("F", "B", disable-src: true, comment: [return NotInAuctionItems])
    _seq("B", "D", comment: [setVariable(\"openAuctions\", openAuctions)])
    _seq("B", "D", comment: [setVariable(\"closedAuction\", closedAuction)])
    _seq("B", "D", comment: [setVariable(\"itemsAvailable\", NotInAuctionItems)])
    _seq("B", "D", comment: [setVariable(\"user\", user)])
    _seq("B", "E", enable-dst: true, comment: "process (SellPage.html, ctx)", lifeline-style: (fill: rgb("#005F0F")))
    _seq("E", "B", disable-src: true, comment: "SellPage.html")
    _seq("B", "A", disable-src: true, comment: "SellPage.html")
  }),
  comment: [
    From the HomePage, by clicking on the link, the site redirects to the `SellPage`, which lists all the auctions associated to that user, also all items that can be added to an auction.

    In order to do so, the program needs the user attribute -- which is again retrieved via the session, user attribute contains all information.

    Then user id are passed to the `findAllOpenAuction()`, `findAllClosedAuctionsAndResult()`, `findAllItemNotInAuction()` method, that returns all the auctions and sellable items. Finally, thymeleaf processes the context and display the `SellPage`.
  ],
  label_: "sellservlet-sequence",
  comment_next_page_: true,
)

#seq_diagram(
  [BuyServlet sequence diagram],
  diagram({
    _par("A", display-name: "HomePage")
    _par("B", display-name: "BuyServlet")
    _par("H", display-name: "Session")
    _par("G", display-name: "Request")
    _par("C", display-name: "AuctionDAO")
    _par("D", display-name: "ctx")
    _par("E", display-name: "Thymeleaf", shape: "custom", custom-image: thymeleaf)

    _seq("A", "B", enable-dst: true, comment: "doGet()")
    _seq("B", "H", enable-dst: true, comment: [getAttribute (\"user\")])
    _seq("H", "B", disable-src: true, comment: [return user])
    _seq("B", "G", enable-dst: true, comment: [getParameter (\"keywords\")])
    _seq("G", "B", disable-src: true, comment: [return keywords])
    _seq("B", "C", enable-dst: true, comment: [findAllOpenAuctionByKeywords(keywords)])
    _seq("C", "B", disable-src: true, comment: [return openAuctions])
    _seq("B", "C", enable-dst: true, comment: [findAllWonAuctions(user_id)])
    _seq("C", "B", disable-src: true, comment: [return wonAuctions])
    _seq("B", "D", comment: [setVariable(\"openAuctions\", openAuctions)])
    _seq("B", "D", comment: [setVariable(\"wonAuctions\", wonAuctions)])
    _seq("B", "D", comment: [setVariable(\"user\", user)])
    _seq("B", "E", enable-dst: true, comment: "process (BuyPage.html, ctx)", lifeline-style: (fill: rgb("#005F0F")))
    _seq("E", "B", disable-src: true, comment: "BuyPage.html")
    _seq("B", "A", disable-src: true, comment: "BuyPage.html")
  }),
  comment: [
    From the HomePage, by clicking on the link, the site redirects to the `BuyPage`, which lists all the auctions won by the user.

    On the BuyPage, there is also a form where the user can enter keywords and submit them to the server to search for auctions containing items with at least one word matching the keywords.

    In order to do so, the program needs the user attribute, parameter keywords is passed with request.

    Then keywords are passed to the `findAllOpenAuctionByKeywords()` method, the user id is passed to `findAllWonAuctions()` method, then the result set of auctions and won auction are save in context, thymeleaf processes the context and display the `BuyPage`.
  ],
  label_: "buyservlet-sequence",
  comment_next_page_: true,
)

#seq_diagram(
  [UploadItemServlet sequence diagram],
  diagram({
    _par("A", display-name: "SellPage")
    _par("B", display-name: "UploadItemServlet")
    _par("H", display-name: "Request")
    _par("G", display-name: "Files")
    _par("C", display-name: "ItemDAO")
    _par("D", display-name: "SellServlet")

    _seq("A", "B", enable-dst: true, comment: "doPost()")
    _seq("B", "H", enable-dst: true, comment: [getParameter(\"price\")])
    _seq("H", "B", disable-src: true, comment: [return price])
    _seq("B", "H", enable-dst: true, comment: [getParameter(\"name\")])
    _seq("H", "B", disable-src: true, comment: [return name])
    _seq("B", "H", enable-dst: true, comment: [getParameter(\"description\")])
    _seq("H", "B", disable-src: true, comment: [return description])
    _seq("B", "H", enable-dst: true, comment: [getPart(\"image\")])
    _seq("H", "B", disable-src: true, comment: [return image])
    _seq("B", "A", enable-dst: true, comment: [[check\ (price, title, \ description)\=0] \ ? redirect `/sell`])
    _seq("B", "A", enable-dst: true, comment: [[check\ (image) = 0] \ ? redirect `/sell`])
    _seq("B", "B", enable-dst: true, comment: [FileName=\ generateFileName(image)])
    _seq("B", "B", enable-dst: true, comment: [FilePath=\ generatePath(FileName)])
    _seq("B", "G", comment: [createDirectories\ (FilePath)])
    _seq("B", "G", comment: [copy(image, FilePath)])
    _seq("B", "C", enable-dst: true, comment: [uploadItem\ (item)])
    _seq("B", "D", enable-dst: true, comment: [redirect `/sell`])
  }),
  comment: [
    The User can upload item from the appropriate form in the SellPage (@sellservlet-sequence). When the POST request is received, the parameters are checked for null values and emptiness.
    Image will be renamed with generateSafeFileName(image) to a random UUID-based file name, which is 36 characters long (including dashes), plus the file extension.
    Before writing the file to disk, the method `createDirectories(relativeFilePath)` create a subdirectory for the user, hence each user has one directory contain all the images.
    Image are written to disk by the `copy(image.getInputStream(), relativeFilePath)()` method, which has two parameters: a Part object, that was received within a multipart/form-data POST request, and a Path object.
    Once this is completed, item's information is passed to `uploadItem(name, description, relativeFilePath, price, user_id)` method of ItemDAO.
    If anything goes wrong, image will be deleted with `Files.deleteIfExists(relativeFilePath)` (omitted).
  ],
  label_: "uploadItem-sequence",
)

#seq_diagram(
  [AuctionServlet sequence diagram],
  diagram({
    _par("A", display-name: "SellPage")
    _par("B", display-name: "AuctionServlet")
    _par("C", display-name: "Request")
    _par("D", display-name: "AuctionDAO")
    _par("G", display-name: "ctx")
    _par("F", display-name: "ItemDAO")
    _par("H", display-name: "Thymeleaf", shape: "custom", custom-image: thymeleaf)

    _seq("A", "B", enable-dst: true, comment: "doGet()")
    _seq("B", "C", enable-dst: true, comment: [getParameter(id)])
    _seq("C", "B", disable-src: true, comment: [return id])
    _seq("B", "D", enable-dst: true, comment: [findById(id)])
    _seq("D", "B", disable-src: true, comment: [return\auction])
    _seq("B", "D", enable-dst: true, comment: [findAllOffersByAuction(id)])
    _seq("D", "B", disable-src: true, comment: [return offers])
    _seq("B", "G", enable-dst: true, comment: [setVariable(\"closeable\", true)])
    _seq("B", "G", enable-dst: true, comment: [setVariable(\"auction\", auction)])
    _seq("B", "G", enable-dst: true, comment: [setVariable(\"offers\", offers)])
    _seq("B", "G", enable-dst: true, comment: [setVariable("user", user)])
    _seq("B", "H", enable-dst: true, comment: "process(DETTAGLIO.html,ctx)", lifeline-style: (fill: rgb("#005F0F")))
    _seq("H", "B", disable-src: true, comment: "DETTAGLIO.html")
    _seq("B", "A", disable-src: true, comment: "DETTAGLIO.html")
  }),
  label_: "auction-sequence",
  comment_next_page_: false,
  add_comment: false
)

#seq_diagram(
  [AuctionServlet sequence diagram],
  diagram({
    _par("A", display-name: "SellPage")
    _par("B", display-name: "AuctionServlet")
    _par("C", display-name: "Request")
    _par("D", display-name: "AuctionDAO")
    _par("F", display-name: "ItemDAO")

    _seq("A", "B", enable-dst: true, comment: "doPost()")
    _seq("B", "C", enable-dst: true, comment: [getParameter(\"title\")])
    _seq("C", "B", disable-src: true, comment: [return title])
    _seq("B", "C", enable-dst: true, comment: [getParameter(\"increment\")])
    _seq("C", "B", disable-src: true, comment: [return increment])
    _seq("B", "C", enable-dst: true, comment: [getParameter(\"endDate")])
    _seq("C", "B", disable-src: true, comment: [return endDate])
    _seq("B", "C", enable-dst: true, comment: [requireParameter("selectedItems")])
    _seq("C", "B", disable-src: true, comment: [return checkedItems])
    _seq("B", "F", enable-dst: true, comment: [calculateTotalPrice(checkedItems)])
    _seq("F", "B", disable-src: true, comment: [return startingPrice])
    _seq("B", "D", enable-dst: true, comment: [createAuction(auction)])
    _seq("D", "B", disable-src: true, comment: [return auction_id])
    _seq("B", "D", enable-dst: true, comment: [insertItems(auction_id, checkIds)])
    _seq("B", "A", enable-dst: true, comment: [redirect `/sell`])
  }),
  comment: [
    The User can create auction filling out form with all required information, in particular at least one auction.When the servlet gets the POST request, it interacts with the AuctionDAO to create the auction with the `createAuction()` method and to add the items with the `insertItems()` method.

    Note that checkIds is a list of integers obtained by converting the strings inside the array returned by the `requireParameter("selectedItems")` method and parsing them with `Integer.parseInt()`.

    `requireParameter(param)` is a custom-method that make sure parameter with name `param` is obtained via request.

    startingPrice of auction is calculated as sum of items inserted.
  ],
  label_: "auction-sequence",
  comment_next_page_: true,
)

#seq_diagram(
  [OfferServlet sequence diagram],
  diagram({
    _par("A", display-name: "BuyPage")
    _par("B", display-name: "OfferServlet")
    _par("C", display-name: "Request")
    _par("D", display-name: "AuctionDAO")
    _par("E", display-name: "ItemDAO")
    _par("F", display-name: "ctx")
    _par("G", display-name: "Thymeleaf", shape: "custom", custom-image: thymeleaf)

    _seq("A", "B", enable-dst: true, comment: "doGet()")
    _seq("B", "C", enable-dst: true, comment: [getParameter ("id")])
    _seq("C", "B", disable-src: true, comment: [return auction_id])
    _seq("B", "D", enable-dst: true, comment: [findById(auction_id)])
    _seq("D", "B", disable-src: true, comment: [return auction])
    _seq("B", "D", enable-dst: true, comment: [findAllOffersByAuction\ (auction_id)])
    _seq("D", "B", disable-src: true, comment: [return offers])
    _seq("B", "D", enable-dst: true, comment: [getMaxOfferOfAuction\ (auction_id)])
    _seq("D", "B", disable-src: true, comment: [return maxOffer])
    _seq("B", "E", enable-dst: true, comment: [findAllItemInAuction\ (auction_id)])
    _seq("E", "B", disable-src: true, comment: [return items])
    _seq("B", "F", enable-dst: true, comment: [setVariable("user", user)])
    _seq("B", "F", enable-dst: true, comment: [setVariable("auction", auction)])
    _seq("B", "F", enable-dst: true, comment: [setVariable("offers", offers)])
    _seq("B", "F", enable-dst: true, comment: [setVariable("items", items)])
    _seq("B", "F", enable-dst: true, comment: [setVariable("minOffer", maxOffer+\ auction.minIncrement)])
    _seq("B", "G", enable-dst: true, comment: "process (OFFERTA.html, ctx)", lifeline-style: (fill: rgb("#005F0F")))
    _seq("G", "B", disable-src: true, comment: "OFFERTA.html")
    _seq("B", "A", disable-src: true, comment: "OFFERTA.html")
  }),
  label_: "offer-sequence",
  comment_next_page_: false,
  add_comment: false
)


#seq_diagram(
  [OfferServlet sequence diagram],
  diagram({
    _par("A", display-name: "BuyPage")
    _par("B", display-name: "OfferServlet")
    _par("C", display-name: "Request")
    _par("D", display-name: "AuctionDAO")

    //doPost()
    _seq("A", "B", enable-dst: true, comment: "doPost()")
    _seq("B", "C", enable-dst: true, comment: [getParameter ("id")])
    _seq("C", "B", disable-src: true, comment: [return auction_id])
    _seq("B", "C", enable-dst: true, comment: [getParameter("offeredPrice")])
    _seq("C", "B", disable-src: true, comment: [return offerPrice])
    _seq("B", "C", enable-dst: true, comment: [findById(auction_id)])
    _seq("C", "B", disable-src: true, comment: [return auction])
    _seq("B", "B", comment: [result=verifyValidity(offeredPrice, auction_id)])
    _seq("B", "D", enable-dst: true, comment: [[result=1] ? insertOffer(user.id, auction_id, offerPrice)])
    _seq("D", "B", disable-src: true, comment: [redirect `/offer`])
  }),
  comment: [
    From BuyPage.html, after getting the renewed BuyPage.html from BuyServlet as result of `doPost()` send with keywords, here you click on any auction sends a `doGet()` to OfferServlet, getting list of offers and items related to that specific auction.

    There is a form in OFFERTA.html, where you can make an offer that is greater than minimum offer imposed by Server, you will be redirected to same page afterwards using PRP.
  ],
  label_: "offer-sequence",
  comment_next_page_: true,
)

#seq_diagram(
  [CloseServlet sequence diagram],
  diagram({
    _par("A", display-name: "DETTAGLIO")
    _par("B", display-name: "CloseServlet")
    _par("C", display-name: "Request")
    _par("D", display-name: "AuctionDAO")
    _par("E", display-name: "ItemDAO")
    _par("F", display-name: "ctx")
    _par("G", display-name: "Thymeleaf", shape: "custom", custom-image: thymeleaf)

    _seq("A", "B", enable-dst: true, comment: "doPost()")
    _seq("B", "C", enable-dst: true, comment: [getParameter\ (\"a_id\")])
    _seq("C", "B", disable-dst: true, comment: [return a_id])
    _seq("B", "D", enable-dst: true, comment: [findById(a_id)])
    _seq("D", "B", disable-src: true, comment: [return auction])
    _seq("B", "D", enable-dst: true, comment: [findAllOffers\ ByAuction(a_id)])
    _seq("D", "B", disable-src: true, comment: [return offers])
    _seq("B", "B", enable-dst: true, comment: [close=isCloseable\ (auction)])
    _seq("B", "D", enable-dst: true, comment: [[close=1]?closeAuction\ (a_id)])
    _seq("B", "D", enable-dst: true, comment: [result=updateResult\ (a_id)])
    _seq("B", "E", enable-dst: true, comment: [[result>1]?sellItemInAuction\ (a_id)])
    _seq("B", "F", enable-dst: true, comment: [setVariable(\"user\",user)])
    _seq("B", "F", enable-dst: true, comment: [setVariable(\"auction\", auction)])
    _seq("B", "F", enable-dst: true, comment: [setVariable(\"offers\", offers)])
    _seq("B", "G", enable-dst: true, comment: "process (DETTAGLIO.html, ctx)", lifeline-style: (fill: rgb("#005F0F")))
    _seq("G", "B", disable-src: true, comment: "DETTAGLIO.html")
    _seq("B", "A", disable-src: true, comment: "DETTAGLIO.html")
  }),
  comment: [
    In DETTAGLIO.html, owner of the current auction can try to close the auction, which is will be handled by CloseServlet, firstly will verify if auction is expired and is currently open with `isCloseable()` method, afterwards it will update Result table in database with `updateResult()` method, it will insert a new record if and only if the auction has a winner, with return value = 1. If result > 1, all items in the auction will be marked as sold with `sellItemInAuction()` method.
  ],
  label_: "close-sequence",
  comment_next_page_: true,
)

#seq_diagram(
  [Event: Login],
  diagram({
    _par("A", display-name: "auction.html")
    _par("B", display-name: [js.js])
    _par("E", display-name: [LoginServlet])
    _par("C", display-name: [UserDAO])
    _par("D", display-name: [Gson])

    _seq("A", "B", comment: [GET])
    _seq("A", "B", enable-dst: true, comment: [Login()], lifeline-style: (fill: rgb("#3178C6")))
    _seq("B", "E", comment: [POST])
    _seq("E", "C", comment: [checkUser (nickname,password)])
    _seq("C", "E", disable-src: true, comment: "return result")
    _seq("E", "E", comment: [[result == 0 || result == 3] \ ? response.setStatus(400)])
    _seq("E", "E", comment: [response.setStatus(200)])
    _seq("E", "D", comment: [toJson(responseData)])
    _seq("D", "E", comment: [return responseData])
    _seq("E", "B", comment: [response(responseData)])
    _seq("B", "B", comment: [[response.status != 200] ? showError()])
    _seq("B", "B", comment: [showMessage()])
    _seq("B", "B", comment: [showPage()])
    _seq("B", "A", disable-src: true, comment: [Redirect])
  }),
  comment: [
    As the server is deployed the `auction.html` requests the associated Javascript files. As they have been loaded thanks to the IIFE, the User is able to Login.
    Once the button has been clicked, Javascript performs a POST request -- via `login()` method -- to the Login servlet, which, as seen in the Login sequence diagram (@login-sequence), checks if the User exists: if that's the case it returns a 200 OK status code, otherwise it returns a 400 Bad Request status code.
    If not, then a error div will appear on top of the navbar.
    Body of response always contains a `Map<key,Object> responseData` containing needed information, it will be omitted for simplicity.
  ],
  label_: "ria-event-login-sequence",
  comment_next_page_: true,
)

#seq_diagram(
  [Event: Register],
  diagram({
    _par("A", display-name: "auction.html")
    _par("B", display-name: [js.js])
    _par("C", display-name: "RegisterServlet")
    _par("D", display-name: "Request")
    _par("E", display-name: "UserDAO")

    // post
    _seq("A", "B", comment: [GET])
    _seq("A", "B", enable-dst: true, comment: [Register()], lifeline-style: (fill: rgb("#3178C6")))
    _seq("B", "C", enable-dst: true, comment: [POST])
    _seq("C", "D", enable-dst: true, comment: [getParameter (\"username\")])
    _seq("D", "C", disable-src: true, comment: [return username])
    _seq("C", "D", enable-dst: true, comment: [getParameter (\"password\")])
    _seq("D", "C", disable-src: true, comment: [return password])
    _seq("C", "D", enable-dst: true, comment: [getParameter (\"first_name\")])
    _seq("D", "C", disable-src: true, comment: [return first_name])
    _seq("C", "D", enable-dst: true, comment: [getParameter (\"last_name\")])
    _seq("D", "C", disable-src: true, comment: [return last_name])
    _seq("C", "D", enable-dst: true, comment: [getParameter (\"address\")])
    _seq("D", "C", disable-src: true, comment: [return address])
    _seq("C", "E", enable-dst: true, comment: "createUser(user)")
    _seq("E", "C", disable-src: true, comment: "return result")
    _seq("C", "C", comment: [[result = 1] ? response.setStatus(200)])
    _seq("C", "C", comment: [[result != 1] response.setStatus(400)])
    _seq("C", "B", comment: [response(responseData)])
    _seq("B", "B", comment: [showMessage()])
    _seq("B", "B", comment: [showPage()])
    _seq("B", "A", disable-src: true, comment: [Redirect])

  }),
  comment_next_page_: true,
  comment: [
    In `auction.html`, the User can register by clicking on the `Register` button, which will send a POST request to the `RegisterServlet`, which will create a new User in the database.

    The `createUser()` method of the `UserDAO` class is called, which returns a boolean value, 1 if the User has been created, 0 otherwise.

    Response status code is set to 200 OK if the User has been created, 400 Bad Request otherwise.

    responseData is omitted, it contains normal message or error message.
  ],
  label_: "ria-event-register-sequence",
)

#seq_diagram(
  [getSellPageInfo sequence diagram],
  diagram({
    _par("A", display-name: "auction.html")
    _par("B", display-name: [js.js])
    _par("C", display-name: "SellServlet")
    _par("E", display-name: "AuctionDAO")
    _par("F", display-name: "ItemDAO")
    _par("G", display-name: "Gson")

    _seq("A", "B", comment: [GET])
    _seq("A", "B", enable-dst: true, comment: [getSell\ PageInfo()], lifeline-style: (fill: rgb("#3178C6")))
    _seq("B", "C", enable-dst: true, comment: [GET])
    _seq("C", "E", enable-dst: true, comment: [findAllOpenAuction(user_id)])
    _seq("E", "C", disable-src: true, comment: [return openAuctions])
    _seq("C", "E", enable-dst: true, comment: [findAllClosed\ AuctionsAndResult(user_id)])
    _seq("E", "C", disable-src: true, comment: [return closedAuction])
    _seq("C", "F", enable-dst: true, comment: [findAllItemNotIn\Auction(user_id)])
    _seq("F", "C", disable-src: true, comment: [return items])
    _seq("C", "G", enable-dst: true, comment: [toJson(openAuctions)])
    _seq("G", "C", disable-src: true, comment: "return oA[JSON]")
    _seq("C", "G", enable-dst: true, comment: [toJson(closedAuction)])
    _seq("G", "C", disable-src: true, comment: "return cA[JSON]")
    _seq("C", "G", enable-dst: true, comment: [toJson(items)])
    _seq("G", "C", disable-src: true, comment: "return nAI[JSON]")
    _seq("C", "C", comment: [response.setStatus(200)])
    _seq("C", "B", comment: [response\ (responseData)])
    _seq("B", "B", comment: [showMessage()])
    _seq("B", "B", comment: [showPage()])
    _seq("B", "A", disable-src: true, comment: [Redirect])
  }),
  comment: [
    When user clicks on `Sell` button, javascript sends a GET request to the `SellServlet`, which returns a list of open auctions and a list of closed auctions and a list of items not in any auction.

    Session object is used to get the current user, which is used to filter the auctions, it will be omitted, user_id will be used directly instead.

    The responseData is a JSON object containing the list of open auctions, the list of closed auctions and the list of available items.

    If response status code is 200 OK, then page will display the `SellView`, also part of setting status code will be omitted for simplicity.

    Same goes for showMessage() and showError(), since they are always invoked after the response is sent.
   ],
  label_: "ria-event-sell-sequence",
  comment_next_page_: true,
)

#seq_diagram(
  [getBuyPageInfo sequence diagram],
  diagram({
    _par("A", display-name: "auction.html")
    _par("B", display-name: [js.js])
    _par("C", display-name: "BuyServlet")
    _par("D", display-name: "AuctionDAO")
    _par("E", display-name: "ItemDAO")
    _par("F", display-name: "Gson")

    _seq("A", "B", comment: [GET])
    _seq("A", "B", enable-dst: true, comment: [getBuy\ PageIn\for()], lifeline-style: (fill: rgb("#3178C6")))
    _seq("B", "C", enable-dst: true, comment: [GET])
    _seq("C", "D", enable-dst: true, comment: [findAllWonAuctions(user_id)])
    _seq("D", "C", disable-src: true, comment: [return wonAuctions])
    _seq("C", "D", enable-dst: true, comment: [find_open_histo-\ rical_auctions(user_id)])
    _seq("D", "C", disable-src: true, comment: [return historyAuctions])
    _seq("C", "F", enable-dst: true, comment: [toJson(wonAuctions)])
    _seq("F", "C", disable-src: true, comment: "return wA[JSON]")
    _seq("C", "F", enable-dst: true, comment: [toJson(historyAuctions)])
    _seq("F", "C", disable-src: true, comment: "return hA[JSON]")
    _seq("C", "B", comment: [response\ (responseData)])
    _seq("B", "B", comment: [showPage()])
    _seq("B", "A", disable-src: true, comment: [Redirect])
  }),
  comment: [
    When user clicks on `Buy` button, javascript sends a GET request to the `BuyServlet`, which returns a list of won auctions and a list of historical auctions.

    Session object is used to get the current user, which is used to filter the auctions.

    The responseData is a JSON object containing the list of won auctions and the list of historical auctions.

    If response status code is 200 OK, then page will display the `BuyView`.
   ],
  label_: "ria-buy-sequence",
  comment_next_page_: true,
)

#seq_diagram(
  [uploadItem sequence diagram],
    diagram({
      _par("A", display-name: "auction.html")
      _par("B", display-name: [js.js])
      _par("C", display-name: "UploadItemServlet")
      _par("D", display-name: "ItemDAO")
      _par("E", display-name: "Files")

      _seq("A", "B", comment: [GET])
      _seq("A", "B", enable-dst: true, comment: [uploadItem()], lifeline-style: (fill: rgb("#3178C6")))
      _seq("B", "C", enable-dst: true, comment: [POST])
      _seq("C", "C", comment: [check(inputs)=0?\ response.setStatus(400)])
      _alt("check(price,title,description,image)!=0)",
      {
        _seq("C", "C", enable-dst: true, comment: [FileName=generate\ FileName(image)])
          _seq("C", "C", enable-dst: true, comment: [FilePath=generate\ Path(FileName)])
          _seq("B", "E", comment: [createDirectories(FilePath)])
          _seq("B", "E", comment: [copy(image, FilePath)])
          _seq("B", "D", enable-dst: true, comment: [uploadItem(item)])
          _seq("C", "C", comment: [response.setStatus(200)])
      }
          
      )
      _seq("C", "B", comment: [response(responseData)])
      _seq("B", "B", comment: [showPage()])
      _seq("B", "A", disable-src: true, comment: [Redirect])
    }),
  comment: [
    Inputs contains (title, description, price, image)
    This is quite similar to the `uploadItem` sequence diagram @uploadItem-sequence, except this time responseData is sent instead of an entire page.

    When user clicks on `uploadItem` button, javascript sends a POST request to the `UploadItemServlet`, which receives the following parameters: `price`, `title`, `description`, `image`.

    During processing, the `check()` method is called to validate the input parameters. If any of them is invalid, the response status code is set to 400 Bad Request and return immediately.
    ],
  label_: "ria-uploadItem-sequence",
  comment_next_page_: true,
)

#seq_diagram(
  [getDetailedPageInfo sequence diagram],
  diagram({
    _par("A", display-name: "auction.html")
    _par("B", display-name: [js.js])
    _par("C", display-name: "AuctionServlet")
    _par("D", display-name: "AuctionDAO")
    _par("E", display-name: "ItemDAO")
    _par("F", display-name: "Gson")

    _seq("A", "B", comment: [GET])
    _seq("A", "B", enable-dst: true, comment: [getDetailed\ PageInfo()], lifeline-style: (fill: rgb("#3178C6")))
    _seq("B", "C", enable-dst: true, comment: [GET])
    _seq("C", "D", enable-dst: true, comment: [findById(id)])
    _seq("D", "C", disable-src: true, comment: [return\auction])
    _seq("C", "D", enable-dst: true, comment: [findAllOffersByAuction(id)])
    _seq("D", "C", disable-src: true, comment: [return offers])
    _seq("C", "E", enable-dst: true, comment: [findAllItemInAuction(id)])
    _seq("D", "C", disable-src: true, comment: [return items])
    _seq("C", "F", enable-dst: true, comment: [toJson(auction)])
    _seq("F", "C", disable-src: true, comment: "return a[JSON]")
    _seq("C", "F", enable-dst: true, comment: [toJson(offers)])
    _seq("F", "C", disable-src: true, comment: "return o[JSON]")
    _seq("C", "F", enable-dst: true, comment: [toJson(items)])
    _seq("F", "C", disable-src: true, comment: "return i[JSON]")
    _seq("C", "C", comment: [response.setStatus(200)])
    _seq("C", "B", comment: [response\ (responseData)])
    _seq("B", "B", comment: [showPage()])
    _seq("B", "A", disable-src: true, comment: [Redirect])
  }),
   label_: "ria-getDetailedPageInfo-sequence",
  comment_next_page_: true,
  comment: [
    When user clicks on any auction in `SellView`, javascript sends a GET request to the `AuctionServlet`, which returns the auction details along with the list of offers and items related to that specific auction.

    The responseData is a JSON object containing the auction details, the list of offers and the list of items.

    If response status code is 200 OK, then page will display the `DetailedView`.
   ],
)

#seq_diagram(
  [createAuction sequence diagram],
  diagram({
    _par("A", display-name: "auction.html")
    _par("B", display-name: [js.js])
    _par("C", display-name: "AuctionServlet")
    _par("D", display-name: "request")
    _par("E", display-name: "AuctionDAO")
    _par("F", display-name: "ItemDAO")

    _seq("A", "B", comment: [GET])
    _seq("A", "B", enable-dst: true, comment: [create\ Auction()], lifeline-style: (fill: rgb("#3178C6")))
    _seq("B", "C", enable-dst: true, comment: [POST])
    _seq("C", "D", enable-dst: true, comment: [getParameter(\"title\")])
    _seq("D", "C", disable-src: true, comment: [return title])
    _seq("C", "D", enable-dst: true, comment: [getParameter(\"increment\")])
    _seq("D", "C", disable-src: true, comment: [return increment])
    _seq("C", "D", enable-dst: true, comment: [getParameter(\"endDate\")])
    _seq("D", "C", disable-src: true, comment: [return endDate])
    _seq("C", "D", enable-dst: true, comment: [requireParameter\ (\"selectedItems\")])
    _seq("D", "C", disable-src: true, comment: [return checkedItems])
    _seq("C", "F", enable-dst: true, comment: [calculateTotalPrice(checkedItems)])
    _seq("F", "C", disable-src: true, comment: [return startingPrice])
    _seq("C", "E", enable-dst: true, comment: [createAuction(auction)])
    _seq("C", "C", comment: [response.setStatus(200)])
    _seq("C", "B", comment: [response\ (responseData)])
    _seq("B", "B", comment: [showPage()])
    _seq("B", "A", disable-src: true, comment: [Redirect])
  }),
  comment: [
    When user clicks on `createAuction` button, javascript sends a POST request to the `AuctionServlet`, which receives the following parameters: `title`, `increment`, `endDate`, `selectedItems`.

    The `createAuction()` method is called to create a new auction with the given parameters.

    If the auction is successfully created, the response status code is set to 200 OK and the responseData is a JSON object containing the newly created auction.

    If the auction creation fails, the response status code is set to 400 Bad Request, here omitted for simplicity, and the responseData contains an error message.

    The page will display the SellView with newly created auction.
  ],
  label_: "ria-createAuction-sequence",
  comment_next_page_: true,
)

#seq_diagram(
  [getOfferPageInfo sequence diagram],
  diagram({
    _par("B", display-name: [js.js])
    _par("A", display-name: "auction.html")
    _par("C", display-name: "OfferServlet")
    _par("E", display-name: "AuctionDAO")
    _par("F", display-name: "ItemDAO")
    _par("G", display-name: "Gson")

    _seq("A", "B", comment: [GET])
    _seq("A", "B", enable-dst: true, comment: [getOffer\ PageInfo()], lifeline-style: (fill: rgb("#3178C6")))
    _seq("B", "C", enable-dst: true, comment: [GET])
    _seq("C", "E", enable-dst: true, comment: [findById(a_id)])
    _seq("E", "C", disable-src: true, comment: [return auction])
    _seq("C", "E", enable-dst: true, comment: [findAllOffersByAuction\ (a_id)])
    _seq("E", "C", disable-src: true, comment: [return offers])
    _seq("C", "E", enable-dst: true, comment: [getMax\ OfferOfAuction(a_id)])
    _seq("E", "C", disable-src: true, comment: [return maxOffer])
    _seq("C", "F", enable-dst: true, comment: [findAllI\temInAuction\ (a_id)])
    _seq("F", "C", disable-src: true, comment: [return items])
    _seq("C", "G", enable-dst: true, comment: [toJson(auction)])
    _seq("G", "C", disable-src: true, comment: "return a[JSON]")
    _seq("C", "G", enable-dst: true, comment: [toJson(offers)])
    _seq("G", "C", disable-src: true, comment: "return o[JSON]")
    _seq("C", "G", enable-dst: true, comment: [toJson(items)])
    _seq("G", "C", disable-src: true, comment: "return i[JSON]")
    _seq("C", "C", comment: [response.\setStatus(200)])
    _seq("C", "B", comment: [response\ (responseData)])
    _seq("B", "B", comment: [showPage()])
    _seq("B", "A", disable-src: true, comment: [Redirect])
  }),
  label_: "ria-getOfferPageInfo-sequence",
  comment_next_page_: true,
  comment: [
    When user clicks on any auction in `BuyView`, javascript sends a GET request to the `OfferServlet`, which returns the auction details along with the list of offers and items related to that specific auction.

    The responseData is a JSON object containing the auction details, the list of offers and the list of items.

    If response status code is 200 OK, then page will display the `OfferView`.
  ],
)


#seq_diagram(
  [offer sequence diagram],
  diagram({
  _par("A", display-name: "auction.html")
      _par("B", display-name: [js.js])
      _par("C", display-name: "OfferServlet")
      _par("D", display-name: "request")
      _par("E", display-name: "AuctionDAO")

    //doPost()
    _seq("A", "B", comment: [GET])
    _seq("A", "B", enable-dst: true, comment: [offer()], lifeline-style: (fill: rgb("#3178C6")))
    _seq("B", "C", enable-dst: true, comment: [POST])
    _seq("C", "D", enable-dst: true, comment: [getParameter ("id")])
    _seq("D", "C", disable-src: true, comment: [return auction_id])
    _seq("C", "D", enable-dst: true, comment: [getParameter("offeredPrice")])
    _seq("D", "C", disable-src: true, comment: [return offerPrice])
    _seq("C", "E", enable-dst: true, comment: [findById(auction_id)])
    _seq("E", "C", disable-src: true, comment: [return auction])
    _seq("C", "C", comment: [result=\ verifyValidity(offeredPrice, auction_id)])
    _seq("C", "E", enable-dst: true, comment: [[result=1] ? insertOffer(user.id, auction_id, offerPrice)])
    _seq("C", "C", comment: [response.setStatus(200)])
    _seq("C", "B", comment: [response\ (responseData)])
    _seq("B", "B", comment: [showPage()])
    _seq("B", "A", disable-src: true, comment: [Redirect])
  }),
  comment: [
    When user clicks on `offer` button in `OfferView`, javascript sends a POST request to the `OfferServlet`, which receives the following parameters: `id`, `offeredPrice`.

    The `offer()` method is called to create a new offer with the given parameters.

    If the offer is successfully created, the response status code is set to 200 OK and the responseData is a JSON object containing the newly created offer.

    If the offer creation fails, the response status code is set to 400 Bad Request, here omitted for simplicity, and the responseData contains an error message.

    The page will display the `OfferView` with newly created offer.
    ],
  label_: "ria-offer-sequence",
  comment_next_page_: true,
)

#seq_diagram(
  [close sequence diagram],
  diagram({
    _par("A", display-name: "auction.html")
    _par("B", display-name: [js.js])
    _par("C", display-name: "CloseServlet")
    _par("D", display-name: "Request")
    _par("E", display-name: "AuctionDAO")
    _par("F", display-name: "ItemDAO")

    _seq("A", "B", comment: [GET])
    _seq("A", "B", enable-dst: true, comment: [offer()], lifeline-style: (fill: rgb("#3178C6")))
    _seq("B", "C", enable-dst: true, comment: [POST])
    _seq("C", "D", enable-dst: true, comment: [getParameter("id")])
    _seq("D", "C", disable-dst: true, comment: [return id])
    _seq("C", "E", enable-dst: true, comment: [findById(id)])
    _seq("E", "C", disable-src: true, comment: [return auction])
    _seq("C", "E", enable-dst: true, comment: [findAllOffers\ ByAuction(id)])
    _seq("E", "C", disable-src: true, comment: [return offers])
    _seq("C", "C", enable-dst: true, comment: [close=isCloseable\ (auction)])
    _seq("C", "E", enable-dst: true, comment: [[close=1]?closeAuction\ (id)])
    _seq("C", "E", enable-dst: true, comment: [result=updateResult\ (id)])
    _seq("C", "F", enable-dst: true, comment: [[result>1]?sellItemInAuction\ (id)])
    _seq("C", "C", comment: [response.setStatus(200)])
    _seq("C", "B", comment: [response\ (responseData)])
    _seq("B", "B", comment: [showPage()])
    _seq("B", "A", disable-src: true, comment: [Redirect])
  }),
  comment: [
    When user clicks on `close` button in `DetailView`, javascript sends a POST request to the `CloseServlet`, which receives the following parameters: `id`.

    The `close()` method is called to close the auction with the given ID.

    If the auction is successfully closed, the response status code is set to 200 OK and the responseData is a JSON object containing the result of the closing process.

    If the auction closing fails, the response status code is set to 400 Bad Request, here omitted for simplicity, and the responseData contains an error message.

    The page will display the `CloseView` depending on the result of the closing process, status will be either closed or open.
  ],
  label_: "ria-close-sequence",
  comment_next_page_: true,
)