// auction-event-listeners.js

document.addEventListener('DOMContentLoaded', function() {
    // get all the elements we need
    const loginPage = document.getElementById('login_page');
    const registerPage = document.getElementById('register_page');
    const buyPage = document.getElementById('buy_page');
    const sellPage = document.getElementById('sell_page');
    const offerPage = document.getElementById('offer_page');
    const detailPage = document.getElementById('detail_page');

    const errorAlert = document.querySelector('.error-alert');
    const messageAlert = document.querySelector('.message-alert');

    const loginForm = document.querySelector('#login_page form');
    const registerForm = document.querySelector('#register_page form');
    const isLoggedIn = localStorage.getItem('isLoggedIn') === 'true';
    // can only be buyPage or sellPage
    const lastPageId = localStorage.getItem('lastPage') || 'buy_page';
    const visitedAuction = JSON.parse(localStorage.getItem("visitedAuctions")) || [];

    const left = document.querySelector('.left');
    const right = document.querySelector('.right');
    const sellButton = document.getElementById('sell-container');
    const buyButton = document.getElementById('buy-container');
    const logoutButton = document.getElementById('logout-container');
    const username = document.getElementById('user-username');
    const goToSellPage = () => showPage('sellPage', 0);
    const goToBuyPage = () => showPage('buyPage', 0);

    const historySpan = document.getElementById('history-no-result-span');
    const historyDiv = document.getElementById('history-div');
    const historyTable = document.getElementById("history-table");
    const wonSpan = document.getElementById('won-no-result-span');
    const wonTable = document.getElementById("won-auctions-table");
    const noResultSpan = document.getElementById('no-result-span-buy');
    const searchResultsDiv = document.getElementById('search-results-div-buy');
    const resultsTable = document.getElementById("search-results-table-buy");

    const openTable = document.getElementById('open-auctions-body');
    const closedTable = document.getElementById('closed-auctions-body');
    const itemTable = document.getElementById("item-available-table");


    let last_username = 'test';
    console.log(visitedAuction);

    const lastPage = document.getElementById(lastPageId);

    if (isLoggedIn) {
        showPage(lastPage, 0);
    }

    // 30 days in milliseconds
    const EXPIRATION_TIME = 30 * 24 * 60 * 60 * 1000;

    function saveHistory(auctionId) {
        const now = Date.now();

        let visited = JSON.parse(localStorage.getItem("visitedAuctions")) || [];

        // filter out expired records
        visited = visited.filter(entry => now - entry.timestamp < EXPIRATION_TIME);

        // if exists, remove old record
        visited = visited.filter(entry => entry.id !== auctionId);

        // insert new record at the beginning
        visited.unshift({ id: auctionId, timestamp: now });

        // save new visited auctions
        localStorage.setItem("visitedAuctions", JSON.stringify(visited));
    }

    // displays error message
    function showError(message) {
        errorAlert.querySelector('p').textContent = message;
        errorAlert.style.display = 'block';
        setTimeout(() => {
            errorAlert.style.display = 'none';
        }, 5000);
    }

    // displays success message
    function showMessage(message) {
        messageAlert.querySelector('p').textContent = message;
        messageAlert.style.display = 'block';
        setTimeout(() => {
            messageAlert.style.display = 'none';
        }, 5000);
    }

    /**
     * changes the current page to the given page
     * @param page
     * @param auctionId
     */
    function showPage(page, auctionId) {
        // hide all other pages
        [loginPage, registerPage, buyPage, sellPage, offerPage, detailPage].forEach(p => {
            if (p) p.style.display = 'none';
        });

        if(page !== loginPage && page!== registerPage){
            sellButton.addEventListener('click', goToSellPage);
            buyButton.addEventListener('click', goToBuyPage);
            logoutButton.addEventListener('click', logout);
            left.style.display = 'block';
            right.style.display = 'block';
            username.textContent = last_username;
        }else{
            sellButton.removeEventListener('click', goToSellPage);
            buyButton.removeEventListener('click', goToBuyPage);
            logoutButton.removeEventListener('click', logout);
            left.style.display = 'none';
            right.style.display = 'none';
            username.textContent = '';
        }

        async function logout() {
            let response = await fetch('login?logout=1');
            if (response.ok) {
                localStorage.setItem('isLoggedIn', 'false');
                localStorage.removeItem('lastPage');
                localStorage.removeItem('visitedAuctions');
                showPage(loginPage, 0);
            } else {
                console.error("Logout failed:", response.status);
                showError('Logout failed');
            }
        }

        function formatDate(rawDate){
            const date = new Date(rawDate);
            const year = date.getFullYear();
            const month = String(date.getMonth() + 1).padStart(2, '0');
            const day = String(date.getDate()).padStart(2, '0');
            const hours = String(date.getHours()).padStart(2, '0');
            const minutes = String(date.getMinutes()).padStart(2, '0');
            return `${year}-${month}-${day}T${hours}:${minutes}`;
        }
        /**
         * get all visited auctions and won auctions
         * @returns {Promise<void>}
         */
        async function getBuyPageInfo(){
            try {
                buyPage.style.display = 'block';
                //renew history, filter out expired records
                let visited = JSON.parse(localStorage.getItem("visitedAuctions")) || [];
                let renewed_visited = visited.filter(entry => Date.now() - entry.timestamp < EXPIRATION_TIME);
                localStorage.setItem("visitedAuctions", JSON.stringify(visited));

                const visitedAuctions = renewed_visited.map(auctionId => `auctionId=${auctionId}`).join('&');
                const response = await fetch(`buy?${visitedAuctions}`);
                if (!response.ok) {
                    showError('Failed to load auctions');
                    return;
                }

                const data = await response.json();
                const {openAuctions, wonAuctions, historyAuctions} = data;

                historySpan.innerHTML = '';
                historyDiv.innerHTML = '';
                historyTable.innerHTML = '';
                wonSpan.innerHTML = '';
                wonTable.innerHTML = '';
                noResultSpan.innerHTML = '';
                searchResultsDiv.innerHTML = '';
                resultsTable.innerHTML = '';

                if (openAuctions.length > 0) {
                    noResultSpan.style.display = 'none';
                    searchResultsDiv.style.display = 'block';
                    openAuctions.forEach(openAuction => {
                        const tr = document.createElement('tr');
                        const tdId = document.createElement('td');
                        const a = document.createElement('a');
                        a.href = `#`;
                        a.textContent = openAuction.auction.auctionId;
                        a.dataset.id = openAuction.auction.auctionId;
                        a.addEventListener('click', () => {
                            saveHistory(a.dataset.id);
                            showPage(offerPage, a.dataset.id);
                        });

                        tdId.appendChild(a);
                        tr.appendChild(tdId);

                        const tdItems = document.createElement('td');
                        openAuction.items.forEach(item => {
                            const div = document.createElement('div');
                            const img = document.createElement('img');
                            img.src = item.cover_image;
                            img.alt = 'Item image';
                            img.style.width = '30px';
                            img.style.height = '30px';
                            img.style.marginLeft = '5px';
                            const span = document.createElement('span');
                            span.textContent = item.title;
                            div.appendChild(img);
                            div.appendChild(span);
                            tdItems.appendChild(div);
                        });
                        tr.appendChild(tdItems);

                        const tdMax = document.createElement('td');
                        tdMax.textContent = openAuction.maxOffer;
                        tr.appendChild(tdMax);

                        const tdTime = document.createElement('td');
                        tdTime.textContent = openAuction.timeLeft;
                        tr.appendChild(tdTime);

                        resultsTable.appendChild(tr);
                    });
                } else {
                    noResultSpan.style.display = 'block';
                    searchResultsDiv.style.display = 'none';
                }

                if (wonAuctions.length > 0) {
                    wonSpan.style.display = 'none';
                    wonAuctions.forEach(wonAuction => {
                        const tr = document.createElement('tr');

                        // Auction ID
                        const tdId = document.createElement('td');
                        tdId.textContent = wonAuction.auction.auctionId;
                        tr.appendChild(tdId);

                        // Item IDs
                        const tdItemIds = document.createElement('td');
                        wonAuction.items.forEach(item => {
                            const span = document.createElement('span');
                            span.textContent = item.id;
                            tdItemIds.appendChild(span);
                            tdItemIds.appendChild(document.createElement('br'));
                        });
                        tr.appendChild(tdItemIds);

                        // Item images + titles
                        const tdItems = document.createElement('td');
                        wonAuction.items.forEach(item => {
                            const div = document.createElement('div');

                            const img = document.createElement('img');
                            img.src = item.cover_image;
                            img.alt = 'Item image';
                            img.style.width = '30px';
                            img.style.height = '30px';
                            img.style.marginLeft = '5px';

                            const span = document.createElement('span');
                            span.textContent = item.title;

                            div.appendChild(img);
                            div.appendChild(span);

                            tdItems.appendChild(div);
                        });
                        tr.appendChild(tdItems);

                        // Item descriptions
                        const tdDescriptions = document.createElement('td');
                        wonAuction.items.forEach(item => {
                            const span = document.createElement('span');
                            span.textContent = item.description;
                            tdDescriptions.appendChild(span);
                            tdDescriptions.appendChild(document.createElement('br'));
                        });
                        tr.appendChild(tdDescriptions);

                        // Item prices
                        const tdPrices = document.createElement('td');
                        wonAuction.items.forEach(item => {
                            const span = document.createElement('span');
                            span.textContent = item.price;
                            tdPrices.appendChild(span);
                            tdPrices.appendChild(document.createElement('br'));
                        });
                        tr.appendChild(tdPrices);

                        wonTable.appendChild(tr);
                    });
                } else {
                    wonSpan.style.display = 'block';
                }

                if (historyAuctions.length > 0) {
                    historySpan.style.display = 'none';
                    historyAuctions.forEach(historyAuction => {
                        const tr = document.createElement('tr');

                        // Auction ID
                        const tdId = document.createElement('td');
                        tdId.textContent = historyAuction.auction.auctionId;
                        tr.appendChild(tdId);

                        // Item IDs
                        const tdItemIds = document.createElement('td');
                        historyAuction.items.forEach(item => {
                            const span = document.createElement('span');
                            span.textContent = item.id;
                            tdItemIds.appendChild(span);
                            tdItemIds.appendChild(document.createElement('br'));
                        });
                        tr.appendChild(tdItemIds);

                        // Item images + titles
                        const tdItems = document.createElement('td');
                        historyAuction.items.forEach(item => {
                            const div = document.createElement('div');

                            const img = document.createElement('img');
                            img.src = item.cover_image;
                            img.alt = 'Item image';
                            img.style.width = '30px';
                            img.style.height = '30px';
                            img.style.marginLeft = '5px';

                            const span = document.createElement('span');
                            span.textContent = item.title;

                            div.appendChild(img);
                            div.appendChild(span);

                            tdItems.appendChild(div);
                        });
                        tr.appendChild(tdItems);

                        // Item descriptions
                        const tdDescriptions = document.createElement('td');
                        historyAuction.items.forEach(item => {
                            const span = document.createElement('span');
                            span.textContent = item.description;
                            tdDescriptions.appendChild(span);
                            tdDescriptions.appendChild(document.createElement('br'));
                        });
                        tr.appendChild(tdDescriptions);

                        // Item prices
                        const tdPrices = document.createElement('td');
                        historyAuction.items.forEach(item => {
                            const span = document.createElement('span');
                            span.textContent = item.price;
                            tdPrices.appendChild(span);
                            tdPrices.appendChild(document.createElement('br'));
                        });
                        tr.appendChild(tdPrices);

                        historyTable.appendChild(tr);
                    });
                } else {
                    historySpan.style.display = 'block';
                }
            } catch (error) {
                console.error('Failed to load auctions:', error);
            }
        }

        async function getSellPageInfo() {
            try {
                sellPage.style.display = 'block';
                const response = await fetch('sell');
                if (!response.ok){
                    showError('Failed to load auctions');
                    return;
                }

                const data = await response.json();
                const { openAuctions, closedAuctions, items } = data;

                openTable.innerHTML = '';
                closedTable.innerHTML = '';
                itemTable.innerHTML = '';

                openAuctions.forEach(openAuction => {
                    const tr = document.createElement('tr');

                    const tdId = document.createElement('td');
                    const a = document.createElement('a');
                    a.href = `#`;
                    a.textContent = openAuction.auction.auctionId;
                    a.dataset.id = openAuction.auction.auctionId;
                    a.addEventListener('click', () => {
                        showPage(detailPage, a.dataset.id);
                        });

                    tdId.appendChild(a);
                    tr.appendChild(tdId);

                    const tdItems = document.createElement('td');
                    openAuction.items.forEach(item => {
                        const div = document.createElement('div');
                        const img = document.createElement('img');
                        img.src = item.cover_image;
                        img.alt = 'Item image';
                        img.style.width = '30px';
                        img.style.height = '30px';
                        img.style.marginLeft = '5px';
                        const span = document.createElement('span');
                        span.textContent = item.title;
                        div.appendChild(img);
                        div.appendChild(span);
                        tdItems.appendChild(div);
                    });
                    tr.appendChild(tdItems);

                    const tdMax = document.createElement('td');
                    tdMax.textContent = openAuction.maxOffer;
                    tr.appendChild(tdMax);

                    const tdTime = document.createElement('td');
                    tdTime.textContent = openAuction.timeLeft;
                    tr.appendChild(tdTime);

                    openTable.appendChild(tr);
                });

                closedAuctions.forEach(closedAuction => {
                    const tr = document.createElement('tr');

                    const tdId = document.createElement('td');
                    const a = document.createElement('a');
                    a.href = `#`;
                    a.textContent = closedAuction.auction.auctionId;
                    a.dataset.id = closedAuction.auction.auctionId;
                    a.addEventListener('click', () => {
                        showPage(detailPage, a.dataset.id);
                    });

                    tdId.appendChild(a);
                    tr.appendChild(tdId);

                    const tdItems = document.createElement('td');
                    tdItems.className = 'col-articoli';
                    closedAuction.items.forEach(item => {
                        const div = document.createElement('div');
                        div.style.whiteSpace = 'nowrap';
                        const img = document.createElement('img');
                        img.src = item.cover_image;
                        img.alt = 'Item image';
                        img.style.width = '30px';
                        img.style.height = '30px';
                        img.style.marginLeft = '5px';
                        const span = document.createElement('span');
                        span.textContent = item.title;
                        div.appendChild(img);
                        div.appendChild(span);
                        tdItems.appendChild(div);
                    });
                    tr.appendChild(tdItems);

                    // ending_at
                    const tdEnding = document.createElement('td');
                    tdEnding.textContent = new Date(closedAuction.auction.ending_at).toLocaleString();
                    tr.appendChild(tdEnding);

                    // startingPrice
                    const tdStart = document.createElement('td');
                    tdStart.textContent = closedAuction.auction.startingPrice;
                    tr.appendChild(tdStart);

                    // minIncrement
                    const tdIncrement = document.createElement('td');
                    tdIncrement.textContent = closedAuction.auction.minIncrement;
                    tr.appendChild(tdIncrement);

                    // closed status
                    const tdStatus = document.createElement('td');
                    tdStatus.textContent = closedAuction.auction.closed ? 'chiuso' : 'open';
                    tr.appendChild(tdStatus);

                    // created_at
                    const tdCreated = document.createElement('td');
                    tdCreated.textContent = new Date(closedAuction.auction.created_at).toLocaleString();
                    tr.appendChild(tdCreated);

                    // winner_name
                    const tdWinner = document.createElement('td');
                    tdWinner.textContent = closedAuction.result.winner_name;
                    tr.appendChild(tdWinner);

                    // final_price
                    const tdFinal = document.createElement('td');
                    tdFinal.textContent = closedAuction.result.final_price;
                    tr.appendChild(tdFinal);

                    // shipping_address
                    const tdAddress = document.createElement('td');
                    tdAddress.textContent = closedAuction.result.shipping_address;
                    tr.appendChild(tdAddress);

                    closedTable.appendChild(tr);
                });


                items.forEach(item => {
                    const tr = document.createElement('tr');
                    const tdBox = document.createElement('td');
                    const label = document.createElement('label');
                    const box = document.createElement('input');
                    tdBox.style.width = '20px';
                    tdBox.style.textAlign = 'center';
                    box.type = 'checkbox';
                    box.className = 'item-checkbox';
                    box.name = 'selectedItems';
                    box.style.width = '20px';
                    box.style.height = '20px';
                    box.value = item.id;

                    label.appendChild(box);
                    tdBox.appendChild(label);
                    tr.appendChild(tdBox);

                    const tdItem = document.createElement('td');
                    const img = document.createElement('img');
                    img.src = item.cover_image;
                    img.alt = 'cover';
                    img.className = 'cover'
                    img.style.width = '50px';
                    img.style.height = 'auto';
                    tdItem.appendChild(img);
                    tr.appendChild(tdItem);

                    // tdId
                    const tdId = document.createElement('td');
                    tdId.textContent = item.id;
                    tr.appendChild(tdId);

                    // title
                    const tdTitle = document.createElement('td');
                    tdTitle.textContent = item.title;
                    tr.appendChild(tdTitle);

                    // tdPrice
                    const tdPrice = document.createElement('td');
                    tdPrice.textContent = item.price;
                    tr.appendChild(tdPrice);

                    // description
                    const tdDescription = document.createElement('td');
                    tdDescription.textContent = item.description;
                    tr.appendChild(tdDescription);
                    itemTable.appendChild(tr);
                });

                const endDateInput = document.getElementById('endDate');
                const now = new Date();

                const year = now.getFullYear();
                const month = String(now.getMonth() + 1).padStart(2, '0');
                const day = String(now.getDate()).padStart(2, '0');
                const hours = String(now.getHours()).padStart(2, '0');
                const minutes = String(now.getMinutes()).padStart(2, '0');

                endDateInput.min = `${year}-${month}-${day}T${hours}:${minutes}`;

            } catch (error) {
                console.error('Failed to load auctions:', error);
            }
        }

        async function getDetailPageInfo(auctionId) {
            try {
                detailPage.style.display = 'block';
                const response = await fetch(`auction?auctionId=${encodeURIComponent(auctionId)}`);
                if (!response.ok){
                    showError('Failed to load auction detail');
                    return;
                }
                const offerTable = document.getElementById('offers-table');
                const detail = document.getElementById('auction-details');
                detail.innerHTML = '';
                offerTable.innerHTML = '';

                const data = await response.json();
                const { auction, offers, closeable  } = data;
                const spans = detail.querySelectorAll('span')
                const status = auction.closed?'Chiusa':'Aperta';
                const formatted = formatDate(auction.ending_at);
                const contents = [auction.auctionId,
                    auction.title, auction.startingPrice,
                    auction.minIncrement, formatted,
                    status]
                spans.forEach((span, index) => {span.textContent = contents[index] || "";})

                offers.forEach(offer => {
                    const tr = document.createElement('tr');
                    const tdId = document.createElement('td');
                    tdId.textContent = offer.id;
                    tr.appendChild(tdId);

                    const tdUserId = document.createElement('td');
                    tdUserId.textContent = offer.userId;
                    tr.appendChild(tdUserId);

                    const tdPrice = document.createElement('td');
                    const spanPrice = document.createElement('span');
                    spanPrice.textContent = offer.offeredPrice;
                    tdPrice.appendChild(spanPrice);
                    tr.appendChild(tdPrice);

                    const tdTime = document.createElement('td');
                    tdTime.textContent = formatDate(offer.offeredTime);
                    tr.appendChild(tdTime);

                    offerTable.appendChild(tr);
                });

                if(offers.length === 0){
                    const tr = document.createElement('tr');
                    const td = document.createElement('td');
                    td.style.textAlign = 'center';
                    td.style.columnSpan = '4';
                    td.textContent = 'Nessuna offerta';
                    tr.appendChild(td);
                    offerTable.appendChild(tr);
                }

                const closeBtn = document.getElementById('closeBtn');
                const closeBtnInput = closeBtn.querySelector('input');
                closeBtnInput.value = auction.auctionId;
                if(closeable){
                    closeBtn.style.display = 'block';
                }else{
                    closeBtn.style.display = 'none';
                }

            } catch (error) {
                console.error('Failed to load auctions:', error);
            }
        }

        async function getOfferPageInfo(auctionId){
            try {
                offerPage.style.display = 'block';
                const response = await fetch(`offer?auctionId=${encodeURIComponent(auctionId)}`);
                if (!response.ok){
                    showError('Failed to load auction detail');
                    return;
                }
                const offerTable = offerPage.querySelector('#offers-table tbody');
                const detail = offerPage.querySelector('.auction-details');
                detail.innerHTML = '';
                offerTable.innerHTML = '';
                detail.dataset.id = auctionId;

                const data = await response.json();
                const { auction, offers, items, minimumOffer  } = data;
                const spans = detail.querySelectorAll('span')
                const status = auction.closed?'Chiusa':'Aperta';
                const formatted = formatDate(auction.ending_at);
                const contents = [auction.auctionId,
                    auction.title, auction.startingPrice,
                    auction.minIncrement, formatted,
                    status]
                spans.forEach((span, index) => {span.textContent = contents[index] || "";})

                offers.forEach(offer => {
                    const tr = document.createElement('tr');
                    const tdId = document.createElement('td');
                    tdId.textContent = offer.id;
                    tr.appendChild(tdId);

                    const tdUserId = document.createElement('td');
                    tdUserId.textContent = offer.userId;
                    tr.appendChild(tdUserId);

                    const tdPrice = document.createElement('td');
                    const spanPrice = document.createElement('span');
                    spanPrice.textContent = offer.offeredPrice;
                    tdPrice.appendChild(spanPrice);
                    tr.appendChild(tdPrice);

                    const tdTime = document.createElement('td');
                    tdTime.textContent = formatDate(offer.offeredTime);
                    tr.appendChild(tdTime);

                    offerTable.appendChild(tr);
                });

                if(offers.length === 0){
                    const tr = document.createElement('tr');
                    const td = document.createElement('td');
                    td.style.textAlign = 'center';
                    td.style.columnSpan = '4';
                    td.textContent = 'Nessuna offerta';
                    tr.appendChild(td);
                    offerTable.appendChild(tr);
                }

                const offerForm = document.getElementById('offer-form');
                const offerInput = offerForm.querySelector('input');
                offerInput.min = minimumOffer;
                offerInput.placeholder = "'Inserisci almeno &euro; '" + minimumOffer;

                const itemTable = document.getElementById('offer-page-items');
                items.forEach(item => {
                    const tr = document.createElement('tr');

                    // tdId
                    const tdId = document.createElement('td');
                    tdId.textContent = item.id;
                    tr.appendChild(tdId);

                    const tdImg = document.createElement('td');
                    const img = document.createElement('img');
                    const tdTitle = document.createElement('td');
                    img.src = item.cover_image;
                    img.alt = 'cover';
                    img.className = 'cover'
                    img.style.width = '50px';
                    img.style.height = 'auto';
                    tdTitle.textContent = item.title;
                    tdImg.appendChild(img);
                    tdImg.appendChild(tdTitle);
                    tr.appendChild(tdImg);

                    // description
                    const tdDescription = document.createElement('td');
                    tdDescription.textContent = item.description;
                    tr.appendChild(tdDescription);

                    // tdPrice
                    const tdPrice = document.createElement('td');
                    tdPrice.textContent = item.price;
                    tr.appendChild(tdPrice);

                    itemTable.appendChild(tr);
                });

            } catch (error) {
                console.error('Failed to load auctions:', error);
            }
        }


        if(page === 'buyPage'){
            getBuyPageInfo()
        }else if(page === 'sellPage'){
            getSellPageInfo();
        }else if(page === 'detailPage'){
            getDetailPageInfo(auctionId);
        }else if(page === 'offerPage'){
            getOfferPageInfo(auctionId)
        }
    }

    /**
     * handle login form submission
     * @type {Element}
     */
    if (loginForm) {
        loginForm.addEventListener('submit', async function(e) {
            e.preventDefault();

            const username = document.getElementById('username').value;
            const password = document.getElementById('password').value;

            // only if exists and not empty
            if (!username || !password) {
                showError('Please fill in all fields');
                return;
            }

            console.log('Login attempt with:', { username, password });
            const urlParams = new URLSearchParams();
            urlParams.append('username', username);
            urlParams.append('password', password);
            fetch('login', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded'
                },
                body: urlParams
            })
                .then(response => {
                    if(!response.ok){
                        return response.json().then(errData => {
                            throw new Error(errData.error || 'Unknown error');
                        });
                    }
                    return response.json();
                })
                .then(data => {
                     console.log('Login successful:', data);
                     last_username = data.username;
                     showMessage(data.message);
                     showPage(lastPage, 0);
                 })
                .catch(error => {
                    console.error('operation failed.:', error.message);
                    showError(error.message);
                 });
        });
    }

    /**
     * verify if inputs are valid for registration
     * @param username
     * @param password
     * @param firstName
     * @param lastName
     * @param address
     * @returns {boolean}
     */
    const verifyInputs = (username, password, firstName, lastName, address) => {
        if (!username || !password || !firstName || !lastName || !address) {
            showError('Please fill in all fields');
            return false;
        }
        const usernamePattern = /^[A-Za-z0-9_]{1,50}$/;
        if (!usernamePattern.test(username)) {
            showError('Username must be 1-50 characters (letters, numbers, underscores only)');
            return false;
        }
        if (password.length < 8 || password.length > 255) {
            showError('Password must be at least 8 characters long and not exceed 255 characters');
            return false;
        }
        if (firstName.length < 1 || firstName.length > 50){
            showError('First name must be between 1 and 50 characters');
            return false;
        }
        if (lastName.length < 1 || lastName.length > 50){
            showError('Last name must be between 1 and 50 characters');
            return false;
        }
        if (address.length < 1 || address.length > 255){
            showError('Address must be between 1 and 255 characters');
            return false;
        }
        return true;
    }

    const showRegisterLink = document.getElementById('register-link');
    if (showRegisterLink) {
        showRegisterLink.addEventListener('click', function(e) {
            e.preventDefault();
            loginForm.reset();
            showPage(registerPage, 0);
        });
    }

    const loginLink = document.querySelector('.login-link a');
    if (loginLink) {
        loginLink.addEventListener('click', function(e) {
            e.preventDefault();
            registerForm.reset();
            showPage(loginPage, 0);
        });
    }

    if (registerForm) {
        registerForm.addEventListener('submit', async function(e) {
            e.preventDefault();

            const username = document.getElementById('username_register').value.trim();
            const password = document.getElementById('password_register').value;
            const firstName = document.getElementById('first-name').value;
            const lastName = document.getElementById('last-name').value;
            const address = document.getElementById('address').value;

            console.log('Registration attempt with:', {
                username, password, firstName, lastName, address
            });

            if (!verifyInputs(username, password, firstName, lastName, address)) {
                return;
            }


            const urlParams = new URLSearchParams();
            urlParams.append('username', username);
            urlParams.append('password', password);
            urlParams.append('firstName', firstName);
            urlParams.append('lastName', lastName);
            urlParams.append('address', address);
            fetch('register', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded'
                },
                body: urlParams
            })
                .then(response => {
                    if(!response.ok){
                        return response.json().then(errData => {
                            throw new Error(errData.error || 'Unknown error');
                        });

                    }
                    return response.json();
                })
                .then(data => {
                    console.log('Registration successful:', data);
                    showMessage(data.message);
                    showPage(loginPage, 0)
                })
                .catch(error => {
                    console.error('operation failed.:', error.message);
                    showError(error.message);
                });
        });
    }

    /**
     * initiate the event listeners for the sell page
     */
    if (sellPage) {
        const uploadItemForm = document.getElementById('create-item-form');
        if (uploadItemForm) {
            uploadItemForm.addEventListener('submit', async function(e) {
                e.preventDefault();

                const itemName = uploadItemForm.querySelector('input[name="item"]').value;
                const price = uploadItemForm.querySelector('input[name="price"]').value;
                const description = uploadItemForm.querySelector('textarea[name="description"]').value;
                const coverImage = uploadItemForm.querySelector('input[name="cover"]').files[0];

                if (!itemName || !price || !description || !coverImage) {
                    showError('Please fill in all required fields');
                    return;
                }

                if (isNaN(price) || Number(price) <= 0) {
                    showError('Price must be a positive number');
                    return;
                }

                const formData = new FormData(e.target);
                console.log('Uploading item:', { itemName, price, description, coverImage });

                await fetch('uploadItem', {
                    method: 'POST',
                    body: formData
                })
                    .then(response => {
                        if(!response.ok){
                            return response.json().then(errData => {
                                throw new Error(errData.error || 'Unknown error');
                            });

                        }
                        return response.json();
                    })
                    .then(data => {
                        showMessage(data.message);
                        localStorage.setItem('lastPage', 'buy_page');
                        showPage(sellPage, 0)
                    })
                    .catch(error => {
                        console.error('operation failed.:', error.message);
                        showError(error.message);
                    });
                e.target.reset();

            });
        }

        const createAuctionForm = document.getElementById('create-auction-form');
        if (createAuctionForm) {
            createAuctionForm.addEventListener('submit', async function(e) {
                e.preventDefault();

                const title = createAuctionForm.querySelector('input[name="title"]').value;
                const minIncrement = createAuctionForm.querySelector('input[name="minIncrement"]').value;
                const endDate = createAuctionForm.querySelector('input[name="endDate"]').value;
                const selectedItems = Array.from(
                    createAuctionForm.querySelectorAll('input[name="selectedItems"]:checked')
                ).map(input => input.value);

                if (!title || !minIncrement || !endDate || selectedItems.length === 0) {
                    showError('Please fill in all fields and select at least one item');
                    return;
                }

                const urlParams = new URLSearchParams();
                urlParams.append('title', title);
                urlParams.append('minIncrement', minIncrement);
                urlParams.append('endDate', endDate);
                selectedItems.forEach(itemId => {
                    urlParams.append('selectedItems', itemId);
                });

                console.log('Creating auction:', { title, minIncrement, endDate, selectedItems });

                if (isNaN(minIncrement) || Number(minIncrement) <= 0 || !Number.isInteger(minIncrement)) {
                    showError('minimum increment must be a positive integer number');
                    return;
                }

                const endDate_Object = new Date(endDate);
                const now = new Date();

                if (endDate_Object <= now) {
                    showError('End date must be in the future');
                    return;
                }

                console.log('Creating auction:', { title, minIncrement, endDate, selectedItems });

                await fetch('auction', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/x-www-form-urlencoded',
                    },
                    body: urlParams
                })
                    .then(response => {
                        if(!response.ok){
                            return response.json().then(errData => {
                                throw new Error(errData.error || 'Unknown error');
                            });
                        }
                        return response.json();
                    })
                    .then(data => {
                        showMessage(data.message);
                        localStorage.setItem('lastPage', 'sell_page');
                        showPage(sellPage, 0);
                    })
                    .catch(error => {
                        console.error('operation failed.:', error.message);
                        showError(error.message);
                    });
                createAuctionForm.reset();
            });
        }
    }

    /**
     * initiate the event listeners for the buy page
     */
    if (buyPage) {
        const searchForm = document.getElementById(`keywords-form`);
        if (searchForm) {
            searchForm.addEventListener('submit', async function(e) {
                e.preventDefault();

                const keywords = document.getElementById('keywords').value || '';

                const urlParams = new URLSearchParams();
                urlParams.append('keywords', keywords);

                const response = await fetch(`buy?keywords=${encodeURIComponent(keywords)}`);

                if(!response.ok){
                    showError('Failed to search auctions');
                    return;
                }

                const data = await response.json();
                const { openAuctions } = data;

                const spanSearchEmpty = document.getElementById('no-result-span');
                const divSearchResults = document.getElementById('search-results-div');
                const searchTable = document.getElementById('search-results-table');
                if(openAuctions.length === 0){
                    spanSearchEmpty.style.display = 'block';
                    divSearchResults.style.display = 'none';
                }else{
                    searchTable.innerHTML = '';
                    spanSearchEmpty.style.display = 'none';
                    divSearchResults.style.display = 'block';
                    openAuctions.forEach(openAuction => {
                        const tr = document.createElement('tr');

                        const tdId = document.createElement('td');
                        const a = document.createElement('a');
                        a.href = `#`;
                        a.textContent = openAuction.auction.auctionId;
                        a.dataset.id = openAuction.auction.auctionId;
                        a.addEventListener('click', () => {
                            saveHistory(a.dataset.id);
                            showPage(offerPage, a.dataset.id);
                        });

                        tdId.appendChild(a);
                        tr.appendChild(tdId);

                        const tdItems = document.createElement('td');
                        openAuctions.items.forEach(item => {
                            const div = document.createElement('div');
                            const img = document.createElement('img');
                            img.src = item.cover_image;
                            img.alt = 'Item image';
                            img.style.width = '30px';
                            img.style.height = '30px';
                            img.style.marginLeft = '5px';
                            const span = document.createElement('span');
                            span.textContent = item.title;
                            div.appendChild(img);
                            div.appendChild(span);
                            tdItems.appendChild(div);
                        });
                        tr.appendChild(tdItems);

                        const tdMax = document.createElement('td');
                        tdMax.textContent = openAuction.maxOffer;
                        tr.appendChild(tdMax);

                        const tdTime = document.createElement('td');
                        tdTime.textContent = openAuction.timeLeft;
                        tr.appendChild(tdTime);

                        searchTable.appendChild(tr);
                    });
                }
                localStorage.setItem('lastPage', 'buyPage');

                console.log('Searching for:', keywords);
            });
        }
    }

    // initiate make offer form event listener OFFERTA.html
    if (offerPage) {
        const offerForm = document.getElementById('offer-form');
        if (offerForm) {
            offerForm.addEventListener('submit', async function(e) {
                e.preventDefault();

                const offeredPrice = offerForm.querySelector('input[name="offeredPrice"]').value;
                const minimumOffer = parseFloat(offerForm.querySelector('input[name="offeredPrice"]').min);
                const auctionId = offerPage.querySelector('.auction-details').dataset.id;
                const MAX = 1e15;

                if (!offeredPrice) {
                    showError('Please enter an offer price');
                    return;
                }else if (parseFloat(offeredPrice) < minimumOffer) {
                    showError(`Your offer must be at least € ${minimumOffer}`);
                    return;
                }else if(parseFloat(offeredPrice) > MAX){
                    showError(`Your offer must be less than or equal to € ${MAX}`);
                    return;
                }

                const urlParams = new URLSearchParams();
                urlParams.append('offeredPrice', offeredPrice);
                urlParams.append('id', auctionId);

                await fetch('offer', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/x-www-form-urlencoded',
                    },
                    body: urlParams
                })
                    .then(response => {
                        if(!response.ok){
                            return response.json().then(errData => {
                                throw new Error(errData.error || 'Unknown error');
                            });
                        }
                        return response.json();
                    })
                    .then(data => {
                        showMessage(data.message);
                        localStorage.setItem('lastPage', 'buy_page');
                        showPage(offerPage, auctionId);
                    })
                    .catch(error => {
                        console.error('operation failed.:', error.message);
                        showError(error.message);
                    });
                offerForm.reset();
            });
        }
    }

    //DETTAGLIO.html
    if (detailPage) {
        const closeAuctionForm = document.getElementById('close-form');
        if (closeAuctionForm) {
            closeAuctionForm.addEventListener('submit', async function(e) {
                e.preventDefault();

                const auctionId = closeAuctionForm.querySelector('input').value;
                console.log('Closing auction:', auctionId);

                const urlParams = new URLSearchParams();
                urlParams.append('id', auctionId);

                await fetch('close', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/x-www-form-urlencoded',
                    },
                    body: urlParams
                })
                    .then(response => {
                        if(!response.ok){
                            return response.json().then(errData => {
                                throw new Error(errData.error || 'Unknown error');
                            });
                        }
                        return response.json();
                    })
                    .then(data => {
                        showMessage(data.message);
                        localStorage.setItem('lastPage', 'buy_page');
                        showPage(detailPage, auctionId);
                    })
                    .catch(error => {
                        console.error('operation failed.:', error.message);
                        showError(error.message);
                    });
            });
        }
    }

    console.log('Auction House event listeners initialized');
});